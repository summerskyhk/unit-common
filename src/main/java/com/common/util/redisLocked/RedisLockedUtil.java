package com.common.util.redisLocked;

import redis.clients.jedis.Jedis;

import java.util.Collections;

/**
 *
 * @author zhoucg
 * @date 2019-01-15
 *
 * 基于jedis实现的分布式的锁
 */
public class RedisLockedUtil {

    private static final String LOCK_SUCCESS = "ok";

    /**
     * NX -- Only set the key if it does not already exist
     * XX -- Only set the key if it already exist
     */
    private static final String SET_IF_NOT_EXIST = "NX";

    /**
     * expire time units: EX = seconds; PX = milliseconds
     */
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    private static final Long RELEASE_SUCCESS = 1L;


    /**
     * 加锁
     * @param jedis redis客户端
     * @param lockKey 锁
     * @param requestId 客户端请求标识
     * @param expireTime 加锁时长
     * @return 是否获取成功
     */
    public static boolean tryGetDistributedLock(Jedis jedis,String lockKey,String requestId,int expireTime) {

        String result = jedis.set(lockKey,requestId,SET_IF_NOT_EXIST,SET_WITH_EXPIRE_TIME,expireTime);
        if(LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }


    /**
     * 解锁过程
     * @param jedis redis 客户端
     * @param lockKey 锁
     * @param requestId 客户端请求标识
     * @return 是否解锁成功
     */
    public static boolean releaseDistributedLock(Jedis jedis,String lockKey,String requestId) {

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey),Collections.singletonList(requestId));

        if(RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }



}
