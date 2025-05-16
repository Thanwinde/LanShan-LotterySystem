package com.lotterysystem.server.service;

import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Record;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.scheduling.annotation.Async;

/**
* @author thanw
* @description 针对表【record】的数据库操作Service
* @createDate 2025-05-14 14:09:21
*/
public interface RecordService extends IService<Record> {

    @Async
    void sendToQueue(String queueName, Object result);

    Result getMyAllPrize(Long userId);

    Result getMyPrizeByLotteryId(Long lotteryId, Long userId);

    void refreshRecord(Long lotteryId);
}
