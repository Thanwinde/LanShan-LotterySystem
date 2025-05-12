package com.lotterysystem.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.server.mapper.LotteryMapper;
import com.lotterysystem.server.pojo.entity.Lottery;
import com.lotterysystem.server.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    final private PrizeServiceImpl prizeService;

    public Lottery getLotteryWithCache(String id){
        return cacheUtil.queryWithMutex("lottery:obj",id,Lottery.class,this::getById);
    }

    public void updateLotteryWithCache(Lottery lottery){
        cacheUtil.update("lottery:obj",lottery.getId(),lottery,Lottery.class,this::getById,this::updateById);
    }

    public void startLottery(String id){
        //TODO搬入奖品到redis


        Lottery lottery = getLotteryWithCache(id);
        lottery.setIsActive(1);
        updateLotteryWithCache(lottery);
        log.info("开始抽奖：{} {}",id,lottery.getName());


    }

    public void endLottery(String id){

        Lottery lottery = getLotteryWithCache(id);
        lottery.setIsActive(0);
        lottery.setIsEnd(1);
        updateLotteryWithCache(lottery);
        log.info("抽奖结束:{} {}",id,lottery.getName());

        //TODO处理结果
    }
}
