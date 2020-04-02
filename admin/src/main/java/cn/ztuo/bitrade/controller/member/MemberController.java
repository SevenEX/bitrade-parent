package cn.ztuo.bitrade.controller.member;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.screen.MemberScreen;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.dto.MemberDTO;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.model.screen.MemberScreen;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.ExcelUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.screen.MemberScreen;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.time.DateUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static cn.ztuo.bitrade.constant.CertifiedBusinessStatus.*;
import static cn.ztuo.bitrade.constant.MemberLevelEnum.IDENTIFICATION;
import static cn.ztuo.bitrade.entity.QMember.member;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 后台管理会员
 * @date 2017/12/25 16:50
 */
@RestController
@RequestMapping("/member")
@Api(tags = "用户管理")
public class MemberController extends BaseAdminController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private BusinessAuthApplyService businessAuthApplyService;

    @Autowired
    private DepositRecordService depositRecordService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private MemberTransactionService transactionService;

    @Autowired
    private OtcWalletService otcWalletService;

    @RequiresPermissions("member:page-query")
    @PostMapping("all")
  //  @AccessLog(module = AdminModule.MEMBER, operation = "所有会员Member")
    @ApiOperation(value = "获取所有用户")
    @MultiDataSource(name = "second")
    public MessageResult all() {
        List<Member> all = memberService.findAll();
        if (all != null && all.size() > 0)
            return success(all);
        return error(messageSource.getMessage("REQUEST_FAILED"));
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("detail")
   // @AccessLog(module = AdminModule.MEMBER, operation = "用户详情")
    @ApiOperation(value = "用户详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true, dataType = "Long"),
    })
    @MultiDataSource(name = "second")
    public MessageResult detail(@RequestParam("id") Long id) {
        Member member = memberService.findOne(id);
        notNull(member, "validate id!");
        List<MemberWallet> list = memberWalletService.findAllByMemberId(member.getId());
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMember(member);
        memberDTO.setList(list);
        return success(memberDTO);
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("delete")
    @AccessLog(module = AdminModule.MEMBER, operation = "删除用户")
    @ApiOperation(value = "删除用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true, dataType = "Long"),
    })
    public MessageResult delete(@RequestParam(value = "id") Long id) {
        Member member = memberService.findOne(id);
        notNull(member, "validate id!");
        member.setStatus(CommonStatus.ILLEGAL);// 修改状态非法
        memberService.save(member);
        return success();
    }

    @RequiresPermissions("member:page-query")
    @PostMapping(value = "update")
    @AccessLog(module = AdminModule.MEMBER, operation = "更新用户")
    @ApiOperation(value = "更新用户")
    public MessageResult update(Member member) {
        if (member.getId() == null) {
            return error("id必须传参");
        }
        Member one = memberService.findOne(member.getId());
        if (one == null) {
            return error("用户不存在");
        }
        if (!StringUtils.isEmpty(member.getUsername())) {
            one.setUsername(member.getUsername());
        }
        if (!StringUtils.isEmpty(member.getPassword())) {
            one.setPassword(member.getPassword());
        }
        if (!StringUtils.isEmpty(member.getRealName())) {
            one.setRealName(member.getRealName());
        }
        //修改等级
        if(!StringUtils.isEmpty(member.getMemberGradeId())){
            one.setMemberGradeId(member.getMemberGradeId());
        }
        Member save = memberService.save(one);
        return success(save);
    }

    /**
     * 会员Member认证商家审核
     * @param id
     * @param status
     * @param detail
     * @return
     */
    @RequiresPermissions("business-auth:apply:page-query")
    @PatchMapping("{id}/audit-business")
    @AccessLog(module = AdminModule.BUSINESSAUTH, operation = "认证商家审核")
    @ApiOperation(value = "认证商家审核")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult auditBusiness(
            @PathVariable("id") Long id,
            @RequestParam("status") CertifiedBusinessStatus status,
            @RequestParam("detail") String detail) {
        Member member = memberService.findOne(id);
        notNull(member, "validate id!");
        //确认是审核中
        isTrue(member.getCertifiedBusinessStatus() == AUDITING, "validate member certifiedBusinessStatus!");
        //确认传入certifiedBusinessStatus值正确，审核通过或者不通过
        isTrue(status == VERIFIED || status == FAILED, "validate certifiedBusinessStatus!");
        //member.setCertifiedBusinessApplyTime(new Date());//time
        List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member, AUDITING);
        Date now=new Date();
        if (status == VERIFIED) {
            //通过
            member.setCertifiedBusinessStatus(VERIFIED);//已认证
            member.setMemberLevel(IDENTIFICATION);//认证商家
            if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
                BusinessAuthApply businessAuthApply = businessAuthApplyList.get(0);
                businessAuthApply.setCertifiedBusinessStatus(VERIFIED);
                businessAuthApply.setUpdateTime(now);
                businessAuthApply.setAuditingTime(now);
                //如果申请的时候选择了保证金策略
                if (businessAuthApply.getBusinessAuthDeposit() != null) {
                    //扣除保证金
                    OtcWallet memberWallet = otcWalletService.findByCoinAndMember(member.getId(),businessAuthApply
                            .getBusinessAuthDeposit().getCoin());
                    memberWallet.setFrozenBalance(memberWallet.getFrozenBalance().subtract(businessAuthApply.getAmount()));
                    DepositRecord depositRecord = new DepositRecord();
                    depositRecord.setId(UUID.randomUUID().toString());
                    depositRecord.setAmount(businessAuthApply.getAmount());
                    depositRecord.setCoin(businessAuthApply.getBusinessAuthDeposit().getCoin());
                    depositRecord.setMember(member);
                    depositRecord.setStatus(DepositStatusEnum.PAY);
                    depositRecordService.create(depositRecord);
                    businessAuthApply.setDepositRecordId(depositRecord.getId());
                    MemberTransaction transaction=new MemberTransaction();
                    transaction.setAmount(businessAuthApply.getAmount().multiply(new BigDecimal(-1)));
                    transaction.setSymbol(businessAuthApply.getBusinessAuthDeposit().getCoin().getUnit());
                    transaction.setAddress("");
                    transaction.setMemberId(member.getId());
                    transaction.setType(TransactionType.DEPOSIT);
                    transaction.setFee(BigDecimal.ZERO);
                    transactionService.save(transaction);
                }
            }
        } else {
            //不通过
            member.setCertifiedBusinessStatus(FAILED);//认证失败
            if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
                BusinessAuthApply businessAuthApply = businessAuthApplyList.get(0);
                businessAuthApply.setCertifiedBusinessStatus(FAILED);
                businessAuthApply.setDetail(detail);
                businessAuthApply.setUpdateTime(now);
                businessAuthApply.setAuditingTime(now);
                //申请商家认证时冻结的金额退回
                if (businessAuthApply.getBusinessAuthDeposit() != null) {
                    OtcWallet memberWallet = otcWalletService.findByCoinAndMember(member.getId(),businessAuthApply
                            .getBusinessAuthDeposit().getCoin());
                    memberWallet.setFrozenBalance(memberWallet.getFrozenBalance().subtract(businessAuthApply.getAmount()));
                    memberWallet.setBalance(memberWallet.getBalance().add(businessAuthApply.getAmount()));
                }
            }
        }
        member.setCertifiedBusinessCheckTime(new Date());
        memberService.save(member);
        return success();
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("page-query")
    @ResponseBody
   // @AccessLog(module = AdminModule.MEMBER, operation = "分页查找会员Member")
    @ApiOperation(value = "分页查询用户")
    @MultiDataSource(name = "second")
    public MessageResult page(
            PageModel pageModel,
            MemberScreen screen) {
        Predicate predicate = getPredicate(screen);
        Page<Member> all = memberService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("business-auth:apply:page-query")
    @PatchMapping("{id}/cancel-business")
    @AccessLog(module = AdminModule.BUSINESSAUTH, operation = "取消认证商家")
    @ApiOperation(value = "取消认证商家")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelBusiness(
            @PathVariable("id") Long id,
            @RequestParam("status") CertifiedBusinessStatus status) {
        Member member = memberService.findOne(id);
        notNull(member, "validate id!");
        //确认是申请取消认证状态
        isTrue(member.getCertifiedBusinessStatus() == CANCEL_AUTH, "validate member certifiedBusinessStatus!");
        //确认传入certifiedBusinessStatus值正确
        isTrue(status == NOT_CERTIFIED || status == VERIFIED, "validate certifiedBusinessStatus!");
        //member.setCertifiedBusinessApplyTime(new Date());//time
        //查询状态为申请取消认证的申请记录
        List<BusinessAuthApply> businessAuthApplyList=businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member,CANCEL_AUTH);
        if (status == VERIFIED) {
            //不允许取消
            member.setCertifiedBusinessStatus(VERIFIED);//状态改回已认证
            if(businessAuthApplyList!=null&&businessAuthApplyList.size()>0){
                businessAuthApplyList.get(0).setCertifiedBusinessStatus(VERIFIED);
            }
        } else {
            //取消认证的申请通过
            member.setCertifiedBusinessStatus(NOT_CERTIFIED);//未认证
            member.setMemberLevel(MemberLevelEnum.REALNAME);
            if(businessAuthApplyList!=null&&businessAuthApplyList.size()>0){
                businessAuthApplyList.get(0).setCertifiedBusinessStatus(NOT_CERTIFIED);
            }
            //商家认证时收取的保证金退回
            List<DepositRecord> depositRecordList=depositRecordService.findByMemberAndStatus(member,DepositStatusEnum.PAY);
            if(depositRecordList!=null&&depositRecordList.size()>0){
                BigDecimal deposit=BigDecimal.ZERO;
                for(DepositRecord depositRecord:depositRecordList){
                    depositRecord.setStatus(DepositStatusEnum.GET_BACK);
                    deposit=deposit.add(depositRecord.getAmount());
                }
                if(businessAuthApplyList!=null&&businessAuthApplyList.size()>0){
                    BusinessAuthApply businessAuthApply=businessAuthApplyList.get(0);
                    MemberWallet memberWallet=memberWalletService.findByCoinUnitAndMemberId(businessAuthApply.getBusinessAuthDeposit().getCoin().getUnit(),member.getId());
                    memberWalletService.increaseBalance(memberWallet.getId(), deposit);
                }
            }
        }
        memberService.save(member);
        return success();
    }

    @RequiresPermissions("member:page-query")
    @GetMapping("{id}/business-auth-detail")
   // @AccessLog(module = AdminModule.MEMBER, operation = "查询会员Member申请资料")
    @ApiOperation(value = "查询商家申请资料详情")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult getBusinessAuthApply(@PathVariable("id") Long id,
                                              @RequestParam("status") CertifiedBusinessStatus status) {
        if (status == null) {
            return MessageResult.error("缺少参数");
        }
        isTrue(status == AUDITING || status == CANCEL_AUTH, "validate certifiedBusinessStatus!");
        Member member = memberService.findOne(id);
        notNull(member, "validate id!");
        //查询申请记录
        List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member, status);
        MessageResult result = MessageResult.success();
        if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
            result.setData(businessAuthApplyList.get(0));
        }
        return result;
    }

    private Predicate getPredicate(MemberScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (screen.getCertifiedBusinessStatus() != null)
            booleanExpressions.add(member.certifiedBusinessStatus.eq(screen.getCertifiedBusinessStatus()));
        if (screen.getStartTime() != null)
            booleanExpressions.add(member.registrationTime.goe(screen.getStartTime()));//大于等于开始时间
        if (screen.getEndTime() != null) {
            booleanExpressions.add(member.registrationTime.loe(DateUtils.addDays(screen.getEndTime(),1)));//小于等于结束时间
        }
        if (!StringUtils.isEmpty(screen.getEmail()))
            booleanExpressions.add(member.email.like("%" + screen.getEmail() + "%"));
        if (!StringUtils.isEmpty(screen.getMobilePhone()))
            booleanExpressions.add(member.mobilePhone.like("%" + screen.getMobilePhone() + "%"));
        if (screen.getMemberId() != null)
            booleanExpressions.add(member.id.eq(screen.getMemberId()));
        if (!StringUtils.isEmpty(screen.getRealName()))
            booleanExpressions.add(member.realName.like("%" + screen.getRealName() + "%"));
        if (!StringUtils.isEmpty(screen.getIdNumber()))
            booleanExpressions.add(member.idNumber.like("%" + screen.getIdNumber() + "%"));
        if (!StringUtils.isEmpty(screen.getAccount()))
            booleanExpressions.add(member.username.like("%" + screen.getAccount() + "%")
                    .or(member.mobilePhone.like(screen.getAccount() + "%"))
                    .or(member.email.like(screen.getAccount() + "%"))
                    .or(member.realName.like("%" + screen.getAccount() + "%")));
        if (screen.getCommonStatus() != null)
            booleanExpressions.add(member.status.eq(screen.getCommonStatus()));
        if (screen.getTransactionStatus() != null)
            booleanExpressions.add(member.transactionStatus.eq(screen.getTransactionStatus()));
        Pattern pattern = Pattern.compile("[0-9]*");
        if (!StringUtils.isEmpty(screen.getKeyWords())&&pattern.matcher(screen.getKeyWords()).matches()) {
            booleanExpressions.add(member.mobilePhone.like("%" + screen.getKeyWords() + "%")
                    .or(member.id.eq(Long.valueOf(screen.getKeyWords())))
                    .or(member.email.like("%" + screen.getKeyWords() + "%")));
        }else if(!StringUtils.isEmpty(screen.getKeyWords())){
            booleanExpressions.add(member.email.like("%" + screen.getKeyWords() + "%"));
        }
        return PredicateUtils.getPredicate(booleanExpressions);
    }

    @RequiresPermissions("member:page-query")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.MEMBER, operation = "导出用户")
    @ApiOperation(value = "导出用户")
    public void outExcel(
            @RequestParam(value = "account", required = false) String account,
            @RequestParam(value = "status", required = false) CommonStatus status,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<BooleanExpression> booleanExpressionList = getBooleanExpressionList(account, status);
        List<Member> list = memberService.queryWhereOrPage(booleanExpressionList, null, null).getContent();
        ExcelUtil.listToExcel(list,Member.class.getDeclaredFields(),response.getOutputStream());
    }

    // 获得条件
    private List<BooleanExpression> getBooleanExpressionList(String account, CommonStatus status) {
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        if (status != null)
            booleanExpressionList.add(member.status.eq(status));
        if (org.apache.commons.lang3.StringUtils.isNotBlank(account))
            booleanExpressionList.add(member.username.like("%" + account + "%")
                    .or(member.mobilePhone.like(account + "%"))
                    .or(member.email.like(account + "%")));
        return booleanExpressionList;
    }


    @RequiresPermissions("otc:businessaudit:business")
    @PostMapping("alter-publish-advertisement-status")
    @AccessLog(module = AdminModule.BUSINESSAUTH, operation = "禁用/解禁发布广告")
    @ApiOperation(value = "禁用/解禁商家发布广告")
    public MessageResult publishAdvertise(@RequestParam("memberId") Long memberId,
                                          @RequestParam("status") BooleanEnum status) {
        Member member = memberService.findOne(memberId);
        if (member.getCertifiedBusinessStatus() != CertifiedBusinessStatus.VERIFIED)
            return error("请先认证商家");
        Assert.notNull(member, "玩家不存在");
        member.setPublishAdvertise(status);
        memberService.save(member);
        return success(status == BooleanEnum.IS_FALSE ? "禁止发布广告成功" : "解除禁止成功");
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("alter-status")
    @AccessLog(module = AdminModule.MEMBER, operation = "禁用/解禁用户账号")
    @ApiOperation(value = "禁用/解禁用户状态")
    public MessageResult ban(@RequestParam("status") CommonStatus status,
                             @RequestParam("memberId") Long memberId) {
        Member member = memberService.findOne(memberId);
        member.setStatus(status);
        memberService.save(member);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("alter-transaction-status")
    @AccessLog(module = AdminModule.MEMBER, operation = "禁用/解禁用户交易状态")
    @ApiOperation(value = "禁用/解禁用户交易状态")
    public MessageResult alterTransactionStatus(
            @RequestParam("status") BooleanEnum status,
            @RequestParam("memberId") Long memberId) {
        Member member = memberService.findOne(memberId);
        member.setTransactionStatus(status);
        memberService.save(member);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("unlock-login")
    @AccessLog(module = AdminModule.MEMBER, operation = "解锁用户登录状态")
    @ApiOperation(value = "解锁用户登录状态")
    public MessageResult unLockLogin(@RequestParam("memberId") Long memberId) {
        Member member = memberService.findOne(memberId);
        member.setLoginLock(BooleanEnum.IS_FALSE);
        memberService.save(member);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("alter-withdrawalStatus-status")
    @AccessLog(module = AdminModule.MEMBER, operation = "禁用/解禁用户提现状态")
    @ApiOperation(value = "禁用/解禁用户提现状态")
    public MessageResult alterWithdrawalStatus(
            @RequestParam("status") BooleanEnum status,
            @RequestParam("memberId") Long memberId) {
        Member member = memberService.findOne(memberId);
        member.setWithdrawalStatus(status);
        memberService.save(member);
        return success(messageSource.getMessage("SUCCESS"));
    }
}
