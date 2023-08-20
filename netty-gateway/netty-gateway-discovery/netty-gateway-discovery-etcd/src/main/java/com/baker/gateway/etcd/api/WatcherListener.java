package com.baker.gateway.etcd.api;


public interface WatcherListener {

    void watcherKeyChanged(EtcdClient etcdClient, EtcdChangedEvent event) throws Exception;

}
