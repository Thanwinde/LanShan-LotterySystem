package com.lotterysystem.server.constant;

/**
 * @author nsh
 * @data 2025/5/14 09:30
 * @description
 **/
public enum CachePrefix {
    LOTTERYOBJ("lottery:obj"),  //获取lottery对象
    USERSLOTTERY("lottery:user"),   //获取某人创建的全部抽奖
    PRIZELIST("prize:list"),    //某个抽奖的奖品表
    PRIZEPOOL("prize:pool"),    //某个抽奖的奖品池（抽奖时使用）
    LOTTERYCOUNT("lottery:count"), //某个抽奖，每个人的抽数
    LOTTERRECORD("lottery:record"), //抽奖总结果
    USERSALLPRIZE("user:allrecord"),  //用户的总奖品
    USERSPRIZE("user:record");  //用户单个抽奖的奖品
    private String prefix;

    CachePrefix(String prefix) {
        this.prefix = prefix;
    }
    public String getPrefix() {
        return prefix;
    }
}
