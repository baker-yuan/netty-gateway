package com.baker.gateway.common.exception;

import com.baker.gateway.common.enums.ResponseCode;

/**
 * 请求路径不匹配的异常定义类
 */
public class GatewayPathNoMatchedException extends GatewayBaseException {

	private static final long serialVersionUID = -6695383751311763169L;

	
	public GatewayPathNoMatchedException() {
		this(ResponseCode.PATH_NO_MATCHED);
	}
	
	public GatewayPathNoMatchedException(ResponseCode code) {
		super(code.getMessage(), code);
	}
	
	public GatewayPathNoMatchedException(Throwable cause, ResponseCode code) {
		super(code.getMessage(), cause, code);
	}
}
