package com.lotterysystem.server.scheduler.job;

import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Lottery;
import com.lotterysystem.server.service.LotteryService;
import com.lotterysystem.server.service.impl.LotteryJobService;
import com.lotterysystem.server.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LotteryJob implements Job {

    final CacheUtil cacheUtil;

    final LotteryJobService lotteryJobService;

    @Override
    public void execute(JobExecutionContext context) {
        String lotteryId = context.getJobDetail().getKey().getName().split("#")[0];
        int type = context.getJobDetail().getJobDataMap().getInt("type");

        if(type == 0){
            lotteryJobService.startLottery(lotteryId);

        }else if(type == 1){
            lotteryJobService.endLottery(lotteryId);

        }
    }
}
