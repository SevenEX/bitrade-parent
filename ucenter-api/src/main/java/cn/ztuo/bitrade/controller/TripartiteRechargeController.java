package cn.ztuo.bitrade.controller;

import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.entity.MemberWallet;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.service.MemberWalletService;
import cn.ztuo.bitrade.util.DESUtil;
import cn.ztuo.bitrade.util.Md5;
import cn.ztuo.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @author Seven
 * @date 2019年07月13日
 */
@RestController
@Slf4j
@RequestMapping(value = "/tripartite/recharge", method = RequestMethod.POST)
public class TripartiteRechargeController {
    @Autowired
    private LocaleMessageSourceService sourceService;
    @Value("${tripartite.recharge.url:}")
    private String url;
    @Value("${tripartite.recharge.key:}")
    private String key;
    @Value("${tripartite.recharge.smac:}")
    private String smac;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberWalletService memberWalletService;


    /**
     * 三方充值接口
     *
     * @param body
     * @return
     */
    @PostMapping("apply")
    @Transactional(rollbackFor = Exception.class)
    public String recharge(@RequestBody String body) throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        try {
            //解密
            JSONObject resJson = JSONObject.parseObject(DESUtil.decrypt(body.trim(), key));
            if (resJson != null) {
                JSONObject data=resJson.getJSONObject("data");
                //验证签名
                if (Md5.md5Digest(data.toJSONString() + smac).equals(resJson.getString("sign"))) {
                    String phone = data.getString("user");
                    BigDecimal amount = data.getBigDecimal("amount");
                    String unit = data.getString("unit");
                    if (!(StringUtils.hasText(phone)&&memberService.findByPhone(phone)!=null)){
                        json.put("code", 1003);
                        json.put("message", "无此用户");
                    }else if (amount.compareTo(BigDecimal.ZERO)<=0){
                        json.put("code", 1004);
                        json.put("message", "充值金额不合法");
                    }else if (!(StringUtils.hasText(unit)&&memberWalletService.findByCoinUnitAndMemberId(unit,memberService.findByPhone(phone).getId())!=null)){
                        json.put("code", 1005);
                        json.put("message", "不支持该币种");
                    }else {
                        MemberWallet memberWallet=memberWalletService.findByCoinUnitAndMemberId(unit,memberService.findByPhone(phone).getId());
                        MessageResult messageResult=memberWalletService.recharge(memberWallet, amount);
                        if (messageResult.getCode()==0){
                            json.put("code", 0);
                            json.put("message", "成功");
                            json.put("user", phone);
                            json.put("unit", unit);
                            json.put("amount", amount);
                        }else {
                            json.put("code", 1006);
                            json.put("message", "失败");
                        }
                    }
                }else {
                    json.put("code", 1002);
                    json.put("message", "签名错误，数据可能被篡改");
                }
            }
        } catch (Exception e) {
            json.put("code", 1001);
            json.put("message", "加密错误");
            e.printStackTrace();
        }
        jsonObject.put("data", json);
        //制作签名
        jsonObject.put("sign", Md5.md5Digest(json.toJSONString() + smac));
        //加密
        String ciphertext = DESUtil.ENCRYPTMethod(jsonObject.toJSONString(), key).toUpperCase();
        return ciphertext;
    }

}
