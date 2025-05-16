package com.lotterysystem.server.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import com.lotterysystem.gateway.util.UserContext;
import com.lotterysystem.server.constant.ResultStatue;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Lottery;
import com.lotterysystem.server.pojo.entity.Record;
import com.lotterysystem.server.service.LotteryActionService;
import com.lotterysystem.server.service.LotteryService;
import com.lotterysystem.server.service.RecordService;
import com.lotterysystem.server.util.CacheUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;


/**
 * @author nsh
 * @data 2025/5/13 19:20
 * @description
 **/
@RequiredArgsConstructor
@Service
@Slf4j
public class LotteryActionImpl implements LotteryActionService {

    private final LotteryService lotteryService;

    final private CacheUtil cacheUtil;

    private final RedisTemplate<String,Long> redisTemplate;

    private final RecordService recordService;

    String normalGrabLua;

    DefaultRedisScript<String> normalGrabScript = new DefaultRedisScript<>();

    @PostConstruct
    protected void init(){
        normalGrabLua = ResourceUtil.readUtf8Str("NormalGrab.lua");
        normalGrabScript.setScriptText(normalGrabLua);
        normalGrabScript.setResultType(String.class);
    }

    @Override
    public Result tryGrab(Long lotteryId) {
        Lottery lottery = lotteryService.getLotteryById(lotteryId);
        if(lottery.getIsEnd() == 1 || lottery.getIsActive() == 0){
            return new Result(ResultStatue.ERROR,"活动已结束或未开始！",null);
        }

        //todo检测是否有抽奖资格
        //todo判断抽奖类型
        if(lottery.getType() == 0)
            return normalGrabImmediately(lottery,500L);
        if(lottery.getType() == 1)
            return normalGrabLate(lottery,500L);

        return new Result(ResultStatue.ERROR,"不支持的抽奖类型!",null);
    }

    //纯运气型，概率多少就是多少
    public Result normalGrabImmediately(Lottery lottery,Long top) {

        Long userId = UserContext.getId();
        String back = redisTemplate.execute(normalGrabScript, Collections.emptyList(),lottery.getId(),userId,top);

        String[] split = back.split("#");
        Long id = Long.valueOf(split[0]);
        String name = split[2];
        if(id == -1)
            return new Result(ResultStatue.SUCCESS,"你已用完抽奖次数！",null);
        if(id == -2 || name.equals("null"))
            return new Result(ResultStatue.SUCCESS,"没中奖！",null);
        Record record = new Record().setLotteryId(lottery.getId()).setLotteryName(lottery.getName()).setPrizeId(id).setPrizeName(name).setUserId(userId).setIsEnd(1);
        recordService.sendToQueue("lottery.resultQueue",record);
        return new Result(ResultStatue.SUCCESS,"中奖了！",name);
    }

    public Result normalGrabLate(Lottery lottery,Long top) {

        Long userId = UserContext.getId();
        String back = redisTemplate.execute(normalGrabScript, Collections.emptyList(),lottery.getId(),userId,top);

        String[] split = back.split("#");
        Long id = Long.valueOf(split[0]);
        String name = split[2];
        if(id == -1)
            return new Result(ResultStatue.SUCCESS,"你已用完抽奖次数！",null);
        if(id == -2 || name.equals("null"))
            return new Result(ResultStatue.SUCCESS,"抽奖成功，请等待开奖！",null);
        Record record = new Record().setLotteryId(lottery.getId()).setLotteryName(lottery.getName()).setPrizeId(id).setPrizeName(name).setUserId(userId).setIsEnd(0);
        recordService.sendToQueue("lottery.resultQueue",record);
        return new Result(ResultStatue.SUCCESS,"抽奖成功，请等待开奖！",null);
    }


}
