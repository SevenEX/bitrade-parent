package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.constant.LegalWalletState;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.screen.LegalWalletScreen;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.CoinService;
import cn.ztuo.bitrade.service.LegalWalletRechargeService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 会员充值
 */
@RestController
@RequestMapping("legal-wallet-recharge")
@Api(tags = "用户充值")
public class LegalWalletRechargeController extends BaseController {
    @Autowired
    private LegalWalletRechargeService legalWalletRechargeService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CoinService coinService;

    /**
     * 会员充值
     *
     * @param model         参数对象
     * @param bindingResult
     * @param user          登录用户
     * @return
     */
    @PostMapping()
    @ApiOperation(value = "充值")
    public MessageResult recharge(
            LegalWalletRechargeModel model,
            BindingResult bindingResult,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Coin coin = coinService.findByUnit(model.getUnit());
        Assert.notNull(coin, msService.getMessage("VALIDATE_COIN_NAME"));
        Assert.isTrue(coin.getHasLegal(), msService.getMessage("VALIDATE_COIN_LEGA"));
        //登录用户
        Member member = memberService.findOne(user.getId());
        //新建充值
        LegalWalletRecharge legalWalletRecharge = new LegalWalletRecharge();
        legalWalletRecharge.setMember(member);//所属会员
        legalWalletRecharge.setCoin(coin);//充值币种
        legalWalletRecharge.setAmount(model.getAmount());//充值金额
        legalWalletRecharge.setPaymentInstrument(model.getPaymentInstrument());//支付凭证
        legalWalletRecharge.setState(LegalWalletState.APPLYING);//状态
        legalWalletRecharge.setPayMode(model.getPayMode());//支付方式
        legalWalletRecharge.setRemark(model.getRemark());//备注
        legalWalletRechargeService.save(legalWalletRecharge);
        return success();
    }

    /**
     * 待条件分页
     *
     * @param pageModel
     * @param user
     * @return
     */
    @GetMapping()
    @ApiOperation(value = "分页查询记录")
    @MultiDataSource(name = "second")
    public MessageResult page(
            PageModel pageModel,
            LegalWalletScreen screen,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Coin coin = null;
        if (StringUtils.isNotBlank(screen.getCoinName())) {
            coin = coinService.findOne(screen.getCoinName());
            Assert.notNull(coin, msService.getMessage("VALIDATE_COIN_NAME"));
            Assert.isTrue(coin.getHasLegal(), msService.getMessage("VALIDATE_COIN_LEGA"));
        }
        BooleanExpression eq = QLegalWalletRecharge.legalWalletRecharge.member.id.eq(user.getId());
        if (coin != null)
            eq.and(QLegalWalletRecharge.legalWalletRecharge.coin.name.eq(screen.getCoinName()));
        if (screen.getState() != null)
            eq.and(QLegalWalletRecharge.legalWalletRecharge.state.eq(screen.getState()));
        Page<LegalWalletRecharge> page = legalWalletRechargeService.findAll(eq, pageModel);
        return success(page);
    }

    /**
     * 详情
     *
     * @param user
     * @param id
     * @return
     */
    @GetMapping("{id}")
    @ApiOperation(value = "充值详情")
    @MultiDataSource(name = "second")
    public MessageResult get(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, @PathVariable("id") Long id) {
        LegalWalletRecharge one = legalWalletRechargeService.findOneByIdAndMemberId(id, user.getId());
        Assert.notNull(one, "validate id!");
        return success(one);
    }
}
