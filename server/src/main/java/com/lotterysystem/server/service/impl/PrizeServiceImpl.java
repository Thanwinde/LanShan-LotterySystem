package com.lotterysystem.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.server.constant.CachePrefix;
import com.lotterysystem.server.pojo.dto.PrizeDTO;
import com.lotterysystem.server.pojo.entity.Prize;
import com.lotterysystem.server.pojo.entity.Record;
import com.lotterysystem.server.pojo.vo.PrizeVO;
import com.lotterysystem.server.service.PrizeService;
import com.lotterysystem.server.mapper.PrizeMapper;
import com.lotterysystem.server.service.RecordService;
import com.lotterysystem.server.util.CacheUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author thanw
* @description 针对表【prize】的数据库操作Service实现
* @createDate 2025-05-12 12:08:52
*/
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
@Schema(description = "处理奖池之类的api，获得奖池，抽奖等")
public class PrizeServiceImpl extends ServiceImpl<PrizeMapper, Prize>
    implements PrizeService{

    final private CacheUtil cacheUtil;

    final private RecordService recordService;

    private final RedisTemplate redisTemplate;

    @Override
    @DS("slave")
    public ArrayList<PrizeVO> getPrizeVOList(Long lotteryId){
        List<Prize> list = cacheUtil.queryWithMutex(CachePrefix.PRIZELIST.getPrefix(), lotteryId, new TypeReference<List<Prize>>() {}, id -> lambdaQuery().eq(Prize::getLotteryId, id).list());
        ArrayList<PrizeVO> prizeVOList = new ArrayList<>();
        for(Prize prize:list){
            PrizeVO vo = BeanUtil.toBean(prize, PrizeVO.class);
            prizeVOList.add(vo);
        }
        return prizeVOList;
    }

    @Override
    public ArrayList<Prize> getPrizeList(Long lotteryId){
        List<Prize> list = cacheUtil.queryWithMutex(CachePrefix.PRIZELIST.getPrefix(), lotteryId, new TypeReference<List<Prize>>() {}, id -> lambdaQuery().eq(Prize::getLotteryId, id).list());
        return new ArrayList<>(list);
    }

    @Override
    @DS("master")
    public void deletePrizeList(Long lotteryId){
        QueryWrapper<Prize> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("lottery_id", lotteryId);
        this.remove(queryWrapper);
        cacheUtil.delete(CachePrefix.PRIZELIST.getPrefix(), lotteryId);
    }

    @Override
    @DS("master")
    public void addPrizeList(Long lotteryId, ArrayList<PrizeDTO> prizes) {
        ArrayList<Prize> prizesList = new ArrayList<>();
        for (PrizeDTO prizeDTO : prizes) {
            Prize prize = BeanUtil.copyProperties(prizeDTO, Prize.class);
            prize.setLotteryId(lotteryId);
            prizesList.add(prize);
        }
        this.saveBatch(prizesList);
    }

    @Override
    @DS("master")
    public void joinToPool(Long lotteryId) {
        List<Prize> list = cacheUtil.queryWithMutex(CachePrefix.PRIZELIST.getPrefix(), lotteryId, new TypeReference<List<Prize>>() {}, id -> lambdaQuery().eq(Prize::getLotteryId, id).list());
        List<String> lists = new ArrayList<>();
        for(Prize prize:list)
            for(int i = 0;i < prize.getFullCount();i++)
                lists.add(prize.getId()+"#"+ i + "#" + prize.getName() +"#" + prize.getRarity() + "#");
        redisTemplate.opsForSet().add(CachePrefix.PRIZEPOOL.getPrefix()+ ":" + lotteryId, lists.toArray());
    }

    @Override
    @DS("master")
    public void updatePrizeCount(Long lotteryId, String lotteryName){

        log.info("结束抽奖：{},抽奖 : {},尝试开放抽奖记录",lotteryId,lotteryName);

        List<Record> records = recordService.getRecordsByLotteryIdWithNoEnd(lotteryId);
        List<Prize> oriPrizeList = getPrizeList(lotteryId);
        List<Prize> newPrizeList = new ArrayList<>();
        Map<Long,Prize> prizeMap = oriPrizeList.stream().collect(Collectors.toMap(Prize::getId, Function.identity()));

        Map<Long,Integer> prizeCnt = new HashMap<>();

        for(Record record:records){
            Long prizeId = record.getPrizeId();
            if(prizeId == -1 || prizeId == -2)
                continue;
            prizeCnt.compute(prizeId,(key, value) -> value == null ? 1 : value + 1);
        }

        Long nullId = null;

        for(Map.Entry<Long,Prize> prizeEntry:prizeMap.entrySet()){
            Prize prize = prizeEntry.getValue();
            if(prize.getName().equals("null")){
                nullId = prize.getId();
                continue;
            }
            prize.setIsEnd(1);
            if(prizeCnt.containsKey(prize.getId())){
                prize.setOutCount(prizeCnt.get(prize.getId()));
            }
            newPrizeList.add(prize);
        }



        this.updateBatchById(newPrizeList);
        if(nullId != null)
            this.removeById(nullId);
    }



}




