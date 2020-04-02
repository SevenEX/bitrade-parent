package cn.ztuo.bitrade.config;

import cn.ztuo.bitrade.vendor.provider.SMSProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsProviderConfig {

    @Value("${sms.gateway:}")
    private String gateway;
    @Value("${sms.username:}")
    private String username;
    @Value("${sms.password:}")
    private String password;
    @Value("${sms.sign:}")
    private String sign;
    @Value("${sms.internationalGateway:}")
    private String internationalGateway;
    @Value("${sms.internationalUsername:}")
    private String internationalUsername;
    @Value("${sms.internationalPassword:}")
    private String internationalPassword;


    @Bean
    public SMSProvider getSMSProvider(@Value("${sms.driver:}") String driverName) {
        if (StringUtils.isEmpty(driverName)) {
            return new cn.ztuo.bitrade.vendor.provider.support.ChuangRuiSMSProvider(gateway, username, password, sign);
        }
        if (driverName.equalsIgnoreCase(cn.ztuo.bitrade.vendor.provider.support.SendCloudSMSProvider.getName())) {
            return new cn.ztuo.bitrade.vendor.provider.support.SendCloudSMSProvider(username, password);
        } else if (driverName.equalsIgnoreCase(cn.ztuo.bitrade.vendor.provider.support.ChuangRuiSMSProvider.getName())) {
            return new cn.ztuo.bitrade.vendor.provider.support.ChuangRuiSMSProvider(gateway, username, password, sign);
        } else if (driverName.equalsIgnoreCase(cn.ztuo.bitrade.vendor.provider.support.EmaySMSProvider.getName())) {
            return new cn.ztuo.bitrade.vendor.provider.support.EmaySMSProvider(gateway, username, password);
        } else if (driverName.equalsIgnoreCase(cn.ztuo.bitrade.vendor.provider.support.HuaXinSMSProvider.getName())) {
            return new cn.ztuo.bitrade.vendor.provider.support.HuaXinSMSProvider(gateway, username, password,internationalGateway,internationalUsername,internationalPassword,sign);
        } else if(driverName.equalsIgnoreCase(cn.ztuo.bitrade.vendor.provider.support.TwoFiveThreeProvider.getName())){
            return new cn.ztuo.bitrade.vendor.provider.support.TwoFiveThreeProvider(gateway,username,password,sign);
        } else if(driverName.equalsIgnoreCase(cn.ztuo.bitrade.vendor.provider.support.QinPengSMSProvider.getName())){
            return new cn.ztuo.bitrade.vendor.provider.support.QinPengSMSProvider(gateway,username,password);
        } else if(driverName.equalsIgnoreCase(cn.ztuo.bitrade.vendor.provider.support.YunpianSMSProvider.getName())){
            return new cn.ztuo.bitrade.vendor.provider.support.YunpianSMSProvider(gateway,password);
        } else {
            return null;
        }
    }
}
