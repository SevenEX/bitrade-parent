package cn.ztuo.bitrade.model.update;

import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.CommonStatus;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CoinUpdate {


    @NotBlank(message = "{Coin.name.blank}")
    private String name;
    /**
     * 中文
     */
    @NotBlank(message = "{Coin.nameCn.blank}")
    private String nameCn;
    /**
     * 缩写
     */
    @NotBlank(message = "{Coin.unit.blank}")
    private String unit;
    /**
     * 状态
     */
    private CommonStatus status = CommonStatus.NORMAL;

    /**
     * 最小提币手续费
     */
    private Double minTxFee;
    /**
     * 对人民币汇率
     */
    private BigDecimal cnyRate;
    /**
     * 最大提币手续费
     */
    private Double maxTxFee;
    /**
     * 对美元汇率
     */
    private BigDecimal usdRate;
    /**
     * 是否支持rpc接口
     */
    private BooleanEnum enableRpc = BooleanEnum.IS_TRUE;

    /**
     * 是否能提币
     */
    private BooleanEnum canWithdraw;

    /**
     * 是否能充币
     */
    private BooleanEnum canRecharge;


    /**
     * 是否能自动提币
     */
    private BooleanEnum canAutoWithdraw;

    /**
     * 提币阈值
     */
    private BigDecimal withdrawThreshold;
    private BigDecimal minWithdrawAmount;
    private BigDecimal maxWithdrawAmount;


    private int withdrawScale;
    /**
     * 提现手续费改为一个值
     */
    private Double txFee;
    /**
     * 最小充币量
     */
    private BigDecimal minRechargeAmount;
    /**
     * 矿工费
     */
    private BigDecimal minerFee;

    private int sort;

    private BigDecimal maxDailyWithdrawRate;

    /**
     * 图片地址
     */
    private String imgUrl;

    /**
     * 发型总量
     */
    private BigDecimal releaseAmount;

    /**
     * 发行时间
     */
    private Date releaseTime;

    /**
     * 众筹价格
     */
    private String fundPrice;

    /**
     * 白皮书
     */
    private String whitePaper;

    /**
     * 官网
     */
    private String website;

    /**
     * 区块查询
     */
    private String blockQuery;

    private String coldWalletAddress ;

    private BigDecimal burnAmount;

    private BigDecimal circulateAmount;
}
