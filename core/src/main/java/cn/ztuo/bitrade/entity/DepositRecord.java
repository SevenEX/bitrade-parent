package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.DepositStatusEnum;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 缴纳押金记录（包括缴纳押金和取回押金）
 * @author zhang yingxin
 * @date 2018/5/5
 */
@Entity
@Data
@Table(name = "deposit_record")
public class DepositRecord {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @Column(precision = 18,scale = 8)
    private BigDecimal amount;

    /**
     * 动作，缴纳或索回
     */
    @Enumerated(EnumType.ORDINAL)
    private DepositStatusEnum status;
}
