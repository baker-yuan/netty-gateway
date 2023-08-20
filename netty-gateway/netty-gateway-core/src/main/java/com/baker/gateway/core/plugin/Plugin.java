package com.baker.gateway.core.plugin;

/**
 * 插件的生命周期管理
 */
public interface Plugin {

    default boolean check() {
        return true;
    }

    void init();

    void destroy();
    
    Plugin getPlugin(String pluginName);

}
