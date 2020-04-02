package cn.ztuo.bitrade.model.create;

import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.Sign;
import cn.ztuo.bitrade.service.CoinService;
import cn.ztuo.bitrade.service.SignService;
import cn.ztuo.bitrade.util.DateUtil;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import org.springframework.util.Assert;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author MrGao
 * @Description:
 * @date 2018/5/311:24
 */
@Data
public class SignCreate {

    /**
     * 赠送币种
     */
    @NotBlank(message = "unit can not be empty!")
    private String unit;

    /**
     * 赠送数目
     */
    @Min(value = 0, message = "amount must gt 0!")
    private BigDecimal amount;

    /**
     * 结束日期
     */
    @NotNull(message = "endDate can not be null!")
    private Date endDate;

    public Sign transformation(SignService signService, CoinService coinService) {
        //校验是否签到进行中
        Sign underway = signService.fetchUnderway();
        Assert.isNull(underway, "validate underway!");

        //校验币种存在
        Coin coin = coinService.findByUnit(unit);
        Assert.notNull(coin, "validate unit!");

        //校验时间
        DateUtil.validateEndDate(endDate);

        //返回对象
        Sign sign = new Sign();
        sign.setAmount(amount);
        sign.setCoin(coin);
        sign.setEndDate(endDate);

        return sign;
    }
}
