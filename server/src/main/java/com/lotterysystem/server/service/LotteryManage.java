package com.lotterysystem.server.service;

import com.lotterysystem.server.pojo.dto.LotteryDTO;
import com.lotterysystem.server.pojo.dto.Result;

/**
 * @author nsh
 * @data 2025/5/10 14:43
 * @description
 **/
public interface LotteryManage {
    public Result addLottery(LotteryDTO lotteryDTO);
}
