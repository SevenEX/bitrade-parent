package cn.ztuo.bitrade.consumer;

import cn.ztuo.bitrade.constant.ActivityRewardType;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.RewardRecordType;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.UUIDUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MemberConsumer {
    private Logger logger = LoggerFactory.getLogger(MemberConsumer.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private RewardActivitySettingService rewardActivitySettingService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private ReleaseBalanceService releaseBalanceService;

    /**
     * 重置用户钱包地址
     *
     * @param record
     */
    @KafkaListener(topics = {"reset-member-address"})
    public void resetAddress(ConsumerRecord<String, String> record) {
        logger.info("handle member-register,key={},value={}", record.key(), record.value());
        String content = record.value();
        JSONObject json = JSON.parseObject(content);
        Coin coin = coinService.findByUnit(record.key());
        Assert.notNull(coin, "coin null");
        if (coin.getEnableRpc() == BooleanEnum.IS_TRUE) {
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(record.key(), json.getLong
                    ("uid"));
            Assert.notNull(memberWallet, "wallet null");
            if(StringUtils.isNotEmpty(coin.getMasterAddress())){
                memberWallet.setAddress(UUIDUtil.getIdFromMemberIdAndUnit(memberWallet.getMemberId(), coin.getUnit()));
            }
            else {
                String account = "U" + json.getLong("uid");

                List<CoinChainRelation> relations = coin.getCoinChainRelationList();
                List<String> coins = Collections.singletonList(record.key());
                if (relations.size() > 0) {
                    coins = relations.stream().map(CoinChainRelation::getCoinKey).collect(Collectors.toList());
                }
                for (String key : coins) {
                    //远程RPC服务URL,后缀为币种单位
                    String serviceName = "SERVICE-RPC-" + key;
                    try {
                        String url = "http://" + serviceName + "/rpc/address/" + account;
                        TreeMap<String, String> map = new TreeMap<>();
                        String param = rewardRecordService.sign(map);
                        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url + param, MessageResult.class);
                        logger.info("remote call:service={},result={}", serviceName, result);
                        if (result.getStatusCode().value() == 200) {
                            MessageResult mr = result.getBody();
                            if (mr.getCode() == 0) {
                                String address = mr.getData().toString();
                                if (StringUtils.equals(record.key(), key)) {
                                    memberWallet.setAddress(address);
                                }
                                MemberWalletRelation memberWalletRelation = new MemberWalletRelation();
                                memberWalletRelation.setCoinKey(key);
                                memberWalletRelation.setAddress(address);
                                memberWalletRelation.setCoinId(record.key());
                                memberWalletRelation.setMemberId(memberWallet.getMemberId());
                                memberWalletService.saveMemberWalletRelation(memberWalletRelation);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("call {} failed,error={}", serviceName, e.getMessage());
                    }
                }
            }
            memberWalletService.save(memberWallet);
        }

    }


    /**
     * 客户注册消息
     *
     * @param content
     */
    @KafkaListener(topics = {"member-register"})
    public void handle(String content) {
        logger.info("handle member-register,data={}", content);
        if (StringUtils.isEmpty(content)) {
            return;
        }
        JSONObject json = JSON.parseObject(content);
        if (json == null) {
            return;
        }
        //获取所有支持的币种
        List<Coin> coins = coinService.findAll();
        for (Coin coin : coins) {
            logger.info("memberId:{},unit:{}", json.getLong("uid"), coin.getUnit());
            MemberWallet wallet = new MemberWallet();
            wallet.setCoin(coin);
            wallet.setMemberId(json.getLong("uid"));
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setFrozenBalance(BigDecimal.ZERO);
            wallet.setReleaseBalance(BigDecimal.ZERO);
            //方案一：用户注册时生成地址
//            if (coin.getEnableRpc() == BooleanEnum.IS_TRUE) {
//                String account = json.getLong("uid").toString();
//                if (StringUtils.isNotEmpty(coin.getMasterAddress())) {
//                    //当使用一个主账户时不取rpc
//                    wallet.setAddress(coin.getMasterAddress() + ":" + account);
//                } else {
//                    //远程RPC服务URL,后缀为币种单位
//                    String serviceName = "SERVICE-RPC-" + coin.getUnit();
//                    try {
//                        String url = "http://" + serviceName + "/rpc/address/{account}";
//                        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class,
// account);
//                        logger.info("remote call:service={},result={}", serviceName, result);
//                        if (result.getStatusCode().value() == 200) {
//                            MessageResult mr = result.getBody();
//                            logger.info("mr={}", mr);
//                            if (mr.getCode() == 0) {
//                                //返回地址成功，调用持久化
//                                String address = (String) mr.getData();
//                                wallet.setAddress(address);
//                            }
//                        }
//                    } catch (Exception e) {
//                        logger.error("call {} failed,error={}", serviceName, e.getMessage());
//                        wallet.setAddress("");
//                    }
//                }
//            } else {
//                wallet.setAddress("");
//            }
            //方案二：用户注册时不生成地址，充币时获取
            wallet.setAddress("");
            //保存
            memberWalletService.save(wallet);
        }
        //注册活动奖励
        RewardActivitySetting rewardActivitySetting = rewardActivitySettingService.findByType(ActivityRewardType
                .REGISTER);
        if (rewardActivitySetting != null) {
            try {
                MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(rewardActivitySetting.getCoin()
                        , json.getLong("uid"));
                BigDecimal amount3 = JSONObject.parseObject(rewardActivitySetting.getInfo()).getBigDecimal("amount");
                if (memberWallet == null || amount3.compareTo(BigDecimal.ZERO) <= 0) {
                    return;
                }
                memberWallet.setReleaseBalance(amount3);
                memberWalletService.save(memberWallet);
                Member member = memberService.findOne(json.getLong("uid"));
                RewardRecord rewardRecord3 = new RewardRecord();
                rewardRecord3.setAmount(amount3);
                rewardRecord3.setCoin(rewardActivitySetting.getCoin());
                rewardRecord3.setMember(member);
                rewardRecord3.setRemark(rewardActivitySetting.getType().getCnName());
                rewardRecord3.setType(RewardRecordType.ACTIVITY);
                rewardRecordService.save(rewardRecord3);
                // 注册送币
                ReleaseBalance releaseBalance = new ReleaseBalance();
                releaseBalance.setMemberId(member.getId());
                releaseBalance.setUserName(member.getUsername());
                releaseBalance.setPhone(member.getMobilePhone());
                releaseBalance.setEmail(member.getEmail());
                releaseBalance.setRegisterTime(new Date());
                releaseBalance.setReleaseBalance(amount3);
                releaseBalance.setCoinName(rewardActivitySetting.getCoin().getName());
                releaseBalance.setCoinUnit(rewardActivitySetting.getCoin().getUnit());
                releaseBalance.setReleaseState("0");
                releaseBalanceService.save(releaseBalance);
            } catch (Exception e) {
                logger.info("注册活动奖励失败 rewardActivitySetting={}", e);
            }
        }
    }
}
