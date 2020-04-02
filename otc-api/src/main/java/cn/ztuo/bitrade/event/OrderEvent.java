package cn.ztuo.bitrade.event;

import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.coin.CoinExchangeFactory;
import cn.ztuo.bitrade.constant.PromotionRewardType;
import cn.ztuo.bitrade.constant.RewardRecordType;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.dao.MemberDao;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;

import static cn.ztuo.bitrade.util.BigDecimalUtils.*;

/**
 * @author Seven
 * @date 2019年01月22日
 */
@Service
public class OrderEvent {
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private OtcWalletService otcWalletService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;
    @Autowired
    private CoinExchangeFactory coins;
    @Value("${otc.reward.type:0}")
    private int otcRewardType;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Value("${channel.enable:false}")
    private Boolean channelEnable;
    @Value("${channel.exchange-rate:0.00}")
    private BigDecimal channelExchangeRate;


    public void onOrderCompleted(Order order) {
        Member member = memberDao.findById(order.getMemberId()).orElse(null);
        member.setTransactions(member.getTransactions() + 1);
        Member member1 = memberDao.findById(order.getCustomerId()).orElse(null);
        member1.setTransactions(member1.getTransactions() + 1);

        //记录首次交易时间
        if (member.getTransactionTime() == null)
            member.setTransactionTime(new Date());
        if (member1.getTransactionTime() == null)
            member1.setTransactionTime(new Date());

        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.TRANSACTION);
        if (rewardPromotionSetting != null && coins.getCny("USDT").compareTo(BigDecimal.ZERO) > 0) {
            Member[] array = {member, member1};
            Arrays.stream(array).forEach(
                x -> {
                    if(otcRewardType==1){
                        if (x.getInviterId() != null&&!(DateUtil.diffDays(new Date(), x.getRegistrationTime()) > rewardPromotionSetting.getEffectiveTime())) {
                            reward(x,rewardPromotionSetting,order);
                        }
                    }else if(otcRewardType==0){
                        //只有首次交易获得佣金
                        if (x.getTransactions() == 1 && x.getInviterId() != null) {
                            reward(x,rewardPromotionSetting,order);
                        }
                    }
                    if(channelEnable && x.getChannelId() > 0 && channelExchangeRate.compareTo(BigDecimal.ZERO) > 0){
                        //处理渠道返佣,按交易数量返相应币种
                        processChannelReward(x,order.getCoin().getUnit(),order.getNumber());
                    }
                }
            );
        }
    }

    /**
     * 渠道法币交易返佣
     * @param member
     * @param symbol
     * @param fee
     */
    public void processChannelReward(Member member,String symbol,BigDecimal fee){
        OtcWallet channelWallet =  otcWalletService.findByCoinUnitAndMemberId(member.getChannelId(),symbol);
        if(channelWallet != null && fee.compareTo(BigDecimal.ZERO) > 0 ){
            BigDecimal amount = fee.multiply(channelExchangeRate).setScale(8, RoundingMode.DOWN);
            if(amount.compareTo(BigDecimal.ZERO) > 0) {
                otcWalletService.increaseBalance(channelWallet.getId(), amount);
                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setAmount(amount);
                memberTransaction.setFee(BigDecimal.ZERO);
                memberTransaction.setMemberId(member.getChannelId());
                memberTransaction.setSymbol(symbol);
                memberTransaction.setType(TransactionType.CHANNEL_AWARD);
                memberTransactionService.save(memberTransaction);
            }
        }
    }

    /**
     * 发放奖励
     * @param x
     * @param rewardPromotionSetting
     * @param order
     */
    private void reward(Member x,RewardPromotionSetting rewardPromotionSetting,Order order){
        Member memberLevel1 = memberDao.findById(x.getInviterId()).orElse(null);
        OtcWallet memberWallet1 = otcWalletService.findByCoinAndMember(memberLevel1.getId(),rewardPromotionSetting
                .getCoin());
        BigDecimal number = order.getNumber();
        if(!rewardPromotionSetting.getCoin().getUnit().equalsIgnoreCase(order.getCoin().getUnit())) {
            //汇率转换
            number = mulRound(order.getNumber(), div(coins.getCny(order.getCoin().getUnit()),coins.getCny(rewardPromotionSetting.getCoin().getUnit())));
        }
        BigDecimal amount1 = mulRound(number, getRate(JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("one")));
        if(amount1.compareTo(BigDecimal.ZERO) > 0 ) {
            memberWallet1.setBalance(add(memberWallet1.getBalance(), amount1));
            otcWalletService.save(memberWallet1);
            RewardRecord rewardRecord1 = new RewardRecord();
            rewardRecord1.setAmount(amount1);
            rewardRecord1.setCoin(rewardPromotionSetting.getCoin());
            rewardRecord1.setMember(memberLevel1);
            rewardRecord1.setRemark(rewardPromotionSetting.getType().getCnName());
            rewardRecord1.setType(RewardRecordType.PROMOTION);
            MemberTransaction transactionLevel1 = new MemberTransaction();
            transactionLevel1.setFee(BigDecimal.ZERO);
            transactionLevel1.setAmount(amount1);
            transactionLevel1.setSymbol(rewardPromotionSetting.getCoin().getUnit());
            transactionLevel1.setType(TransactionType.PROMOTION_AWARD);
            transactionLevel1.setMemberId(memberLevel1.getId());
            memberTransactionService.save(transactionLevel1);
            rewardRecordService.save(rewardRecord1);
        }
        if (memberLevel1.getInviterId() != null) {
            Member memberLevel2 = memberDao.findById(memberLevel1.getInviterId()).orElse(null);
            OtcWallet memberWallet2 = otcWalletService.findByCoinAndMember(memberLevel2.getId(),
                    rewardPromotionSetting.getCoin());
            BigDecimal amount2 = mulRound(number, getRate(JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("two")));
            if(amount2.compareTo(BigDecimal.ZERO) > 0) {
                memberWallet2.setBalance(add(memberWallet2.getBalance(), amount2));
                RewardRecord rewardRecord2 = new RewardRecord();
                rewardRecord2.setAmount(amount2);
                rewardRecord2.setCoin(rewardPromotionSetting.getCoin());
                rewardRecord2.setMember(memberLevel2);
                rewardRecord2.setRemark(rewardPromotionSetting.getType().getCnName());
                rewardRecord2.setType(RewardRecordType.PROMOTION);
                rewardRecordService.save(rewardRecord2);

                MemberTransaction transactionLevel2 = new MemberTransaction();
                transactionLevel2.setFee(BigDecimal.ZERO);
                transactionLevel2.setAmount(amount2);
                transactionLevel2.setSymbol(rewardPromotionSetting.getCoin().getUnit());
                transactionLevel2.setType(TransactionType.PROMOTION_AWARD);
                transactionLevel2.setMemberId(memberLevel2.getId());
                memberTransactionService.save(transactionLevel2);
            }
        }
    }
}
