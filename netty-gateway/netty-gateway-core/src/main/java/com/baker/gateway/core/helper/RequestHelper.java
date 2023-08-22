package com.baker.gateway.core.helper;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baker.gateway.common.util.Pair;
import com.baker.gateway.core.context.AttributeKey;
import com.baker.gateway.core.context.GatewayContext;
import com.baker.gateway.core.context.GatewayRequest;
import org.apache.commons.lang3.StringUtils;

import com.baker.gateway.common.config.DynamicConfigManager;
import com.baker.gateway.common.config.Rule;
import com.baker.gateway.common.config.ServiceDefinition;
import com.baker.gateway.common.config.ServiceInvoker;
import com.baker.gateway.common.constants.BasicConst;
import com.baker.gateway.common.constants.GatewayProtocol;
import com.baker.gateway.common.enums.ResponseCode;
import com.baker.gateway.common.exception.GatewayNotFoundException;
import com.baker.gateway.common.util.AntPathMatcher;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpUtil;

/**
 * 解析请求信息，构建上下文对象
 */
public class RequestHelper {

	/**
	 * 解析FullHttpRequest 构建GatewayContext核心构建方法
	 */
	public static GatewayContext doContext(FullHttpRequest request, ChannelHandlerContext ctx) {

		//	1. 构建请求对象GatewayRequest
		GatewayRequest gatewayRequest = doRequest(request, ctx);
		
		//	2. 根据请求对象里的serviceId，获取资源服务信息(也就是服务定义信息)
		Pair<ServiceInvoker, ServiceDefinition> pair = getServiceDefinition(gatewayRequest);
		ServiceDefinition serviceDefinition = pair.getValue();
		gatewayRequest.setServiceId(serviceDefinition.getServiceId());


		//	4. 根据请求对象获取服务定义对应的方法调用，然后获取对应的规则
		ServiceInvoker serviceInvoker = pair.getKey();
		Integer ruleId = serviceInvoker.getRuleId();
		if (ruleId == null || ruleId == 0) {
			throw new GatewayNotFoundException(ResponseCode.RULE_NOT_CONFIG);
		}
		Rule rule = DynamicConfigManager.getInstance().getRule(ruleId);
		
		//	5. 构建我们而定GatewayContext对象
		GatewayContext gatewayContext = new GatewayContext.Builder()
				.setProtocol(serviceDefinition.getProtocol())
				.setGatewayRequest(gatewayRequest)
				.setNettyCtx(ctx)
				.setKeepAlive(HttpUtil.isKeepAlive(request))
				.setRule(rule)
				.build();
		
		//	6. 设置SR
		gatewayContext.setSRTime(gatewayRequest.getBeginTime());
		
		//	7. 设置一些必要的上下文参数用于后面使用
		putContext(gatewayContext, serviceInvoker);
		
		return gatewayContext;
	}
	
	/**
	 * 构建GatewayRequest请求对象
	 */
	private static GatewayRequest doRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
		HttpHeaders headers = fullHttpRequest.headers();

		String host = headers.get(HttpHeaderNames.HOST);
		HttpMethod method = fullHttpRequest.method();
		String uri = fullHttpRequest.uri();
		String clientIp = getClientIp(ctx, fullHttpRequest);
		String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
		Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);

		GatewayRequest gatewayRequest = new GatewayRequest(charset,
				clientIp,
				host,
				uri,
				method,
				contentType,
				headers,
				fullHttpRequest);

		return gatewayRequest;
	}
	
	/**
	 * 获取客户端ip
	 */
	private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
		String xForwardedValue = request.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);
		String clientIp = null;
		if(StringUtils.isNotEmpty(xForwardedValue)) {
			List<String> values = Arrays.asList(xForwardedValue.split(", "));
			if(!values.isEmpty() && StringUtils.isNotBlank(values.get(0))) {
				clientIp = values.get(0);
			}
		}
		if(clientIp == null) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
			clientIp = inetSocketAddress.getAddress().getHostAddress();
		}
		return clientIp;
	}

	/**
	 * 通过请求对象获取服务资源信息
	 */
	private static Pair<ServiceInvoker, ServiceDefinition> getServiceDefinition(GatewayRequest gatewayRequest) {
		//	ServiceDefinition就是在网关服务初始化的时候(加载的时候)，从缓存信息里获取
		Pair<ServiceInvoker, ServiceDefinition> serviceDefinition = DynamicConfigManager.getInstance().getServiceInvokerMapMap(gatewayRequest.getPath());
		//	做异常情况判断
		if(serviceDefinition == null) {
			throw new GatewayNotFoundException(ResponseCode.SERVICE_DEFINITION_NOT_FOUND);
		}
		return serviceDefinition;
	}

	/**
	 * 根据请求对象和服务定义对象获取对应的ServiceInvoke
	 */
	private static ServiceInvoker getServiceInvoker(GatewayRequest gatewayRequest, ServiceDefinition serviceDefinition) {
		Map<String, ServiceInvoker> invokerMap = serviceDefinition.getInvokerMap();
		ServiceInvoker serviceInvoker = invokerMap.get(gatewayRequest.getPath());
		if(serviceInvoker == null) {
			throw new GatewayNotFoundException(ResponseCode.SERVICE_INVOKER_NOT_FOUND);
		}
		return serviceInvoker;
	}

	/**
	 * 设置必要的上下文方法
	 */
	private static void putContext(GatewayContext gatewayContext, ServiceInvoker serviceInvoker) {
		switch (gatewayContext.getProtocol()) {
			case GatewayProtocol.HTTP:
				gatewayContext.putAttribute(AttributeKey.HTTP_INVOKER, serviceInvoker);
				break;
			case GatewayProtocol.DUBBO:
				gatewayContext.putAttribute(AttributeKey.DUBBO_INVOKER, serviceInvoker);
				break;
			default:
				break;
		}
	}

	
}
