package com.lotterysystem.server.controller;

import com.lotterysystem.server.pojo.dto.LotteryDTO;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.service.LotteryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    //TODO 实装再加上RequestBody
    @PostMapping
    public Result addLottery(@RequestBody LotteryDTO lotteryDTO) throws Exception {
        return lotteryService.addLottery(lotteryDTO);
    }

    @GetMapping("/your")
    public Result getYourLottery() {
        return lotteryService.getYourLottery();
    }

    @PutMapping
    public Result updateLottery(@RequestBody LotteryDTO lotteryDTO) throws Exception {
        return lotteryService.updateLottery(lotteryDTO);
    }

    @GetMapping
    public Result getLottery(@RequestParam("id") Long id) {
        return lotteryService.getLottery(id);
    }

    @DeleteMapping
    public Result deleteLottery(@RequestParam("id") Long id) throws Exception {
        return lotteryService.deleteLottery(id);
    }

    @PostMapping("/stop")
    public Result stopLottery(@RequestParam("id") Long id) {
        return lotteryService.stopLottery(id);
    }


}
