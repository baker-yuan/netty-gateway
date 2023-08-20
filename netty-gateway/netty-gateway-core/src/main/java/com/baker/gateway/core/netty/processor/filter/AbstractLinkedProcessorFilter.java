package com.baker.gateway.core.netty.processor.filter;

import com.baker.gateway.core.context.Context;
import com.baker.gateway.core.helper.ResponseHelper;

/**
 * 抽象的带有链表形式的过滤器
 */
public abstract class AbstractLinkedProcessorFilter<T> implements ProcessorFilter<Context> {

	/**
	 * 做一个链表里面的一个元素，必须要有下一个元素的引用
	 */
	protected AbstractLinkedProcessorFilter<T> next = null;
	
	@Override
	public void fireNext(Context ctx, Object... args) throws Throwable {
		//	上下文生命周期
		if(ctx.isTerminated()) {
			return;
		}

		// 写回响应标记，标记当前Context/请求需要写回，执行ctx.writeAndFlush(response)，将状态流转为COMPLETED
		if(ctx.isWrittened()) {
			ResponseHelper.writeResponse(ctx);
		}

		if(next != null) {
			if(!next.check(ctx)) {
				// 跳过当前节点
				next.fireNext(ctx, args);
			} else {
				// 执行当前节点
				next.transformEntry(ctx, args);
			}
		} else {
			// 没有下一个节点了，已经到了链表的最后一个节点
			ctx.terminated();
        }
	}
	
	@Override
	public void transformEntry(Context ctx, Object... args) throws Throwable {
		// 子类调用：这里就是真正执行下一个节点(元素)的操作
		entry(ctx, args);
	}

	/**
	 * 设置下一个节点
	 */
	public void setNext(AbstractLinkedProcessorFilter<T> next) {
		this.next = next;
	}

	/**
	 * 获取下一个节点
	 */
	public AbstractLinkedProcessorFilter<T> getNext() {
		return next;
	}
}
