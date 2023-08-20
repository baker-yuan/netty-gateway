package com.baker.gateway.common.exception;

import com.baker.gateway.common.enums.ResponseCode;

import lombok.Getter;

/**
 * 连接异常定义类
 */
public class GatewayConnectException extends GatewayBaseException {

	private static final long serialVersionUID = -8503239867913964958L;

	@Getter
	private final String serviceId;
	
	@Getter
	private final String requestUrl;
	
	public GatewayConnectException(String serviceId, String requestUrl) {
		this.serviceId = serviceId;
		this.requestUrl = requestUrl;
	}
	
	public GatewayConnectException(Throwable cause, String serviceId, String requestUrl, ResponseCode code) {
		super(code.getMessage(), cause, code);
		this.serviceId = serviceId;
		this.requestUrl = requestUrl;
	}

}
