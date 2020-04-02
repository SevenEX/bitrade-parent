package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.CommonStatus;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author Seven
 * @date 2019年02月27日
 */
@Entity
@Data
public class TransferAddress {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @JoinColumn(name = "coin_id")
    @ManyToOne
    private Coin coin;
    private String address;
    @Column(columnDefinition = "decimal(18,6) comment '转账手续费率'")
    private BigDecimal transferFee=BigDecimal.ZERO;
    @Column(columnDefinition = "decimal(20,8) comment '最低转账数目'")
    private BigDecimal minAmount=BigDecimal.ZERO;
    @Enumerated(EnumType.ORDINAL)
    private CommonStatus status=CommonStatus.NORMAL;
}
