package com.lotterysystem.gateway.util;

public class UserContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setId(Long id) {
        threadLocal.set(id);
    }

    public static Long getId() {
        return threadLocal.get();
    }
    public static void removeId() {
        threadLocal.remove();
    }

}