package com.lotterysystem.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.gateway.util.UserContext;
import com.lotterysystem.server.constant.CachePrefix;
import com.lotterysystem.server.constant.ResultStatue;

import com.lotterysystem.server.mapper.LotteryMapper;
import com.lotterysystem.server.pojo.dto.LotteryDTO;
import com.lotterysystem.server.pojo.dto.PrizeDTO;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Lottery;

import com.lotterysystem.server.pojo.entity.Prize;
import com.lotterysystem.server.pojo.entity.Record;
import com.lotterysystem.server.pojo.vo.LotteryVO;
import com.lotterysystem.server.pojo.vo.PrizeVO;
import com.lotterysystem.server.scheduler.LotteryScheduler;
import com.lotterysystem.server.service.LotteryService;
import com.lotterysystem.server.service.PrizeService;
import com.lotterysystem.server.service.RecordService;
import com.lotterysystem.server.util.CacheUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
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

    final LotteryJobService lotteryJobService;

    final RecordService recordService;

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
            cacheUtil.delete(CachePrefix.USERSLOTTERY.getPrefix(), lottery.getCreatedBy());
            return new Result(ResultStatue.SUCCESS,"创建成功!",null);
        }

        else

            return new Result(ResultStatue.ERROR,"创建失败!",null);
    }


    @Override
    public Result getLottery(Long lotteryId) {
        Lottery lottery = cacheUtil.queryWithMutex(CachePrefix.LOTTERYOBJ.getPrefix(), lotteryId, new TypeReference<Lottery>() {}, this::getById);
        if(lottery == null)
            return new Result(ResultStatue.SUCCESS,"查询成功！",null);
        LotteryVO lotteryVO = null;
        if(lottery != null) {
            List<Prize> prize = prizeService.getPrizeList(lotteryId);
            ArrayList<PrizeVO> prizeVOS = new ArrayList<>();
            for (Prize prize1 : prize) {
                PrizeVO prizeVO = BeanUtil.toBean(prize1, PrizeVO.class);
                prizeVOS.add(prizeVO);
            }
            lotteryVO = BeanUtil.copyProperties(lottery, LotteryVO.class);
            lotteryVO.setPrizes(prizeVOS);
        }
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
        cacheUtil.update(CachePrefix.LOTTERYOBJ.getPrefix(),lottery.getId(),lottery,new TypeReference<Lottery>() {},this::getById,this::updateById);

        ArrayList<PrizeDTO> PrizeDTOs = lotteryDTO.getPrizes();

        prizeService.deletePrizeList(lotteryDTO.getId());

        prizeService.addPrizeList(lotteryDTO.getId(), PrizeDTOs);

        return new Result(ResultStatue.SUCCESS,"更新成功！",null);
    }

    @Override
    public Result stopLottery(Long id) {
        Lottery lottery = cacheUtil.queryWithMutex(CachePrefix.LOTTERYOBJ.getPrefix(), id, new TypeReference<Lottery>() {}, this::getById);

        if(lottery == null || lottery.getIsActive() == 0 || lottery.getIsEnd() == 1)
            return new Result(ResultStatue.ERROR,"活动已经结束,未开始或不存在！",null);

        try {
            lotteryScheduler.cancelLottery(id + "#1");
        } catch (Exception e) {
            log.error("尝试取消失败！{}",id);
            throw new RuntimeException(e);
        }

        lotteryJobService.endLottery(lottery);

        return new Result(ResultStatue.SUCCESS,"停止成功！",null);
    }


    @Schema(description = "会删除所有数据，包括任务与结果")
    @Override
    public Result deleteLottery(Long id)  {
        Lottery lottery =  cacheUtil.queryWithMutex(CachePrefix.LOTTERYOBJ.getPrefix(), id, new TypeReference<Lottery>() {}, this::getById);
        if(lottery == null)
            return new Result(ResultStatue.NOT_FOUND,"未找到活动！",null);
        Long userId = UserContext.getId();
        String auth = UserContext.getAuth();
        if(!userId.equals(lottery.getCreatedBy()) && !auth.equals("admin"))
            return new Result(ResultStatue.ERROR,"你不能删除不是你的抽奖活动！",null);

        if(lottery.getIsActive() == 1)
            return new Result(ResultStatue.ERROR,"请先停止正在进行的抽奖再删除！",null);

        try {
            lotteryScheduler.cancelLottery(id + "#0");
            lotteryScheduler.cancelLottery(id + "#1");
        } catch (Exception e) {
            log.error("删除任务出错:{}",lottery);
            throw new RuntimeException(e);
        }

        this.removeById(id);
        cacheUtil.delete(CachePrefix.LOTTERYOBJ.getPrefix(),id);
        prizeService.deletePrizeList(id);
        cacheUtil.delete(CachePrefix.USERSLOTTERY.getPrefix(),lottery.getCreatedBy());
        return new Result(ResultStatue.SUCCESS,"删除成功！",null);
    }

    @Override
    public Lottery getLotteryById(Long id) {
        return cacheUtil.queryWithMutex(CachePrefix.LOTTERYOBJ.getPrefix(), id, new TypeReference<Lottery>() {}, this::getById);
    }

    @Override
    public JSONObject getOverView(Long userId){
        JSONObject jsonObject = cacheUtil.queryWithMutexWithTick(CachePrefix.USERSALLINFO.getPrefix(), userId, new TypeReference<JSONObject>() {
        }, this::getAllInfo);
        JSONObject json = new JSONObject();
        Integer creatLotteryCnt = (Integer) jsonObject.get("YourCreatLotteryCnt");
        Integer joinLotteryCnt = (Integer) jsonObject.get("YourJoinLotteryCnt");
        Integer rewardCnt = (Integer) jsonObject.get("YourRewardCnt");
        json.putOnce("YourCreatLotteryCnt", creatLotteryCnt);
        json.putOnce("YourJoinLotteryCnt", joinLotteryCnt);
        json.putOnce("YourRewardCnt", rewardCnt);
        return json;
    }


    @Override
    public JSONObject getAllInfo(Long userId){
        List<Record> records = recordService.getMyAllPrizeForAPI(userId);
        List<Long> lotterieIds = cacheUtil.queryWithMutex(CachePrefix.USERSLOTTERY.getPrefix(),userId,new TypeReference<List<Long>>() {}, id -> lambdaQuery().eq(Lottery::getCreatedBy,id)
                .select(Lottery::getId).list().stream().map(Lottery::getId).collect(Collectors.toList()));
        Map<Long,List<Record>> recordMap = new HashMap<>();
        Integer lotteryCnt = 0,rewardCnt = 0;
        for(Record record : records){
            if(record.getPrizeId() != -2 && record.getPrizeId() != -1 && !record.getPrizeName().equals("null")){
                rewardCnt++;
            }
            recordMap.computeIfAbsent(record.getLotteryId(), k -> new ArrayList<>()).add(record);
        }
        JSONObject result = new JSONObject();

        JSONObject join = new JSONObject();
        for(Map.Entry<Long,List<Record>> entry : recordMap.entrySet()){
            join.putOnce(entry.getKey().toString(),entry.getValue());
            lotteryCnt++;
        }
        result.putOnce("YourCreatLotteryCnt",lotterieIds.size());
        result.putOnce("YourJoinLotteryCnt", lotteryCnt);
        result.putOnce("YourRewardCnt", rewardCnt);
        result.putOnce("YourLottery",lotterieIds);
        result.putOnce("record",join);
        return result;
    }


    @Override
    public JSONObject getAllLottery(int currentPage) {
        String auth = UserContext.getAuth();
        if(!auth .equals("admin"))
            return null;
        int pageSize = 100;
        IPage<Lottery> page = new Page<>(currentPage, pageSize);
        List<Lottery> lotterieIds = list(page);
        JSONObject json = new JSONObject();
        json.putOnce("AllLottery",lotterieIds);

        return json;
    }


}
