package com.baker.gateway.disruptor.quicklystart;

import java.nio.ByteBuffer;

import com.lmax.disruptor.RingBuffer;

/**
 * 生产者对象
 */
public class OrderEventProducer {

	private RingBuffer<OrderEvent> ringBuffer;
	
	public OrderEventProducer(RingBuffer<OrderEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	public void putData(ByteBuffer bb) {
		//	先获取下一个可用的序号
		long sequence = ringBuffer.next();
		try {
			//	通过可用的序号获取对应下标的数据OrderEvent
			OrderEvent event = ringBuffer.get(sequence);
			//	重新设置内容
			event.setValue(bb.getLong(0));			
		} finally {
			//	publish
			ringBuffer.publish(sequence);
		}
	}
	
}
