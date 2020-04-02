package cn.ztuo.bitrade.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.IntegrationRecordType;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.constant.WithdrawStatus;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.MessageResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TreeMap;

@Component
public class FinanceConsumer {
    private Logger logger = LoggerFactory.getLogger(FinanceConsumer.class);
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private WithdrawRecordService withdrawRecordService;
    @Autowired
    private DataDictionaryService dictionaryService ;
    @Autowired
    private IntegrationRecordService integrationRecordService ;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberGradeService gradeService;

    private String serviceName = "bitrade-market";


    /**
     * 处理充值消息，key值为币种的名称（注意是全称，如Bitcoin）
     *
     * @param record
     */
    @KafkaListener(topics = {"deposit"})
    public void handleDeposit(ConsumerRecord<String, String> record) {
        logger.info("topic={},accessKey={},value={}", record.topic(), record.key(), record.value());
        if (StringUtils.isEmpty(record.value())) {
            return;
        }
        JSONObject json = JSON.parseObject(record.value());
        if (json == null) {
            return;
        }
        BigDecimal amount = json.getBigDecimal("amount");
        String txid = json.getString("txid");
        String address = json.getString("address");
        CoinChainRelation relation = coinService.findRelationByKey(record.key());
        Coin coin;
        Long memberId;
        if(relation == null) {
            coin = coinService.findOne(record.key());
            if(coin == null) {
                return;
            }
            MemberWallet wallet = walletService.findByCoinAndAddress(coin, address);
            if(wallet == null) {
                return;
            }
            memberId = wallet.getMemberId();
        }
        else {
            coin = relation.getCoin();
            MemberWalletRelation walletRelation = walletService.findWalletRelationByCoinKeyAndAddress(relation.getCoinKey(), address);
            if(walletRelation == null) {
                return;
            }
            memberId = walletRelation.getMemberId();
        }


        logger.info("coin={}", coin);
        if (coin != null
                && walletService.findDeposit(address, txid) == null
                && amount.compareTo(coin.getMinRechargeAmount()) >= 0) {
            // 充值
            MessageResult mr = walletService.recharge(coin, memberId, address, amount, txid, BooleanEnum.IS_FALSE);
            //处理积分消息 获取币币充值积分赠送比例
            try {
                DataDictionary dictionary = dictionaryService.findByBond(SysConstant.INTEGRATION_GIVING_EXCHANGE_RECHARGE_USDT_RATE);
                Long integration ;
                if("usdt".equalsIgnoreCase(coin.getUnit())){
                    integration = amount.multiply(new BigDecimal(dictionary.getValue())).setScale(0, BigDecimal.ROUND_DOWN).longValue();
                }else {
                    String legalCoin = "USD";
                    String unit = coin.getUnit();
                    String url = "http://" + serviceName + "/market/exchange-rate/{legalCoin}/{coin}";
                    ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class,legalCoin, unit);
                    logger.info("remote call:url={},baseCoin={},unit={},result={}", url,legalCoin,unit, result);
                    if (result.getStatusCode().value() == 200 && result.getBody().getCode() == 0) {
                        BigDecimal rate = new BigDecimal((String) result.getBody().getData());
                        BigDecimal coverUsd = amount.multiply(rate);
                        integration = amount.multiply(new BigDecimal(dictionary.getValue())).setScale(0, BigDecimal.ROUND_DOWN).longValue();
                    } else {
                        integration= 0L;
                        logger.info("获取币种对usd汇率失败={}",result);
                    }
                }
                //给会员加积分
                Member member = memberService.findOne(memberId);
                Long totalIntegration = member.getIntegration()+integration;
                //判断等级是否满足 V5 V6 不改变等级 只加积分
                MemberGrade grade = gradeService.findOne(member.getMemberGradeId());
                if(grade.getId()!=5L && grade.getId()!=6L) {
                    if (grade.getGradeBound() < totalIntegration) {
                        member.setMemberGradeId(member.getMemberGradeId() + 1);
                    }
                }
                member.setIntegration(totalIntegration);
                IntegrationRecord integrationRecord = new IntegrationRecord();
                integrationRecord.setAmount(integration);
                integrationRecord.setMemberId(member.getId());
                integrationRecord.setCreateTime(new Date());
                integrationRecord.setType(IntegrationRecordType.COIN_RECHARGE_GIVING);
                integrationRecordService.save(integrationRecord);
            } catch (Exception e) {
                logger.info("币币充值积分赠送失败={}",e);
            }
            logger.info("wallet recharge result:{}", mr);
        }
    }

    /**
     * 处理提交请求,调用钱包rpc，自动转账
     *
     * @param record
     */
    @KafkaListener(topics = {"withdraw"})
    public void handleWithdraw(ConsumerRecord<String, String> record) {
        logger.info("topic={},accessKey={},value={}", record.topic(), record.key(), record.value());
        if (StringUtils.isEmpty(record.value())) {
            return;
        }
        JSONObject json = JSON.parseObject(record.value());
        Long withdrawId = json.getLong("withdrawId");
        String coinUnit = json.getString("coinUnit");
        try {
            String serviceName = "SERVICE-RPC-" + record.key().toUpperCase();
            String url = "http://" + serviceName + "/rpc/withdraw";
            Coin coin = coinService.findByUnit(coinUnit);
            if (coin != null && coin.getCanAutoWithdraw() == BooleanEnum.IS_TRUE) {
                logger.info("coin = {}",coin.toString());
                BigDecimal minerFee = coin.getMinerFee();
                String remark = json.containsKey("remark") ? json.getString("remark") : "";
                TreeMap<String, String> map = new TreeMap<>();
                map.put("address",json.getString("address"));
                map.put("amount",json.getString("arriveAmount"));
                map.put("fee",minerFee.toString());
                map.put("remark",remark);
                map.put("sync","false");
                map.put("withdrawId",withdrawId.toString());
                String param = coinService.sign(map);

                MessageResult result = restTemplate.getForObject(url+ param,
                        MessageResult.class);
                logger.info("result = {}", result);
                if (result.getCode() == 0 && result.getData() != null) {
                    //处理成功,data为txid，更新业务订单
                    String txid = (String) result.getData();
                    withdrawRecordService.withdrawSuccess(withdrawId, txid);
                }
                else if(result.getCode() == 200){
                    //提币转账中，等待通知
                    logger.info("====================== 提币转为异步转账 ==================================");
                    withdrawRecordService.withdrawTransfering(withdrawId);
                }
                else {
                    logger.info("====================== 自动转账失败，转为人工处理 ==================================");
                    //自动转账失败，转为人工处理
                    withdrawRecordService.autoWithdrawFail(withdrawId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("auto withdraw failed,error={}", e.getMessage());
            //自动转账失败，转为人工处理
            withdrawRecordService.autoWithdrawFail(withdrawId);
        }
    }

    /**
     * 异步打钱后返回状态
     * @param record
     */
    @KafkaListener(topics = {"withdraw-notify"})
    public void withdrawNotify(ConsumerRecord<String, String> record){
        logger.info("topic={},accessKey={},value={}", record.topic(), record.key(), record.value());
        if (StringUtils.isEmpty(record.value())) {
            return;
        }
        JSONObject json = JSON.parseObject(record.value());
        Long withdrawId = json.getLong("withdrawId");
        WithdrawRecord withdrawRecord=withdrawRecordService.findOne(withdrawId);
        if(withdrawRecord==null){
            return;
        }
        String txid=json.getString("txid");
        int status=json.getInteger("status");
        //转账失败，状态变回等待放币
        if(status==0){
            //自动转账失败，转为人工处理
            withdrawRecord.setIsAuto(BooleanEnum.IS_FALSE);
            withdrawRecord.setStatus(WithdrawStatus.WAITING);
            withdrawRecordService.save(withdrawRecord);

        }else if(status==1){
            withdrawRecordService.withdrawSuccess(withdrawId, txid);
        }
    }
}
