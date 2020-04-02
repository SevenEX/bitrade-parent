package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.RewardRecordType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 返佣榜单
 * @author Seven
 * @date 2019年03月08日
 */
@Data
@Entity
@IdClass(RewardStatisticsId.class)
public class RewardStatistics {

    private BigDecimal amount;
    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne
    private Member member;

    @Id
    @JoinColumn(name = "order_member_id", nullable = false)
    @ManyToOne
    private Member orderMember;
    /**
     * 创建时间
     */
    @Id
    private String createTime;

}
