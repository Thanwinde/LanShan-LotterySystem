package com.lotterysystem.gateway.constant;

/**
 * @author nsh
 * @data 2025/5/31 20:54
 * @description
 **/
public enum LimiterType {
    SYSTEMGLOBE("LIMITER:SYSTEMGLOBE"),
    USERGLOBE("LIMITER:USERGLOBE"),
    ADMINGLOBE("LIMITER:ADMINGLOBE"),
    BANNEDGLOBE("LIMITER:BANNEDGLOBE"),
    CHANGELOTTERY("LIMITER:CHANGELOTTERY"),
    ADMINCHANGELOTTERY("LIMITER:ADMINCHANGELOTTERY"),
    GRABACTION("LIMITER:GRABACTION"),

    ;
    private final String type;
    LimiterType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

}
