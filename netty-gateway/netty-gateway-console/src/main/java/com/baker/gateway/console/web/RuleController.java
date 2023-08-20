package com.baker.gateway.console.web;

import java.util.List;

import com.baker.gateway.console.dto.RuleDTO;
import com.baker.gateway.console.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baker.gateway.common.config.Rule;

/**
 * 规则控制层
 */
@RestController
public class RuleController {

    @Autowired
    private RuleService ruleService;

    @GetMapping("rule/getList")
    public List<Rule> getList(@RequestParam("prefixPath") String prefixPath) throws Exception {
        return ruleService.getRuleList(prefixPath);
    }

    @PostMapping("rule/add")
    public void addRule(@RequestBody @Validated RuleDTO.AddOrUpdateRuleDTO ruleDTO) throws Exception {
        Rule rule = new Rule();
        rule.setId(ruleDTO.getId());
        rule.setName(ruleDTO.getName());
        rule.setProtocol(rule.getProtocol());
        rule.setOrder(ruleDTO.getOrder());
        rule.setFilterConfigs(ruleDTO.getFilterConfigs());
        ruleService.addRule(ruleDTO.getPrefixPath(), rule);
    }

    @PostMapping("rule/update")
    public void updateRule(@RequestBody @Validated RuleDTO.AddOrUpdateRuleDTO ruleDTO) throws Exception {
        Rule rule = new Rule();
        rule.setId(ruleDTO.getId());
        rule.setName(ruleDTO.getName());
        rule.setProtocol(rule.getProtocol());
        rule.setOrder(ruleDTO.getOrder());
        rule.setFilterConfigs(ruleDTO.getFilterConfigs());
        ruleService.updateRule(ruleDTO.getPrefixPath(), rule);
    }

    @PostMapping("rule/delete")
    public void deleteRule(@RequestBody @Validated RuleDTO.DeleteRuleDTO ruleDTO) {
        ruleService.deleteRule(ruleDTO.getPrefixPath(), ruleDTO.getId());
    }

}