package com.baker.gateway.console;

import java.util.Map;

import com.baker.gateway.console.consumer.MQConsumerFactory;
import com.baker.gateway.console.consumer.MetricConsumer;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ConsumerContainer {

	public ConsumerContainer(GatewayConsoleProperties gatewayConsoleProperties) {
		for(int i = 0; i < gatewayConsoleProperties.getConsumerNum(); i ++) {
			MQConsumerFactory.getInstance().createConsumer(gatewayConsoleProperties.getKafkaAddress(),
					gatewayConsoleProperties.getGroupId(),
					gatewayConsoleProperties.getTopicNamePrefix(),
					i);
		}
	}
	
	public void start() {
		runReactorConsumer();
	}
	
	public void stop() {
		MQConsumerFactory.getInstance().stopConsumers();
	}

	private void runReactorConsumer() {
		for(Map.Entry<String, MetricConsumer> me : MQConsumerFactory.getConsumers().entrySet()) {
			MetricConsumer mqConsumer = me.getValue();
			mqConsumer.start();
		}
		log.info("#ConsumerServer# started...");
	}
	
}