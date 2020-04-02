package cn.ztuo.bitrade.constant;

public class RedissonKeyConstant {
    public static final String GENERATE_MIN_KLINE = "generate:min_kline:";

    public static final String GENERATE_HOUR_KLINE = "generate:hour_kline:";

    public static final String GENERATE_DAY_KLINE = "generate:day_kline:";

    public static final String CACHE_LOCALE = "cache:locale";

    public static final String CACHE_LOCALE_EXTEND = "cache:locale:extend";

    public static final String SECURITY_VERIFY = "security_verify:";
    /**
     * 手机号->验证码，带有效期
     */
    public static final String PHONE_VERIFY_CODE_MAP_CACHE = "phone_verify_code:";
    public static final String EMAIL_VERIFY_CODE_MAP_CACHE = "email_verify_code:";
    public static final String CACHE_DEFAULT_RATE = "cache:default_rate";
    public static final String CACHE_TRADE_SNAPSHOT = "cache:trade_snapshot";

    public static final String JOB_ADVERTISE_AUTO_OFF = "job:advertise_auto_off:";
}
