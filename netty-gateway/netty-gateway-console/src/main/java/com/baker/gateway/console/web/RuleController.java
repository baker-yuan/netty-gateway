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

    @GetMapping("/rule/getList")
    public List<Rule> getList() throws Exception {
        return ruleService.getRuleListByDb();
    }

    @PostMapping("/rule/addOrUpdate")
    public void addRule(@RequestBody @Validated RuleDTO.AddOrUpdateRuleDTO ruleDTO) throws Exception {
        Rule rule = new Rule();
        rule.setId(ruleDTO.getId());
        rule.setName(ruleDTO.getName());
        rule.setOrder(ruleDTO.getOrder());
        rule.setFilterConfigs(ruleDTO.getFilterConfigs());
        ruleService.addOrUpdateToDb(rule);
    }

    @DeleteMapping("/rule/delete")
    public void deleteRule(@RequestBody @Validated RuleDTO.DeleteRuleDTO ruleDTO) {
        ruleService.deleteRuleToDb(ruleDTO.getId());
    }

}
