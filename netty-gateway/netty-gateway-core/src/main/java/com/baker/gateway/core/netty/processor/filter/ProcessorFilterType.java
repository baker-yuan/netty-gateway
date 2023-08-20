package com.baker.gateway.core.netty.processor.filter;

/**
 * 过滤器的类型定义
 */
public enum ProcessorFilterType {
	PRE("PRE", "前置过滤器"),
	/**
	 * 中置过滤器负责路由转发，只有一个
	 */
	ROUTE("ROUTE", "中置过滤器"),
	ERROR("ERROR", "异常处理过滤器"),
	POST("POST", "后置过滤器");
	
	private final String code ;
	private final String message;
	
	ProcessorFilterType(String code, String message){
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	
}
