package com.lotterysystem.server.service;

import com.lotterysystem.server.pojo.dto.Result;

/**
 * @author nsh
 * @data 2025/5/13 19:20
 * @description
 **/
public interface LotteryActionService {
    Result tryGrab(Long lotteryId);

}
