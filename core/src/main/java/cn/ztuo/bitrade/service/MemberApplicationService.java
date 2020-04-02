package cn.ztuo.bitrade.service;


import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.dao.MemberApplicationDao;
import cn.ztuo.bitrade.dao.MemberDao;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.Base.BaseService;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static cn.ztuo.bitrade.constant.AuditStatus.AUDIT_DEFEATED;
import static cn.ztuo.bitrade.constant.AuditStatus.AUDIT_SUCCESS;
import static cn.ztuo.bitrade.constant.RealNameStatus.NOT_CERTIFIED;
import static cn.ztuo.bitrade.constant.RealNameStatus.VERIFIED;
import static cn.ztuo.bitrade.entity.QMemberApplication.memberApplication;

/**
 * @author MrGao
 * @description 会员审核单Service
 * @date 2017/12/26 15:10
 */
@Slf4j
@Service
public class MemberApplicationService extends BaseService {

    @Autowired
    private MemberApplicationDao memberApplicationDao;


    @Autowired
    private MemberDao memberDao;

    @Autowired
    private IntegrationRecordService integrationRecordService;

    @Autowired
    private MemberGradeService gradeService ;

    @Autowired
    private  DataDictionaryService dictionaryService;

    @Override
    public List<MemberApplication> findAll() {
        return memberApplicationDao.findAll();
    }

    public Page<MemberApplication> findAll(Predicate predicate, Pageable pageable) {
        return memberApplicationDao.findAll(predicate, pageable);
    }

    public MemberApplication findOne(Long id) {
        return memberApplicationDao.findById(id).orElse(null);
    }

    public MemberApplication save(MemberApplication memberApplication) {
        return memberApplicationDao.save(memberApplication);
    }

    public List<MemberApplication> findLatelyReject(Member member) {
        return memberApplicationDao.findMemberApplicationByMemberAndAuditStatusOrderByIdDesc(member, AuditStatus.AUDIT_DEFEATED);
    }

    public int findSuccessRealAuthByIdCard(String idCard){
        List<MemberApplication> list=memberApplicationDao.findSuccessMemberApplicationsByIdCard(idCard, AuditStatus.AUDIT_ING, AuditStatus.AUDIT_SUCCESS);
        return list.size();
    }

    public MemberApplication findSuccessRecord(String idCard){
        List<MemberApplication> list=memberApplicationDao.findSuccessMemberApplicationsByIdCard(idCard, AuditStatus.AUDIT_ING, AuditStatus.AUDIT_SUCCESS);
        return list.get(0);
    }


    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param predicateList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<MemberApplication> query(List<Predicate> predicateList, Integer pageNo, Integer pageSize) {
        List<MemberApplication> list;
        JPAQuery<MemberApplication> jpaQuery = queryFactory.selectFrom(memberApplication);
        if (predicateList != null) {
            jpaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        }
        jpaQuery.orderBy(memberApplication.createTime.desc());
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    /**
     * 审核通过
     *
     * @param application
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditPass(MemberApplication application) {
        int kycStatus = application.getKycStatus();
        Member member = application.getMember();
        if (kycStatus == 5){
            //实名会员
            member.setMemberLevel(MemberLevelEnum.REALNAME);
            //会员身份证号码
            member.setIdNumber(application.getIdCard());
            //添加会员真实姓名
            member.setRealName(application.getRealName());
            //会员状态修改已认证
            member.setRealNameStatus(VERIFIED);
            //kyc 待二级审核
            member.setKycStatus(4);
            member.setApplicationTime(new Date());
            application.setKycStatus(4);
        }
        memberDao.save(member);
        //审核成功
        application.setAuditStatus(AUDIT_SUCCESS);

        memberApplicationDao.save(application);
    }

    /**
     * 审核通过
     *
     * @param application
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditPass1(MemberApplication application) {
        int kycStatus = application.getKycStatus();
        Member member = application.getMember();
        if (kycStatus == 5){
            //实名会员
            member.setMemberLevel(MemberLevelEnum.REALNAME);
            //会员身份证号码
            member.setIdNumber(application.getIdCard());
            //添加会员真实姓名
            member.setRealName(application.getRealName());
            //会员状态修改已认证
            member.setRealNameStatus(VERIFIED);
            //kyc 待二级审核
            member.setKycStatus(1);
            member.setApplicationTime(new Date());
            application.setKycStatus(1);
        }
        //kyc 二级审核通过赠送积分
        if (kycStatus == 6){
            member.setKycStatus(4);
            application.setKycStatus(4);
            //实名后赠送积分 需要两者都实名 才赠送积分
            try {
                if(member.getInviterId()!=null) {
                    Member inviteMember = memberDao.findById(member.getInviterId()).orElse(null);
                    if(inviteMember.getRealNameStatus()==RealNameStatus.VERIFIED && inviteMember.getKycStatus() == 4) {
                        promotion(inviteMember);
                    }
                }
            } catch (Exception e) {
                log.info("实名注册积分则送失败={}",e);
            }
        }
        memberDao.save(member);
        //审核成功
        application.setAuditStatus(AUDIT_SUCCESS);

        memberApplicationDao.save(application);
    }



    private synchronized void promotion(Member inviteMember) {
        DataDictionary dataDictionary = dictionaryService.findByBond(SysConstant.INTEGRATION_GIVING_ONE_INVITE);
        //增加积分并生成记录
        increaseIntegration(inviteMember, dataDictionary);
        if (inviteMember.getInviterId() != null) {
            Member inviteMember2 = memberDao.findById(inviteMember.getInviterId()).orElse(null);
            if(inviteMember2.getRealNameStatus()==RealNameStatus.VERIFIED) {
                promotionLevelTwo(inviteMember2);
            }

        }
    }

    private void promotionLevelTwo( Member inviteMember2) {
        DataDictionary dataDictionary = dictionaryService.findByBond(SysConstant.INTEGRATION_GIVING_TWO_INVITE);
        //增加积分并生成记录
        increaseIntegration(inviteMember2, dataDictionary);
    }

    private void increaseIntegration(Member member, DataDictionary dataDictionary) {
        if (dataDictionary != null) {
            if(StringUtils.isNumeric(dataDictionary.getValue())) {
                Long integrationAmount = Long.parseLong(dataDictionary.getValue());
                member.setIntegration(member.getIntegration()+integrationAmount);

                // 统计推广用户实名认证的总积分
                member.setGeneralizeTotal(member.getGeneralizeTotal()+integrationAmount);

                //判断当前积分符合哪个会员等级
                MemberGrade grade = gradeService.findOne(member.getMemberGradeId());
                if(grade.getId()!=5L && grade.getId()!=6L) {
                    Long integration = member.getIntegration();
                    if (grade.getGradeBound() < integration) {
                        member.setMemberGradeId(member.getMemberGradeId() + 1);
                    }
                }
                memberDao.save(member);
                IntegrationRecord integrationRecord = new IntegrationRecord();
                integrationRecord.setAmount(integrationAmount);
                integrationRecord.setMemberId(member.getId());
                integrationRecord.setCreateTime(new Date());
                integrationRecord.setType(IntegrationRecordType.PROMOTION_GIVING);
                integrationRecordService.save(integrationRecord);
            }
        }
    }


    public long countAuditing(){
        return memberApplicationDao.countAllByAuditStatus(AuditStatus.AUDIT_ING);
    }




    /**
     * 审核不通过
     *
     * @param application
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditNotPass(MemberApplication application) {
        Member member = application.getMember();
        int kycStatus = application.getKycStatus();

        if (kycStatus == 5){
            application.setKycStatus(2);
            member.setKycStatus(2);
            //会员实名状态未认证
            member.setRealNameStatus(NOT_CERTIFIED);
            //审核失败
            application.setAuditStatus(AUDIT_DEFEATED);
        }
        if (kycStatus == 6){
            application.setKycStatus(3);
            member.setKycStatus(3);
        }
        memberDao.save(member);
        memberApplicationDao.save(application);
    }


    public MemberApplication findMemberApplicationByKycStatusIn(List<Integer> kycStatus,Member member){
        return memberApplicationDao.findMemberApplicationByKycStatusInAndMember(kycStatus,member);
    }


}
