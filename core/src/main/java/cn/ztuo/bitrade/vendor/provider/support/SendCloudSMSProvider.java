package cn.ztuo.bitrade.vendor.provider.support;

import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vendor.provider.SMSProvider;
import com.google.common.collect.ImmutableMap;
import com.sendcloud.sdk.builder.SendCloudBuilder;
import com.sendcloud.sdk.config.Config;
import com.sendcloud.sdk.core.SendCloud;
import com.sendcloud.sdk.model.SendCloudSms;
import com.sendcloud.sdk.util.ResponseData;

import java.util.Map;

public class SendCloudSMSProvider implements SMSProvider {

    public SendCloudSMSProvider(String username, String password) {
        Config.sms_user = username;
        Config.sms_key = password;
    }

    public static String getName() {
        return "sendcloud";
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws Exception {
        return null;
    }

    @Override
    public MessageResult sendVerifyMessage(String mobile, String verifyCode) throws Exception {
        return sendTemplateMessage(39150, mobile, false, ImmutableMap.of("code", verifyCode));
    }

    @Override
    public MessageResult sendInternationalMessage(String code, String phone) throws Exception {
        return sendTemplateMessage(39151, phone, true, ImmutableMap.of("code", code));
    }

    @Override
    public MessageResult sendTemplateMessage(int templateId, String phone, boolean isInternational, Map<String, String> params) throws Exception {
        SendCloudSms sms = new SendCloudSms();
        if(isInternational){
            sms.setMsgType(2);
        }
        else {
            sms.setMsgType(0);
        }
        sms.setTemplateId(templateId);
        sms.addPhone(phone);
        params.forEach(sms::addVars);
        SendCloud sc = SendCloudBuilder.build();
        try {
            return parseResult(sc.sendSms(sms));
        } catch (Throwable e) {
            throw new Exception(e);
        }
    }

    @Override
    public MessageResult sendLoginMessage(String ip, String phone) throws Exception {
        return null;
    }
    private MessageResult parseResult(ResponseData res) {
        MessageResult mr = new MessageResult(res.getStatusCode(), res.getMessage());
        if(mr.getCode() == 200){
            mr.setCode(0);
        }
        return mr;
    }
}
