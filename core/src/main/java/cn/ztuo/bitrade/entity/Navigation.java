package cn.ztuo.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.SysHelpClassification;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author MrGao
 * @description 系统帮助
 * @date 2018/1/9 9:38
 */
@Entity
@Data
@Table(name = "navigation")
public class Navigation {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Excel(name = "编号", orderNum = "1", width = 20)
    private Long id;

    private String title;

    private String type;

    private String url = "";

    @Excel(name = "创建时间", orderNum = "1", width = 20)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @NotNull(message = "状态不能为空")
    private CommonStatus status = CommonStatus.NORMAL;

    private int sort = 0 ;

    /**
     * 语种（"en-US", "zh-CN", "ja-JP", "ko-KR", "ar-AE"）
     */
    @Column(name = "LOCALE", nullable = false, length = 6)
    private String locale;

}
