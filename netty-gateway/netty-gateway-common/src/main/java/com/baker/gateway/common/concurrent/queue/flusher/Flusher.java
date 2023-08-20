package com.baker.gateway.common.concurrent.queue.flusher;

/**
 * Flusher接口定义
 */
public interface Flusher<E> {

	/**
	 * 添加元素方法
	 */
	void add(E event);
	
	/**
	 * 添加多个元素
	 */
	void add(@SuppressWarnings("unchecked") E... event);
	
	/**
	 * 尝试添加一个元素，如果添加成功返回true 失败返回false
	 */
	boolean tryAdd(E event);
	
	/**
	 * 尝试添加多个元素，如果添加成功返回true 失败返回false
	 */
	boolean tryAdd(@SuppressWarnings("unchecked")E... event);


	boolean isShutdown();
	

	void start();
	

	void shutdown();
}
