package com.baker.gateway.client;

/**
 * 注册服务的协议枚举类
 */
public enum GatewayProtocol {

	HTTP("http", "http协议"),
	DUBBO("dubbo", "http协议");
	
	private final String code;
	
	private final String desc;
	
	GatewayProtocol(String code, String desc){
		this.code = code;
		this.desc = desc;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDesc() {
		return desc;
	}
	
}
