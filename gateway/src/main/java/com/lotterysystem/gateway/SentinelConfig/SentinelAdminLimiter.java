package com.lotterysystem.gateway.SentinelConfig;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;


/**
 * @author nsh
 * @data 2025/5/18 19:21
 * @description
 **/
public class SentinelAdminLimiter {

    public static boolean tryAccess(Long userId) {
        try (Entry entry = SphU.entry("admin-global-qps", EntryType.IN, 1, userId)) {
            return true;
        } catch (BlockException e) {
            return false;
        }
    }
}
