package com.lotterysystem.server;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String str = "{\n" +
                "    \"blackList\": [\n" +
                "        12,\n" +
                "        13\n" +
                "    ],\n" +
                "    \"randomFortuneNum\": 5,\n" +
                "    \"weightList\": [\n" +
                "        {\n" +
                "            \"id\": 123,\n" +
                "            \"weight\": 20\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 567,\n" +
                "            \"weight\": 50\n" +
                "        }\n" +
                "    ],\n" +
                "    \"attend\": [\n" +
                "        2420876526,\n" +
                "        3204789832\n" +
                "    ]\n" +
                "}";
        JSONObject config = JSONUtil.parseObj(str);

        // 使用 toList 来转换为 List<Long>
        List<Long> black = config.getJSONArray("blackList").toList(Long.class);
        List<Long> attend = config.getJSONArray("attend").toList(Long.class);

        // 获取 weightList 的数组
        JSONArray array = config.getJSONArray("weightList");

        // 初始化映射
        Map<Long, Integer> weightMap = new HashMap<>();
        Map<Long, Boolean> attendMap = new HashMap<>();
        Map<Long, Boolean> blackMap = new HashMap<>();

        // 填充 weightMap
        for (int i = 0; i < array.size(); i++) {
            JSONObject weightObj = array.getJSONObject(i);
            weightMap.put(weightObj.getLong("id"), weightObj.getInt("weight"));
        }

        // 填充 attendMap 和 blackMap
        for (Long a : black) {
            blackMap.put(a, true); // 将黑名单用户添加到 blackMap
        }
        for (Long a : attend) {
            attendMap.put(a, true); // 将参与用户添加到 attendMap
        }

        // 输出调试信息
        System.out.println(666);
        System.out.println("Weight Map: " + weightMap);
        System.out.println("Attend Map: " + attendMap);
        System.out.println("Black Map: " + blackMap);
    }
}
