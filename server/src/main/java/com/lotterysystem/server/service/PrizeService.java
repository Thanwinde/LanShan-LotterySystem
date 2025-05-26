package com.lotterysystem.server.service;

import com.lotterysystem.server.pojo.dto.PrizeDTO;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Prize;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lotterysystem.server.pojo.vo.PrizeVO;

import java.util.ArrayList;

/**
* @author thanw
* @description 针对表【prize】的数据库操作Service
* @createDate 2025-05-12 12:08:52
*/
public interface PrizeService extends IService<Prize> {


    ArrayList<PrizeVO> getPrizeVOList(Long lotteryId);

    ArrayList<Prize> getPrizeList(Long lotteryId);

    void deletePrizeList(Long lotteryId);

    void addPrizeList(Long id, ArrayList<PrizeDTO> prizes);

    void joinToPool(Long lotteryId);

    void updatePrizeCount(Long lotteryId, String lotteryName);
}
