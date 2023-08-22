package com.baker.gateway.common.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 服务调用的接口模型描述
 */
public interface ServiceInvoker {
	/**
	 * 获取真正的服务调用的全路径
	 */
	String getInvokerPath();
	void setInvokerPath(String invokerPath);
	
	/**
	 * 获取指定服务调用绑定的唯一规则
	 */
	Integer getRuleId();
	void setRuleId(Integer ruleId);
	
	/**
	 * 获取该服务调用(方法)的超时时间
	 */
	int getTimeout();
	void setTimeout(int timeout);
}
