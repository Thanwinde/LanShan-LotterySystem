package com.lotterysystem.server.service;

import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Record;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.util.List;

/**
* @author thanw
* @description 针对表【record】的数据库操作Service
* @createDate 2025-05-14 14:09:21
*/
public interface RecordService extends IService<Record> {

    @Async
    void sendToQueue(String queueName,String key ,Object result);

    Result getMyAllPrize(Long userId);

    List<Record> getMyAllPrizeForAPI(Long userId);

    Result getMyPrizeByLotteryId(Long lotteryId, Long userId);

    List<Record> getRecordsByLotteryId(Long lotteryId);

    List<Record> getRecordsByLotteryIdWithNoEnd(Long lotteryId);

    void refreshRecord(Long lotteryId);

    String getRecordsWithExcel(Long lotteryId);
}
