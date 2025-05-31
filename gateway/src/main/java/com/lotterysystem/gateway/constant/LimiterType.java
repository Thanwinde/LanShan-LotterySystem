package com.lotterysystem.gateway.constant;

/**
 * @author nsh
 * @data 2025/5/31 20:54
 * @description
 **/
public enum LimiterType {
    SYSTEMGLOBE("SYSTEMGLOBE","8000"),
    USERGLOBE("USERGLOBE-","5"),
    ADMINGLOBE("ADMINGLOBE-","20"),
    BANNEDGLOBE("BANNEDGLOBE-","2"),
    CHANGELOTTERY("CHANGELOTTERY-","1"),
    ADMINCHANGELOTTERY("ADMINCHANGELOTTERY-","5"),
    GRABACTION("GRABACTION-","8000"),

    ;
    private final String type;
    private final String qps;
    LimiterType(String type, String qps) {
        this.type = type;
        this.qps = qps;
    }
    public String getType() {
        return type;
    }
    public String getQps() {
        return qps;
    }
}
