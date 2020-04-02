package cn.ztuo.bitrade.constant;

/**
 * 短信验证码类型，前缀枚举
 *
 * @author Paradise
 */
public enum SmsCodePrefixEnum {
    /**
     * 新增管理员 短信验证码前缀
     */
    ADMIN_ADD_PHONE_PREFIX,
    /**
     * 修改管理员 短信验证码前缀
     */
    ADMIN_UPDATE_PHONE_PREFIX,
    /**
     * 删除管理员 短信验证码前缀
     */
    ADMIN_DEL_PHONE_PREFIX,
    /**
     * 角色管理相关验证码前缀
     */
    ROLE_DEL_PHONE_PREFIX,
    ROLE_ADD_PHONE_PREFIX,
    ROLE_UPDATE_PHONE_PREFIX,
    /**
     * 币种管理相关验证码前缀
     */
    COIN_UPDATE_PHONE_PREFIX,
    COIN_ADD_PHONE_PREFIX,
    COIN_DEL_PHONE_PREFIX,
    /**
     * 申诉管理
     */
    APPEAL_CANCEL_PHONE_PREFIX,

    APPEAL_CONFIRM_PHONE_PREFIX,
    /**
     * 返佣配置
     */
    REWARD_MERGE_PHONE_PREFIX,
    /**
     * 阶梯费率配置
     */
    GRADE_UPDATE_PHONE_PREFIX,
    /**
     * 资产管理-充币
     */
    WALLET_RECHARGE_PHONE_PREFIX,
    /**
     * 币种管理-转入冷钱包
     */
    COIN_TRANSFER_PHONE_PREFIX,

    /**
     * 商家手续费费率配置
     */
    BUSINESS_UPDATE_PHONE_PREFIX,

    /**
     * SE抵扣折扣率配置
     */
    SE_DISCOUNT_UPDATE_PHONE_PREFIX,
    ;
}
