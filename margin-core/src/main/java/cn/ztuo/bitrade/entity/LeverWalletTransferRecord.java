package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 杠杆钱包充入、转出记录
 */
@Entity
@Data
@Table
public class LeverWalletTransferRecord {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private Long memberId;

    private String memberName;

    @ManyToOne
    @JoinColumn(name = "lever_coin_id")
    private LeverCoin leverCoin;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @Column(columnDefinition = "decimal(20,8) comment '金额'")
    private BigDecimal amount=BigDecimal.ZERO;

    @JsonIgnore
    @Version
    private int version;

    /**
     * 类型，0转入，1转出
     */
    private Integer type;

    /**
     * 划转时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
