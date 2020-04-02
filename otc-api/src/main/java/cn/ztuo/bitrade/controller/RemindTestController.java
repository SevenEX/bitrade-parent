package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.entity.Country;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.Order;
import cn.ztuo.bitrade.entity.OtcCoin;
import cn.ztuo.bitrade.remind.RemindService;
import cn.ztuo.bitrade.remind.RemindType;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 提醒测试接口
 *
 * @author Paradise
 */
@RestController
@RequestMapping("/remind")
@Api("RemindTestController")
public class RemindTestController {

    private final RemindService remindService;

    public RemindTestController(RemindService remindService) {
        this.remindService = remindService;
    }

    @RequestMapping("/remind")
    @ApiOperation("提醒测试")
    public MessageResult remind(RemindType type) {
        Member member = new Member();
        member.setMobilePhone("15655189198");
        Country country = new Country();
        country.setAreaCode("86");
        member.setCountry(country);
        member.setEmail("824507210@qq.com");
        member.setEmailRemind("1");
        member.setSmsRemind("1");
        member.setUsername("Paradise");

        Order order = new Order();
        order.setOrderSn("202002291703");
        order.setNumber(new BigDecimal("100.250"));
        OtcCoin coin = new OtcCoin();
        coin.setUnit("ETH");
        order.setCoin(coin);
        remindService.sendInfo(member, order, type);
        return MessageResult.success();
    }
}
