package com.baker.gateway.core.plugin;

/**
 * 插件的生命周期管理
 */
public interface Plugin {
    /**
     * 是否启用插件
     */
    default boolean check() {
        return true;
    }

    /**
     * 插件初始化
     */
    void init();

    /**
     * 插件销毁
     */
    void destroy();

    /**
     * 获取插件
     *
     * @param pluginName 插件名称
     * @return 插件
     */
    Plugin getPlugin(String pluginName);

}
