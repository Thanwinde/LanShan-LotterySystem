package com.lotterysystem.gateway.SentinelConfig;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author nsh
 * @data 2025/5/18 20:51
 * @description
 **/
public class SentinelNormalLimiter {

    public static boolean tryAccess(String sessionId) {
        try (Entry entry = SphU.entry("normal-global-qps", EntryType.IN, 1, sessionId)) {
            return true;
        } catch (BlockException e) {
            return false;
        }
    }
}
