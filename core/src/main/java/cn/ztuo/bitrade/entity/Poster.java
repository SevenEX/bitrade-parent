package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.CommonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 海报实体信息
 *
 * @author dz
 * @date 2018/1/9 9:38
 */
@Entity
@Data
@Table(name = "poster")
@ApiModel(description = "海报配置", value = "海报配置")
public class Poster {
    @ApiModelProperty(value = "ID(新增不需要)")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @ApiModelProperty(value = "备注信息")
    private String remark;

    @ApiModelProperty(value = "海报图片url")
    @NotBlank
    private String img = "";

    @ApiModelProperty(hidden = true)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(hidden = true)
    @NotNull(message = "状态不能为空")
    private CommonStatus status = CommonStatus.NORMAL;

    /**
     * 语种（"en-US", "zh-CN", "ja-JP", "ko-KR", "ar-AE"）
     */
    @Column(name = "locale", nullable = false, length = 6)
    @NotBlank
    private String locale;

    @ApiModelProperty(value = "名称")
    @NotBlank
    private String name;

    @ApiModelProperty(value = "排序")
    @NotNull
    private Integer sort;

}
