package com.lotterysystem.server.controller;

import cn.hutool.core.codec.Hashids;
import com.lotterysystem.gateway.util.UserContext;
import com.lotterysystem.server.constant.ResultStatue;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.service.LotteryActionService;
import com.lotterysystem.server.service.RecordService;
import com.lotterysystem.server.service.impl.LotteryActionImpl;
import com.lotterysystem.server.service.impl.LotteryJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author nsh
 * @data 2025/5/11 20:26
 * @description
 **/
@RestController
@RequestMapping("/api/prize")
@RequiredArgsConstructor
public class PrizeController {

    private final LotteryActionService lotteryAction;

    private final RecordService recordService;

    Hashids hashids = Hashids.create(Hashids.DEFAULT_ALPHABET,6);

    @PostMapping("/grab")
    public Result grabPrize(@RequestBody Long lotteryId) {
        return lotteryAction.tryGrab(lotteryId);
    }

    @PostMapping("/passgrab")
    public Result passGrabPrize(@RequestBody String password) {
        long[] lotteryid;
        try{
            lotteryid = hashids.decode(password);
        }catch (Exception e){
            return new Result(ResultStatue.FORBIDDEN,"抽奖邀请码不存在或错误！",null);
        }
        return lotteryAction.tryGrab(lotteryid[0]);
    }

    @GetMapping
    public Result getMyPrize(@RequestParam("lotteryid") Long lotteryId) {
        Long userId = UserContext.getId();
        if(lotteryId == -1)
            return recordService.getMyAllPrize(userId);
        else
            return recordService.getMyPrizeByLotteryId(lotteryId , userId);
    }
}
