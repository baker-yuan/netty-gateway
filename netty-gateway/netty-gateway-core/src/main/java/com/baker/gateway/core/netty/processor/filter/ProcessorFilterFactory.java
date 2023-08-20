package com.baker.gateway.core.netty.processor.filter;

import java.util.List;

import com.baker.gateway.core.context.Context;

/**
 * 过滤器工厂接口
 */
public interface ProcessorFilterFactory {
	/**
	 * 根据过滤器类型，添加一组过滤器，用于构建过滤器链
	 */
	void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters) throws Exception;

	/**
	 * 正常情况下执行过滤器链条
	 */
	void doFilterChain(Context ctx) throws Exception;

	/**
	 * 错误、异常情况下执行该过滤器链条
	 */
	void doErrorFilterChain(Context ctx) throws Exception;
	
	/**
	 * 获取指定类类型的过滤器
	 */
	<T> T getFilter(Class<T> t) throws Exception;
	
	/**
	 * 获取指定ID的过滤器
	 */
	<T> T getFilter(String filterId) throws Exception;
}
