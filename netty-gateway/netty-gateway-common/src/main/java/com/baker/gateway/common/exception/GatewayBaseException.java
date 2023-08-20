package com.baker.gateway.common.exception;

import com.baker.gateway.common.enums.ResponseCode;

/**
 * 网关最基础的异常定义类
 */
public class GatewayBaseException extends RuntimeException {

    private static final long serialVersionUID = -5658789202563433456L;
    
    public GatewayBaseException() {
    }

    protected ResponseCode code;

    public GatewayBaseException(String message, ResponseCode code) {
        super(message);
        this.code = code;
    }

    public GatewayBaseException(String message, Throwable cause, ResponseCode code) {
        super(message, cause);
        this.code = code;
    }

    public GatewayBaseException(ResponseCode code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public GatewayBaseException(String message, Throwable cause,
                                boolean enableSuppression, boolean writableStackTrace, ResponseCode code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
    
    public ResponseCode getCode() {
        return code;
    }

}
