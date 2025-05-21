package com.lotterysystem.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.server.constant.CachePrefix;
import com.lotterysystem.server.pojo.dto.PrizeDTO;
import com.lotterysystem.server.pojo.entity.Prize;
import com.lotterysystem.server.pojo.entity.Record;
import com.lotterysystem.server.pojo.vo.PrizeVO;
import com.lotterysystem.server.service.PrizeService;
import com.lotterysystem.server.mapper.PrizeMapper;
import com.lotterysystem.server.util.CacheUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    private final RedisTemplate redisTemplate;

    @Override
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
    public void deletePrizeList(Long lotteryId){
        QueryWrapper<Prize> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("lottery_id", lotteryId);
        this.remove(queryWrapper);
        cacheUtil.delete(CachePrefix.PRIZELIST.getPrefix(), lotteryId);
    }

    @Override
    public void addPrizeList(Long lotteryId, ArrayList<PrizeDTO> prizes) {
        ArrayList<Prize> prizesList = new ArrayList<>();
        for (PrizeDTO prizeDTO : prizes) {
            Prize prize = BeanUtil.copyProperties(prizeDTO, Prize.class);
            prize.setLotteryId(lotteryId);
            prizesList.add(prize);
        }
        log.info("加入奖品池");
        this.saveBatch(prizesList);
    }

    @Override
    public void joinToPool(Long lotteryId) {
        List<Prize> list = cacheUtil.queryWithMutex(CachePrefix.PRIZELIST.getPrefix(), lotteryId, new TypeReference<List<Prize>>() {}, id -> lambdaQuery().eq(Prize::getLotteryId, id).list());
        for(Prize prize:list){
            for(int i = 0;i < prize.getFullCount();i++)
                redisTemplate.opsForSet().add(CachePrefix.PRIZEPOOL.getPrefix()+ ":" + lotteryId, prize.getId()+"#"+ i + "#" + prize.getName() +"#" + prize.getRarity() + "#");
        }

    }

    @Override
    public void deleteLotteryActionCache(Long lotteryId, String lotteryName){

        log.info("结束抽奖：{},{},尝试开放抽奖记录",lotteryId,lotteryName);

        Map<Long,Integer> prizeCnt = new HashMap<>();

        for(Map.Entry<Long,Integer> entry:prizeCnt.entrySet()){
            if (entry.getKey() == -1 || entry.getKey() == -2)
                continue;
            Prize prize = this.lambdaQuery().eq(Prize::getId,entry.getKey()).one();
            prize.setOutCount(entry.getValue());
            prize.setIsEnd(1);
            this.updateById(prize);
        }

        List<Long> ids = prizeCnt.keySet().stream()
                .filter(id -> id != -1 && id != -2)
                .collect(Collectors.toList());

        Map<Long, Prize> prizeMap = this.listByIds(ids).stream()
                .collect(Collectors.toMap(Prize::getId, Function.identity()));

        List<Prize> updateList = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : prizeCnt.entrySet()) {

            Prize prize = prizeMap.get(entry.getKey());
            if (prize == null) continue;

            prize.setOutCount(entry.getValue());
            prize.setIsEnd(1);
            updateList.add(prize);
        }

        this.updateBatchById(updateList);

    }



}




