package cn.ztuo.bitrade.cache;

import com.alibaba.fastjson.JSON;

public class DefaultMethodParamSerializer implements MethodParamSerializer {
    @Override
    public String serialize(Object[] params) {
        return JSON.toJSONString(params);
    }
}
