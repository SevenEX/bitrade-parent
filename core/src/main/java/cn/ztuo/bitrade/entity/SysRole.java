package cn.ztuo.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 系统角色
 *
 * @author Seven
 * @date 2019年12月18日
 */
@Entity
@Data
@Table(name = "admin_role")
public class SysRole {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 角色名
     */
    @Excel(name = "角色名", orderNum = "1", width = 20)
    @NotNull(message = "{ROLE.NAME.NULL}")
    private String role;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    @Excel(name = "创建时间", orderNum = "1", width = 20)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.PERSIST)//该fetch保证能通过id查到role，否则会报延迟加载异常
    @JoinTable(name = "admin_role_permission",
            uniqueConstraints = {@UniqueConstraint(columnNames={"role_id", "rule_id"})},
            joinColumns = {@JoinColumn(name = "role_id",referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "rule_id",referencedColumnName = "id")})
    @JsonIgnore
    private List<SysPermission> permissions;

    public SysRole(Long id, String role) {
        this.id = id;
        this.role = role;
    }

    public SysRole(Long id, String role,String description) {
        this.id = id;
        this.role = role;
        this.description = description ;
    }

    public SysRole(){}

}
