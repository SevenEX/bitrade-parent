package cn.ztuo.bitrade.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Seven
 * @date 2019年12月19日
 */
@AllArgsConstructor
@Getter
public enum AdminModule {
    CMS("系统管理"),
    COMMON("COMMON"),
    EXCHANGE("币币交易"),
    FINANCE("资金管理"),
    MEMBER("用户管理"),
    OTC("法币交易"),
    SYSTEM("系统管理"),
    PROMOTION("PROMOTION"),
    INDEX("首页"),
    IEO("IEO"),
    GIFT("糖果管理"),
    MARGIN("杠杆交易"),
    BUSINESSAUTH("商家认证"),
    CHANNEL("渠道管理"),
    ADVERTISE("轮播管理"),
    HELP("帮助中心"),
    POSTER("海报管理"),
    WORKORDER("工单管理"),
    INVITE("邀请管理"),
    RECHARGE("充值管理"),
    WITHDRAW("提现管理"),
    ANNOUNCEMENT("通知公告"),
    APP("APP版本管理"),
    COIN("币种管理"),
    EMPLOYEE("管理员管理");
    @Setter
    private String title;
}