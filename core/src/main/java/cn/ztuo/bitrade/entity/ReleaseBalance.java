package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
public class ReleaseBalance {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    // 用户id
    private Long memberId;
    // 币种名字
    private String coinName;
    // 币种单位
    private String coinUnit;
    /**
     * 注册时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registerTime;
    /**
     * 昵称
     */
    private String userName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 释放余额
     */
    private BigDecimal releaseBalance;
    /**
     * 释放余额状态  0 - 未审核   1 - 已审核
     */
    private String releaseState;
    /**
     * 释放时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date releaseTime;
}
