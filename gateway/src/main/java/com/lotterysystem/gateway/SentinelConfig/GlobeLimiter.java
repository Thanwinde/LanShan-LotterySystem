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
public class GlobeLimiter {

    public static boolean tryAdminAccess(Long sessionId) {
        try (Entry entry = SphU.entry("admin-global-qps", EntryType.IN, 1, sessionId)) {
            return true;
        } catch (BlockException e) {
            return false;
        }
    }

    public static boolean tryUserAccess(Long sessionId) {
        try (Entry entry = SphU.entry("user-global-qps", EntryType.IN, 1, sessionId)) {
            return true;
        } catch (BlockException e) {
            return false;
        }
    }

    public static boolean tryBannedAccess(Long sessionId) {
        try (Entry entry = SphU.entry("banned-global-qps", EntryType.IN, 1, sessionId)) {
            return true;
        } catch (BlockException e) {
            return false;
        }
    }

    public static boolean tryNormalAccess(String sessionId) {
        try (Entry entry = SphU.entry("normal-global-qps", EntryType.IN, 1, sessionId)) {
            return true;
        } catch (BlockException e) {
            return false;
        }
    }

    public static boolean tryGrabAccess(Long UserId) {
        try (Entry entry = SphU.entry("grab-qps", EntryType.IN, 1, UserId)) {
            return true;
        } catch (BlockException e) {
            return false;
        }
    }

}
