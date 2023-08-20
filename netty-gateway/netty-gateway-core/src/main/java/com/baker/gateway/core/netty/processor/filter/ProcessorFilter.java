package com.baker.gateway.core.netty.processor.filter;

/**
 * 执行过滤器的接口操作
 */
public interface ProcessorFilter<T> {

	/**
	 * 过滤器是否执行的校验方法
	 */
	boolean check(T t) throws Throwable;
	
	/**
	 * 真正执行过滤器的方法
	 */
	void entry(T t, Object... args) throws Throwable;
	
	/**
	 * 触发下一个过滤器执行
	 */
	void fireNext(T t, Object... args) throws Throwable;
	
	/**
	 * 对象传输的方法
	 */
	void transformEntry(T t, Object... args) throws Throwable;

	/**
	 * 过滤器初始化的方法，如果子类有需求则进行覆盖
	 */
	default void init() throws Exception {}
	
	/**
	 * 过滤器销毁的方法，如果子类有需求则进行覆盖
	 */
	default void destroy() throws Exception {}
	
	/**
	 * 过滤器刷新的方法，如果子类有需求则进行覆盖
	 */
	default void refresh() throws Exception {}
}
