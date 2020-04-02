package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.dto.SmsDTO;
import cn.ztuo.bitrade.service.MemberWalletService;
import cn.ztuo.bitrade.service.SmsService;
import cn.ztuo.bitrade.util.MessageResult;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Description:
 * @author: Seven
 * @date: create in 10:47 2018/6/28
 * @Modified:
 */
@RestController
@RequestMapping("/smstest")
@ToString
public class TestController extends BaseController{
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private MemberWalletService memberWalletService;
    @RequestMapping(value = "sms",method = RequestMethod.GET)
    public MessageResult testSms(){
        SmsDTO smsDTO = smsService.getByStatus();
        System.out.println(smsDTO);
        System.out.println(success("succss",smsDTO));
        
        return success(smsDTO);
    }

}
