package com.lotterysystem.server.util;


import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CacheUtil {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public <R, ID> R queryWithMutex(String keyPrefix, ID id, TypeReference<R> type, Function<ID, R> dbFallback) {
        R r;
        String key = keyPrefix +":"+ id;
        String json = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json,type,false);
        }
        if (json != null) {
            return null;
        }
        try {

            if (!tryLock(keyPrefix, id)) {
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback);
            } else {

            r = dbFallback.apply(id);

            if (r == null) {
                redisTemplate.opsForValue().set(key, "", 60L + RandomUtil.randomLong(15) , TimeUnit.SECONDS);
                return null;
            }

            json = JSONUtil.toJsonStr(r);
            redisTemplate.opsForValue().set(key, json, 30L * 60 + RandomUtil.randomLong(15), TimeUnit.SECONDS);

            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {

            unlock(keyPrefix,id);

        }
        return r;
    }

    public <R, ID> R queryWithMutexWithTick(String keyPrefix, ID id, TypeReference<R> type, Function<ID, R> dbFallback) {
        R r;
        String key = keyPrefix +":"+ id;
        String json = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {

            //log.info("Redis缓存命中! {}", json);

            return JSONUtil.toBean(json,type,false);

        }
        if (json != null) {

            //log.info("Redis空缓存命中!");
            return null;

        }
        //没有数据，拿锁到mysql查
        try {

            if (!tryLock(keyPrefix, id)) {
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback);
            } else {


                //log.info("成功获取锁！");

                r = dbFallback.apply(id);

                if (r == null) {
                    redisTemplate.opsForValue().set(key, "", 30L + RandomUtil.randomLong(5) , TimeUnit.SECONDS);
                    //log.info("未找到对象，添加空缓存!");
                    return null;
                }

                json = JSONUtil.toJsonStr(r);
                redisTemplate.opsForValue().set(key, json,  30L + RandomUtil.randomLong(5), TimeUnit.SECONDS);
                //log.info("新增缓存: {}", json);

            }

        } catch (InterruptedException e) {

            throw new RuntimeException(e);

        } finally {
            //log.info("成功解锁！");
            unlock(keyPrefix,id);
        }
        return r;
    }

    public <R, ID> List<R> MultiQueryWithMutex(String keyPrefix, List<ID> ids, TypeReference<R> type, Function<ID, R> dbFallback) {

        List<String> idStrs = ids.stream().map(id -> keyPrefix + ":" + id).collect(Collectors.toList());

        List<R> result = new ArrayList<>();
        // 1. 批量从 Redis Hash 中查询
        List<String> cachedJsons = redisTemplate.opsForValue().multiGet(idStrs);

        for(int i = 0;i < idStrs.size();i++) {
            if(cachedJsons.get(i) == null){
                result.add(queryWithMutex(keyPrefix,ids.get(i),type,dbFallback));
            }else
                result.add(JSONUtil.toBean(cachedJsons.get(i),type,false));
        }

        return result;
    }

    public <R, ID> List<R> MultiQueryWithMutexWithTick(String keyPrefix, List<ID> ids, TypeReference<R> type, Function<ID, R> dbFallback) {

        List<String> idStrs = ids.stream().map(id -> keyPrefix + ":" + id).collect(Collectors.toList());

        List<R> result = new ArrayList<>();
        // 1. 批量从 Redis Hash 中查询
        List<String> cachedJsons = redisTemplate.opsForValue().multiGet(idStrs);

        for(int i = 0;i < idStrs.size();i++) {
            if(cachedJsons.get(i) == null){
                result.add(queryWithMutexWithTick(keyPrefix,ids.get(i),type,dbFallback));
            }else
                result.add(JSONUtil.toBean(cachedJsons.get(i),type,false));
        }

        return result;
    }




    public <ID,R> void update(String keyPrefix, ID id, R content, TypeReference<R> type, Function<ID, R> dbFallback,Consumer<R> dbUpdate){
        dbUpdate.accept(content);
        R r = dbFallback.apply(id);
        String key = keyPrefix +":" + id;
        redisTemplate.delete(key);
        String json = JSONUtil.toJsonStr(r);
        redisTemplate.opsForValue().set(key,json,30L * 60 + RandomUtil.randomLong(15), TimeUnit.SECONDS);
        //log.info("新增缓存 {}",content);
    }

    private <ID>boolean tryLock(String key,ID id) {
        String lockKey = "lock:" + key +":"+ id;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS);
        return success != null && success;  // 如果成功获取锁，返回 true
    }

    private <ID>void unlock(String key,ID id) {
        String lockKey = "lock:" + key +":"+ id;
        // 解锁时，确保只有持有锁的线程才能释放锁
        redisTemplate.delete(lockKey);
    }

    public <ID>Boolean delete(String keyPrefix, ID id) {
        return redisTemplate.delete(keyPrefix + ":" + id);
    }



}
