package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.system.CoinExchangeFactory;
import cn.ztuo.bitrade.util.MaskUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 推广
 *
 * @author Seven
 * @date 2019年03月19日
 */
@RestController
@RequestMapping(value = "/promotion")
@Api(tags = "邀请管理")
public class PromotionController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private MemberTransactionService transactionService;

    @Autowired
    private CoinExchangeFactory coinExchangeFactory;

    @Autowired
    private RewardStatisticsService rewardStatisticsService;
    @Autowired
    private DataDictionaryService dictionaryService ;



    /**
     * 推广记录查询
     *
     * @param member
     * @return
     */
    @RequestMapping(value = "/record" ,method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "邀请记录查询")
    @MultiDataSource(name = "second")
    public MessageResult promotionRecord(
            PageModel pageModel,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member) {

        Predicate predicate = QMember.member.inviterId.eq(member.getId());
        Page<Member> page = memberService.findAll(predicate,pageModel.getPageable());

        List<Member> list = page.getContent() ;

        List<PromotionMember> list1 = list.stream().map(x ->
                PromotionMember.builder().createTime(x.getRegistrationTime())
                        .level(PromotionLevel.ONE)
                        .username(StringUtils.isEmpty(x.getMobilePhone())?
                                MaskUtil.maskEmail(x.getEmail()):MaskUtil.maskMobile(x.getMobilePhone()))
                        .build()
        ).collect(Collectors.toList());
        if (list.size() > 0) {
            list.stream().forEach(x -> {
                if (x.getPromotionCode() != null) {
                    list1.addAll(memberService.findPromotionMember(x.getId()).stream()
                            .map(y ->
                                    PromotionMember.builder().createTime(y.getRegistrationTime())
                                            .level(PromotionLevel.TWO)
                                            .username(StringUtils.isEmpty(y.getMobilePhone())?
                                                    MaskUtil.maskEmail(y.getEmail()):MaskUtil.maskMobile(y.getMobilePhone()))
                                            .build()
                            ).collect(Collectors.toList()));
                }
            });
        }

        MessageResult messageResult = MessageResult.success();
        PageResult<PromotionMember> pageResult = new PageResult<>(list1.stream().sorted((x, y) -> {
            if (x.getCreateTime().after(y.getCreateTime())) {
                return -1;
            } else {
                return 1;
            }
        }).collect(Collectors.toList()) ,pageModel.getPageNo()+1,page.getSize(),page.getTotalElements());
        messageResult.setData(pageResult);
        return messageResult;
    }

    /**
     * 推广奖励记录
     *
     * @param member
     * @return
     */
    @RequestMapping(value = "/reward/record",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "邀请奖励记录查询")
    @MultiDataSource(name = "second")
    public MessageResult rewardRecord(
            PageModel pageModel,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member) {
        Predicate predicate = QRewardRecord.rewardRecord.member.id.eq(member.getId()).and(QRewardRecord.rewardRecord.type.eq(RewardRecordType.PROMOTION));

        Page<RewardRecord> page = rewardRecordService.findAll(predicate,pageModel);

        List<RewardRecord> list = page.getContent() ;

        MessageResult result = MessageResult.success();

        PageResult<PromotionRewardRecord> pageResult = new PageResult<>(list.stream().map(x ->
                PromotionRewardRecord.builder().amount(x.getAmount())
                        .createTime(x.getCreateTime())
                        .remark(x.getRemark())
                        .symbol(x.getCoin().getUnit())
                        .orderMember(StringUtils.isEmpty(x.getOrderMember().getMobilePhone())?
                                MaskUtil.maskEmail(x.getOrderMember().getEmail()):MaskUtil.maskMobile(x.getOrderMember().getMobilePhone()))
                        .build()
        ).collect(Collectors.toList()),pageModel.getPageNo()+1,page.getSize(),page.getTotalElements());
        result.setData(pageResult);
        return result;
    }

    /**
     * 获取推广统计数据
     * @param member
     * @return
     */
    @RequestMapping(value = "/summary",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取推广统计数据")
    @MultiDataSource(name = "second")
    public  Map<String,Object> summary(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member){
        Map<String,Object> map = new HashMap<>();
        /*Predicate predicate = QRewardRecord.rewardRecord.member.id.eq(member.getId()).and(QRewardRecord.rewardRecord.type.eq(RewardRecordType.PROMOTION));
        map.put("summary",rewardRecordService.findCount(predicate));*/
        Predicate predicate1 = QMember.member.inviterId.eq(member.getId());
        map.put("count",memberService.findCount(predicate1));
        Predicate predicate2 = QMember.member.inviterParentId.eq(member.getId());
        map.put("indirectCount",memberService.findCount(predicate2));
        List<Map<String, Object>> results = transactionService.findTransactionSum(member.getId(), TransactionType.PROMOTION_AWARD);
        BigDecimal amount = BigDecimal.ZERO;
        for(Map<String, Object> result:results){
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(result.get("symbol").toString());
            amount = amount.add(new BigDecimal(result.get("amount").toString()).multiply((rate!=null&&rate.getUsdRate()!=null)?rate.getUsdRate():BigDecimal.ZERO));
        }
        CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get("SE");

        map.put("amount",amount.setScale(2,BigDecimal.ROUND_HALF_UP));
        map.put("amountSE",amount.divide((rate==null&&rate.getUsdRate()!=null)?BigDecimal.ZERO:rate.getUsdRate(),0, RoundingMode.HALF_DOWN));
        return map;
    }


    /**
     * 推广奖励记录
     * @return
     */
    @RequestMapping(value = "/reward/statistics",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "邀请奖励榜单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "month", value = "月份 格式 YYYY-MM 不传默认为当月", required = false, dataType = "Long"),
    })
    @MultiDataSource(name = "second")
    public MessageResult RewardStatistics(String month) {
        if(StringUtils.isEmpty(month)){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            month = sdf.format(new Date());
        }
        DataDictionary dictionary = dictionaryService.findByBond(SysConstant.COMMISSION_DISPLAY_QUANTITY);
        List<Object[]> list = rewardStatisticsService.findAll(month,Integer.valueOf(dictionary.getValue()));
        Map map = new HashMap();
        list.forEach(itme->{
            Member member1 = memberService.findOne(Long.valueOf(itme[2].toString()));
            itme[2] = StringUtils.isEmpty(member1.getMobilePhone())?
                    MaskUtil.maskEmail(member1.getEmail()):MaskUtil.maskMobile(member1.getMobilePhone());
        });
        MessageResult result = MessageResult.success();

        List<PromotionRewardStatistics> pageResult = list.stream().map(x ->
                PromotionRewardStatistics.builder().amount(new BigDecimal(x[1].toString()).setScale(2,RoundingMode.HALF_DOWN))
                        .createTime(x[0].toString())
                        .orderMember(x[2].toString())
                        .build()
        ).collect(Collectors.toList());
        result.setData(pageResult);
        return result;
    }
}
