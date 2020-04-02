package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.CertifiedBusinessStatus;
import cn.ztuo.bitrade.dao.*;
import cn.ztuo.bitrade.entity.BusinessCancelApply;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.service.Base.TopBaseService;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BusinessCancelApplyService extends TopBaseService<BusinessCancelApply,BusinessCancelApplyDao>{

    @Autowired
    public void setDao(BusinessCancelApplyDao dao) {
        super.setDao(dao);
    }

    @Autowired
    private OrderDao orderDao ;

    @Autowired
    private AppealDao appealDao ;

    @Autowired
    private AdvertiseDao advertiseDao ;

    @Autowired
    private MemberDao memberDao ;


    public List<BusinessCancelApply> findByMemberAndStaus(Member member, CertifiedBusinessStatus status){
        return dao.findByMemberAndStatusOrderByIdDesc(member,status);
    }

    public List<BusinessCancelApply> findByMember(Member member){
        return dao.findByMemberOrderByIdDesc(member);
    }

    public Map<String,Object> getBusinessOrderStatistics(Long memberId) {
       return orderDao.getBusinessStatistics(memberId);
    }

    public  Map<String,Object> getBusinessAppealStatistics(Long memberId){
        Map<String,Object> map = new HashedMap();
        Long complainantNum = appealDao.getBusinessAppealInitiatorIdStatistics(memberId);
        Long defendantNum = appealDao.getBusinessAppealAssociateIdStatistics(memberId);
        map.put("defendantNum",defendantNum);
        map.put("complainantNum",complainantNum);
        return map ;
    }

    public Long getAdvertiserNum(Long memberId) {
        Member member = memberDao.findById(memberId).orElse(null);
        return advertiseDao.getAdvertiseNum(member);
    }

    public long countAuditing(){
        return dao.countAllByStatus(CertifiedBusinessStatus.CANCEL_AUTH);
    }


}
