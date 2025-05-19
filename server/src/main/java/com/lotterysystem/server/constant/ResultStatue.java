package com.lotterysystem.server.constant;

public enum ResultStatue {
    //枚举类，记录请求状态
    SUCCESS(200),ERROR(500),NOT_FOUND(404),UNAUTHORIZED(401),FORBIDDEN(403),SC_SERVICE_UNAVAILABLE(503);
    private int code;
    ResultStatue(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}
