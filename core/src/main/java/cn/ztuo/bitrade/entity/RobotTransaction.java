package cn.ztuo.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.ztuo.bitrade.constant.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: RobotTransaction
 * 机器人交易明细
 * @author: MrGao
 * @create: 2019/04/30 14:13
 */
@Entity
@Data
public class RobotTransaction {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Excel(name = "会员id", orderNum = "2", width = 25)
    private Long memberId;
    /**
     * 交易金额
     */
    @Excel(name = "交易金额", orderNum = "3", width = 25)
    @Column(columnDefinition = "decimal(20,8) comment '充币金额'")
    private BigDecimal amount;

    /**
     * 创建时间
     */
    @Excel(name = "创建时间", orderNum = "4", width = 25)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 交易类型
     */
    @Excel(name = "交易类型", orderNum = "5", width = 25)
    @Enumerated(EnumType.ORDINAL)
    private TransactionType type;
    /**
     * 币种名称，如 BTC
     */
    private String symbol;

    /**
     * 交易手续费
     * 提现和转账才有手续费，充值没有;如果是法币交易，只收发布广告的那一方的手续费
     */
    @Column(precision = 19,scale = 8)
    private BigDecimal fee = BigDecimal.ZERO ;

}
