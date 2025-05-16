package com.lotterysystem.server.service.impl;

import cn.hutool.core.lang.TypeReference;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.server.constant.CachePrefix;
import com.lotterysystem.server.mapper.LotteryMapper;
import com.lotterysystem.server.pojo.entity.Lottery;
import com.lotterysystem.server.service.RecordService;
import com.lotterysystem.server.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    final PrizeServiceImpl prizeService;

    final RecordServiceImpl recordService;

    public Lottery getLotteryWithCache(Long lotteryId){
        return cacheUtil.queryWithMutex(CachePrefix.LOTTERYOBJ.getPrefix(), lotteryId,new TypeReference<Lottery>() {},this::getById);
    }

    public void updateLotteryWithCache(Lottery lottery){
        cacheUtil.update(CachePrefix.LOTTERYOBJ.getPrefix(),lottery.getId(),lottery,new TypeReference<Lottery>() {},this::getById,this::updateById);
    }

    public void startLottery(Long lotteryId){
        //搬入奖品到redis,创表
        prizeService.joinToPool(lotteryId);

        Lottery lottery = getLotteryWithCache(lotteryId);
        lottery.setIsActive(1);
        updateLotteryWithCache(lottery);
        log.info("开始抽奖：{} {}",lotteryId,lottery.getName());
    }

    public void endLottery(Long lotteryId){

        Lottery lottery = getLotteryWithCache(lotteryId);
        lottery.setIsActive(0);
        lottery.setIsEnd(1);
        updateLotteryWithCache(lottery);
        log.info("抽奖结束:{} {}",lotteryId,lottery.getName());

        cacheUtil.delete(CachePrefix.PRIZEPOOL.getPrefix(), lotteryId);
        cacheUtil.delete(CachePrefix.LOTTERYCOUNT.getPrefix(), lotteryId);
        prizeService.deleteLotteryActionCache(lotteryId,lottery.getName());
        recordService.refreshRecord(lotteryId);

    }

    public void endLottery(Lottery lottery){

        lottery.setIsActive(0);
        lottery.setIsEnd(1);
        updateLotteryWithCache(lottery);
        log.info("抽奖结束:{} {}",lottery.getId(),lottery.getName());

        cacheUtil.delete(CachePrefix.PRIZEPOOL.getPrefix(), lottery.getId());
        cacheUtil.delete(CachePrefix.LOTTERYCOUNT.getPrefix(), lottery.getId());
        prizeService.deleteLotteryActionCache(lottery.getId(),lottery.getName());
        recordService.refreshRecord(lottery.getId());

    }
}
