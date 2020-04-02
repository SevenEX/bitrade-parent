package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.MemberGrade;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberService;
import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.IntegrationRecord;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.IntegrationRecordService;
import cn.ztuo.bitrade.service.MemberGradeService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RedisUtil;
import cn.ztuo.bitrade.vo.IntegrationRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * @description: MemberIntegrationGradeController
 * @author: MrGao
 * @create: 2019/04/26 16:45
 */
@RestController
@RequestMapping("integration")
@Api(tags = "用户等级管理")
public class MemberIntegrationGradeController extends BaseController {


    @Autowired
    private MemberGradeService gradeService;
    @Autowired
    private RedisUtil redisUtil ;
    @Autowired
    private IntegrationRecordService recordService ;

    @Autowired
    private MemberService memberService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * 查询所有等级说明
     * @return
     */
    @RequestMapping(value = "grade",method = RequestMethod.GET)
    @ApiOperation(value = "查询所有等级说明")
    @MultiDataSource(name = "second")
    public MessageResult queryGradeInfo(){
        return  success(gradeService.findAll());
    }

    /**
     * 查询该用户当天累计提币次数和数量
     * @param user
     * @return
     */
    @RequestMapping(value = "day_withdraw/limit",method = RequestMethod.GET)
    @ApiOperation(value = "查询该用户当天累计提币次数和数量")
    @MultiDataSource(name = "second")
    public MessageResult queryDayWithdrawLimit(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user){
        Object count = redisUtil.get(SysConstant.CUSTOMER_DAY_WITHDRAW_TOTAL_COUNT+user.getId());
        count = count==null?0:count;
        Object amount = redisUtil.get(SysConstant.CUSTOMER_DAY_WITHDRAW_COVER_USD_AMOUNT+user.getId());
        amount = amount==null?0:amount;
        JSONObject result = new JSONObject();
        result.put("count",count);
        result.put("amount",amount);
        return success(result);
    }

    /**
     * 分页查询用户积分记录
     * @param queryVo
     * @param user
     * @return
     */
    @RequestMapping(value = "record/page_query",method = RequestMethod.POST)
    @ApiOperation(value = "分页查询用户积分记录")
    @MultiDataSource(name = "second")
    public MessageResult queryIntegration4PageQuery(IntegrationRecordVO queryVo,
                                                    @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user){
        MessageResult mr = new MessageResult(0,messageSource.getMessage("SUCCESS"));
        queryVo.setUserId(user.getId());
        Page<IntegrationRecord> page =  recordService.findRecord4Page(queryVo);
        mr.setTotal(page.getTotalElements());
        mr.setData(page.getContent());
        return mr;
    }

    /**
     * 查询所有等级说明
     * @return
     */
    @RequestMapping(value = "rateDetail",method = RequestMethod.GET)
    @ApiOperation(value = "查询挂单吃单费率")
    @MultiDataSource(name = "second")
    public MessageResult queryRateDetail(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user){
        MemberGrade memberGrade = gradeService.findOne(user.getMemberGradeId());
        return  success(memberGrade);
    }

}
