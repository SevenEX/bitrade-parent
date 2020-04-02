package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.IntegrationRecordType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: IntegrationRecord
 * @author: MrGao
 * @create: 2019/04/25 16:45
 */
@Data
@Entity
public class IntegrationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * 会员id  外键
     */
    private Long memberId;
    /**
     * 赠送类型
     */
    @Enumerated(EnumType.ORDINAL)
    private IntegrationRecordType type;
    /**
     * 赠送数量
     */
    private Long amount;
    /**
     * 创建时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
