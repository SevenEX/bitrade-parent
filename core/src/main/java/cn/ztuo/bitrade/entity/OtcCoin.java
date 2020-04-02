package cn.ztuo.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.CommonStatus;
import lombok.Data;
import javax.validation.constraints.NotBlank;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * OTC币种
 *
 * @author Seven
 * @date 2019年01月09日
 */
@Entity
@Data
public class OtcCoin {
    @Excel(name = "otc货币编号", orderNum = "1", width = 20)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Excel(name = "otc货币名称", orderNum = "1", width = 20)
    @NotBlank(message = "{Coin.name.blank}")
    private String name;

    /**
     * 中文
     */
    @Excel(name = "otc货币单位中文名称", orderNum = "1", width = 20)
    @NotBlank(message = "{Coin.nameCn.blank}")
    private String nameCn;

    /**
     * 缩写
     */
    @Excel(name = "otc货币单位", orderNum = "1", width = 20)
    @NotBlank(message = "{Coin.unit.blank}")
    private String unit;

    /**
     * 状态
     */
    @Enumerated(EnumType.ORDINAL)
    private CommonStatus status = CommonStatus.NORMAL;

    /**
     * 交易手续费率
     */
    @Column(columnDefinition = "decimal(12,6) comment '交易手续费率'")
    private BigDecimal jyRate;

    @Column(columnDefinition = "decimal(20,8) comment '卖出广告最低发布数量'")
    private BigDecimal sellMinAmount;

    @Column(columnDefinition = "decimal(20,8) comment '买入广告最低发布数量'")
    private BigDecimal buyMinAmount;

    @Excel(name = "otc货币单位", orderNum = "1", width = 20)
    private int sort;

    /** 是否是平台币 */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isPlatformCoin=BooleanEnum.IS_FALSE;
    /**
     * 币种精度
     */
    @Column(columnDefinition = "int(11) default 8 comment '币种精度'")
    private Integer coinScale=8;

    /**
     * 最大交易时间
     */
    @Column(columnDefinition = "int(11) default 0 comment '广告上架后自动下架时间，单位为秒，0表示不过期'")
    private Integer maxTradingTime=0;

    @Column(columnDefinition = "int(11) default 0 comment '最大挂单数量，0表示不限制'")
    private Integer maxVolume=0;
}
