package com.baker.gateway.core.plugin.metric.kafka;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.baker.gateway.core.GatewayConfigLoader;
import org.apache.commons.lang3.StringUtils;

import com.baker.gateway.common.metric.TimeSeries;
import com.baker.gateway.core.plugin.Plugin;

import lombok.extern.slf4j.Slf4j;

/**
 * kafka指标收集插件
 */
@Slf4j
public final class MetricKafkaClientPlugin implements Plugin {

    private MetricKafkaClientCollector metricKafkaClientCollector;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    private String address;

    public MetricKafkaClientPlugin() {
    }
    
    @Override
    public boolean check() {
    	this.address = GatewayConfigLoader.getGatewayConfig().getKafkaAddress();
        return !StringUtils.isBlank(this.address);
    }
    
    private boolean checkInit() {
    	return this.initialized.get() && this.metricKafkaClientCollector != null;
    }
    
	@Override
	public void init() {
		if(check()) {
			//	初始化kafka
			this.metricKafkaClientCollector = new MetricKafkaClientCollector(this.address);
			this.metricKafkaClientCollector.start();	
			this.initialized.compareAndSet(false, true);
		}
	}
	
	@Override
	public void destroy() {
		if(checkInit()) {
			this.metricKafkaClientCollector.shutdown();	
			this.initialized.compareAndSet(true, false);
		}
	}

    public <T extends TimeSeries> void send(T metric) {
        try {
        	if(checkInit()) {
                metricKafkaClientCollector.sendAsync(metric.getDestination(), metric,
                        (metadata, exception) -> {
                            if (exception != null) {
                                log.error("#MetricKafkaClientSender# callback exception, metric: {}, {}", metric.toString(), exception.getMessage());
                            }
                        }
                );       		
        	}
        } catch (Exception e) {
            log.error("#MetricKafkaClientSender# send exception, metric: {}", metric.toString(), e);
        }
    }

    public <T extends TimeSeries> void sendBatch(List<T> metricList) {
        for (T metric : metricList) {
            send(metric);
        }
    }

	@Override
	public Plugin getPlugin(String pluginName) {
		if(checkInit() && (MetricKafkaClientPlugin.class.getName()).equals(pluginName)) {
			return this;
		}
		throw new RuntimeException("#MetricKafkaClientPlugin# pluginName: " + pluginName + " is no matched");
	}

}
