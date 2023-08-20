package com.baker.gateway.disruptor.quicklystart;

import org.apache.commons.lang3.RandomUtils;

import com.lmax.disruptor.EventHandler;

/**
 * 事件
 */
public class OrderEventHandler implements EventHandler<OrderEvent> {

	@Override
	public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws Exception {
		Thread.sleep(RandomUtils.nextInt(1, 100));
		System.err.println("消费者消费：" + event.getValue());
	}

}
