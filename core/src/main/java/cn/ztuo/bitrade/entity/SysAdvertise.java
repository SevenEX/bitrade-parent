package cn.ztuo.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.SysAdvertiseLocation;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import javax.validation.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author MrGao
 * @description 系统广告
 * @date 2018/1/6 15:06
 */
@Entity
@Data
@Table
public class SysAdvertise {

    @Id
    private String serialNumber;//编号
    @Excel(name = "系统广告名称", orderNum = "1", width = 20)
    @NotBlank(message = "{SysAdvertise.name.blank}")
    private String name;//名称
    @Excel(name = "系统广告位置", orderNum = "1", width = 20)
    @NotNull(message = "{SYS.ADVERTISE.LOCATION}")
    private SysAdvertiseLocation sysAdvertiseLocation;//广告位置
    //@Excel(name = "开始时间", orderNum = "1", width = 20)
    //@NotBlank(message = "开始时间不能空")
    private String startTime;//开始时间
    //@Excel(name = "结束时间", orderNum = "1", width = 20)
    //@NotBlank(message = "结束时间不能为空")
    private String endTime;//结束时间
    @Excel(name = "url", orderNum = "1", width = 20)
    @NotBlank(message = "{SysAdvertise.url.blank}")
    private String url;

    /**
     * 图片链接url
     */
    private String linkUrl;

    private String remark;//备注

    @NotNull(message = "{STATUS.NULL}")
    private CommonStatus status = CommonStatus.NORMAL;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Column(columnDefinition = "TEXT")
    @Basic(fetch = FetchType.LAZY)
    private String content;

    private String author;

    private int sort = 0 ;

    /**
     * 语种（"en-US", "zh-CN", "ja-JP", "ko-KR", "ar-AE"）
     */
    @Column(name = "LOCALE", nullable = false, length = 6)
    private String locale;

}
