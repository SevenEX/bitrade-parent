package cn.ztuo.bitrade.db;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * <p>多数据源切换的 aop</p>
 *
 * @author maxzhao
 * @date 2019-06-26 16:22
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(prefix = "gt.maxzhao.boot", name = "multiDatasourceOpen", havingValue = "true")
public class MultiDataSourceAop implements Ordered {

    public MultiDataSourceAop() {
        log.info("多数据源初始化 AOP ");
    }

    @Pointcut(value = "@annotation(cn.ztuo.bitrade.annotation.MultiDataSource)")
    private void cut() {
    }

    @Around("cut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        Signature signature = point.getSignature();
        MethodSignature methodSignature ;
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        methodSignature = (MethodSignature) signature;
        //获取当点方法的注解
        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());

        MultiDataSource datasource = currentMethod.getAnnotation(MultiDataSource.class);
        if (datasource != null) {
            DynamicDataSource.setDataSourceDbName(datasource.name());
            log.debug("设置数据源为：" + datasource.name());
        } else {
            DynamicDataSource.setDataSourceDbName("main");
            log.debug("设置数据源为：默认  -->  main");
        }
        try {
            return point.proceed();
        } finally {
            log.debug("清空数据源信息！");
            DynamicDataSource.clearDataSourceDbName();
        }
    }

    /**
     * aop的顺序要早于spring的事务
     */
    @Override
    public int getOrder() {
        return 1;
    }
}