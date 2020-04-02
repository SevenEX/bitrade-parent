package cn.ztuo.bitrade.model.screen;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberWalletScreen extends AccountScreen{

    String unit ;

    String walletAddress ;

    BigDecimal minBalance ;

    BigDecimal maxBalance ;

    BigDecimal minFrozenBalance;

    BigDecimal maxFrozenBalance ;

    BigDecimal minAllBalance ;

    BigDecimal maxAllBalance ;

}
