package com.lotterysystem.server.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.gateway.util.MyJWTUtil;
import com.lotterysystem.gateway.util.UserContext;
import com.lotterysystem.server.constant.AuthStatue;
import com.lotterysystem.server.constant.ResultStatue;

import com.lotterysystem.server.mapper.UserMapper;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.User;
import com.lotterysystem.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nsh
 * @data 2025/5/10 12:33
 * @description
 **/
@Service
@DS("master")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    @DS("slave")
    public Result AdminLogin(String name, String password , HttpServletRequest request) {

        User user = lambdaQuery().eq(User::getUsername, name).eq(User::getPassword, password).one();
        if (user != null) {
            Map<String,Object> map = new HashMap<>();
            map.put("userId",user.getId());
            map.put("name",user.getUsername());
            map.put("auth",user.getAuthority());
            String jwt = MyJWTUtil.createJWT(map);
            return new Result(ResultStatue.SUCCESS,"登录成功!",jwt);
        }
        return new Result(ResultStatue.UNAUTHORIZED,"登录失败！",null);
    }

    @Override
    @DS("slave")
    public Result getAllUser(int curPage){
        Integer auth = UserContext.getAuth();
        if(auth != AuthStatue.ADMIN.getCode()){
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
