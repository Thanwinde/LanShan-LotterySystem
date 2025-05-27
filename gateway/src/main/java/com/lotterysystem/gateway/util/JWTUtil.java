package com.lotterysystem.gateway.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import java.util.Date;
import java.util.Map;

/**
 * @author nsh
 * @data 2025/5/27 13:02
 * @description
 **/
public class JWTUtil {
    static String key = "loveCQUPT5mm";

    public static String createJWT(Map<String, String> claims) {
        String token = JWT.create()
                .addPayloads(claims)
                .setExpiresAt(DateUtil.offsetHour(new Date(), 1))
                .setKey(key.getBytes())
                .sign();
        return token;
    }

    public static Map<String,String> parseToken(String token) {
        // 1.校验token是否为空
        if (token == null) {
            return null;
        }
        // 2.校验并解析jwt
        JWT jwt;
        try {
            jwt = JWT.of(token);
        } catch (Exception e) {
            return null;
        }
        // 2.校验jwt是否有效
        if (!jwt.verify()) {
            // 验证失败
           return null;
        }
        // 3.校验是否过期
        try {
            JWTValidator.of(jwt).validateDate();
        } catch (ValidateException e) {
            return null;
        }
        // 4.数据格式校验
        Map<String,String> userPayload = (Map<String, String>) jwt.getPayload();
        if (userPayload == null) {
            return null;
        }

        // 5.数据解析
        try {
            return userPayload;
        } catch (RuntimeException e) {
            // 数据格式有误
            return null;
        }
    }
}
