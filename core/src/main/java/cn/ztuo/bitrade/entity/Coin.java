package cn.ztuo.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.dto.CoinDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author MrGao
 * @description
 * @date 2017/12/29 14:14
 */
@Entity
@Data
@Table(name = "coin")
@ToString(exclude = {"coinChainRelationList"})
@EqualsAndHashCode(exclude = {"coinChainRelationList"})
public class Coin {
    @Id
    @NotBlank(message = "{Coin.name.blank}")
    @Excel(name = "货币", orderNum = "1", width = 20)
    private String name;
    /**
     * 中文
     */
    @Excel(name = "中文名称", orderNum = "1", width = 20)
    @NotBlank(message = "{Coin.nameCn.blank}")
    private String nameCn;
    /**
     * 缩写
     */
    @Excel(name = "单位", orderNum = "1", width = 20)
    @NotBlank(message = "{Coin.unit.blank}")
    private String unit;
    /**
     * 状态
     */
    @Enumerated(EnumType.ORDINAL)
    private CommonStatus status = CommonStatus.NORMAL;

    /**
     * 最小提币手续费
     */
    @Excel(name = "最小提币手续费", orderNum = "1", width = 20)
    private BigDecimal minTxFee;
    /**
     * 对人民币汇率
     */
    @Excel(name = "对人民币汇率", orderNum = "1", width = 20)
    @Column(columnDefinition = "decimal(20,8) default 0.00 comment '人民币汇率'")
    private BigDecimal cnyRate;
    /**
     * 最大提币手续费
     */
    /*@Deprecated*/
    @Excel(name = "最大提币手续费", orderNum = "1", width = 20)
    private BigDecimal maxTxFee;
    /**
     * 对美元汇率
     */
    @Excel(name = "对美元汇率", orderNum = "1", width = 20)
    @Column(columnDefinition = "decimal(20,8) default 0.00 comment '美元汇率'")
    private BigDecimal usdRate;
    /**
     * 对新加坡币汇率
     */
    @Excel(name = "对新加坡币汇率", orderNum = "1", width = 20)
    @Column(columnDefinition = "decimal(12,6) default 0.00 comment '对新加坡币汇率'")
    private BigDecimal sgdRate;

    /**
     * 是否支持rpc接口
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum enableRpc = BooleanEnum.IS_TRUE;

    /**
     * 排序
     */
    private int sort=10;

    /**
     * 是否能提币
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum canWithdraw=BooleanEnum.IS_TRUE;

    /**
     * 是否能充币
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum canRecharge=BooleanEnum.IS_TRUE;


    /**
     * 是否能转账
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum canTransfer = BooleanEnum.IS_TRUE;

    /**
     * 是否能自动提币
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum canAutoWithdraw=BooleanEnum.IS_FALSE;

    /**
     * 提币阈值
     */
    @Column(columnDefinition = "decimal(20,8) comment '自动提现阈值'")
    private BigDecimal withdrawThreshold;
    @Column(columnDefinition = "decimal(20,8) comment '最小提币数量'")
    private BigDecimal minWithdrawAmount;
    @Column(columnDefinition = "decimal(20,8) comment '最大提币数量'")
    private BigDecimal maxWithdrawAmount;

    /**
     * 是否是平台币
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int default 0 comment '是否为平台币'")
    private BooleanEnum isPlatformCoin = BooleanEnum.IS_FALSE;

    /**
     * 是否是法币
     */
    @Column(name = "has_legal", columnDefinition = "bit default 0", nullable = false)
    private Boolean hasLegal = false;

    @Transient
    private BigDecimal allBalance ;

    private String coldWalletAddress ;

    @Transient
    private BigDecimal hotAllBalance ;

    /**
     * 转账时付给矿工的手续费
     */
    @Column(columnDefinition = "decimal(20,8) default 0 comment '矿工费'")
    private BigDecimal minerFee = BigDecimal.ZERO;

    @Column(columnDefinition = "int default 4 comment '提币精度'")
    private int withdrawScale;

    @Column(columnDefinition = "decimal(20,8) default 0 comment '最小充币数量'")
    private BigDecimal minRechargeAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "varchar(64) default null comment '主充值地址'")
    private String masterAddress;

    @Column(columnDefinition = "decimal(20,8) default 0 comment '单日最大提币量'")
    private BigDecimal maxDailyWithdrawRate=BigDecimal.ZERO;

    /**
     * 图片地址
     */
    @Excel(name = "图片地址", orderNum = "1", width = 20)
    @Column(columnDefinition = "varchar(255) default null comment '图片地址'")
    private String imgUrl;

    /**
     * 发型总量
     */
    @Excel(name = "发型总量", orderNum = "1", width = 20)
    @Column(columnDefinition = "decimal(20,8) default 0 comment '发型总量'")
    private BigDecimal releaseAmount;

    /**
     * 发行时间
     */
    @Excel(name = "发行时间", orderNum = "1", width = 20)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Column(columnDefinition = "datetime NULL DEFAULT NULL COMMENT '发行时间'")
    private Date releaseTime;

    /**
     * 众筹价格
     */
    @Excel(name = "众筹价格", orderNum = "1", width = 20)
    @Column(columnDefinition = "varchar(255) default null comment '众筹价格'")
    private String fundPrice;

    /**
     * 白皮书
     */
    @Excel(name = "白皮书", orderNum = "1", width = 20)
    @Column(columnDefinition = "varchar(255) default null comment '白皮书'")
    private String whitePaper;

    /**
     * 官网
     */
    @Excel(name = "官网", orderNum = "1", width = 20)
    @Column(columnDefinition = "varchar(255) default null comment '官网'")
    private String website;

    /**
     * 区块查询
     */
    @Excel(name = "区块查询", orderNum = "1", width = 20)
    @Column(columnDefinition = "varchar(255) default null comment '区块查询'")
    private String blockQuery;

    @Transient
    private CoinDTO coinInfo;

    /**
     * 是否是结算币种
     */
    @Column(name = "is_settlement", columnDefinition = "bit default 0", nullable = false)
    private Boolean isSettlement = false;

    @Column(columnDefinition = "decimal(20,8) default 0 comment '销毁总量'")
    private BigDecimal burnAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "decimal(20,8) default 0 comment '流通总量'")
    private BigDecimal circulateAmount = BigDecimal.ZERO;

    @OneToMany(fetch = FetchType.EAGER, mappedBy="coin")
    @JsonIgnore
    private List<CoinChainRelation> coinChainRelationList;
}
