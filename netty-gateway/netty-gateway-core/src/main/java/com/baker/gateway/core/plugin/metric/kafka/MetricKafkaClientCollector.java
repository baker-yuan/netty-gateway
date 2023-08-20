package com.baker.gateway.core.plugin.metric.kafka;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.baker.gateway.common.metric.MetricClientCollector;
import com.baker.gateway.common.metric.MetricException;
import com.baker.gateway.common.metric.TimeSeries;
import com.baker.gateway.common.util.FastJsonConvertUtil;

/**
 * kafka指标收集
 */
public final class MetricKafkaClientCollector implements MetricClientCollector {

    /**
     * 每个发送批次大小为 16K
     **/
    private final int batchSize = 1024 * 16;

    /**
     * batch 没满的情况下默认等待 100 ms
     **/
    private final int lingerMs = 100;

    /**
     * producer 的缓存为 64M
     **/
    private final int bufferMemory = 1024 * 1024 * 64;

    /**
     * 需要确保写入副本 leader
     **/
    private final String acks = "1";

    /**
     * 为了减少带宽，使用 lz4 压缩
     **/
    private final String compressionType = "lz4";

    /**
     * 当 memory buffer 满了之后，send() 在抛出异常之前阻塞的最长时间
     **/
    private final int blockMs = 10000;

    private final String serializerClass = "org.apache.kafka.common.serialization.StringSerializer";

    private final Properties props;

    private KafkaProducer<String, String> producer;

    public MetricKafkaClientCollector(String address) {
        props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, address);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, blockMs);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializerClass);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializerClass);
    }


    @Override
    public void start() {
        this.producer = new KafkaProducer<>(props);
    }

    /**
     * 同步发送
     */
    public <T extends TimeSeries> RecordMetadata sendSync(String topic, T message) throws InterruptedException, ExecutionException, MetricException {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(message);
        Future<RecordMetadata> future = producer.send(new ProducerRecord<>(topic, FastJsonConvertUtil.convertObjectToJSON(message)));
        return future.get();
    }

    /**
     * 异步发送
     */
    public <T extends TimeSeries> void sendAsync(String topic, T message) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(message);
        producer.send(new ProducerRecord<>(topic, FastJsonConvertUtil.convertObjectToJSON(message)));
    }

    /**
     * 异步发送，带回调函数
     *
     * @param topic    主题
     * @param message  消息
     * @param callback 异步回调
     */
    public <T extends TimeSeries> void sendAsync(String topic, T message, Callback callback) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(message);
        producer.send(new ProducerRecord<>(topic, FastJsonConvertUtil.convertObjectToJSON(message)), callback);
    }

    @Override
    public void shutdown() {
        this.producer.close();
    }
}
