package com.baker.gateway.common.config;

/**
 * 抽象的服务调用接口实现类
 */
public class AbstractServiceInvoker implements ServiceInvoker {
	/**
	 * 方法全路径
	 */
	protected String invokerPath;
	/**
	 * 规则id
	 */
	protected Integer ruleId;
	/**
	 * 调用接口超时时间
	 */
	protected Integer timeout = 5000;

	@Override
	public String getInvokerPath() {
		return invokerPath;
	}

	@Override
	public void setInvokerPath(String invokerPath) {
		this.invokerPath = invokerPath;
	}

	@Override
	public Integer getRuleId() {
		return ruleId;
	}

	@Override
	public void setRuleId(Integer ruleId) {
		this.ruleId = ruleId;		
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
