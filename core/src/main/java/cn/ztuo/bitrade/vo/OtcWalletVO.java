package cn.ztuo.bitrade.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/5/5 5:03 PM
 */
@Data
public class OtcWalletVO {


    private String coinName;

    private BigDecimal amount;

    /**
     * 0-币币转法币，1-法币转币币
     */
    private String direction;

//    private String jyPassword;
}
