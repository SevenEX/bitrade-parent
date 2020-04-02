package cn.ztuo.bitrade.util;

import lombok.AllArgsConstructor;
import org.redisson.api.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Redis工具类
 */
public class RedissonUtil {

    private static final String BUCKET_PREFIX = "bucket:";
    private static final String SET_PREFIX = "set:";
    private static final String SET_CACHE_PREFIX = "set_cache:";
    private static final String MAP_PREFIX = "map:";
    private static final String MAP_CACHE_PREFIX = "map_cache:";
    private static final String LIST_PREFIX = "list:";
    private static final String SEQ_PREFIX = "seq:";
    private static final String LOCK_PREFIX = "lock:";

    private static RedissonClient redissonClient;

    static {
        SpringContextUtil.getApplicationContextAsync((context) -> redissonClient = context.getBean(RedissonClient.class));
    }

    public static RedissonClient getRedissonClient() {
        return redissonClient;
    }

    /**
     * 获取一个普通键值
     * @param key 键
     * @param <T> 类型，复杂类型需要实现Serializable或Externalizable接口
     * @return 值
     */
    public static <T> RBucket<T> getBucket(String key) {
        return redissonClient.getBucket(BUCKET_PREFIX + key);
    }

    /**
     * 获取一个集合
     * @param key 键
     * @param <T> 类型，复杂类型需要实现Serializable或Externalizable接口
     * @return 集合
     */
    public static <T> RSet<T> getSet(String key) {
        return redissonClient.getSet(SET_PREFIX + key);
    }

    /**
     * 获取一个带TTL的集合
     * @param key 键
     * @param <T> 类型，复杂类型需要实现Serializable或Externalizable接口
     * @return 集合
     */
    public static <T> RSetCache<T> getSetCache(String key) {
        return redissonClient.getSetCache(SET_CACHE_PREFIX + key);
    }

    /**
     * 获取一个键值对映射表
     * @param key 键
     * @param <K> 键类型，复杂类型需要实现Serializable或Externalizable接口
     * @param <V> 值类型，复杂类型需要实现Serializable或Externalizable接口
     * @return 键值对映射表
     */
    public static <K, V> RMap<K, V> getMap(String key) {
        return redissonClient.getMap(MAP_PREFIX + key);
    }


    /**
     * 获取一个带TTL的键值对映射表
     * @param key 键
     * @param <K> 键类型，复杂类型需要实现Serializable或Externalizable接口
     * @param <V> 值类型，复杂类型需要实现Serializable或Externalizable接口
     * @return 键值对映射表
     */
    public static <K, V> RMapCache<K, V> getMapCache(String key) {
        return redissonClient.getMapCache(MAP_CACHE_PREFIX + key);
    }

    /**
     * 获取一个列表
     * @param key 键
     * @param <T> 类型，复杂类型需要实现Serializable或Externalizable接口
     * @return 列表
     */
    public static <T> RList<T> getList(String key) {
        return redissonClient.getList(LIST_PREFIX + key);
    }

    public static RAtomicLong getAtomicLong(String key) {
        return redissonClient.getAtomicLong(SEQ_PREFIX + key);
    }

    /**
     * 获取一个公平锁
     * @param key 锁名
     * @return 公平锁
     */
    public static RLock getFairLock(String key) {
        return redissonClient.getFairLock(LOCK_PREFIX + key);
    }

    /**
     * 获取多个公平锁
     * @param keys 锁名列表
     * @return 多个锁
     */
    public static RLock getMultiLock(String... keys) {
        RLock[] locks = new RLock[keys.length];
        for (int i = 0; i < keys.length; i++) {
            locks[i] = redissonClient.getFairLock(LOCK_PREFIX + keys[i]);
        }
        return redissonClient.getMultiLock(locks);
    }

    @AllArgsConstructor
    public static class TransactionWrapper {
        private RTransaction rTransaction;

        public <T> RBucket<T> getBucket(String key) {
            return rTransaction.getBucket(BUCKET_PREFIX + key);
        }

        public <T> RSet<T> getSet(String key) {
            return rTransaction.getSet(SET_PREFIX + key);
        }

        public <T> RSetCache<T> getSetCache(String key) {
            return rTransaction.getSetCache(SET_CACHE_PREFIX + key);
        }

        public <K, V> RMap<K, V> getMap(String key) {
            return rTransaction.getMap(MAP_PREFIX + key);
        }

        public <K, V> RMapCache<K, V> getMapCache(String key) {
            return rTransaction.getMapCache(MAP_CACHE_PREFIX + key);
        }

    }
}
