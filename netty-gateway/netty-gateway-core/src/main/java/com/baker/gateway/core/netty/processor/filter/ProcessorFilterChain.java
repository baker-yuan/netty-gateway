package com.baker.gateway.core.netty.processor.filter;

/**
 * 链表的抽象接口，添加一些简单的操作方法
 */
public abstract class ProcessorFilterChain<T> extends AbstractLinkedProcessorFilter<T> {

	/**
	 * 在链表的头部添加元素
	 */
	public abstract void addFirst(AbstractLinkedProcessorFilter<T> filter);
	
	/**
	 * 在链表的尾部添加元素
	 */
	public abstract void addLast(AbstractLinkedProcessorFilter<T> filter);
	
}
