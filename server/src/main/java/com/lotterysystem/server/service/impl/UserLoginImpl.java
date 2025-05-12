package com.lotterysystem.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.server.constant.ResultStatue;

import com.lotterysystem.server.mapper.UserMapper;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.User;
import com.lotterysystem.server.service.UserLogin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/**
 * @author nsh
 * @data 2025/5/10 12:33
 * @description
 **/
@Service
public class UserLoginImpl extends ServiceImpl<UserMapper, User> implements UserLogin {
    @Override
    public Result AdminLogin(String name, String password , HttpServletRequest request) {

        User user = lambdaQuery().eq(User::getUsername, name).eq(User::getPassword, password).one();
        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("id", user.getId());
            session.setAttribute("name", user.getUsername());
            session.setAttribute("auth", user.getAuthority());
            return new Result(ResultStatue.SUCCESS,"登录成功!",user.getId());
        }
        return new Result(ResultStatue.UNAUTHORIZED,"登录失败！",null);
    }
}
