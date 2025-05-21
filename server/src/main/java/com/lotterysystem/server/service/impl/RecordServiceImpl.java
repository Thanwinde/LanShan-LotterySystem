package com.lotterysystem.server.service.impl;

import cn.hutool.core.lang.TypeReference;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.server.constant.CachePrefix;
import com.lotterysystem.server.constant.ResultStatue;
import com.lotterysystem.server.mapper.RecordMapper;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Record;
import com.lotterysystem.server.service.RecordService;
import com.lotterysystem.server.util.CacheUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final CacheUtil cacheUtil;

    private final BlockingQueue<Record> queue = new LinkedBlockingQueue<>();


    @Async
    @Override
    public void sendToQueue(String queueName, Object result) {
        rabbitTemplate.convertAndSend(queueName, result);
    }

    @PostConstruct
    public void batching(){
        log.info("收集抽奖批处理");
        new Thread(()->{
            List<Record> buffer = new ArrayList<>();
            while(true){
                try{
                    Record record = queue.poll(200, TimeUnit.MILLISECONDS);
                    if(record != null){
                        buffer.add(record);
                    }
                    if (buffer.size() >= 500 || (record == null && !buffer.isEmpty())) {
                        saveBatch(new ArrayList<>(buffer));
                        buffer.clear();
                    }
                }catch (InterruptedException e){
                    log.info("被中断！" + e.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    @RabbitListener(queues = "lottery.resultQueue")
    public void saveRecordByMySQL(Record record) {
        boolean ok = queue.offer(record);
        if (!ok) {
            // 再试一次，等待 50ms 看看有没有空位
            try {
                ok = queue.offer(record, 50, TimeUnit.MILLISECONDS);
                if (!ok) {
                    log.error("写入队列失败！ {}", record);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("写入队列时被中断失败！", e);
            }
        }
    }

    //只会返回end为1的记录
    @Override
    public Result getMyAllPrize(Long userId) {
        List<Record> records = cacheUtil.queryWithMutexWithTick(CachePrefix.USERSALLRECORD.getPrefix(), userId, new TypeReference<List<Record>>() {
        }, id -> lambdaQuery().eq(Record::getUserId, userId).eq(Record::getIsEnd,1).list());
        return new Result(ResultStatue.SUCCESS,"查询成功!",records);
    }

    @Override
    public List<Record> getMyAllPrizeForAPI(Long userId) {
        List<Long> recordIds = cacheUtil.queryWithMutexWithTick(CachePrefix.USERSALLRECORD.getPrefix(), userId, new TypeReference<List<Long>>() {
        }, id -> lambdaQuery().eq(Record::getUserId, userId).eq(Record::getIsEnd,1).select(Record::getId).list().stream().map(Record::getId).toList());

        List<Record> records = cacheUtil.MultiQueryWithMutexWithTick(CachePrefix.LOTTERRECORD.getPrefix(), recordIds,new TypeReference<Record>(){},
                id -> lambdaQuery().eq(Record::getId, id).eq(Record::getIsEnd,1).one());

        return records;
    }
    //todo
    @Override
    public Result getMyPrizeByLotteryId(Long lotteryId, Long userId){
        List<Record> records = cacheUtil.queryWithMutexWithTick(CachePrefix.USERSPRIZE.getPrefix(), userId, new TypeReference<List<Record>>() {
        }, id -> lambdaQuery().eq(Record::getUserId, userId).eq(Record::getLotteryId, lotteryId).eq(Record::getIsEnd,1).list());
        return new Result(ResultStatue.SUCCESS,"查询成功!",records);
    }

    @Override
    public List<Record> getRecordsByLotteryId(Long lotteryId){
        List<Record> records = cacheUtil.queryWithMutex(CachePrefix.LOTTERRECORD.getPrefix(),lotteryId, new TypeReference<List<Record>>() {
        }, id -> lambdaQuery().eq(Record::getLotteryId, lotteryId).eq(Record::getIsEnd,1).list());
        return records;
    }

    @Override
    public void refreshRecord(Long lotteryId){
        List<Record> list = lambdaQuery().eq(Record::getLotteryId, lotteryId).list();
        for (Record record : list) {
            record.setIsEnd(1);
        }
        this.updateBatchById(list);
    }


}
