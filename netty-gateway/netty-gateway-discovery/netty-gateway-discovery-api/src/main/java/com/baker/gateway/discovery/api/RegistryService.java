package com.baker.gateway.discovery.api;

/**
 * 注册服务接口
 */
public interface RegistryService extends Registry {
    /**
     * 添加一堆的监听事件
     *
     * @param superPath 父节点目录
     * @param notify    监听函数
     */
    void addWatcherListeners(String superPath, Notify notify);

    /**
     * 初始化注册服务
     *
     * @param registryAddress 注册服务地址
     */
    void initialized(String registryAddress);
}
