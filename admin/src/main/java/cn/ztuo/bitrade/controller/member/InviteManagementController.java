package cn.ztuo.bitrade.controller.member;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.QMember;
import cn.ztuo.bitrade.model.screen.MemberScreen;
import cn.ztuo.bitrade.service.InviteManagementService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.service.MemberTransactionService;
import cn.ztuo.bitrade.system.CoinExchangeFactory;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import cn.ztuo.bitrade.vo.InviteManagementVO;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.ztuo.bitrade.entity.QMember.member;


@RestController
@Slf4j
@RequestMapping("invite/management")
@Api(tags = "邀请管理")
public class InviteManagementController extends BaseController {

    @Autowired
    private InviteManagementService inviteManagementService;

    @Autowired
    private MemberTransactionService transactionService;

    @Autowired
    private CoinExchangeFactory coinExchangeFactory;

    @Autowired
    private MemberService memberService;

    /**
     * 邀请管理默认查询所有的用户
     *
     * @return
     */
    @RequiresPermissions("invite/management:query")
   // @AccessLog(module = AdminModule.INVITE, operation = "邀请管理默认查询所有的用户")
    @RequestMapping(value = "look", method = RequestMethod.POST)
    @ApiOperation(value = "邀请管理列表")
    @MultiDataSource(name = "second")
    public MessageResult lookAll(@RequestBody InviteManagementVO inviteManagementVO) {
        log.info("默认查询所有的用户 lookAll ={}", inviteManagementVO);
        Page<Member> page = inviteManagementService.lookAll(inviteManagementVO);
//        List<Member> content = page.getContent();
//        return successDataAndTotal(content, page.getTotalElements());
        return success(page);
    }

    /**
     * 条件查询
     */
   // @AccessLog(module = AdminModule.INVITE, operation = "邀请管理多条件查询")
    @RequestMapping(value = "query", method = RequestMethod.POST)
    @ApiOperation(value = "邀请管理多条件查询")
    @MultiDataSource(name = "second")
    public MessageResult queryCondition(@RequestBody InviteManagementVO inviteManagementVO) {
        log.info("默认查询所有的用户 QueryCondition ={}", inviteManagementVO);
        Page<Member> page = inviteManagementService.queryCondition(inviteManagementVO);
//        List<Member> content = page.getContent();
//        return successDataAndTotal(content, page.getTotalElements());
        return success(page);
    }

    /**
     * 根据id查询一级二级用户
     */
    //@AccessLog(module = AdminModule.INVITE, operation = "根据id查询一级二级用户")
    @RequestMapping(value = "info", method = RequestMethod.POST)
    @ApiOperation(value = "根据id查询一级二级用户")
    @MultiDataSource(name = "second")
    public MessageResult queryId(@RequestBody InviteManagementVO inviteManagementVO) {
        log.info("根据id查询一级二级用户 queryById={}", inviteManagementVO);
        Page<Member> page = inviteManagementService.queryId(inviteManagementVO);
//        List<Member> content = page.getContent();
//        return successDataAndTotal(content,page.getTotalElements() );
        return success(page);
    }

    @PostMapping("page-query")
    @ResponseBody
    @ApiOperation(value = "分页查询用户")
    @MultiDataSource(name = "second")
    public MessageResult page(
            PageModel pageModel,
            MemberScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        booleanExpressions.add(member.inviterId.isNotNull());
        if (screen.getStartTime() != null)
            booleanExpressions.add(member.registrationTime.goe(screen.getStartTime()));//大于等于开始时间
        if (!StringUtils.isEmpty(screen.getPromotionCode()))
            booleanExpressions.add(member.promotionCode.like("%"+screen.getPromotionCode()+"%"));
        if (screen.getEndTime() != null) {
            booleanExpressions.add(member.registrationTime.loe(screen.getEndTime()));//小于等于结束时间
        }
        if (screen.getMemberId() != null)
            booleanExpressions.add(member.id.eq(screen.getMemberId()).or(member.inviterId.eq(screen.getMemberId())));
        Pattern pattern = Pattern.compile("[0-9]*");
        if (!StringUtils.isEmpty(screen.getKeyWords())&&pattern.matcher(screen.getKeyWords()).matches()) {
            booleanExpressions.add(QMember.member.mobilePhone.like("%" + screen.getKeyWords() + "%")
                    .or(QMember.member.id.eq(Long.valueOf(screen.getKeyWords())))
                    .or(QMember.member.email.like("%" + screen.getKeyWords() + "%")));
        }else if(!StringUtils.isEmpty(screen.getKeyWords())){
            booleanExpressions.add(QMember.member.email.like("%" + screen.getKeyWords() + "%"));
        }
        Page<Member> all = memberService.findAll(PredicateUtils.getPredicate(booleanExpressions), pageModel.getPageable());
        return success(all);
    }
    /**
     * 获取邀请人数据统计
     * @return
     */
    @RequestMapping(value = "/statistics",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取邀请人数据统计")
    @MultiDataSource(name = "second")
    public MessageResult statistics(Long memberId){
        Map<String,Object> map = new HashMap<>();
        Predicate predicate1 = QMember.member.inviterId.eq(memberId);
        map.put("count",memberService.findCount(predicate1));
        Predicate predicate2 = QMember.member.inviterParentId.eq(memberId);
        map.put("indirectCount",memberService.findCount(predicate2));
        return success(map);
    }

    /**
     * 返佣总额统计
     * @return
     */
    @RequestMapping(value = "/summary",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "返佣总额统计")
    @MultiDataSource(name = "second")
    public MessageResult summary(Long memberId){
        List<Map<String, Object>> results = transactionService.findTransactionSum(memberId, TransactionType.PROMOTION_AWARD);
        BigDecimal amount = BigDecimal.ZERO;
        for(Map<String, Object> result:results){
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(result.get("symbol").toString());
            amount = amount.add(new BigDecimal(result.get("amount").toString()).multiply((rate==null&&rate.getUsdRate()!=null)?BigDecimal.ZERO:rate.getUsdRate()));
        }
        return success(amount.setScale(2,BigDecimal.ROUND_HALF_UP));
    }
}
