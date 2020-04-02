package cn.ztuo.bitrade.event;

import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.dao.MemberDao;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.BigDecimalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * @author Seven
 * @date 2019年01月09日
 */
@Service
public class MemberEvent {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private MemberPromotionService memberPromotionService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Value("${slot.enable:false}")
    private boolean enableSlot;
    /**
     * 如果值为1，推荐注册的推荐人必须被推荐人实名认证才能获得奖励
     */
    @Value("${commission.need.real-name:0}")
    private int needRealName;

    /**
     * 注册成功事件
     *
     * @param member 持久化对象
     */
    public void onRegisterSuccess(Member member, String promotionCode) throws InterruptedException {
        JSONObject json = new JSONObject();
        json.put("uid", member.getId());
        kafkaTemplate.send("member-register", json.toJSONString());
        if (enableSlot) {
            //开启Slot模块
            kafkaTemplate.send("member-register-notify", json.toJSONString());
        }
        //推广活动
        if (StringUtils.hasText(promotionCode)) {
            // 父级
            Member member1 = memberDao.findMemberByPromotionCode(promotionCode);
            if (member1 != null) {
                member.setInviterId(member1.getId());
                // 父级的父级
                if(!StringUtils.isEmpty(member1.getInviterId())) {
                    Member member2 = memberDao.findById(member1.getInviterId()).orElse(null);
                    if (member2 != null) {
                        // 在注册用户存入父级的父级id
                        member.setInviterParentId(member2.getId());
                        memberDao.save(member);
                    }
                }
                promotion(member1, member);
                Long channelId = member1.getIsChannel() == BooleanEnum.IS_TRUE ? member1.getId() : member1.getChannelId();
                if (channelId > 0) {
                    memberDao.updateChannelId(member.getId(), channelId);
                }
            }
        }

    }

    /**
     * 登录成功事件
     *
     * @param member 持久化对象
     */
    public void onLoginSuccess(Member member, String ip) {

    }

    private void promotion(Member member1, Member member) {
        //如果不需要实名，直接发放奖励
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.REGISTER);
        if (needRealName == 0) {
            if (rewardPromotionSetting != null) {
                reward(rewardPromotionSetting, member1, "one");
            }
        }
        member1.setFirstLevel(member1.getFirstLevel() + 1);
        // member.setInviterId(member1.getId());
        MemberPromotion one = new MemberPromotion();
        one.setInviterId(member1.getId());
        one.setInviteesId(member.getId());
        one.setLevel(PromotionLevel.ONE);
        memberPromotionService.save(one);
        if (rewardPromotionSetting != null && rewardPromotionSetting.getInfo() != null && member1.getInviterId() != null) {
            BigDecimal amount2 = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("two");
            //如果推荐奖励二级不为0
            if (amount2 != null && amount2.compareTo(BigDecimal.ZERO) > 0) {
                Member member2 = memberDao.findById(member1.getInviterId()).orElse(null);
                if (needRealName == 0) {
                    promotionLevelTwo(rewardPromotionSetting, member2, member);
                }
            }
        }
    }

    private void promotionLevelTwo(RewardPromotionSetting rewardPromotionSetting, Member member2, Member member) {
        if (needRealName == 0) {
            if (rewardPromotionSetting != null) {
                reward(rewardPromotionSetting, member2, "two");
            }
        }
        member2.setSecondLevel(member2.getSecondLevel() + 1);
        MemberPromotion two = new MemberPromotion();
        two.setInviterId(member2.getId());
        two.setInviteesId(member.getId());
        two.setLevel(PromotionLevel.TWO);
        memberPromotionService.save(two);
        if (member2.getInviterId() != null) {
            Member member3 = memberDao.findById(member2.getInviterId()).orElse(null);
            member3.setThirdLevel(member3.getThirdLevel() + 1);
        }
    }

    public void reward(RewardPromotionSetting rewardPromotionSetting, Member member, String level) {
        MemberWallet memberWallet1 = memberWalletService.findByCoinAndMember(rewardPromotionSetting.getCoin(), member);
        BigDecimal amount = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal(level);
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            memberWalletService.increaseBalance(memberWallet1.getId(), amount);
            RewardRecord rewardRecord = new RewardRecord();
            rewardRecord.setAmount(amount);
            rewardRecord.setCoin(rewardPromotionSetting.getCoin());
            rewardRecord.setMember(member);
            rewardRecord.setRemark(rewardPromotionSetting.getType().getCnName());
            rewardRecord.setType(RewardRecordType.PROMOTION);
            rewardRecordService.save(rewardRecord);
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setFee(BigDecimal.ZERO);
            memberTransaction.setAmount(amount);
            memberTransaction.setSymbol(rewardPromotionSetting.getCoin().getUnit());
            memberTransaction.setType(TransactionType.PROMOTION_AWARD);
            memberTransaction.setMemberId(member.getId());
            memberTransactionService.save(memberTransaction);
        }
    }

}
