package com.lotterysystem.gateway.SentinelConfig;

import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

/**
 * @author nsh
 * @data 2025/5/25 11:51
 * @description
 **/
@Configuration
public class SentinelFlowRuleConfig {

    @PostConstruct
    public void initAllFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule1 = new FlowRule();
        rule1.setClusterMode(true);
        rule1.setResource("grab-global");
        rule1.setCount(10);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rules.add(rule1);

        FlowRuleManager.loadRules(rules);
    }

}
