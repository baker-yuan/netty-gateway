package com.baker.gateway.common.metric;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Unary Metric Struct：一元数据指标结构
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class Metric extends TimeSeries implements Serializable {

	private static final long serialVersionUID = 4754755625101487737L;

	/**
     * 	指标名称，由两部分组成，业务线
     */
    @JSONField(name = "mame")
    protected String name;

    /**
     * 	标取值
     */
    @JSONField(name = "value")
    protected Number value;

    /**
     * 	标签集合
     */
    protected Map<String, String> tags = new HashMap<String, String>();
    
    /**
     * 		{
     * 			metricName: key
     * 			metricValue: Long
     * 			timestamp: 时间戳
     * 			tags:{ k1:v1 , k2:v2}
     * 		}
     * 
     * <B>构造方法</B><BR>
     */

    private Metric() {
    }
    
    /**
     * @param name      指标名称
     * @param value     指标取值
     * @param timestamp 时间戳
     * @param tags      标签集合
     * @param destination   sink目标源
     * @return Metric
     */
    public static Metric create(String name, Number value, long timestamp, Map<String, String> tags, String destination, boolean enablePartition) {
        Metric metric = new Metric();
        metric.setName(name);
        metric.setValue(value);
        metric.setTimestamp(timestamp);
        metric.getTags().putAll(tags);
    	metric.setDestination(destination);
    	metric.setEnablePartitionHash(enablePartition);
    	return metric;
    }

}
