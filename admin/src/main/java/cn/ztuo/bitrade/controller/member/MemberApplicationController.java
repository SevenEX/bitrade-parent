package cn.ztuo.bitrade.controller.member;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.util.DateUtil;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.AuditStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.screen.MemberApplicationScreen;
import cn.ztuo.bitrade.entity.MemberApplication;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberApplicationService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static cn.ztuo.bitrade.entity.QMember.member;
import static cn.ztuo.bitrade.entity.QMemberApplication.memberApplication;
import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 实名审核单
 * @date 2017/12/26 15:05
 */
@RestController
@RequestMapping("member/member-application")
@Api(tags = "用户实名管理")
public class MemberApplicationController extends BaseAdminController {

    @Autowired
    private MemberApplicationService memberApplicationService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("member:member-application:page-query")
    @PostMapping("all")
    //@AccessLog(module = AdminModule.MEMBER, operation = "所有会员MemberApplication认证信息")
    @ApiOperation(value = "获取所有用户认证信息")
    @MultiDataSource(name = "second")
    public MessageResult all() {
        List<MemberApplication> all = memberApplicationService.findAll();
        if (all != null && all.size() > 0)
            return success(all);
        return error(messageSource.getMessage("NO_DATA"));
    }

    @RequiresPermissions("member:member-application:page-query")
    @PostMapping("detail")
    //@AccessLog(module = AdminModule.MEMBER, operation = "会员MemberApplication认证信息详情")
    @ApiOperation(value = "获取用户认证信息详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(@RequestParam("id") Long id) {
        MemberApplication memberApplication = memberApplicationService.findOne(id);
        notNull(memberApplication, "validate id!");
        return success(memberApplication);
    }

    @RequiresPermissions("member:member-application:page-query")
    @PostMapping("page-query")
   // @AccessLog(module = AdminModule.MEMBER, operation = "分页查找会员MemberApplication认证信息")
    @ApiOperation(value = "分页获取用户认证信息")
    @MultiDataSource(name = "second")
    public MessageResult queryPage(PageModel pageModel, MemberApplicationScreen screen) {
        List<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (screen.getMemberId() != null) {
            booleanExpressions.add(memberApplication.member.id.eq(screen.getMemberId()));
        }
        if (screen.getKycStatus() != null) {
            booleanExpressions.add(memberApplication.kycStatus.eq(screen.getKycStatus()));
        }
        if(!StringUtils.isEmpty(screen.getIdNumber())) {
            booleanExpressions.add(memberApplication.idCard.like("%" + screen.getIdNumber() + "%"));
        }
        if(!StringUtils.isEmpty(screen.getRealName())) {
            booleanExpressions.add(memberApplication.realName.like("%" + screen.getRealName() + "%"));
        }
        if(screen.getApproveStartTime()!=null){
            booleanExpressions.add(memberApplication.createTime.goe(screen.getApproveStartTime()));
        }
        if(screen.getApproveEndTime()!=null){
            booleanExpressions.add(memberApplication.createTime.loe(DateUtil.dateAddDay(screen.getApproveEndTime(),1)));
        }
        if(screen.getAuditStartTime()!=null){
            booleanExpressions.add(memberApplication.updateTime.goe(screen.getAuditStartTime()));
        }
        if(screen.getAuditEndTime()!=null){
            booleanExpressions.add(memberApplication.updateTime.loe(screen.getAuditEndTime()));
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        if (!StringUtils.isEmpty(screen.getKeyWords())&&pattern.matcher(screen.getKeyWords()).matches()) {
            booleanExpressions.add(memberApplication.member.mobilePhone.like("%" + screen.getKeyWords() + "%")
                    .or(memberApplication.member.id.eq(Long.valueOf(screen.getKeyWords())))
                    .or(memberApplication.member.email.like(screen.getKeyWords() + "%")));
        }else if(!StringUtils.isEmpty(screen.getKeyWords())){
            booleanExpressions.add(memberApplication.member.email.like("%" + screen.getKeyWords() + "%"));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<MemberApplication> all = memberApplicationService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("member:member-application:page-query")
    @PatchMapping("{id}/pass")
    @AccessLog(module = AdminModule.MEMBER, operation = "用户认证信息审核通过")
    @ApiOperation(value = "用户认证信息审核通过")
    public MessageResult pass(@PathVariable("id") Long id) {
        //校验
        MemberApplication application = memberApplicationService.findOne(id);
        notNull(application, "validate id!");
        //Assert.isTrue(application.getAuditStatus()== AuditStatus.AUDIT_ING,"该项审核已经被处理过，请刷新.....");
        Assert.isTrue(application.getKycStatus() == 5 || application.getKycStatus() == 6 || application.getKycStatus
                () == 4,"该项审核已经被处理过，请刷新.....");
        //业务
        memberApplicationService.auditPass(application);
        //返回
        return success();
    }

    @RequiresPermissions("member:member-application:page-query")
    @PatchMapping("{id}/no-pass")
    @AccessLog(module = AdminModule.MEMBER, operation = "用户认证信息审核不通过")
    @ApiOperation(value = "用户认证信息审核不通过")
    public MessageResult noPass(
            @PathVariable("id") Long id,
            @RequestParam(value = "rejectReason", required = false) String rejectReason) {
        //校验
        MemberApplication application = memberApplicationService.findOne(id);
        notNull(application, "validate id!");
//        Assert.isTrue(application.getAuditStatus()== AuditStatus.AUDIT_ING,"该项审核已经被处理过，请刷新.....");
        Assert.isTrue(application.getKycStatus() == 5 || application.getKycStatus() == 6 || application.getKycStatus
                () == 4,"该项审核已经被处理过，请刷新.....");
        //业务
        application.setRejectReason(rejectReason);//拒绝原因
        memberApplicationService.auditNotPass(application);
        //返回
        return success();
    }
}
