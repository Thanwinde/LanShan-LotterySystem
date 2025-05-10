package com.lotterysystem.server.controller;

import com.lotterysystem.server.pojo.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author nsh
 * @data 2025/5/10 14:43
 * @description
 **/
@Controller
@RequiredArgsConstructor
@Slf4j
public class LotteryManage {
    @PostMapping
    public Result addLottery(){
        return null;
    }
}
