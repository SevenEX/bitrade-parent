package cn.ztuo.bitrade.controller.member;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.constant.WithdrawStatus;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.MemberTransaction;
import cn.ztuo.bitrade.model.screen.LegalWalletWithdrawScreen;
import cn.ztuo.bitrade.entity.LegalWalletWithdraw;
import cn.ztuo.bitrade.entity.MemberWallet;
import cn.ztuo.bitrade.entity.QLegalWalletWithdraw;
import cn.ztuo.bitrade.service.LegalWalletWithdrawService;
import cn.ztuo.bitrade.service.MemberTransactionService;
import cn.ztuo.bitrade.service.MemberWalletService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;

@RestController
@RequestMapping("legal-wallet-withdraw")
@Api(tags = "法币钱包提现管理")
public class LegalWalletWithdrawController extends BaseAdminController {
    @Autowired
    private LegalWalletWithdrawService legalWalletWithdrawService;

    @Autowired
    private MemberWalletService walletService;

    @Autowired
    private MemberTransactionService memberTransactionService ;

    @GetMapping("page")
    @ApiOperation(value = "提现列表")
    @MultiDataSource(name = "second")
    public MessageResult page(
            PageModel pageModel,
            LegalWalletWithdrawScreen screen) {
        Predicate predicate = getPredicate(screen);
        Page<LegalWalletWithdraw> page = legalWalletWithdrawService.findAll(predicate, pageModel);
        return success(page);
    }

    @GetMapping("{id}")
    @ApiOperation(value = "提现详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(@PathVariable("id") Long id) {
        LegalWalletWithdraw one = legalWalletWithdrawService.findOne(id);
        Assert.notNull(one, "validate id!");
        return success(one);
    }

    //审核通过
    @PatchMapping("{id}/pass")
    @ApiOperation(value = "提现审核通过")
    @AccessLog(module = AdminModule.WITHDRAW, operation = "提现审核通过")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult pass(@PathVariable("id") Long id) {
        // 校验数据
        LegalWalletWithdraw one = legalWalletWithdrawService.findOne(id);

        MemberTransaction memberTransaction = new MemberTransaction() ;
        memberTransaction.setAmount(one.getAmount());
        memberTransaction.setAddress("");
        memberTransaction.setMemberId(one.getMember().getId());
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setSymbol(one.getCoin().getUnit());
        memberTransaction.setType(TransactionType.WITHDRAW);
        memberTransactionService.save(memberTransaction);
        Assert.notNull(one, "validate id!");
        Assert.isTrue(one.getStatus() == WithdrawStatus.PROCESSING, "审核已结束!");
        //审核通过
        legalWalletWithdrawService.pass(one);
        return success();
    }

    //审核不通过
    @PatchMapping("{id}/no-pass")
    @ApiOperation(value = "提现审核不通过")
    @AccessLog(module = AdminModule.WITHDRAW, operation = "提现审核不通过")
    public MessageResult noPass(@PathVariable("id") Long id) {
        //校验 提现
        LegalWalletWithdraw one = legalWalletWithdrawService.findOne(id);
        Assert.notNull(one, "validate id!");
        Assert.isTrue(one.getStatus() == WithdrawStatus.PROCESSING, "审核已结束!");
        //校验钱包
        MemberWallet wallet = walletService.findByCoinAndMember(one.getCoin(), one.getMember());
        Assert.notNull(wallet, "wallet null!");
        //不通过 修改钱包 提现单状态
        legalWalletWithdrawService.noPass(wallet, one);
        return success(one);
    }

    //确认打款 即上传打款凭证
    @PatchMapping("{id}/remit")
    @ApiOperation(value = "确认打款（上传打款凭证）")
    @AccessLog(module = AdminModule.WITHDRAW, operation = "确认打款（上传打款凭证）")
    public MessageResult remit(
            @PathVariable("id") Long id,
            @RequestParam("paymentInstrument") String paymentInstrument) {
        //校验提现单
        LegalWalletWithdraw one = legalWalletWithdrawService.findOne(id);
        Assert.notNull(one, "validate id!");
        Assert.isTrue(one.getStatus() == WithdrawStatus.WAITING, "打款已结束!");
        //校验钱包
        MemberWallet wallet = walletService.findByCoinAndMember(one.getCoin(), one.getMember());
        Assert.notNull(wallet, "wallet null!");
        //打款操作
        legalWalletWithdrawService.remit(paymentInstrument, one, wallet);
        return success(one);
    }


    //条件
    private Predicate getPredicate(LegalWalletWithdrawScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotBlank(screen.getUsername()))
            booleanExpressions.add(QLegalWalletWithdraw.legalWalletWithdraw.member.username.eq(screen.getUsername()));
        if (screen.getStatus() != null)
            booleanExpressions.add(QLegalWalletWithdraw.legalWalletWithdraw.status.eq(screen.getStatus()));
        if (StringUtils.isNotBlank(screen.getCoinName()))
            booleanExpressions.add(QLegalWalletWithdraw.legalWalletWithdraw.coin.name.eq(screen.getCoinName()));
        return PredicateUtils.getPredicate(booleanExpressions);
    }
}
