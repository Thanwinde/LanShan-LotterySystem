package com.lotterysystem.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.gateway.util.UserContext;
import com.lotterysystem.server.constant.ResultStatue;

import com.lotterysystem.server.mapper.LotteryMapper;
import com.lotterysystem.server.pojo.dto.LotteryDTO;
import com.lotterysystem.server.pojo.dto.PrizeDTO;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Lottery;

import com.lotterysystem.server.pojo.entity.Prize;
import com.lotterysystem.server.pojo.vo.LotteryVO;
import com.lotterysystem.server.pojo.vo.PrizeVO;
import com.lotterysystem.server.scheduler.LotteryScheduler;
import com.lotterysystem.server.service.LotteryService;
import com.lotterysystem.server.service.PrizeService;
import com.lotterysystem.server.util.CacheUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author nsh
 * @data 2025/5/10 14:43
 * @description
 **/
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LotteryServiceImpl extends ServiceImpl<LotteryMapper, Lottery> implements LotteryService {

    final CacheUtil cacheUtil;

    final LotteryScheduler lotteryScheduler;

    final PrizeService prizeService;

    @Override
    public Result addLottery(LotteryDTO lotteryDTO) {
        if(lotteryDTO.getStartTime().getTime() < System.currentTimeMillis() || lotteryDTO.getEndTime().getTime()< lotteryDTO.getStartTime().getTime()){
            return new Result(ResultStatue.ERROR,"创建失败!时间不合法!",null);
        }

        Lottery lottery = BeanUtil.toBean(lotteryDTO, Lottery.class);
        Long userId = UserContext.getId();
        String name = UserContext.getName();
        lottery.setIsActive(0);
        lottery.setIsEnd(0);
        lottery.setCreatedBy(userId);
        lottery.setCreatorName(name);
        lottery.setCreatedAt(new Date());
        lottery.setUpdatedAt(new Date());

        if(this.save(lottery)){
            try {
                lotteryScheduler.scheduleLottery(lottery.getId() + "#0",0,lottery.getStartTime());
                lotteryScheduler.scheduleLottery(lottery.getId() + "#1",1,lottery.getEndTime());
            } catch (Exception e) {
                log.error("新建出错！{}",lottery);
                throw new RuntimeException(e);
            }
            ArrayList<PrizeDTO> PrizeDTOs = lotteryDTO.getPrizes();
            prizeService.addPrizeList(lottery.getId(),PrizeDTOs);
            return new Result(ResultStatue.SUCCESS,"创建成功!",null);
        }

        else
            return new Result(ResultStatue.ERROR,"创建失败!",null);
    }

    @Override
    public Result getYourLottery() {
        Long userId = UserContext.getId();
        List<Lottery> lotteries = cacheUtil.queryWithMutex("lottery:obj",userId,List.class,id ->lambdaQuery().eq(Lottery::getCreatedBy,id).list());
        ArrayList<LotteryVO> lotteryVOS = new ArrayList<>();
        for (Lottery lottery : lotteries) {
            Long lotteryId = lottery.getId();
            List<Prize> prize = prizeService.getPrizeList(lotteryId);
            ArrayList<PrizeVO> prizeVOS = new ArrayList<>();
            for(Prize prize1 : prize){
                PrizeVO prizeVO = BeanUtil.copyProperties(prize1,PrizeVO.class);
                prizeVOS.add(prizeVO);
            }
            LotteryVO lotteryVO = BeanUtil.toBean(lottery, LotteryVO.class);
            lotteryVO.setPrizes( prizeVOS);
            lotteryVOS.add(lotteryVO);
        }

        return new Result(ResultStatue.SUCCESS, "查询成功！!", lotteryVOS);
    }

    @Override
    public Result getLottery(Long lotteryId) {
        Lottery lottery = cacheUtil.queryWithMutex("lottery:obj", lotteryId, Lottery.class, this::getById);
        List<Prize> prize = prizeService.getPrizeList(lotteryId);
        ArrayList<PrizeVO> prizeVOS = new ArrayList<>();
        for(Prize prize1 : prize){
            PrizeVO prizeVO = BeanUtil.toBean(prize1,PrizeVO.class);
            prizeVOS.add(prizeVO);
        }
        LotteryVO lotteryVO = BeanUtil.copyProperties(lottery, LotteryVO.class);
        lotteryVO.setPrizes(prizeVOS);

        return new Result(ResultStatue.SUCCESS,"查询成功！",lotteryVO);
    }

    @Override
    public Result updateLottery(LotteryDTO lotteryDTO) {
        Lottery lottery = lambdaQuery().eq(Lottery::getId,lotteryDTO.getId()).one();
        Long userId = UserContext.getId();
        String auth = UserContext.getAuth();

        if(!userId.equals(lottery.getCreatedBy()) && !auth.equals("admin"))
            return new Result(ResultStatue.FORBIDDEN,"你不能更改不是你发布的抽奖活动！",null);
        if(lottery.getIsActive() == 1)
            return new Result(ResultStatue.ERROR,"已经开始，无法修改抽奖内容!请先停止再取消！",null);
        if(lottery.getIsEnd() == 1)
            return new Result(ResultStatue.ERROR,"已经结束，无法修改抽奖内容!",null);
        if(lotteryDTO.getStartTime().getTime() < System.currentTimeMillis() || lotteryDTO.getEndTime().getTime()< lotteryDTO.getStartTime().getTime()){
            return new Result(ResultStatue.ERROR,"修改失败!时间不合法!",null);
        }

            try {
                if(!lottery.getStartTime().equals(lotteryDTO.getStartTime()))
                    lotteryScheduler.rescheduleLottery(lotteryDTO.getId()+"#0", lotteryDTO.getStartTime());
                if(!lottery.getEndTime().equals(lotteryDTO.getEndTime()))
                    lotteryScheduler.rescheduleLottery(lotteryDTO.getId()+"#1", lotteryDTO.getEndTime());
            } catch (Exception e) {
                log.error("更新任务出错:{}",lottery);
                throw new RuntimeException(e);
            }
        lottery = BeanUtil.toBean(lotteryDTO, Lottery.class);
        lottery.setUpdatedAt(new Date());
        cacheUtil.update("lottery:obj",lottery.getId(),lottery,Lottery.class,this::getById,this::updateById);
        return new Result(ResultStatue.SUCCESS,"更新成功！",null);
    }

    @Override
    public Result stopLottery(Long id) {
        Lottery lottery = cacheUtil.queryWithMutex("lottery:obj", id, Lottery.class, this::getById);

        if(lottery == null || lottery.getIsActive() == 0 || lottery.getIsEnd() == 1)
            return new Result(ResultStatue.ERROR,"活动已经结束,未开始或不存在！",null);

        lottery.setIsActive(0);
        lottery.setIsEnd(1);

        try {
            lotteryScheduler.cancelLottery(id + "#1");
        } catch (Exception e) {
            log.error("尝试取消失败！{}",id);
            throw new RuntimeException(e);
        }

        cacheUtil.update("lottery:obj",lottery.getId(),lottery,Lottery.class,this::getById,this::updateById);

        //TODO结果收集

        return new Result(ResultStatue.SUCCESS,"停止成功！",null);
    }


    @Schema(description = "会删除所有数据，包括任务与结果")
    @Override
    public Result deleteLottery(Long id)  {
        Lottery lottery = lambdaQuery().eq(Lottery::getId,id).one();
        Long userId = UserContext.getId();
        String auth = UserContext.getAuth();
        if(!userId.equals(lottery.getCreatedBy()) && !auth.equals("admin"))
            return new Result(ResultStatue.FORBIDDEN,"你不能删除不是你的抽奖活动！",null);

        if(lottery.getIsActive() == 1)
            return new Result(ResultStatue.FORBIDDEN,"请先停止正在进行的抽奖再删除！",null);
        try {
            lotteryScheduler.cancelLottery(id + "#0");
            lotteryScheduler.cancelLottery(id + "#1");
        } catch (Exception e) {
            log.error("删除任务出错:{}",lottery);
            throw new RuntimeException(e);
        }

        this.removeById(id);
        cacheUtil.delete("lottery:obj",id);
        if(lottery.getIsEnd() == 0){
            //todo 删除结果
        }
            else{

        }
        return new Result(ResultStatue.SUCCESS,"删除成功！",null);
    }


}
