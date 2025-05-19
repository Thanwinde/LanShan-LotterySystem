package com.lotterysystem.gateway.SentinelConfig;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.stereotype.Component;


/**
 * @author nsh
 * @data 2025/5/19 10:32
 * @description
 **/
@Component
public class LotteryActionLimiter {

    public static boolean tryUserAccess(Long userId) {
        try (Entry entry = SphU.entry("user-LotteryChangeAction-qps", EntryType.IN, 1, userId)) {
            return true;
        } catch (BlockException e) {
            return false;
        }
    }

    public static boolean tryAdminAccess(Long userId) {
        try (Entry entry = SphU.entry("admin-LotteryChangeAction-qps", EntryType.IN, 1, userId)) {
            return true;
        } catch (BlockException e) {
            return false;
        }
    }

}
