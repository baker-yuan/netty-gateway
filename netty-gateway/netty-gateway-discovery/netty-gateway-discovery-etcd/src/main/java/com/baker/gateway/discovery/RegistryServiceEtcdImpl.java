package com.baker.gateway.discovery;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baker.gateway.common.util.Pair;
import com.baker.gateway.discovery.api.Notify;
import com.baker.gateway.discovery.api.Registry;
import com.baker.gateway.discovery.api.RegistryService;
import com.baker.gateway.etcd.api.EtcdChangedEvent;
import com.baker.gateway.etcd.api.EtcdChangedEvent.Type;
import com.baker.gateway.etcd.api.EtcdClient;
import com.baker.gateway.etcd.api.WatcherListener;
import com.baker.gateway.etcd.core.EtcdClientImpl;

import io.etcd.jetcd.KeyValue;
import lombok.extern.slf4j.Slf4j;

/**
 * 注册中心实现类
 */
@Slf4j
public class RegistryServiceEtcdImpl implements RegistryService {

    private EtcdClient etcdClient;

    private final Map<String, String> cachedMap = new HashMap<>();

    @Override
    public void initialized(String registryAddress) {
        //	初始化etcd客户端对象
        etcdClient = new EtcdClientImpl(registryAddress,
                true,
                "",
                null,
                null,
                null);
        //	添加异常的过期处理监听
        etcdClient.addHeartBeatLeaseTimeoutNotifyListener(() -> cachedMap.forEach((key, value) -> {
            try {
                registerEphemeralNode(key, value);
            } catch (Exception e) {
                //	ignore
                log.error("#RegistryServiceEtcdImpl.initialized# HeartBeatLeaseTimeoutListener: timeoutNotify is error", e);
            }
        }));
    }

    /**
     * 根据一个路径做多种实现，服务的子节点变更 添加监听
     *
     * @see RegistryService#addWatcherListeners(java.lang.String, Notify)
     */
    @Override
    public void addWatcherListeners(String superPath, Notify notify) {
        etcdClient.addWatcherListener(superPath + Registry.SERVICE_PREFIX, true, new InnerWatcherListener(notify));
        etcdClient.addWatcherListener(superPath + Registry.INSTANCE_PREFIX, true, new InnerWatcherListener(notify));
        etcdClient.addWatcherListener(superPath + Registry.RULE_PREFIX, true, new InnerWatcherListener(notify));
        //	网关服务本身发变更
        etcdClient.addWatcherListener(superPath + Registry.GATEWAY_PREFIX, true, new InnerWatcherListener(notify));
    }

    static class InnerWatcherListener implements WatcherListener {

        private final Notify notify;

        public InnerWatcherListener(Notify notify) {
            this.notify = notify;
        }

        @Override
        public void watcherKeyChanged(EtcdClient etcdClient, EtcdChangedEvent event) throws Exception {
            Type type = event.getType();
            KeyValue curtKeyValue = event.getCurtKeyValue();
            switch (type) {
                case PUT:
                    notify.put(curtKeyValue.getKey().toString(Charset.defaultCharset()),
                            curtKeyValue.getValue().toString(Charset.defaultCharset()));
                    break;
                case DELETE:
                    notify.delete(curtKeyValue.getKey().toString(Charset.defaultCharset()));
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 注册临时节点
     *
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    @Override
    public long registerEphemeralNode(String key, String value) throws Exception {
        long leaseId = this.etcdClient.getHeartBeatLeaseId();
        cachedMap.put(key, value);
        return this.etcdClient.putKeyWithLeaseId(key, value, leaseId);
    }

    @Override
    public void registerPathIfNotExists(String path, String value, boolean isPersistent) throws Exception {
        if (!isExistKey(path)) {
            if (isPersistent) {
                // 永久节点
                registerPersistentNode(path, value);
            } else {
                // 非永久节点
                registerEphemeralNode(path, value);
            }
        }
    }

    @Override
    public void registerPersistentNode(String key, String value) throws Exception {
        this.etcdClient.putKey(key, value);
    }

    @Override
    public List<Pair<String, String>> getListByPrefixKey(String prefix) {
        List<KeyValue> list = this.etcdClient.getKeyWithPrefix(prefix);
        List<Pair<String, String>> result = new ArrayList<>();
        for (KeyValue kv : list) {
            result.add(new Pair<>(kv.getKey().toString(Charset.defaultCharset()),
                    kv.getValue().toString(Charset.defaultCharset())));
        }
        return result;
    }

    @Override
    public Pair<String, String> getByKey(String key) throws Exception {
        KeyValue kv = etcdClient.getKey(key);
        return new Pair<>(kv.getKey().toString(Charset.defaultCharset()), kv.getValue().toString(Charset.defaultCharset()));
    }

    @Override
    public boolean isExistKey(String key) throws Exception {
        KeyValue kv = etcdClient.getKey(key);
        if (kv == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void deleteByKey(String key) {
        etcdClient.deleteKey(key);
    }

    /**
     * @see Registry#close()
     */
    @Override
    public void close() {
        if (etcdClient != null) {
            etcdClient.close();
        }
    }

}
