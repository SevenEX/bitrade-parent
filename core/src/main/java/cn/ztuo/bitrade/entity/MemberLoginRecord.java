package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.annotation.Excel;
import cn.ztuo.bitrade.constant.LoginStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 会员用户
 *
 * @author Seven
 * @date 2019年01月02日
 */
@Entity
@Data
public class MemberLoginRecord {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private  Long memberId;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date loginTime;

    /**
     * 登录状态，0成功 1失败
     */
    private LoginStatus status;

    private String ip;

    private String way;

}