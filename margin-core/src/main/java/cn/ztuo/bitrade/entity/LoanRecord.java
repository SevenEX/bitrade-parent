package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.ztuo.bitrade.constant.BooleanEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 借贷记录
 * @author zhang yingxin
 * @date 2018/5/25
 */
@Entity
@Data
@Table
public class LoanRecord {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long memberId;
    private String memberName;

    /**
     * 借贷币种
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
     * 未归还的借贷金额（每次还款会用这个字段的值减去还款数量）
     */
    @Column(columnDefinition = "decimal(20,8) comment '未归还的借贷金额'")
    private BigDecimal amount;

    @Column(columnDefinition = "decimal(20,8) comment '借贷利率'")
    private BigDecimal interestRate;

    /**
     * 借贷金额（记录值，不变化）
     */
    @Column(columnDefinition = "decimal(20,8) comment '借贷金额（记录值，不变化）'")
    private BigDecimal loanBalance=BigDecimal.ZERO;

    /**
     * 累计利息
     */
    @Column(columnDefinition = "decimal(20,8) comment '累计利息'")
    private BigDecimal accumulative=BigDecimal.ZERO;

    /**
     * 借款时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonIgnore
    @Version
    private int version;

    /**
     * 是否已经还款，0否，1是
     */
    private BooleanEnum repayment=BooleanEnum.IS_FALSE;
}
