package cn.ztuo.bitrade.controller.member;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.dto.MemberWalletDTO;
import cn.ztuo.bitrade.dto.OtcWalletDTO;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.model.screen.MemberWalletScreen;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("member/member-wallet")
@Slf4j
@Api(tags = "资产管理")
public class MemberWalletController extends BaseAdminController {

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberService memberService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private LocaleMessageSourceService messageSource;
    @Autowired
    private LockPositionRecordService lockPositionRecordService;

    @Autowired
    private OtcWalletService otcWalletService;


    @RequiresPermissions("member:page-query")
    @PostMapping("balance")
   // @AccessLog(module = AdminModule.MEMBER, operation = "余额管理")
    @ApiOperation(value = "余额管理")
    @MultiDataSource(name = "second")
    public MessageResult getBalance(
            PageModel pageModel,
            MemberWalletScreen screen) {
        QMemberWallet qMemberWallet = QMemberWallet.memberWallet;
        QMember qMember = QMember.member;
        List<Predicate> criteria = new ArrayList<>();
        if (StringUtils.hasText(screen.getAccount())) {
            criteria.add(qMember.username.like("%" + screen.getAccount() + "%")
                    .or(qMember.mobilePhone.like(screen.getAccount() + "%"))
                    .or(qMember.email.like(screen.getAccount() + "%"))
                    .or(qMember.realName.like("%" + screen.getAccount() + "%")));
        }
        if (!StringUtils.isEmpty(screen.getWalletAddress())) {
            criteria.add(qMemberWallet.address.eq(screen.getWalletAddress()));
        }

        if (!StringUtils.isEmpty(screen.getUnit())) {
            criteria.add(qMemberWallet.coin.unit.eq(screen.getUnit()));
        }

        if (screen.getMaxAllBalance() != null) {
            criteria.add(qMemberWallet.balance.add(qMemberWallet.frozenBalance).loe(screen.getMaxAllBalance()));
        }
        if (screen.getMinAllBalance() != null) {
            criteria.add(qMemberWallet.balance.add(qMemberWallet.frozenBalance).goe(screen.getMinAllBalance()));
        }
        if (screen.getMaxBalance() != null) {
            criteria.add(qMemberWallet.balance.loe(screen.getMaxBalance()));
        }
        if (screen.getMinBalance() != null) {
            criteria.add(qMemberWallet.balance.goe(screen.getMinBalance()));
        }
        if (screen.getMaxFrozenBalance() != null) {
            criteria.add(qMemberWallet.frozenBalance.loe(screen.getMaxFrozenBalance()));
        }
        if (screen.getMinFrozenBalance() != null) {
            criteria.add(qMemberWallet.frozenBalance.goe(screen.getMinFrozenBalance()));
        }
        Page<MemberWalletDTO> page = memberWalletService.joinFind(criteria, qMember, qMemberWallet, pageModel);
        return success(messageSource.getMessage("SUCCESS"), page);
    }

    /**
     * 查询所有法币账户
     * @param pageModel
     * @param screen
     * @return
     */
    @RequiresPermissions("member:page-query")
    @PostMapping("otc/balance")
   // @AccessLog(module = AdminModule.MEMBER, operation = "法币余额管理")
    @ApiOperation(value = "法币余额管理")
    @MultiDataSource(name = "second")
    public MessageResult getOtcBalance(
            PageModel pageModel,
            MemberWalletScreen screen) {
        QOtcWallet qMemberWallet = QOtcWallet.otcWallet;
        QMember qMember = QMember.member;
        List<Predicate> criteria = new ArrayList<>();
        if (StringUtils.hasText(screen.getAccount())) {
            criteria.add(qMember.username.like("%" + screen.getAccount() + "%")
                    .or(qMember.mobilePhone.like(screen.getAccount() + "%"))
                    .or(qMember.email.like(screen.getAccount() + "%"))
                    .or(qMember.realName.like("%" + screen.getAccount() + "%")));
        }

        if (!StringUtils.isEmpty(screen.getUnit())) {
            criteria.add(qMemberWallet.coin.unit.eq(screen.getUnit()));
        }

        if (screen.getMaxAllBalance() != null) {
            criteria.add(qMemberWallet.balance.add(qMemberWallet.frozenBalance).loe(screen.getMaxAllBalance()));
        }
        if (screen.getMinAllBalance() != null) {
            criteria.add(qMemberWallet.balance.add(qMemberWallet.frozenBalance).goe(screen.getMinAllBalance()));
        }
        if (screen.getMaxBalance() != null) {
            criteria.add(qMemberWallet.balance.loe(screen.getMaxBalance()));
        }
        if (screen.getMinBalance() != null) {
            criteria.add(qMemberWallet.balance.goe(screen.getMinBalance()));
        }
        if (screen.getMaxFrozenBalance() != null) {
            criteria.add(qMemberWallet.frozenBalance.loe(screen.getMaxFrozenBalance()));
        }
        if (screen.getMinFrozenBalance() != null) {
            criteria.add(qMemberWallet.frozenBalance.goe(screen.getMinFrozenBalance()));
        }
        Page<OtcWalletDTO> page = otcWalletService.joinFind(criteria, qMember, qMemberWallet, pageModel);
        return success(messageSource.getMessage("SUCCESS"), page);
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("recharge")
    @AccessLog(module = AdminModule.FINANCE, operation = "充值")
    @ApiOperation(value = "充值")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult recharge(
            @RequestParam("unit") String unit,
            @RequestParam("uid") Long uid,
            @RequestParam("code") String code,
            @SessionAttribute(SysConstant.SESSION_ADMIN) @ApiIgnore Admin currentAdmin,
            @RequestParam("amount") BigDecimal amount) {
        checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.WALLET_RECHARGE_PHONE_PREFIX);
        Coin coin = coinService.findByUnit(unit);
        if (coin == null) {
            return error("币种不存在");
        }
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, uid);
        Assert.notNull(memberWallet, "wallet null");
        memberWalletService.increaseBalance(memberWallet.getId(), amount);

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(amount);
        memberTransaction.setMemberId(memberWallet.getMemberId());
        memberTransaction.setSymbol(unit);
        memberTransaction.setFeeUnit(unit);
        memberTransaction.setType(TransactionType.ADMIN_RECHARGE);
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        memberTransactionService.save(memberTransaction);
        memberWalletService.save(memberWallet);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("reset-address")
    @AccessLog(module = AdminModule.FINANCE, operation = "重置钱包地址")
    @ApiOperation(value = "重置钱包地址")
    public MessageResult resetAddress(String unit, long uid) {
        Member member = memberService.findOne(uid);
        Assert.notNull(member, "member null");
        try {
            JSONObject json = new JSONObject();
            json.put("uid", member.getId());
            log.info("kafkaTemplate send : topic = {reset-member-address} , unit = {} , uid = {}", unit, json);
            kafkaTemplate.send("reset-member-address", unit, json.toJSONString());
            return MessageResult.success(messageSource.getMessage("SUCCESS"));
        } catch (Exception e) {
            return MessageResult.error(messageSource.getMessage("REQUEST_FAILED"));
        }
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("lock-wallet")
    @AccessLog(module = AdminModule.FINANCE, operation = "锁定钱包")
    @ApiOperation(value = "锁定钱包")
    public MessageResult lockWallet(Long uid, String unit) {
        if (memberWalletService.lockWallet(uid, unit)) {
            return success(messageSource.getMessage("SUCCESS"));
        } else {
            return error(500, messageSource.getMessage("REQUEST_FAILED"));
        }
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("unlock-wallet")
    @AccessLog(module = AdminModule.FINANCE, operation = "解锁钱包")
    @ApiOperation(value = "解锁钱包")
    public MessageResult unlockWallet(Long uid, String unit) {
        if (memberWalletService.unlockWallet(uid, unit)) {
            return success(messageSource.getMessage("SUCCESS"));
        } else {
            return error(500, messageSource.getMessage("REQUEST_FAILED"));
        }
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("lock-position")
    @AccessLog(module = AdminModule.FINANCE, operation = "锁仓")
    @ApiOperation(value = "锁仓")
    public MessageResult lockPosition(Long uid, String unit, BigDecimal amount, String reason, Date unlockTime) {
        if(uid==null||StringUtils.isEmpty(unit)||amount==null||amount.compareTo(BigDecimal.ZERO)<=0){
            return MessageResult.error(messageSource.getMessage("Incorrect_Parameters"));
        }
        Member member=memberService.findOne(uid);
        if(member==null){
            return MessageResult.error(messageSource.getMessage("Incorrect_Parameters"));
        }
        Coin coin=coinService.findByUnit(unit);
        if(coin==null){
            return MessageResult.error(messageSource.getMessage("Incorrect_Parameters"));
        }
        MemberWallet memberWallet=memberWalletService.findByCoinAndMember(coin,member);
        if(memberWallet==null){
            return MessageResult.error(messageSource.getMessage("WALLET_NOT_FOUND"));
        }
        if(memberWallet.getBalance().compareTo(amount)<0){
            return MessageResult.error(messageSource.getMessage("BALANCE_NOT_ENOUGH"));
        }
        return lockPositionRecordService.lockPosition(memberWallet,amount,member,reason,unlockTime);
    }

    @RequiresPermissions("member:page-query")
    @PostMapping("unlock-position")
    @AccessLog(module = AdminModule.FINANCE, operation = "解锁锁仓金额")
    @ApiOperation(value = "解锁锁仓金额")
    public MessageResult unlockPosition(@RequestParam("lockPositionId") Long lockPositionId) {
        LockPositionRecord lockPositionRecord=lockPositionRecordService.findById(lockPositionId);
        if(lockPositionRecord==null){
            return MessageResult.error(messageSource.getMessage("Incorrect_Parameters"));
        }
        return lockPositionRecordService.unlock(lockPositionRecord);
    }


    /**
     * 查询法币账户
     * @param memberId
     * @return
     */
    @RequiresPermissions("member:page-query")
    @RequestMapping(value = "otc/query/{memberId}",method = RequestMethod.GET)
   // @AccessLog(module = AdminModule.MEMBER, operation = "查询法币账户")
    @ApiOperation(value = "查询法币账户")
    @MultiDataSource(name = "second")
    public MessageResult queryOtcWallet(@PathVariable("memberId")Long memberId) {
        log.info("---------查询用户法币账户:" + memberId);
        List<OtcWallet> result = otcWalletService.findByMemberId(memberId);
        return success(result);
    }

    /**
     * 锁定法币钱包
     * @param uid
     * @param unit
     * @return
     */
    @RequiresPermissions("member:page-query")
    @RequestMapping(value = "otc/lock-wallet",method = RequestMethod.POST)
    @AccessLog(module = AdminModule.FINANCE, operation = "锁定法币钱包")
    @ApiOperation(value = "锁定法币钱包")
    public MessageResult lockOtcWallet(Long uid, String unit) {
        if (otcWalletService.lockWallet(uid, unit)) {
            return success(messageSource.getMessage("SUCCESS"));
        } else {
            return error(500, "账号不存在");
        }
    }

    /**
     * 解锁法币钱包
     * @param uid
     * @param unit
     * @return
     */
    @RequiresPermissions("member:page-query")
    @RequestMapping(value = "otc/unlock-wallet",method = RequestMethod.POST)
    @AccessLog(module = AdminModule.FINANCE, operation = "解锁法币钱包")
    @ApiOperation(value = "解锁法币钱包")
    public MessageResult unlockOtcWallet(Long uid, String unit) {
        if (otcWalletService.unlockWallet(uid, unit)) {
            return success(messageSource.getMessage("SUCCESS"));
        } else {
            return error(500, "账号不存在");
        }
    }


}
