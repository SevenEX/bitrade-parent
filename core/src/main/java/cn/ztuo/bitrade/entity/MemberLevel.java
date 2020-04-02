package cn.ztuo.bitrade.entity;

import lombok.Data;
import javax.validation.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author MrGao
 * @description 会员等级实体
 * @date 2017/12/26 17:12
 */
@Data
@Entity
@Table(name = "member_level")
public class MemberLevel {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    @NotBlank(message = "{MemberLevel.name.blank}")
    private String name;
    @NotNull(message = "{PARAM.NULL}")
    private Boolean isDefault;
    private String remark;

}
