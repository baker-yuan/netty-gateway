package com.baker.gateway.common.exception;

import com.baker.gateway.common.enums.ResponseCode;

import lombok.Getter;


public class DubboConnectException extends GatewayConnectException {
	
    private static final long serialVersionUID = -5658789202509033456L;

    @Getter
    private final String interfaceName;
    @Getter
    private final String methodName;

    public DubboConnectException(String uniqueId, String requestUrl, String interfaceName, String methodName) {
        super(uniqueId, requestUrl);
        this.interfaceName = interfaceName;
        this.methodName = methodName;
    }

    public DubboConnectException(Throwable cause, String uniqueId, String requestUrl, 
    		String interfaceName, String methodName, ResponseCode code) {
        super(cause, uniqueId, requestUrl, code);
        this.interfaceName = interfaceName;
        this.methodName = methodName;
    }
    
}
