package com.lotterysystem.server.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.server.constant.CachePrefix;
import com.lotterysystem.server.mapper.LotteryMapper;
import com.lotterysystem.server.pojo.entity.Lottery;
import com.lotterysystem.server.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nsh
 * @data 2025/5/11 15:52
 * @description
 **/
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LotteryJobService extends ServiceImpl<LotteryMapper, Lottery> {

    final private CacheUtil cacheUtil;

    final private RedisTemplate<String,Long> redisTemplate;

    final PrizeServiceImpl prizeService;

    final RecordServiceImpl recordService;


    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    public Lottery getLotteryWithCache(Long lotteryId){
        return cacheUtil.queryWithMutex(CachePrefix.LOTTERYOBJ.getPrefix(), lotteryId,new TypeReference<Lottery>() {},this::getById);
    }

    public void updateLotteryWithCache(Lottery lottery){
        cacheUtil.update(CachePrefix.LOTTERYOBJ.getPrefix(),lottery.getId(),lottery,new TypeReference<Lottery>() {},this::getById,this::updateById);
    }

    @DS("master")
    public void startLottery(Long lotteryId){
        //搬入奖品到redis,创表
        prizeService.joinToPool(lotteryId);

        Lottery lottery = getLotteryWithCache(lotteryId);

        JSONObject config= JSONUtil.parseObj(lottery.getRuleConfig());

        Integer time = config.getInt("time");
        List<Long> black = config.getJSONArray("blackList").toList(Long.class);
        List<Long> attend = config.getJSONArray("attend").toList(Long.class);


        JSONArray array = config.getJSONArray("weightList");

        Map<String, Integer> weightMap = new HashMap<>();
        Map<String, Integer> attendMap = new HashMap<>();
        Map<String, Integer> blackMap = new HashMap<>();

        // 填充 weightMap
        for (int i = 0; i < array.size(); i++) {
            JSONObject weightObj = array.getJSONObject(i);
            weightMap.put(weightObj.getLong("id").toString(), weightObj.getInt("weight"));
        }
        weightMap.put("-1", time);
        // 填充 attendMap 和 blackMap
        for (Long a : black) {
            blackMap.put(a.toString(), 1); // 将黑名单用户添加到 blackMap
        }
        for (Long a : attend) {
            attendMap.put(a.toString(), 1); // 将参与用户添加到 attendMap
        }
        if(attendMap.isEmpty())
            attendMap.put("-1", 1);


        redisTemplate.opsForHash().putAll(CachePrefix.BLACKLIST.getPrefix() + ":" + lotteryId, blackMap);
        redisTemplate.opsForHash().putAll(CachePrefix.ATTEND.getPrefix() + ":" + lotteryId, attendMap);
        redisTemplate.opsForHash().putAll(CachePrefix.WEIGHT.getPrefix() + ":" + lotteryId, weightMap);

        lottery.setIsActive(1);
        updateLotteryWithCache(lottery);
        log.info("开始抽奖：{} {}",lotteryId,lottery.getName());
    }
    //结束抽奖活动，并处理表
    @DS("master")
    public void endLottery(Long lotteryId){

        Lottery lottery = getLotteryWithCache(lotteryId);
        lottery.setIsActive(0);
        lottery.setIsEnd(1);
        updateLotteryWithCache(lottery);
        log.info("抽奖结束:{} {}",lotteryId,lottery.getName()); //更新状态

        cacheUtil.delete(CachePrefix.PRIZEPOOL.getPrefix(), lotteryId); //删除奖池表
        cacheUtil.delete(CachePrefix.LOTTERYCOUNT.getPrefix(), lotteryId);  //删除用户抽奖次数表
        cacheUtil.delete(CachePrefix.WEIGHT.getPrefix(), lottery.getId());  //删除权重表
        cacheUtil.delete(CachePrefix.ATTEND.getPrefix(), lottery.getId());  //删除参与者表
        cacheUtil.delete(CachePrefix.BLACKLIST.getPrefix(), lottery.getId());   //删除黑名单表
        scheduleFinalizeLottery(lottery.getId(),lottery.getName());
//        prizeService.updatePrizeCount(lotteryId,lottery.getType()); //更新奖品数到mysql
//        recordService.refreshRecord(lotteryId); //更新Record，isend设成1，让其可以被访问（对于非延时没有用）

    }
    @DS("master")
    public void endLottery(Lottery lottery){

        lottery.setIsActive(0);
        lottery.setIsEnd(1);
        updateLotteryWithCache(lottery);
        log.info("抽奖结束:{} {}",lottery.getId(),lottery.getName());
        cacheUtil.delete(CachePrefix.PRIZEPOOL.getPrefix(), lottery.getId());   //删除奖池表
        cacheUtil.delete(CachePrefix.LOTTERYCOUNT.getPrefix(), lottery.getId());//删除用户抽奖次数表
        cacheUtil.delete(CachePrefix.WEIGHT.getPrefix(), lottery.getId());  //删除权重表
        cacheUtil.delete(CachePrefix.ATTEND.getPrefix(), lottery.getId());  //删除参与者表
        cacheUtil.delete(CachePrefix.BLACKLIST.getPrefix(), lottery.getId());   //删除黑名单表
        scheduleFinalizeLottery(lottery.getId(),lottery.getName());

//        prizeService.updatePrizeCount(lottery.getId(),lottery.getType());//更新奖品数到mysql
//        recordService.refreshRecord(lottery.getId());//更新Record，isend设成1，让其可以被访问（对于非延时没有用）

    }
    @DS("master")
    public void scheduleFinalizeLottery(Long lotteryId, String name) {
        taskExecutor.execute(()->{
            log.info("准备刷新抽奖记录和奖品数据 lotteryId={}", lotteryId);
            try {
                Thread.sleep(60*1000);
                log.info("开始刷新抽奖记录和奖品数据 lotteryId={}", lotteryId);
                prizeService.updatePrizeCount(lotteryId, name);
                recordService.refreshRecord(lotteryId);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }



}
