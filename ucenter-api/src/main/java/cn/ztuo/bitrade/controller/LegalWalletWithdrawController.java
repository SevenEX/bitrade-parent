package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.WithdrawStatus;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.CoinService;
import cn.ztuo.bitrade.service.LegalWalletWithdrawService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.service.MemberWalletService;
import cn.ztuo.bitrade.util.BigDecimalUtils;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 提现
 */
@RestController
@RequestMapping("legal-wallet-withdraw")
@Api(tags = "用户提现")
public class LegalWalletWithdrawController extends BaseController {
    @Autowired
    private LegalWalletWithdrawService legalWalletWithdrawService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService walletService;

    @GetMapping()
    @ApiOperation(value = "分页查询提现列表")
    @MultiDataSource(name = "second")
    public MessageResult page(
            PageModel pageModel,
            @RequestParam(value = "state", required = false) WithdrawStatus status,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        BooleanExpression eq = QLegalWalletWithdraw.legalWalletWithdraw.member.id.eq(user.getId());
        if (status != null) eq.and(QLegalWalletWithdraw.legalWalletWithdraw.status.eq(status));
        Page<LegalWalletWithdraw> page = legalWalletWithdrawService.findAll(eq, pageModel);
        return success(page);
    }

    @PostMapping()
    @ApiOperation(value = "提现申请")
    public MessageResult post(
            LegalWalletWithdrawModel model,
            BindingResult bindingResult,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) return result;
        // 合法币种
        Coin coin = coinService.findByUnit(model.getUnit());
        Assert.notNull(coin, msService.getMessage("VALIDATE_COIN_NAME"));
        Assert.isTrue(coin.getHasLegal(), msService.getMessage("VALIDATE_COIN_LEGA"));
        //用户提现的币种钱包
        MemberWallet wallet = walletService.findOneByCoinNameAndMemberId(coin.getName(), user.getId());
        Assert.notNull(wallet, msService.getMessage("WALLET_NULL"));
        Assert.isTrue(BigDecimalUtils.compare(wallet.getBalance(), model.getAmount()), msService.getMessage("VALIDATE_BALANCE"));
        //提现人
        Member member = memberService.findOne(user.getId());
        Assert.notNull(member, msService.getMessage("VALIDATE_LOGIN_USER"));
        //创建 提现
        LegalWalletWithdraw legalWalletWithdraw = new LegalWalletWithdraw();
        legalWalletWithdraw.setMember(member);
        legalWalletWithdraw.setAccount(model.getAccount());
        legalWalletWithdraw.setCoin(coin);
        legalWalletWithdraw.setAmount(model.getAmount());
        legalWalletWithdraw.setPayMode(model.getPayMode());
        legalWalletWithdraw.setStatus(WithdrawStatus.PROCESSING);
        legalWalletWithdraw.setRemark(model.getRemark());
        //提现操作
        legalWalletWithdrawService.withdraw(wallet, legalWalletWithdraw);
        return success();
    }

    @GetMapping("{id}")
    @ApiOperation(value = "提现详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(
            @PathVariable Long id,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        LegalWalletWithdraw one = legalWalletWithdrawService.findDetailWeb(id, user.getId());
        Assert.notNull(one, msService.getMessage("VALIDATE_ID"));
        return success(one);
    }

}
