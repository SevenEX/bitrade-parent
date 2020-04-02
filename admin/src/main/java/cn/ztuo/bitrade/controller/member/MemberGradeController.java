package cn.ztuo.bitrade.controller.member;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.SmsCodePrefixEnum;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.entity.Admin;
import cn.ztuo.bitrade.entity.MemberGrade;
import cn.ztuo.bitrade.service.MemberGradeService;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * @description: MemberGradeController
 * @author: MrGao
 * @create: 2019/04/25 16:12
 */
@Slf4j
@RestController
@RequestMapping("member/grade")
@Api(tags = "等级配置")
public class MemberGradeController extends BaseController {

    @Autowired
    private MemberGradeService gradeService ;

    @RequiresPermissions("system:coin:rate")
    @RequestMapping(value = "all",method = RequestMethod.GET)
  //  @AccessLog(module = AdminModule.MEMBER, operation = "所有等级配置")
    @ApiOperation(value = "获取所有等级配置")
    @MultiDataSource(name = "second")
    public MessageResult findAll() {
        List<MemberGrade> memberGrades = gradeService.findAll();
        return success(memberGrades);
    }

    @RequiresPermissions("system:coin:rate")
    @PostMapping("update")
    @AccessLog(module = AdminModule.MEMBER, operation = "更新等级配置")
    @ApiOperation(value = "更新等级配置")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult update(@RequestBody MemberGrade memberGrade, String code,
                                @SessionAttribute(SysConstant.SESSION_ADMIN) @ApiIgnore Admin currentAdmin) {
        checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.GRADE_UPDATE_PHONE_PREFIX);
        if (memberGrade.getId() == null) {
            return error("主键不得为空");
        }
        MemberGrade one = gradeService.findOne(memberGrade.getId());
        if (one == null) {
            return error("修改对象不存在");
        }
        if (memberGrade.getId() == 1) {
            MemberGrade grade = gradeService.findOne(2L);
            if (memberGrade.getGradeBound() > grade.getGradeBound()) {
                return error("V1积分边界不允许大于V2积分边界");
            }
        } else if (memberGrade.getId() == 2) {
            MemberGrade maxGrade = gradeService.findOne(3L);
            MemberGrade minGrade = gradeService.findOne(1L);
            if (memberGrade.getGradeBound() < minGrade.getGradeBound()) {
                return error("V2积分边界不允许小于V1积分边界");
            }
            if (memberGrade.getGradeBound() > maxGrade.getGradeBound()) {
                return error("V2积分边界不允许大于V3积分边界");
            }
        }else if(memberGrade.getId()==3) {
            MemberGrade maxGrade = gradeService.findOne(4L);
            MemberGrade minGrade = gradeService.findOne(2L);
            if(memberGrade.getGradeBound()<minGrade.getGradeBound()){
                return error("V3积分边界不允许小于V2积分边界");
            }
            if(memberGrade.getGradeBound()>maxGrade.getGradeBound()){
                return error("V3积分边界不允许大于V4积分边界");
            }
        }else if(memberGrade.getId()==4){
            MemberGrade maxGrade = gradeService.findOne(5L);
            MemberGrade minGrade = gradeService.findOne(3L);
            if(memberGrade.getGradeBound()<minGrade.getGradeBound()){
                return error("V4积分边界不允许小于V3积分边界");
            }
            if(memberGrade.getGradeBound()>maxGrade.getGradeBound()){
                return error("V4积分边界不允许大于V5积分边界");
            }
        }else if(memberGrade.getId()==5){
            MemberGrade minGrade = gradeService.findOne(4L);
            if(memberGrade.getGradeBound()<minGrade.getGradeBound()){
                return error("V5积分边界不允许小于V4积分边界");
            }
        }
        MemberGrade save = gradeService.save(memberGrade);
        return success(save);
    }

    @RequiresPermissions("system:coin:rate")
    @PostMapping("updateGrade")
    @AccessLog(module = AdminModule.SYSTEM, operation = "费率配置")
    @ApiOperation(value = "费率配置")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateGrade(MemberGrade memberGrade) {
        return success(gradeService.updateMemberGrade(memberGrade.getExchangeFeeRate(), memberGrade.getExchangeMakerFeeRate()));
    }

    @RequiresPermissions("system:coin:rate")
    @PostMapping("updateOtcFee")
    @AccessLog(module = AdminModule.SYSTEM, operation = "修改商家手续费费率")
    @ApiOperation(value = "修改商家手续费费率")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateOtcFee(MemberGrade memberGrade, String code,
                                     @SessionAttribute(SysConstant.SESSION_ADMIN) @ApiIgnore Admin currentAdmin) {
        checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.BUSINESS_UPDATE_PHONE_PREFIX);
        return success(gradeService.updateOtcFee(memberGrade.getOtcFeeRate()));
    }

    @RequiresPermissions("system:coin:rate")
    @RequestMapping(value = "getOtcFee",method = RequestMethod.GET)
    @ApiOperation(value = "获取商家手续费费率")
    @MultiDataSource(name = "second")
    public MessageResult getOtcFee() {
        MemberGrade memberGrades = gradeService.findOne(1L);
        return success(memberGrades.getOtcFeeRate());
    }

    @PostMapping("updateSeDiscountRate")
    @AccessLog(module = AdminModule.SYSTEM, operation = "修改SE抵扣折扣率")
    @ApiOperation(value = "修改SE抵扣折扣率")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateSeDiscountRate(MemberGrade memberGrade, String code,
                                              @SessionAttribute(SysConstant.SESSION_ADMIN) @ApiIgnore Admin currentAdmin) {
        checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.SE_DISCOUNT_UPDATE_PHONE_PREFIX);
        return success(gradeService.updateSeDiscountRate(memberGrade.getSeDiscountRate()));
    }

    @RequestMapping(value = "getSeDiscountRate",method = RequestMethod.GET)
    @ApiOperation(value = "获取SE抵扣折扣率")
    @MultiDataSource(name = "second")
    public MessageResult getSeDiscountRate() {
        MemberGrade memberGrades = gradeService.findOne(1L);
        return success(memberGrades.getSeDiscountRate());
    }
}
