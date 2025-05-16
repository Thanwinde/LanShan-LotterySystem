package com.lotterysystem.server.service;

import com.lotterysystem.server.pojo.dto.Result;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author nsh
 * @data 2025/5/10 12:32
 * @description
 **/
public interface UserLoginService {

    public Result AdminLogin(String username, String password, HttpServletRequest request);
}
