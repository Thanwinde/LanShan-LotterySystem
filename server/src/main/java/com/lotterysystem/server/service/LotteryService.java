package com.lotterysystem.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lotterysystem.server.pojo.dto.LotteryDTO;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Lottery;

/**
* @author thanw
* @description 针对表【lottery】的数据库操作Service
* @createDate 2025-05-10 18:35:53
*/
public interface LotteryService {
    Result addLottery(LotteryDTO lotteryDTO) throws Exception;

    Result getYourLottery();

    Result getLottery(Long id);

    Result updateLottery(LotteryDTO lotteryDTO) throws Exception;

    Result stopLottery(Long id);

    Result deleteLottery(Long id) throws Exception;

}
