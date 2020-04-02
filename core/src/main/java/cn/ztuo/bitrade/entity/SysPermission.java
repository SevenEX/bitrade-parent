package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.validation.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 系统权限
 *
 * @author Seven
 * @date 2019年12月18日
 */
@Entity
@Data
@Table(name = "admin_permission")
public class SysPermission {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotBlank(message="{SysPermission.title.blank}")
    private String title;

    private String description;

    /**
     * 为0表示是菜单
     */
    private Long parentId=0L;

    private Integer sort = 0;

    @NotBlank(message="{SysPermission.name.blank}")
    private String name;

   @ManyToMany(cascade = CascadeType.PERSIST,mappedBy = "permissions",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SysRole> roles ;
}
