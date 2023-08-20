package com.baker.gateway.disruptor.quicklystart;

import com.lmax.disruptor.EventFactory;

/**
 * ds的事件工厂类
 */
public class OrderEventFactory implements EventFactory<OrderEvent>{

	@Override
	public OrderEvent newInstance() {
		return new OrderEvent();
	}

}
