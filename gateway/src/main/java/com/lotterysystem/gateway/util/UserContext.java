package com.lotterysystem.gateway.util;

public class UserContext {

    public static ThreadLocal<Long> threadLocalId = new ThreadLocal<>();

    public static ThreadLocal<Integer> threadLocalAuth = new ThreadLocal<>();

    public static ThreadLocal<String> threadLocalName = new ThreadLocal<>();

    public static void setId(Long id) {
        threadLocalId.set(id);
    }

    public static Long getId() {
        return threadLocalId.get();
    }

    public static void setName(String name) {
        threadLocalName.set(name);
    }

    public static String getName() {
        return threadLocalName.get();
    }

    public static void setAuth(Integer auth) {
        threadLocalAuth.set(auth);
    }

    public static Integer getAuth() {
        return threadLocalAuth.get();
    }

    public static void remove() {
        threadLocalId.remove();
        threadLocalAuth.remove();
        threadLocalName.remove();
    }




}