package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.SeFeeChangeStatus;
import cn.ztuo.bitrade.constant.SeFeeChangeType;
import cn.ztuo.bitrade.constant.SeFeeChangeWay;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 *
 * @author Seven
 * @date 2019年01月02日
 */
@Entity
@Data
public class SeFeeChangeRecord {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ApiModelProperty(value="用户ID")
    private  Long memberId;

    @ApiModelProperty(value="操作时间")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value="类型")
    private SeFeeChangeType type;

    @ApiModelProperty(value="状态")
    private SeFeeChangeStatus status;

    @ApiModelProperty(value="操作方式")
    private SeFeeChangeWay way;

}