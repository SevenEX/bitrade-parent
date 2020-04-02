package cn.ztuo.bitrade.constant;

/**
 * 系统常量
 *
 * @author Seven
 * @date 2019年12月18日
 */
public class SysConstant {
    /**
     * session常量
     */
    public static final String SESSION_ADMIN = "ADMIN_MEMBER";

    public static final String SESSION_MEMBER = "API_MEMBER";

    /**
     * 验证码
     */
    public static final String PHONE_BIND_CODE_PREFIX = "PHONE_BIND_CODE_";

    public static final String EMAIL_BIND_CODE_PREFIX = "EMAIL_BIND_CODE_";

    public static final String TOKEN_ADD_ADDRESS = "TOKEN_ADD_ADDRESS";
    public static final String TOKEN_RESET_PASSWORD = "TOKEN_RESET_PASSWORD";
    public static final String TOKEN_RESET_GOOGLE_AUTH = "TOKEN_RESET_GOOGLE_AUTH";
    public static final String TOKEN_RESET_TRANS_PASSWORD = "TOKEN_RESET_TRANS_PASSWORD";
    public static final String TOKEN_WITHDRAW_AUTH = "TOKEN_WITHDRAW_AUTH";
    public static final String TOKEN_REGISTER_MAIL = "TOKEN_REGISTER_MAIL";
    public static final String TOKEN_REGISTER_PHONE = "TOKEN_REGISTER_PHONE";
    public static final String TOKEN_API_BIND = "TOKEN_API_BIND";

    public static final String TOKEN_SWITCH_VERIFY = "TOKEN_SWITCH_VERIFY";
    /**
     * 解绑邮箱验证码
     */
    public static final String TOKEN_EMAIL_UNTIE = "TOKEN_EMAIL_UNTIE";
    public static final String TOKEN_PHONE_UNTIE = "TOKEN_PHONE_UNTIE";
    public static final String TOKEN_EMAIL_BIND = "TOKEN_EMAIL_BIND";
    public static final String TOKEN_PHONE_BIND = "TOKEN_PHONE_BIND";

    /**
     * 换绑邮箱验证码
     */
    public static final String EMAIL_UPDATE_CODE_PREFIX = "EMAIL_UPDATE_CODE_";

    public static final String ADMIN_LOGIN_PHONE_PREFIX = "ADMIN_LOGIN_PHONE_PREFIX_";

    public static final String ADMIN_COIN_REVISE_PHONE_PREFIX = "ADMIN_COIN_REVISE_PHONE_PREFIX_";
    public static final String ADMIN_COIN_TRANSFER_COLD_PREFIX = "ADMIN_COIN_TRANSFER_COLD_PREFIX_";
    public static final String ADMIN_EXCHANGE_COIN_SET_PREFIX = "ADMIN_EXCHANGE_COIN_SET_PREFIX_";

    /**
     * 新增管理员 短信验证码前缀
     */
    public static final String ADMIN_ADD_PHONE_PREFIX = "ADMIN_ADD_PHONE_PREFIX";
    /**
     * 修改管理员 短信验证码前缀
     */
    public static final String ADMIN_UPDATE_PHONE_PREFIX = "ADMIN_UPDATE_PHONE_PREFIX";
    /**
     * 删除管理员 短信验证码前缀
     */
    public static final String ADMIN_DEL_PHONE_PREFIX = "ADMIN_DEL_PHONE_PREFIX";

    /**
     * 角色管理相关验证码前缀
     */
    public static final String ROLE_DEL_PHONE_PREFIX = "ROLE_DEL_PHONE_PREFIX";
    public static final String ROLE_ADD_PHONE_PREFIX = "ROLE_ADD_PHONE_PREFIX";
    public static final String ROLE_UPDATE_PHONE_PREFIX = "ROLE_UPDATE_PHONE_PREFIX";

    /**
     * 币种管理相关验证码前缀
     */
    public static final String COIN_UPDATE_PHONE_PREFIX = "COIN_UPDATE_PHONE_PREFIX";
    public static final String COIN_ADD_PHONE_PREFIX = "COIN_ADD_PHONE_PREFIX";
    public static final String COIN_DEL_PHONE_PREFIX = "COIN_DEL_PHONE_PREFIX";
    /**
     * 申诉管理
     */
    public static final String APPEAL_CANCEL_PHONE_PREFIX = "APPEAL_CANCEL_PHONE_PREFIX";
    public static final String APPEAL_CONFIRM_PHONE_PREFIX = "APPEAL_CONFIRM_PHONE_PREFIX";

    /**
     * 返佣配置
     */
    public static final String REWARD_MERGE_PHONE_PREFIX = "REWARD_MERGE_PHONE_PREFIX";
    /**
     * 费率配置
     */
    public static final String GRADE_UPDATE_PHONE_PREFIX = "GRADE_UPDATE_PHONE_PREFIX";
    /**
     * 资产管理-充币
     */
    public static final String WALLET_RECHARGE_PHONE_PREFIX = "WALLET_RECHARGE_PHONE_PREFIX";


    /**
     * 防攻击验证
     */
    public static final String ANTI_ATTACK_ = "ANTI_ATTACK_";
    /**
     * 防止注册机器人
     */
    public static final String ANTI_ROBOT_REGISTER = "ANTI_ROBOT_REGISTER_";
    /**
     * 60亿BHB累计(过期时间为15分钟)
     */
    public static final String BHB_AMOUNT="BHB_AMOUNT";
    public static final int BHB_AMOUNT_EXPIRE_TIME=900;


    /**
     * 公告页缓存
     */
    public static final String NOTICE_DETAIL = "notice_detail_";
    public static final int NOTICE_DETAIL_EXPIRE_TIME=300;

    /**
     * 帮助页缓存(首页)
     */
    public static final String SYS_HELP = "SYS_HELP";
    public static final int SYS_HELP_EXPIRE_TIME=300;


    /**
     * 帮助页缓存(类别页)
     */
    public static final String SYS_HELP_CATE = "SYS_HELP_CATE_";
    public static final int SYS_HELP_CATE_EXPIRE_TIME=300;

    /**
     * 帮助页缓存(详情页)
     */
    public static final String SYS_HELP_DETAIL = "SYS_HELP_DETAIL_";
    public static final int SYS_HELP_DETAIL_EXPIRE_TIME=300;

    /**
     * 帮助页缓存(该分类置顶文章)
     */
    public static final String SYS_HELP_TOP = "SYS_HELP_TOP_";
    public static final int SYS_HELP_TOP_EXPIRE_TIME=300;


    //字典表数据缓存
    public static final String DATA_DICTIONARY_BOUND_KEY= "data_dictionary_bound_key_";
    public static final int DATA_DICTIONARY_BOUND_EXPIRE_TIME= 604800;

    //盘口数据
    public static final String EXCHANGE_INIT_PLATE_SYMBOL_KEY="EXCHANGE_INIT_PLATE_SYMBOL_KEY_";
    public static final int EXCHANGE_INIT_PLATE_SYMBOL_EXPIRE_TIME= 18000;

    /**
     * 盘口数据所有交易对
     */
    public static final String EXCHANGE_INIT_PLATE_ALL_SYMBOLS = "EXCHANGE_INIT_PLATE_ALL_SYMBOLS";



    /**
     * 用户币币交易订单时间限制
     */
    public static final String USER_ADD_EXCHANGE_ORDER_TIME_LIMIT= "USER_ADD_EXCHANGE_ORDER_TIME_LIMIT_";
    public static final int USER_ADD_EXCHANGE_ORDER_TIME_LIMIT_EXPIRE_TIME= 20;

    /**
     * 空投锁
     */
    public static final String HANDLE_AIRDROP_LOCK="HANDLE_AIRDROP_LOCK_";
    /**
     * 登录锁，连续账号密码错误时启用
     */
    public static final String LOGIN_LOCK="LOGIN_LOCK_";
    /**
     * 实名用户一级推荐实名用户赠送积分
     */
    public static final String INTEGRATION_GIVING_ONE_INVITE = "integration_giving_one_invite";
    /**
     * 实名用户二级推荐实名用户赠送积分
     */
    public static final String INTEGRATION_GIVING_TWO_INVITE = "integration_giving_two_invite";
    /**
     * 法币交易人民币对积分比例
     */
    public static final String INTEGRATION_GIVING_OTC_BUY_CNY_RATE = "integration_giving_otc_buy_cny_rate";
    /**
     * 币币充值USDT对积分比例
     */
    public static final String INTEGRATION_GIVING_EXCHANGE_RECHARGE_USDT_RATE = "integration_giving_exchange_recharge_usdt_rate";

    /**
     * 全平台是否允许提币
     */
    public static final String CAN_WITHDRAW = "can_withdraw";

    /**
     * SE最小精度
     */
    public static final String SE_MIN_SCALE = "se_min_scale";

    /**
     * 首页榜单显示数量
     */
    public static final String HOME_PAGE_DISPLAY_QUANTITY = "home_page_display_quantity";

    /**
     * 邀请榜单显示数量
     */
    public static final String COMMISSION_DISPLAY_QUANTITY = "commission_display_quantity";
    /**
     * 用户每日提币笔数
     */
    public static final String CUSTOMER_DAY_WITHDRAW_TOTAL_COUNT = "CUSTOMER_DAY_WITHDRAW_TOTAL_COUNT_";
    /**
     * 用户每日提币数量折合USDT
     */
    public static final String CUSTOMER_DAY_WITHDRAW_COVER_USD_AMOUNT ="CUSTOMER_DAY_WITHDRAW_COVER_USD_AMOUNT_";
    /**
     * 等级缓存
     */
    public static final String CUSTOMER_INTEGRATION_GRADE="CUSTOMER_INTEGRATION_GRADE_";

    /**
     * 默认交易对儿缓存
     */
    public static final String DEFAULT_SYMBOL = "DEFAULT_SYMBOL";

    /**
     * 默认交易区缓存
     */
    public static final String DEFAULT_AREA = "DEFAULT_AREA_";

    /**
     * 固定地址外链缓存
     */
    public static final String EXTERNAL_LINKS = "EXTERNAL_LINKS_";
    public static final int EXTERNAL_LINKS_EXPIRE_TIME=300;

    /**
     * 非固定地址外链缓存
     */
    public static final String NAVIGATION = "NAVIGATION_";
    public static final int NAVIGATION_EXPIRE_TIME=300;

    /**
     * 1、修改/重置登录密码
     * 2、关闭安全验证
     *
     * 24H不允许提币、不允许币币划转到法币账户
     */
    public static final String WITHDRAW_LOCK = "WITHDRAW_LOCK_";
    public static final int WITHDRAW_LOCK_EXPIRE_TIME=24;


    /**
     * API权限0、读取、1、提币、2、交易
     */
    public static final String LIMIT_READ = "0";
    public static final String LIMIT_WITHDRAW = "1";
    public static final String LIMIT_TRANSACTION = "2";

}
