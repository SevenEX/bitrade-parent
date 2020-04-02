package cn.ztuo.bitrade.controller.promotion;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.PromotionRewardType;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.QMember;
import cn.ztuo.bitrade.entity.RewardPromotionSetting;
import cn.ztuo.bitrade.model.screen.MemberPromotionScreen;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberPromotionService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.service.RewardPromotionSettingService;
import cn.ztuo.bitrade.util.BigDecimalUtils;
import cn.ztuo.bitrade.util.ExcelUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import cn.ztuo.bitrade.vo.PromotionMemberVO;
import cn.ztuo.bitrade.vo.RegisterPromotionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("promotion/member")
@Slf4j
@Api(tags = "推荐会员管理")
public class MemberPromotionController {

    @Autowired
    private MemberService memberService ;

    @Autowired
    private MemberPromotionService memberPromotionService ;

    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService ;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @PostMapping("page-query")
    @RequiresPermissions("promotion:member:page-query")
    //@AccessLog(module = AdminModule.PROMOTION,operation = "推荐会员分页查询")
    @ApiOperation(value = "推荐会员分页查询")
    @MultiDataSource(name = "second")
    public MessageResult page(PageModel pageModel, MemberPromotionScreen screen){
        Map<String,Object> map = getMemberPromotions(pageModel,screen,false);
        PageImpl<Object> pageImpl = new PageImpl<>((List<Object>)map.get("list"),pageModel.getPageable(),(long)map.get("total"));
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"),pageImpl);
    }


    @RequiresPermissions("promotion:member:details")
    @PostMapping("details")
   // @AccessLog(module = AdminModule.PROMOTION,operation = "推荐会员明细")
    @ApiOperation(value = "推荐会员明细")
    @MultiDataSource(name = "second")
    public MessageResult promotionDetails(PageModel pageModel,
                                          @RequestParam("memberId")Long memberId){
        pageModel.setSort();
        Page<RegisterPromotionVO> page = memberPromotionService.getPromotionDetails(memberId,pageModel);
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"),page);
    }

    @RequiresPermissions("promotion:member:out-excel")
    @GetMapping("out-excel")
    @ApiOperation(value = "导出邀请统计")
    @AccessLog(module = AdminModule.INVITE,operation = "导出邀请统计")
    @MultiDataSource(name = "second")
    public void outExcel(PageModel pageModel, MemberPromotionScreen screen,
                         HttpServletResponse response) throws Exception {
        Map<String,Object> map = getMemberPromotions(pageModel,screen,true);
        ExcelUtil.listToExcel((List<PromotionMemberVO>)map.get("list"),PromotionMemberVO.class.getDeclaredFields(),response.getOutputStream());
    }

    private Map getMemberPromotions(PageModel pageModel, MemberPromotionScreen screen,boolean out){
        List<BooleanExpression> booleanExpressions = new ArrayList<>() ;
        if(!StringUtils.isEmpty(screen.getAccount()))
            booleanExpressions.add(QMember.member.username.like("%"+screen.getAccount()+"%")
                    .or(QMember.member.realName.like("%"+screen.getAccount()+"%"))
                    .or(QMember.member.mobilePhone.like(screen.getAccount()+"%"))
                    .or(QMember.member.email.like(screen.getAccount()+"%")));
        if(screen.getMinPromotionNum()!=-1)
            booleanExpressions.add(QMember.member.firstLevel.add(QMember.member.secondLevel).goe(screen.getMinPromotionNum()));

        if(screen.getMaxPromotionNum()!=-1)
            booleanExpressions.add(QMember.member.firstLevel.add(QMember.member.secondLevel).loe(screen.getMaxPromotionNum()));

        RewardPromotionSetting setting = rewardPromotionSettingService.findByType(PromotionRewardType.REGISTER);

        Page<Member> page = memberService.findAll(PredicateUtils.getPredicate(booleanExpressions),pageModel.getPageable());

        List<Member> list0 = null ;

        if(out){
            list0 = memberService.findAll(PredicateUtils.getPredicate(booleanExpressions));
        }else{
            list0 = page.getContent() ;
        }

        log.info("out-excel results = {}",list0);

        List<PromotionMemberVO> list = list0.stream().map(x -> PromotionMemberVO.builder().id(x.getId())
                .username(x.getUsername())
                .email(x.getEmail())
                .mobilePhone(x.getMobilePhone())
                .realName(x.getRealName())
                .promotionCode(x.getPromotionCode())
                .promotionNum(x.getFirstLevel()+x.getSecondLevel())
                .reward(
                       getReward(x,setting)
                )
                .build())
                .collect(Collectors.toList());
        Map<String,Object> map1 = new HashMap();
        map1.put("total",page.getTotalElements());
        map1.put("list",list);
        return map1 ;
    }

    private Map<String ,String> getReward(Member x,RewardPromotionSetting setting){

        Map<String,String> map = new HashMap<>();

        Assert.notNull(setting,"注册奖励配置 null");

        String info = setting.getInfo() ;

        JSONObject jsonObject = JSONObject.parseObject(info);
        BigDecimal one = jsonObject.getBigDecimal("one");

        BigDecimal two = jsonObject.getBigDecimal("two");
        map.put(setting.getCoin().getName(),
                BigDecimalUtils.mul(one,new BigDecimal(x.getFirstLevel()+"")).add(
                        BigDecimalUtils.mul(two,new BigDecimal(x.getSecondLevel()+""))
                ).toString()) ;
        return map ;
    }
}
