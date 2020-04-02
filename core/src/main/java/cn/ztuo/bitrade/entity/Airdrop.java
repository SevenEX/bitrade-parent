package cn.ztuo.bitrade.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 空投记录
 */
@Entity
@Data
@Table
public class Airdrop {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Date createTime;
    @JoinColumn(name = "admin_id")
    @ManyToOne
    private Admin admin;//操作人
    private Integer errorIndex;//报错时的下标
    private Integer successCount;//成功的数量
    private String fileName;
    private Integer status;//0解析中，1成功，2失败
    @Column(columnDefinition="TEXT")
    private String errorMsg;//报错信息
}
