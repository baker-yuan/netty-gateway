package com.baker.gateway.core.context;

import org.asynchttpclient.Response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.baker.gateway.common.enums.ResponseCode;
import com.baker.gateway.common.util.JSONUtil;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Data;

/**
 * 网关响应封装类
 */
@Data
public class GatewayResponse {
	/**
	 * 响应头
	 */
	private HttpHeaders responseHeaders = new DefaultHttpHeaders();

	/**
	 * 额外的响应结果
	 */
	private final HttpHeaders extraResponseHeaders = new DefaultHttpHeaders();

	/**
	 * 返回的响应内容
	 */
	private String content;

	/**
	 * 返回响应状态码
	 */
	private HttpResponseStatus httpResponseStatus;

	/**
	 * 响应对象
	 */
	private Response futureResponse;
	
	private GatewayResponse() {
	}

	/**
	 * 设置响应头信息
	 */
	public void putHeader(CharSequence key, CharSequence val) {
		responseHeaders.add(key, val);
	}

	/**
	 * 构建网关响应对象
	 *
	 * @param futureResponse org.asynchttpclient.Response
	 * @return com.baker.gateway.core.context.gatewayResponse
	 */
	public static GatewayResponse buildGatewayResponse(Response futureResponse) {
		GatewayResponse gatewayResponse = new GatewayResponse();
		gatewayResponse.setFutureResponse(futureResponse);
		gatewayResponse.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
		return gatewayResponse;
	}
	
	/**
	 * 返回一个json类型的响应信息，失败时候使用
	 */
	public static GatewayResponse buildGatewayResponse(ResponseCode code, Object... args) {
		ObjectNode objectNode = JSONUtil.createObjectNode()
				.put(JSONUtil.STATUS, code.getStatus().code())
				.put(JSONUtil.CODE, code.getCode())
				.put(JSONUtil.MESSAGE, code.getMessage());

		GatewayResponse gatewayResponse = new GatewayResponse();
		gatewayResponse.setHttpResponseStatus(code.getStatus());
		gatewayResponse.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
		gatewayResponse.setContent(JSONUtil.toJSONString(objectNode));
		return gatewayResponse;
	}
	
	/**
	 * 返回一个json类型的响应信息，成功时候使用
	 */
	public static GatewayResponse buildGatewayResponseObj(Object data) {
		ObjectNode objectNode = JSONUtil.createObjectNode()
				.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code())
				.put(JSONUtil.CODE, ResponseCode.SUCCESS.getCode())
				.putPOJO(JSONUtil.DATA, data);

		GatewayResponse gatewayResponse = new GatewayResponse();
		gatewayResponse.setHttpResponseStatus(ResponseCode.SUCCESS.getStatus());
		gatewayResponse.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
		gatewayResponse.setContent(JSONUtil.toJSONString(objectNode));
		return gatewayResponse;
	}

}
