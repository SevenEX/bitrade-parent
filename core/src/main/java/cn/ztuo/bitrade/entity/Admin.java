package cn.ztuo.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.ztuo.bitrade.constant.CommonStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import javax.validation.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Seven
 * @date 2019年12月18日
 */
@Entity
@Data
@Table
public class Admin implements Serializable {
    @Excel(name = "用户编号", orderNum = "1", width = 25)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Excel(name = "用户登录名", orderNum = "1", width = 25)
    @NotBlank(message = "{Admin.username.blank}")
    @Column(nullable = false,unique = true)
    private String username;

    @JsonIgnore
    private String password;

    private CommonStatus enable;

    @Excel(name = "用户最后登录时间", orderNum = "1", width = 25)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    @Excel(name = "最后登录ip", orderNum = "1", width = 25)
    private String lastLoginIp;

    @NotNull(message = "{ROLE.ID.NULL}")
    private Long roleId = 1L;


    @NotFound(action= NotFoundAction.IGNORE)
    @JoinColumn(name = "department_id")
    @ManyToOne(cascade = CascadeType.MERGE)
    private Department department;

    @Excel(name = "真实姓名", orderNum = "1", width = 25)
    @NotBlank(message = "{Admin.realName.blank}")
    private String realName;

    @Excel(name = "联系号码", orderNum = "1", width = 25)
    @NotBlank(message = "{Admin.mobilePhone.blank}")
    private String mobilePhone;


    private String qq;

    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 状态
     */
    private CommonStatus status = CommonStatus.NORMAL;

    @Transient
    private String roleName ;

    @Excel(name = "googleKey", orderNum = "1", width = 20)
    private String googleKey;

    @Excel(name = "googleState", orderNum = "1", width = 20)
    private Integer googleState=0;

    @Excel(name = "googleDate", orderNum = "1", width = 20)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date googleDate;

    private String areaCode;

    /**
     * 创建时间
     */
    @Excel(name = "createTime", orderNum = "1", width = 20)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
