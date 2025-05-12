package com.lotterysystem.server.controller;

import com.lotterysystem.server.constant.ResultStatue;
import com.lotterysystem.server.pojo.dto.LoginDTO;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.scheduler.LotteryScheduler;
import com.lotterysystem.server.service.UserLogin;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nsh
 * @data 2025/5/9 21:33
 * @description
 **/
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    final UserLogin userLogin;

    @Schema(description = "登录")
    @PostMapping("/admin/login")
    public Result AdminLogin(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if(session != null && session.getAttribute("id") != null){
            return new Result(ResultStatue.SUCCESS,"你已登录！",session.getAttribute("id"));
        }
        return userLogin.AdminLogin(loginDTO.getUsername(), loginDTO.getPassword(), request);
    }
}
