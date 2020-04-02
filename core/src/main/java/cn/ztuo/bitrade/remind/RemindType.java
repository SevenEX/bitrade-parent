package cn.ztuo.bitrade.remind;

/**
 * 提醒类型
 *
 * @author Paradise
 */
public enum RemindType {

    /**
     * 下单
     */
    ORDER,
    /**
     * 买家已付款
     */
    PAY,
    /**
     * 卖家已放币
     */
    RELEASE,
    /**
     * 新的申诉
     */
    APPEAL,
    /**
     * 订单超时取消
     */
    CANCEL;
}
