package cn.ztuo.bitrade.controller.member;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.screen.LegalWalletRechargeScreen;
import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.constant.LegalWalletState;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.MemberTransaction;
import cn.ztuo.bitrade.model.screen.LegalWalletRechargeScreen;
import cn.ztuo.bitrade.entity.LegalWalletRecharge;
import cn.ztuo.bitrade.entity.MemberWallet;
import cn.ztuo.bitrade.entity.QLegalWalletRecharge;
import cn.ztuo.bitrade.service.LegalWalletRechargeService;
import cn.ztuo.bitrade.service.MemberTransactionService;
import cn.ztuo.bitrade.service.MemberWalletService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.screen.LegalWalletRechargeScreen;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * MrGao
 */
@RestController
@RequestMapping("legal-wallet-recharge")
@Api(tags = "充值管理")
public class LegalWalletRechargeController extends BaseAdminController {
    @Autowired
    private LegalWalletRechargeService legalWalletRechargeService;
    @Autowired
    private MemberWalletService walletService;

    @Autowired
    private MemberTransactionService memberTransactionService ;

    @GetMapping("page")
    @ApiOperation(value = "充值列表")
    @MultiDataSource(name = "second")
    public MessageResult page(
            PageModel pageModel,
            LegalWalletRechargeScreen screen) {
        Predicate predicate = getPredicate(screen);
        Page<LegalWalletRecharge> page = legalWalletRechargeService.findAll(predicate, pageModel);
        return success(page);
    }

    @GetMapping("{id}")
    @ApiOperation(value = "充值列表")
    @MultiDataSource(name = "second")
    public MessageResult id(@PathVariable("id") Long id) {
        LegalWalletRecharge legalWalletRecharge = legalWalletRechargeService.findOne(id);
        Assert.notNull(legalWalletRecharge, "validate id!");
        return success(legalWalletRecharge);
    }

    //充值通过
    @PatchMapping("{id}/pass")
    @ApiOperation(value = "充值通过")
    @AccessLog(module = AdminModule.RECHARGE, operation = "充值通过")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult pass(@PathVariable("id") Long id) {
        //充值校验
        LegalWalletRecharge legalWalletRecharge = legalWalletRechargeService.findOne(id);
        MemberTransaction memberTransaction = new MemberTransaction() ;
        memberTransaction.setAmount(legalWalletRecharge.getAmount());
        memberTransaction.setAddress("");
        memberTransaction.setMemberId(legalWalletRecharge.getMember().getId());
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setSymbol(legalWalletRecharge.getCoin().getUnit());
        memberTransaction.setType(TransactionType.LEGAL_RECHARGE);
        memberTransactionService.save(memberTransaction);

        Assert.notNull(legalWalletRecharge, "validate id!");
        Assert.isTrue(legalWalletRecharge.getState() == LegalWalletState.APPLYING, "申请已经结束!");
        //校验钱包
        MemberWallet wallet = walletService.findByCoinAndMember(legalWalletRecharge.getCoin(), legalWalletRecharge.getMember());
        org.springframework.util.Assert.notNull(wallet, "wallet null!");
        //充值请求通过业务
        legalWalletRechargeService.pass(wallet, legalWalletRecharge);
        return success();
    }

    //虚假充值
    @PatchMapping("{id}/no-pass")
    @ApiOperation(value = "充值不通过")
    @AccessLog(module = AdminModule.RECHARGE, operation = "充值不通过")
    public MessageResult noPass(@PathVariable("id") Long id) {
        LegalWalletRecharge legalWalletRecharge = legalWalletRechargeService.findOne(id);
        Assert.notNull(legalWalletRecharge, "validate id!");
        Assert.isTrue(legalWalletRecharge.getState() == LegalWalletState.APPLYING, "申请已经结束!");
        legalWalletRechargeService.noPass(legalWalletRecharge);
        return success();
    }

    //条件
    private Predicate getPredicate(LegalWalletRechargeScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotBlank(screen.getUsername()))
            booleanExpressions.add(QLegalWalletRecharge.legalWalletRecharge.member.username.eq(screen.getUsername()));
        if (screen.getStatus() != null)
            booleanExpressions.add(QLegalWalletRecharge.legalWalletRecharge.state.eq(screen.getStatus()));
        if (StringUtils.isNotBlank(screen.getCoinName()))
            booleanExpressions.add(QLegalWalletRecharge.legalWalletRecharge.coin.name.eq(screen.getCoinName()));
        return PredicateUtils.getPredicate(booleanExpressions);
    }
}
