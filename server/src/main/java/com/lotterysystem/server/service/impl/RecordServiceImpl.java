package com.lotterysystem.server.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lotterysystem.server.constant.CachePrefix;
import com.lotterysystem.server.constant.ResultStatue;
import com.lotterysystem.server.mapper.RecordMapper;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Record;
import com.lotterysystem.server.service.RecordActionService;
import com.lotterysystem.server.service.RecordService;
import com.lotterysystem.server.util.CacheUtil;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author nsh
 * @data 2025/5/14 13:38
 * @description
 **/
@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record>
        implements RecordService {

    private final RabbitTemplate rabbitTemplate;

    private final RedisTemplate redisTemplate;

    private final RecordActionService recordActionService;

    private final CacheUtil cacheUtil;

    private final BlockingQueue<Record> queue = new LinkedBlockingQueue<>();

    @Autowired
    @Qualifier("batchingExecutor")
    private ThreadPoolTaskExecutor taskExecutor;


    @Async("queueExecutor")
    @Override
    public void sendToQueue(String queueName, String key,Object result) {
        rabbitTemplate.convertAndSend(queueName,key,result);
    }

    @PostConstruct
    @DS("master")
    public void batching(){
        log.info("启动收集抽奖批处理线程");
        taskExecutor.execute(()->{
            List<Record> buffer = new ArrayList<>();
            while(true){
                try{
                    Record record = queue.poll(200, TimeUnit.MILLISECONDS);

                    if(record != null){
                        buffer.add(record);
                    }
                    if (buffer.size() >= 500 || (record == null && !buffer.isEmpty())) {
                        recordActionService.batchingSave(new ArrayList<>(buffer));
                        buffer.clear();
                    }
                }catch (InterruptedException e){
                    log.info("被中断！" + e.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }



    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "lottery.queue1", durable = "true", arguments = {
                    @Argument(name = "x-queue-mode",value = "lazy"),
                    @Argument(name = "x-dead-letter-exchange", value = "lottery.dead"),
                    @Argument(name = "x-dead-letter-routing-key",value ="dead")
            }),
            exchange = @Exchange(name = "lottery.exchange1", type = ExchangeTypes.DIRECT),
            key = "grab"
    ))
    @DS("master")
    public void saveRecordByMySQL(Record record) {
        boolean ok = queue.offer(record);
        if (!ok) {
            log.warn("业务处理失败，将触发重试或进入死信队列：{}", record);
            throw new RuntimeException("模拟业务异常，触发重试");
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "lottery.deadQueue", durable = "true", arguments = {
                    @Argument(name = "x-queue-mode",value = "lazy"),
            }),
            exchange = @Exchange(name = "lottery.dead", type = ExchangeTypes.DIRECT),
            key = "dead"
    ))
    public void deadMsgHandler(Record record) {
        log.info("记录无法记录！{}",record);
    }




    //只会返回end为1的记录
    @Override
    @DS("slave")
    public Result getMyAllPrize(Long userId) {
        return new Result(ResultStatue.SUCCESS,"查询成功!",getMyAllPrizeForAPI(userId));
    }

    @Override
    @DS("slave")
    public List<Record> getMyAllPrizeForAPI(Long userId) {

        List<Long> recordIds = cacheUtil.queryWithMutexWithTick(CachePrefix.USERSALLRECORD.getPrefix(), userId, new TypeReference<List<Long>>() {
        }, id -> lambdaQuery().eq(Record::getUserId, userId).eq(Record::getIsEnd,1).select(Record::getId).list().stream().map(Record::getId).toList());

        List<Record> records = cacheUtil.MultiQueryWithMutex(CachePrefix.ONERECORD.getPrefix(), recordIds,new TypeReference<Record>(){},
                id -> lambdaQuery().eq(Record::getId, id).eq(Record::getIsEnd,1).one());

        return records;
    }

    @Override
    @DS("slave")
    public Result getMyPrizeByLotteryId(Long lotteryId, Long userId){
        List<Record> records = getRecordsByLotteryId(lotteryId);
        List<Record> target = new ArrayList<>();
        for (Record record : records) {
            if (record.getUserId().equals(userId)) {
                target.add(record);
            }
        }
        return new Result(ResultStatue.SUCCESS,"查询成功!",target);
    }

    @Override
    @DS("slave")
    public List<Record> getRecordsByLotteryId(Long lotteryId){

        List<Long> recordIds = cacheUtil.queryWithMutexWithTick(CachePrefix.LOTTERRECORD.getPrefix(),lotteryId, new TypeReference<List<Long>>() {
        }, id -> lambdaQuery().eq(Record::getLotteryId, lotteryId).eq(Record::getIsEnd,1).select(Record::getId).list().stream().map(Record::getId).toList());

        return cacheUtil.MultiQueryWithMutex(CachePrefix.ONERECORD.getPrefix(), recordIds,new TypeReference<Record>() {},
                id -> lambdaQuery().eq(Record::getId, id).eq(Record::getIsEnd,1).one());
    }

    @Override
    @DS("master")
    public List<Record> getRecordsByLotteryIdWithNoEnd(Long lotteryId){
        List<Record> records = lambdaQuery().eq(Record::getLotteryId, lotteryId).eq(Record::getIsEnd,0).list();
        return records;
    }

    @Override
    @DS("master")
    public void refreshRecord(Long lotteryId){
        List<Record> list = lambdaQuery().eq(Record::getLotteryId, lotteryId).list();
        for (Record record : list) {
            record.setIsEnd(1);
        }
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            ObjectMapper objectMapper = new ObjectMapper();
            for (Record record : list) {
                String key = CachePrefix.LOTTERRECORD.getPrefix() + ":" + record.getLotteryId();
                long expireSeconds = 60 * 30 + RandomUtil.randomLong(15L); // 30分钟 + 随机15秒

                try {
                    byte[] keyBytes = redisTemplate.getStringSerializer().serialize(key);
                    byte[] valueBytes = objectMapper.writeValueAsBytes(record);

                    connection.setEx(keyBytes, expireSeconds, valueBytes);
                } catch (Exception e) {
                    log.info("刷新抽奖结束缓存异常！{}",e.getMessage());
                }
            }
            return null;
        });


        this.updateBatchById(list);
    }




}
