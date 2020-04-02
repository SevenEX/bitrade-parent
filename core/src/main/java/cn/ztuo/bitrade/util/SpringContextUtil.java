package cn.ztuo.bitrade.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    /**
     *  Spring应用上下文环境
     */
    private static ApplicationContext applicationContext;

    private static ConcurrentLinkedQueue<Consumer<ApplicationContext>> consumers = new ConcurrentLinkedQueue<>();


    /**
     * 实现ApplicationContextAware接口的回调方法，设置上下文环境
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext)
            throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
        applyAsyncQuest();
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void getApplicationContextAsync(Consumer<ApplicationContext> action) {
        consumers.add(action);
        applyAsyncQuest();
    }

    private static void applyAsyncQuest() {
        if (applicationContext != null) {
            Consumer<ApplicationContext> consumer;
            do {
                consumer = consumers.poll();
                if(consumer != null)
                    consumer.accept(applicationContext);
                else
                    break;
            }while (true);
        }
    }

    /**
     * 获取对象 这里重写了bean方法，起主要作用
     */
    public static Object getBean(String beanId) throws BeansException {
        return applicationContext.getBean(beanId);
    }

    public static <T> T getBean(String beanId, Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(beanId, requiredType);
    }
    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(requiredType);
    }
}