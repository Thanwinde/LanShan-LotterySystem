package com.lotterysystem.server.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.gateway.util.UserContext;
import com.lotterysystem.server.constant.ResultStatue;

import com.lotterysystem.server.mapper.UserMapper;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.User;
import com.lotterysystem.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author nsh
 * @data 2025/5/10 12:33
 * @description
 **/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
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

    @Override
    public Result getAllUser(int curPage){
        String auth = UserContext.getAuth();
        if(!auth.equals("admin")){
            return new Result(ResultStatue.SUCCESS,"无权限！",null);
        }
        int pageSize = 100;
        IPage<User> page = new Page<>(curPage, pageSize);
        List<User> users = lambdaQuery().list(page);
        for (User user : users) {
            user.setPassword(null);
        }
        return new Result(ResultStatue.SUCCESS,"查询成功！",users);
    }
}
