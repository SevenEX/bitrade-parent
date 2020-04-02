package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.CommonStatus;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

/**
 * @author MrGao
 * @description
 * @date 2017/12/29 14:14
 */
@Entity
@Data
@Table(name = "coin_area")
@ToString
public class CoinArea {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    /**
     * 中文
     */
    private String name;

    private int sort;

    private CommonStatus status = CommonStatus.NORMAL;
}
