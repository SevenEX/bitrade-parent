package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.BooleanEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @author Seven
 * @date 2019年01月16日
 */
@Builder
@Data
public class MemberAccount {
    private String realName;
    private BooleanEnum bankVerified;
    private BooleanEnum aliVerified;
    private BooleanEnum wechatVerified;
    private BankInfo bankInfo;
    private Alipay alipay;
    private WechatPay wechatPay;
    private Integer memberLevel;
}
