package com.baker.gateway.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务定义注解类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GatewayService {

	/**
	 * 服务的唯一ID
	 */
	String serviceId();
	
	/**
	 * 对应服务的版本号
	 */
	String version() default "1.0.0";
	
	/**
	 * 协议类型
	 */
	GatewayProtocol protocol();
	
	/**
	 * 服务URL前缀，全局唯一
	 */
	String basePath();
	
}
