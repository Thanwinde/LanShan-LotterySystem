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
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author nsh
 * @data 2025/5/14 13:38
 * @description
 **/
@RequiredArgsConstructor
@Service
@Transactional
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record>
        implements RecordService {

    private final RabbitTemplate rabbitTemplate;

    private final CacheUtil cacheUtil;

    @Async
    @Override
    public void sendToQueue(String queueName, Object result) {
        rabbitTemplate.convertAndSend(queueName, result);
    }

    @RabbitListener(queues = "lottery.resultQueue")
    public void saveRecordByMySQL(Record record) {
        save(record);
    }
    //只会返回end为1的记录
    @Override
    public Result getMyAllPrize(Long userId) {
        List<Record> records = cacheUtil.queryWithMutexWithTick(CachePrefix.USERSALLPRIZE.getPrefix(), userId, new TypeReference<List<Record>>() {
        }, id -> lambdaQuery().eq(Record::getUserId, userId).eq(Record::getIsEnd,1).list());
        return new Result(ResultStatue.SUCCESS,"查询成功!",records);
    }

    @Override
    public List<Record> getMyAllPrizeForAPI(Long userId) {
        List<Record> records = cacheUtil.queryWithMutexWithTick(CachePrefix.USERSALLPRIZE.getPrefix(), userId, new TypeReference<List<Record>>() {
        }, id -> lambdaQuery().eq(Record::getUserId, userId).eq(Record::getIsEnd,1).list());
        return records;
    }

    @Override
    public Result getMyPrizeByLotteryId(Long lotteryId, Long userId){
        List<Record> records = cacheUtil.queryWithMutexWithTick(CachePrefix.USERSPRIZE.getPrefix(), userId, new TypeReference<List<Record>>() {
        }, id -> lambdaQuery().eq(Record::getUserId, userId).eq(Record::getLotteryId, lotteryId).eq(Record::getIsEnd,1).list());
        return new Result(ResultStatue.SUCCESS,"查询成功!",records);
    }

    @Override
    public void refreshRecord(Long lotteryId){
        List<Record> list = lambdaQuery().eq(Record::getLotteryId, lotteryId).list();
        for (Record record : list) {
            record.setIsEnd(1);
            this.updateById(record);
        }
    }


}
