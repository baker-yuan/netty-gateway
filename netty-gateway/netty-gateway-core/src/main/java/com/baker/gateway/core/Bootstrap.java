package com.baker.gateway.core;

import com.baker.gateway.core.discovery.RegistryManager;
import com.baker.gateway.core.plugin.PluginManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 网关项目启动主入口
 */
@Slf4j
public class Bootstrap {

	public static void main(String[] args) {
		//	1. 加载网关的配置信息
		GatewayConfig gatewayConfig = GatewayConfigLoader.getInstance().load(args);
		
		//	2. 插件初始化的工作
		PluginManager.getPlugin().init();
		
		//	3. 初始化服务注册管理中心（服务注册管理器），监听动态配置的新增、修改、删除
		try {
			RegistryManager.getInstance().initialized(gatewayConfig);
		} catch (Exception e) {
			log.error("#Bootstrap# RegistryManager is failed", e);
		}

		//	4. 启动容器
		GatewayContainer gatewayContainer = new GatewayContainer(gatewayConfig);
		gatewayContainer.start();

		// 5. 关闭钩子
		Runtime.getRuntime().addShutdownHook(new Thread(gatewayContainer::shutdown));
	}

}
