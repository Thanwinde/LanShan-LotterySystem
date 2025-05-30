package com.lotterysystem.server.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.lotterysystem.gateway.util.UserContext;
import com.lotterysystem.server.constant.ResultStatue;
import com.lotterysystem.server.pojo.dto.LoginDTO;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.service.UserService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author nsh
 * @data 2025/5/9 21:33
 * @description
 **/
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    final UserService userLogin;

    @Schema(description = "登录")
    @PostMapping("/admin/login")
    public Result AdminLogin(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {

        Long id = UserContext.getId();
        if (id != null){
            return new Result(ResultStatue.SUCCESS,"你已登录！",id);
        }

        return userLogin.AdminLogin(loginDTO.getUsername(), loginDTO.getPassword(), request);
    }

    @GetMapping("/admin/users")
    public Result getAllUser(@RequestParam int page){
        return userLogin.getAllUser(page);
    }

}
