package com.baker.gateway.core.netty.processor.filter;

import lombok.Data;

/**
 * 所有的过滤器配置实现类的Base类
 */
@Data
public class FilterConfig {
	
	/**
	 * 	是否打印日志
	 */
	private boolean loggable = false;

}
