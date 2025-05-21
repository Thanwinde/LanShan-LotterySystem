package com.lotterysystem.server.controller;

import cn.hutool.json.JSONObject;
import com.lotterysystem.gateway.SentinelConfig.LotteryActionLimiter;
import com.lotterysystem.gateway.util.UserContext;
import com.lotterysystem.server.constant.AuthStatue;
import com.lotterysystem.server.constant.ResultStatue;
import com.lotterysystem.server.pojo.dto.LotteryDTO;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Lottery;
import com.lotterysystem.server.service.LotteryService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author nsh
 * @data 2025/5/10 14:43
 * @description
 **/
@RequestMapping("/api/lottery")
@RestController
@RequiredArgsConstructor
@Slf4j
public class LotteryController {

    final LotteryService lotteryService;

    @PostMapping
    @Schema(description = "新增抽奖活动")
    public Result addLottery(@RequestBody LotteryDTO lotteryDTO) throws Exception {
        if(!chooseLimiter())
            return defaultFallback();
        return lotteryService.addLottery(lotteryDTO);
    }

    //获取所有的抽奖信息（只有抽奖，管理员用）
    @GetMapping("/admin/all")
    public List<Lottery> getAllLottery(@RequestParam int page) {
        return lotteryService.getAllLottery(page);
    }

    @PutMapping
    @Schema(description = "更新抽奖，比较吃性能")
    public Result updateLottery(@RequestBody LotteryDTO lotteryDTO) throws Exception {
        if(!chooseLimiter())
            return defaultFallback();
        return lotteryService.updateLottery(lotteryDTO);
    }

    @GetMapping
    public Result getLottery(@RequestParam("id") Long id) {
        return lotteryService.getLottery(id);
    }

    @DeleteMapping
    @Schema(description = "会删除所有数据，包括任务与结果")
    public Result deleteLottery(@RequestParam("id") Long id) throws Exception {
        if(!chooseLimiter())
            return defaultFallback();
        return lotteryService.deleteLottery(id);
    }

    @PostMapping("/stop")
    @Schema(description = "提前停止抽奖活动,比较吃性能")
    public Result stopLottery(@RequestParam("id") Long id) {
        if(!chooseLimiter())
            return defaultFallback();
        return lotteryService.stopLottery(id);
    }

    @GetMapping("/icreate")
    public Result getOverview() {
        return new Result(ResultStatue.SUCCESS,"查询成功!",lotteryService.getAllMyLottery(UserContext.getId())) ;
    }

    @GetMapping("/ijoin")
    public Result getAllInfo(){
        return new Result(ResultStatue.SUCCESS,"查询成功!",lotteryService.getAllJoinLottery(UserContext.getId())) ;
    }

    public boolean chooseLimiter(){
        Long userId = UserContext.getId();
        Integer auth = UserContext.getAuth();
        if(auth == AuthStatue.USER.getCode())
            return LotteryActionLimiter.tryUserAccess(userId);
        else
            return LotteryActionLimiter.tryAdminAccess(userId);
    }

    public Result defaultFallback() {
        return new Result(ResultStatue.SC_SERVICE_UNAVAILABLE,"您操作得太快，请待会再重试！",null);
    }

}
