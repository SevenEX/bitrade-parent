package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.BooleanEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(uniqueConstraints ={@UniqueConstraint(columnNames={"memberId", "createTime"})})
public class MemberWalletSeHistory {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long memberId;

    /**
     * 可用余额
     */
    @Column(columnDefinition = "decimal(20,8) comment '可用余额'")
    private BigDecimal balance;
    /**
     * 冻结余额
     */
    @Column(columnDefinition = "decimal(20,8) comment '冻结余额'")
    private BigDecimal frozenBalance;

    /**
     * 释放余额
     */
    @Column(columnDefinition = "decimal(20,8) comment '释放余额'")
    private BigDecimal releaseBalance;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
}
