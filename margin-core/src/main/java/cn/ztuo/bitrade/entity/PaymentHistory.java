package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.enums.PaymentType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 借贷、还款、划转记录
 */
@Entity
@Data
@Table
public class PaymentHistory {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long memberId;

    //只在类型是借贷和还款时有值
    private Long loanRecordId;

    /**
     * 还款币种
     */
    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    /**
     * 杠杆币对
     */
    @ManyToOne
    @JoinColumn(name="lever_coin_id")
    private LeverCoin leverCoin;

    /**
     * 记录类型，0转入，1转出，2借贷，3还款
     */
    private PaymentType paymentType;
    /**
     * 还款时状态，0正常，1爆仓，2穿仓
     */
    private Integer status=0;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Column(columnDefinition = "decimal(20,8) comment '金额'")
    private BigDecimal amount=BigDecimal.ZERO;

    @Column(columnDefinition = "decimal(20,8) comment '还款的本金数额'")
    private BigDecimal principal=BigDecimal.ZERO;

    @Column(columnDefinition = "decimal(20,8) comment '还款的利息数额'")
    private BigDecimal interest=BigDecimal.ZERO;

    @Column(columnDefinition = "decimal(20,8) comment '借贷利率'")
    private BigDecimal interestRate;
}
