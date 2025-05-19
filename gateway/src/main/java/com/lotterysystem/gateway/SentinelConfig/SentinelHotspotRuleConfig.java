package com.lotterysystem.gateway.SentinelConfig;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SentinelHotspotRuleConfig {

    @PostConstruct
    public void initAllParamFlowRules() {
        ParamFlowRule userRule = new ParamFlowRule("user-global-qps")
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(5);

        ParamFlowRule adminRule = new ParamFlowRule("admin-global-qps")
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(20);

        ParamFlowRule normalRule = new ParamFlowRule("normal-global-qps")
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(1.5);

        ParamFlowRuleManager.loadRules(Arrays.asList(userRule, adminRule,normalRule));
    }
}