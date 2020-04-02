package cn.ztuo.bitrade.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
public class PlatformTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "decimal(20,8) default 0.00 ")
    private BigDecimal amount;
    private String bizOrderId;
    //1. 充值，2 提现 3 奖励 ， 30 法币交易手续费，31 币币交易手续费 32 币币交易市价买找零 20 商家保证金 21 商家退保,31
    private int type;
    // 1. 表示增加，2. 表示减少，3. 表示从会员向手续费池划转，4 表示从手续费池向会员划转
    private int direction;
    private String day;
    private Date time;
}
