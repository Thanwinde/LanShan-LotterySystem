package com.lotterysystem.server;

import cn.hutool.core.codec.Hashids;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;

/**
 * @author nsh
 * @data 2025/5/16 14:18
 * @description
 **/
@SpringBootTest
@ContextConfiguration
public class test {



    @Test
    public void contextLoads() {
        Hashids hashids = Hashids.create((Hashids.DEFAULT_ALPHABET));
        long[] de = hashids.decode("nleXp");
        System.out.println(Arrays.toString(de));
    }

    @Test
    public void contextLoads1() {
        Hashids hashids = Hashids.create((Hashids.DEFAULT_ALPHABET));
        String de = hashids.encode(123456);
        System.out.println(de);
    }
}
