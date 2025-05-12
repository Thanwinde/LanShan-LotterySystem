package com.lotterysystem.server.scheduler;

import com.lotterysystem.server.scheduler.job.LotteryJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryScheduler {

    private final  Scheduler scheduler;

    @PostConstruct
    public void init() throws SchedulerException {
        if (!scheduler.isStarted()) {
            scheduler.start();
            log.info("启动Quartz");
        }
    }

    public void scheduleLottery(String lotteryId,Integer type,Date startTime) throws Exception {

        JobDetail job = JobBuilder.newJob(LotteryJob.class)
                .withIdentity(lotteryId)
                .storeDurably(false)
                .usingJobData("type", type)
                .build();
        Trigger trigger;
        if(type == 0) {
            trigger = TriggerBuilder.newTrigger()
                    .forJob(job)
                    .withIdentity(lotteryId + "_trigger")
                    .startAt(startTime)
                    .build();
        }else{
            trigger = TriggerBuilder.newTrigger()
                    .forJob(job)
                    .withIdentity(lotteryId + "_trigger")
                    .startAt(startTime)
                    .build();
        }
        log.info("创建任务:{}",lotteryId);
        scheduler.scheduleJob(job, trigger);
    }

    public void cancelLottery(String lotteryId) throws Exception {
        log.info("取消节点:{}",lotteryId);
        scheduler.deleteJob(new JobKey(lotteryId));
    }

    public void rescheduleLottery(String lotteryId, Date newStartTime) throws Exception {
        Trigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(lotteryId + "_trigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionIgnoreMisfires())
                .startAt(newStartTime)
                .build();
        log.info("修改任务:{}",lotteryId);
        scheduler.rescheduleJob(new TriggerKey(lotteryId + "_trigger"), newTrigger);
    }
}
