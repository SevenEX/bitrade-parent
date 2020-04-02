package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author MrGao
 * @description 公告
 * @date 2018/3/5 14:59
 */
@Entity
@Data
@Table
public class Announcement {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull(message = "{TITLE.NULL}")
    private String title;

    @Column(columnDefinition="TEXT")
    @Basic(fetch=FetchType.LAZY)
    private String content;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //是否显示
    private Boolean isShow;

    @Column(nullable = true)
    private String imgUrl;

    private int sort = 0 ;

    /**
     * 是否置顶（0，置顶  1，不置顶（默认））
     */
    private String isTop = "1" ;

    /**
     * 语种（"en-US", "zh-CN", "ja-JP", "ko-KR", "ar-AE"）
     */
    @Column(name = "LOCALE", nullable = false, length = 6)
    private String locale;
}
