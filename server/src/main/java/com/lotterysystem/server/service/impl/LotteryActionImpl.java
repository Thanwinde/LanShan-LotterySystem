package com.lotterysystem.server.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.RandomUtil;
import com.lotterysystem.gateway.util.UserContext;
import com.lotterysystem.server.constant.CachePrefix;
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
        Lottery lottery = lotteryService.getLotteryByIdForAPI(lotteryId);



        if(lottery == null){
            return  new Result(ResultStatue.ERROR,"活动已结束或未开始！",null);
        }

        Long userId = UserContext.getId();
        String userName = UserContext.getName();
        if(lottery.getIsEnd() == 1 || lottery.getIsActive() == 0){
            return new Result(ResultStatue.ERROR,"活动已结束或未开始！",null);
        }

        Long top = Long.valueOf( (Integer) redisTemplate.opsForHash().get(CachePrefix.WEIGHT.getPrefix() + ":" + lotteryId,"-1"));
        Object open =  redisTemplate.opsForHash().get(CachePrefix.ATTEND.getPrefix() + ":" + lotteryId, "-1");
        if(open == null){
            Object ex = redisTemplate.opsForHash().get(CachePrefix.ATTEND.getPrefix() + ":" + lotteryId,userName);
            if(ex == null) return new Result(ResultStatue.ERROR,"很抱歉，你不被允许参加！",null);
        }   //通过attend判断允不允许参加，-1则允许所有人

        if(lottery.getType() == 0)
            return normalGrabImmediately(lottery,top);
        if(lottery.getType() == 1)
            return normalGrabLate(lottery,top);

        return new Result(ResultStatue.ERROR,"不支持的抽奖类型!",null);
    }

    //纯运气型，来的早就能抢到,如果想实现和时间无关就加入空奖，奖名为"null"就行
    //可用：黑名单，黑名单者获奖概率降低50%，权重：一定几率重掷，取最高者
    public Result normalGrabImmediately(Lottery lottery,Long top) {
        Long userId = UserContext.getId();
        String back;
        Object isBlack =  redisTemplate.opsForHash().get(CachePrefix.BLACKLIST.getPrefix() + ":" + lottery.getId(), userId.toString());
        //黑名单判断
        if(isBlack != null){
            back = redisTemplate.execute(normalGrabScript, Collections.emptyList(),lottery.getId(),userId,top,1,0);
            log.info("黑幕！");
        }
        else{
            Integer fortune = (Integer) redisTemplate.opsForHash().get(CachePrefix.WEIGHT.getPrefix() + ":" + lottery.getId(), userId.toString());
            if (fortune != null && RandomUtil.randomLong(0L,100L) <= fortune){
                back = redisTemplate.execute(normalGrabScript, Collections.emptyList(),lottery.getId(),userId,top,0,1);
                log.info("重掷！");
            }
            else
                back = redisTemplate.execute(normalGrabScript, Collections.emptyList(),lottery.getId(),userId,top,0,0);
        }


        String[] split = back.split("#");
        Long id = Long.valueOf(split[0]);
        String name = split[2];
        String rarity = split[3];
        if(id == -1)
            return new Result(ResultStatue.SUCCESS,"你已用完抽奖次数！",null);

        Record record = new Record().setLotteryId(lottery.getId()).setLotteryName(lottery.getName()).setPrizeId(id).setPrizeName(name).setUserId(userId).setIsEnd(1);
        recordService.sendToQueue("lottery.resultQueue",record);
        if(id == -2 || name.equals("null"))
            return new Result(ResultStatue.SUCCESS,"没中奖！",null);
        return new Result(ResultStatue.SUCCESS,"中奖了！稀有度：" + rarity,name);
    }
    //纯运气型，来的早就能抢到,如果想实现和时间无关就加入空奖，奖名为"null"就行，延时开奖
    public Result normalGrabLate(Lottery lottery,Long top) {

        Long userId = UserContext.getId();
        String back;
        Integer isBlack = (Integer) redisTemplate.opsForHash().get(CachePrefix.BLACKLIST.getPrefix() + ":" + lottery.getId(), userId);
        //黑名单判断
        if(isBlack != null){
            back = redisTemplate.execute(normalGrabScript, Collections.emptyList(),lottery.getId(),userId,top,1);
            log.info("黑幕！");
        }
        else
            back = redisTemplate.execute(normalGrabScript, Collections.emptyList(),lottery.getId(),userId,top,0);

        String[] split = back.split("#");
        Long id = Long.valueOf(split[0]);
        String name = split[2];
        String rarity = split[3];
        if(id == -1)
            return new Result(ResultStatue.SUCCESS,"你已用完抽奖次数！",null);
        if(id == -2 || name.equals("null")){

        }
        Record record = new Record().setLotteryId(lottery.getId()).setLotteryName(lottery.getName()).setPrizeId(id).setPrizeName(name).setUserId(userId).setIsEnd(0);
        recordService.sendToQueue("lottery.resultQueue",record);
        return new Result(ResultStatue.SUCCESS,"抽奖成功，请等待开奖！",null);
    }


}
