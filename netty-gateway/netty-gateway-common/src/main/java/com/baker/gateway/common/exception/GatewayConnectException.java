package com.baker.gateway.common.exception;

import com.baker.gateway.common.enums.ResponseCode;

import lombok.Getter;

/**
 * 连接异常定义类
 */
public class GatewayConnectException extends GatewayBaseException {

	private static final long serialVersionUID = -8503239867913964958L;

	@Getter
	private final String uniqueId;
	
	@Getter
	private final String requestUrl;
	
	public GatewayConnectException(String uniqueId, String requestUrl) {
		this.uniqueId = uniqueId;
		this.requestUrl = requestUrl;
	}
	
	public GatewayConnectException(Throwable cause, String uniqueId, String requestUrl, ResponseCode code) {
		super(code.getMessage(), cause, code);
		this.uniqueId = uniqueId;
		this.requestUrl = requestUrl;
	}

}
