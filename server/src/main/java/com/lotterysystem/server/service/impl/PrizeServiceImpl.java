package com.lotterysystem.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.server.pojo.dto.PrizeDTO;
import com.lotterysystem.server.pojo.entity.Prize;
import com.lotterysystem.server.pojo.vo.PrizeVO;
import com.lotterysystem.server.service.PrizeService;
import com.lotterysystem.server.mapper.PrizeMapper;
import com.lotterysystem.server.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
* @author thanw
* @description 针对表【prize】的数据库操作Service实现
* @createDate 2025-05-12 12:08:52
*/
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PrizeServiceImpl extends ServiceImpl<PrizeMapper, Prize>
    implements PrizeService{

    final private CacheUtil cacheUtil;

    @Override
    public ArrayList<PrizeVO> getPrizeVOList(Long lotteryId){
        List<Prize> list = cacheUtil.queryWithMutex("prize:list", lotteryId, List.class, id -> lambdaQuery().eq(Prize::getLotteryId, id).list());
        ArrayList<PrizeVO> prizeVOList = new ArrayList<>();
        for(Prize prize:list){
            PrizeVO vo = BeanUtil.toBean(prize, PrizeVO.class);
            prizeVOList.add(vo);
        }
        return prizeVOList;
    }

    @Override
    public ArrayList<Prize> getPrizeList(Long lotteryId){
        List<Prize> list = cacheUtil.queryWithMutex("prize:list", lotteryId, List.class, id -> lambdaQuery().eq(Prize::getLotteryId, id).list());
        return new ArrayList<>(list);
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
}




