package cn.ztuo.bitrade.event;

import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.constant.PromotionRewardType;
import cn.ztuo.bitrade.constant.RewardRecordType;
import cn.ztuo.bitrade.dao.MemberDao;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.service.OtcWalletService;
import cn.ztuo.bitrade.service.RewardPromotionSettingService;
import cn.ztuo.bitrade.service.RewardRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

import static cn.ztuo.bitrade.util.BigDecimalUtils.add;
import static cn.ztuo.bitrade.util.BigDecimalUtils.getRate;
import static cn.ztuo.bitrade.util.BigDecimalUtils.mulRound;


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

    public void onOrderCompleted(Order order) {
        Member member = memberDao.findById(order.getMemberId()).orElse(null);
        member.setTransactions(member.getTransactions() + 1);
        Member member1 = memberDao.findById(order.getCustomerId()).orElse(null);
        member1.setTransactions(member1.getTransactions() + 1);
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.TRANSACTION);
        if (rewardPromotionSetting != null) {
            Member[] array = {member, member1};
            Arrays.stream(array).forEach(
                x -> {
                    if (x.getTransactions() == 1 && x.getInviterId() != null) {
                        Member member2 = memberDao.findById(x.getInviterId()).orElse(null);
                        OtcWallet memberWallet1 = otcWalletService.findByCoinAndMember(member2.getId(),rewardPromotionSetting
                                .getCoin());
                        BigDecimal amount1 = mulRound(order.getNumber(), getRate(JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("one")));
                        memberWallet1.setBalance(add(memberWallet1.getBalance(), amount1));
                        otcWalletService.save(memberWallet1);
                        RewardRecord rewardRecord1 = new RewardRecord();
                        rewardRecord1.setAmount(amount1);
                        rewardRecord1.setCoin(rewardPromotionSetting.getCoin());
                        rewardRecord1.setMember(member2);
                        rewardRecord1.setRemark(rewardPromotionSetting.getType().getCnName());
                        rewardRecord1.setType(RewardRecordType.PROMOTION);
                        rewardRecordService.save(rewardRecord1);
                        if (member2.getInviterId() != null) {
                            Member member3 = memberDao.findById(member2.getInviterId()).orElse(null);
                            OtcWallet memberWallet2 = otcWalletService.findByCoinAndMember(member3.getId(),rewardPromotionSetting
                                    .getCoin());
                            BigDecimal amount2 = mulRound(order.getNumber(), getRate(JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("two")));
                            memberWallet2.setBalance(add(memberWallet2.getBalance(), amount2));
                            RewardRecord rewardRecord2 = new RewardRecord();
                            rewardRecord2.setAmount(amount2);
                            rewardRecord2.setCoin(rewardPromotionSetting.getCoin());
                            rewardRecord2.setMember(member3);
                            rewardRecord2.setRemark(rewardPromotionSetting.getType().getCnName());
                            rewardRecord2.setType(RewardRecordType.PROMOTION);
                            rewardRecordService.save(rewardRecord2);
                        }
                    }
                }
            );
        }
    }
}
