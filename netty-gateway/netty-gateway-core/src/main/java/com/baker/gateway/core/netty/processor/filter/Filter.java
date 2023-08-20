package com.baker.gateway.core.netty.processor.filter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 过滤器注解类
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Filter {

	/**
	 * 过滤器的唯一ID，必填
	 */
	String id();
	
	/**
	 * 过滤器的名字
	 */
	String name() default "";
	
	/**
	 * 过滤器的类型
	 */
	ProcessorFilterType value();
	
	/**
	 * 过滤器的排序，按照此排序从小到大依次执行过滤器
	 */
	int order() default 0;
}
