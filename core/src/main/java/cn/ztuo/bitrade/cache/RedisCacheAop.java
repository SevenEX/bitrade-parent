package cn.ztuo.bitrade.cache;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.annotation.RedisCache;
import cn.ztuo.bitrade.db.DynamicDataSource;
import cn.ztuo.bitrade.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RMapCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * <p>Redis方法缓存 aop</p>
 *
 * @author Zane
 * @version  2019-12-03 16:22
 */
@Aspect
@Component
@Slf4j
@Order(5)
public class RedisCacheAop {

    public RedisCacheAop() {
        log.info("Redis方法缓存 AOP ");
    }

    @Around(value = "@annotation(cn.ztuo.bitrade.annotation.RedisCache) && @annotation(redisCache)",
            argNames = "joinPoint, redisCache")
    public Object authLogic(ProceedingJoinPoint joinPoint, RedisCache redisCache) throws Throwable {
        try {
            if(StringUtils.isEmpty(redisCache.value())){
                log.error("错误: Redis方法缓存必须指定key!");
                return joinPoint.proceed();
            }
            String paramKey = "default";
            if(!redisCache.ignoreParam()){
                MethodParamSerializer paramSerializer = redisCache.paramSerializer().newInstance();
                paramKey = paramSerializer.serialize(joinPoint.getArgs());
            }
            RMapCache<String, Object> mapCache = RedissonUtil.getMapCache(redisCache.value());
            Object cache = mapCache.get(paramKey);
            if(cache == null){
                Object returnVal = joinPoint.proceed();
                if(returnVal != null) {
                    mapCache.fastPut(paramKey, returnVal, redisCache.ttl(), TimeUnit.SECONDS);
                }
                return returnVal;
            }
            return cache;
        } catch (Throwable ignored){
            return joinPoint.proceed();
        }
    }
}