package com.lotterysystem.server.controller;

import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.service.PrizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nsh
 * @data 2025/5/11 20:26
 * @description
 **/
@RestController
@RequestMapping("/api/grabprize")
@RequiredArgsConstructor
public class PrizeController {

    private final PrizeService prizeService;

    @GetMapping
    public Result getPrize(String LotteryId) {
        return null;
    }
}
