package com.baker.gateway.core.netty.processor.filter;

import com.baker.gateway.core.context.Context;

/**
 * 最终的链表实现类
 */
public class DefaultProcessorFilterChain extends ProcessorFilterChain<Context> {
	/**
	 * 过滤器链唯一标识
	 */
	private final String id;
	
	public DefaultProcessorFilterChain(String id) {
		this.id = id;
	}
	
	/**
	 * 虚拟头结点：dummyHead
	 */
	AbstractLinkedProcessorFilter<Context> first = new AbstractLinkedProcessorFilter<Context>() {

		@Override
		public boolean check(Context ctx) {
			return true;
		}

		@Override
		public void entry(Context ctx, Object... args) throws Throwable {
			super.fireNext(ctx, args);
		}

	};
	
	/**
	 * 	尾节点
	 */
	AbstractLinkedProcessorFilter<Context> end = first;

	@Override
	public void addFirst(AbstractLinkedProcessorFilter<Context> filter) {
		// first -> end
		// first -> filter -> end
		filter.setNext(first.getNext());
		first.setNext(filter);
		if(end == first) {
			end = filter;
		}
	}

	@Override
	public void addLast(AbstractLinkedProcessorFilter<Context> filter) {
		end.setNext(filter);
		end = filter;
	}
	
	@Override
	public void setNext(AbstractLinkedProcessorFilter<Context> filter) {
		addLast(filter);
	}
	
	@Override
	public AbstractLinkedProcessorFilter<Context> getNext() {
		return first.getNext();
	}
	
	
	@Override
	public boolean check(Context ctx) {
		return true;
	}
	
	@Override
	public void entry(Context ctx, Object... args) throws Throwable {
		first.transformEntry(ctx, args);
	}

	public String getId() {
		return id;
	}

}
