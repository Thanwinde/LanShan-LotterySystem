package com.lotterysystem.gateway.SentinelConfig;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
//限流顺序: 全局整体限流 > 其他接口的限流
public class SentinelHotspotRuleConfig {

    @PostConstruct
    public void initAllParamFlowRules() {
        //对普通用户的整体限流
        ParamFlowRule userRule = new ParamFlowRule("user-global-qps")
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(5);

        //对管理员的限流
        ParamFlowRule adminRule = new ParamFlowRule("admin-global-qps")
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(10);

        //对黑名单的限流
        ParamFlowRule bannedRule = new ParamFlowRule("banned-global-qps")
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(1.5);

        //对登录等无权限接口的限流
        ParamFlowRule normalRule = new ParamFlowRule("normal-global-qps")
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(1.5);

        //对用户修改抽奖行为的限流
        ParamFlowRule UserLotteryActionRule = new ParamFlowRule("user-LotteryChangeAction-qps")
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(1.1);

        //对管理员修改抽奖行为的限流
        ParamFlowRule AdminLotteryActionRule = new ParamFlowRule("admin-LotteryChangeAction-qps")
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(5);

        //对抽奖行为的限流
        ParamFlowRule GrabRule = new ParamFlowRule("grab-qps")
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(10);

        ParamFlowRuleManager.loadRules(Arrays.asList(userRule, adminRule,bannedRule,normalRule,UserLotteryActionRule,AdminLotteryActionRule,GrabRule));
    }
}