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

        Map<String,Integer> mp = redisTemplate.opsForHash().entries(CachePrefix.LOTTERRECORD.getPrefix() + ":" + lotteryId);
        Map<Long,Integer> prizeCnt = new HashMap<>();
        redisTemplate.delete(CachePrefix.LOTTERRECORD.getPrefix() + ":" + lotteryId);
        ArrayList<Record> records = new ArrayList<>();
        Long cnt = 0L;
        for(Map.Entry<String,Integer > entry:mp.entrySet()){
            Long userId = Long.valueOf(entry.getValue());
            String res = entry.getKey();
            String[] split = res.replace("\"", "").split("#");
            Long prizeId = Long.valueOf(split[0]);
            String prizeName = split[2];
            records.add(new Record().setId(cnt++).setUserId(userId).setLotteryId(lotteryId).setLotteryName(lotteryName).setPrizeId(prizeId).setPrizeName(prizeName).setIsEnd(1));
            prizeCnt.compute(prizeId,(key, value) -> value == null ? 1 : value + 1);
        }

        String json = JSONUtil.toJsonStr(records);
        redisTemplate.opsForValue().set(CachePrefix.LOTTERRECORD.getPrefix() + ":" + lotteryId, records,30L * 60 + RandomUtil.randomLong(15), TimeUnit.SECONDS);
        log.info("将刚才的抽奖记录为访问缓存");
        //把中奖记录换到prize记录中
        for(Map.Entry<Long,Integer> entry:prizeCnt.entrySet()){
            if (entry.getKey() == -1 || entry.getKey() == -2)
                continue;
            Prize prize = this.lambdaQuery().eq(Prize::getId,entry.getKey()).one();
            prize.setOutCount(entry.getValue());
            prize.setIsEnd(1);
            this.updateById(prize);
        }
    }



}




