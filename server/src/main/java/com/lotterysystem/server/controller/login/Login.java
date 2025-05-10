package com.lotterysystem.server.controller.login;

import com.lotterysystem.server.constant.ResultStatue;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.service.UserLogin;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
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
public class Login {

    final UserLogin userLogin;
    @Schema(description = "登录")
    @PostMapping("/admin/login")
    public Result AdminLogin(String username, String password, HttpServletRequest request) {
        //Cookie[] cookies = request.getCookies();
        HttpSession session = request.getSession(false);
        if(session != null && session.getAttribute("id") != null){
            return new Result(ResultStatue.SUCCESS,"你已登录！",session.getAttribute("id"));
        }
        return userLogin.AdminLogin(username, password,request);
    }
}
