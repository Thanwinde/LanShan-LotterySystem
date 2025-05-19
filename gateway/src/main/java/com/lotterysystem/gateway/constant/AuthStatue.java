package com.lotterysystem.gateway.constant;

/**
 * @author nsh
 * @data 2025/5/19 13:59
 * @description
 **/
public enum AuthStatue {
    USER(0),
    ADMIN(1),
    VIP(2),
    BANNED(3),
    ;
    private final int value;
    AuthStatue(int value) {
        this.value = value;
    }
    public int getCode() {
        return value;
    }
}
