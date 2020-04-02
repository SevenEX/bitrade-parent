package cn.ztuo.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.WorkOrderType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @description 工单管理
 */
@Entity
@Data
@Table(name = "work_order")
public class WorkOrder {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Excel(name = "编号", orderNum = "1", width = 20)
    private Long id;

    @Excel(name = "UID", orderNum = "1", width = 20)
    private Long memberId;

    @Excel(name = "描述", orderNum = "1", width = 20)
    @NotBlank(message = "{WorkOrder.description.blank}")
    private String description;

    @Excel(name = "分类", orderNum = "1", width = 20)
    @NotNull(message = "{WORK.ORDER.TYPE.NULL}")
    private WorkOrderType type;

    @Excel(name = "图片地址", orderNum = "1", width = 20)
    private String imgUrl = "";

    @Excel(name = "联系方式", orderNum = "1", width = 20)
    private String contact;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private CommonStatus status;


    @Excel(name = "回复时间", orderNum = "1", width = 20)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date replyTime;

    @Column(columnDefinition="TEXT")
    @Basic(fetch=FetchType.LAZY)
    private String detail = "";

}
