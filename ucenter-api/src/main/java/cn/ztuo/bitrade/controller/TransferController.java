package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.util.*;
import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.exception.InformationExpiredException;
import cn.ztuo.bitrade.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;
import static cn.ztuo.bitrade.util.BigDecimalUtils.*;
import static org.springframework.util.Assert.*;

/**
 * @author Seven
 * @date 2019年02月27日
 */
@RestController
@Slf4j
@Api(tags = "转账")
@RequestMapping(value = "/transfer", method = RequestMethod.POST)
public class TransferController extends BaseController {
    @Autowired
    private LocaleMessageSourceService sourceService;
    @Autowired
    private TransferAddressService transferAddressService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private TransferRecordService transferRecordService;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${transfer.url:}")
    private String url;
    @Value("${transfer.key:}")
    private String key;
    @Value("${transfer.smac:}")
    private String smac;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private OtcCoinService otcCoinService ;

    @Autowired
    private LeverCoinService leverCoinService ;

    @Autowired
    private LocaleMessageSourceService messageSource;
    /**
     * 根据币种查询转账地址等信息
     *
     * @param unit
     * @param user
     * @return
     */
    @RequestMapping("address")
    @ApiOperation("根据币种查询转账地址等信息")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult transferAddress(String unit, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Coin coin = coinService.findByUnit(unit);
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, user.getId());
        List<TransferAddress> list = transferAddressService.findByCoin(coin);
        MessageResult result = MessageResult.success();
        result.setData(TransferAddressInfo.builder().balance(memberWallet.getBalance())
                .info(list.stream().map(x -> {
                    HashMap<String, Object> map = new HashMap<>(3);
                    map.put("address", x.getAddress());
                    map.put("rate", x.getTransferFee());
                    map.put("minAmount", x.getMinAmount());
                    return map;
                }).collect(Collectors.toList())).build());
        return result;
    }

    /**
     * 转账申请
     *
     * @param user
     * @param unit
     * @param address
     * @param amount
     * @param fee
     * @param jyPassword
     * @param remark
     * @return
     * @throws Exception
     */
    @RequestMapping("apply")
    @ApiOperation("转账申请")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult transfer(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, String unit, String address,
                                  BigDecimal amount, BigDecimal fee, String jyPassword, String remark) throws Exception {
        hasText(jyPassword, sourceService.getMessage("MISSING_JYPASSWORD"));
        hasText(unit, sourceService.getMessage("MISSING_COIN_TYPE"));
        Coin coin = coinService.findByUnit(unit);
        notNull(coin, sourceService.getMessage("COIN_ILLEGAL"));
        isTrue(coin.getStatus().equals(CommonStatus.NORMAL) && coin.getCanTransfer().equals(BooleanEnum.IS_TRUE), sourceService.getMessage("COIN_NOT_SUPPORT"));
        TransferAddress transferAddress = transferAddressService.findOnlyOne(coin, address);
        isTrue(transferAddress != null, sourceService.getMessage("WRONG_ADDRESS"));
        isTrue(fee.compareTo(BigDecimalUtils.mulRound(amount, BigDecimalUtils.getRate(transferAddress.getTransferFee()))) == 0, sourceService.getMessage("FEE_ERROR"));
        Member member = memberService.findOne(user.getId());
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, sourceService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(Md5.md5Digest(jyPassword + member.getSalt()).toLowerCase().equals(mbPassword), sourceService.getMessage("ERROR_JYPASSWORD"));
        memberWalletService.findWalletForUpdate(user.getId(), coin);
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, user.getId());
        isTrue(compare(memberWallet.getBalance(), BigDecimalUtils.add(amount, fee)), sourceService.getMessage("INSUFFICIENT_BALANCE"));
        int result = memberWalletService.deductBalance(memberWallet, BigDecimalUtils.add(amount, fee));
        if (result <= 0) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        TransferRecord transferRecord = new TransferRecord();
        transferRecord.setAmount(amount);
        transferRecord.setCoin(coin);
        transferRecord.setMemberId(user.getId());
        transferRecord.setFee(fee);
        transferRecord.setAddress(address);
        transferRecord.setRemark(remark);
        TransferRecord transferRecord1 = transferRecordService.save(transferRecord);

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAddress(address);
        memberTransaction.setAmount(BigDecimalUtils.add(fee, amount));
        memberTransaction.setMemberId(user.getId());
        memberTransaction.setSymbol(coin.getUnit());
        memberTransaction.setCreateTime(transferRecord1.getCreateTime());
        memberTransaction.setType(TransactionType.TRANSFER_ACCOUNTS);
        memberTransaction.setFee(transferRecord.getFee());
        memberTransactionService.save(memberTransaction);
        if (transferRecord1 == null) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        } else {
            JSONObject json = new JSONObject();
            //会员id
            json.put("uid", user.getId());
            //转账数目
            json.put("amount", amount);
            //转账手续费
            json.put("fee", fee);
            //币种单位
            json.put("coin", coin.getUnit());
            //转账地址
            json.put("address", address);
            //转账记录ID
            json.put("recordId", transferRecord1.getId());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", json);
            jsonObject.put("sign", Md5.md5Digest(json.toJSONString() + smac));
            String ciphertext = DESUtil.ENCRYPTMethod(jsonObject.toJSONString(), key).toUpperCase();
            String response = restTemplate.postForEntity(url, ciphertext, String.class).getBody();
            JSONObject resJson = JSONObject.parseObject(DESUtil.decrypt(response.trim(), key));
            if (resJson != null) {
                //验证签名
                if (Md5.md5Digest(resJson.getJSONObject("data").toJSONString() + smac).equals(resJson.getString("sign"))) {
                    if (resJson.getJSONObject("data").getIntValue("returnCode") == 1) {
                        transferRecord1.setOrderSn(resJson.getJSONObject("data").getString("returnMsg"));
                        log.info("============》转账成功");
                    }
                }
            }
            return MessageResult.success();
        }
    }

    /**
     * 转账记录
     *
     * @param user
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("record")
    @ApiOperation("转账记录")
    @MultiDataSource(name = "second")
    public MessageResult pageWithdraw(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, int pageNo, int pageSize) {
        MessageResult mr = new MessageResult(0, messageSource.getMessage("SUCCESS"));
        Page<TransferRecord> records = transferRecordService.findAllByMemberId(user.getId(), pageNo, pageSize);
        mr.setData(records);
        return mr;
    }

    @RequestMapping(value = "support_coin",method = RequestMethod.POST)
    //@ApiOperation("转账记录")
    public MessageResult getSupportTransferCoin(String coinUnit){
        List<OtcCoin> otcCoins =new ArrayList<>();
        List<LeverCoin> leverCoins  ;
        if(!StringUtils.isEmpty(coinUnit)){
           OtcCoin otcCoin = otcCoinService.findOtcCoinByUnitAndStatus(coinUnit,CommonStatus.NORMAL);
           otcCoins.add(otcCoin);
            leverCoins = leverCoinService.findLeverCoinByCoinUnitAndEnable(coinUnit,BooleanEnum.IS_TRUE);
        }else {
            otcCoins = otcCoinService.getNormalCoin();
            leverCoins = leverCoinService.findByEnable(BooleanEnum.IS_TRUE);
        }
        JSONObject resultJson = new JSONObject();
        resultJson.put("supportOtcCoins",otcCoins);
        resultJson.put("supportLeverCoins",leverCoins);
      return success(resultJson);
    }

}
