package com.baker.gateway.client.core;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.baker.gateway.client.core.autoconfigure.GatewayProperties;
import com.baker.gateway.common.util.JSONUtil;
import org.apache.commons.lang3.StringUtils;

import com.baker.gateway.common.config.ServiceDefinition;
import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.common.constants.BasicConst;
import com.baker.gateway.common.util.FastJsonConvertUtil;
import com.baker.gateway.common.util.ServiceLoader;
import com.baker.gateway.discovery.api.Registry;
import com.baker.gateway.discovery.api.RegistryService;

import lombok.extern.slf4j.Slf4j;

/**
 * 抽象注册管理器
 */
@Slf4j
public abstract class AbstractClientRegisterManager {

	public static final String PROPERTIES_PATH = "gateway.properties";
	public static Properties properties = new Properties();


	public static final String ENV_KEY = "env";
	public static final String CONSOLE_URL_KEY = "consoleUrl";
	/**
	 * 控制台地址
	 */
	protected static String consoleUrl;
	/**
	 * 环境属性
	 */
	protected static String env;

	/**
	 * 是否注册过
	 */
	protected volatile boolean whetherStart = false;


	//	静态代码块读取gateway.properties配置文件
	static {
		InputStream is;
		is = AbstractClientRegisterManager.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH);
		try {
			if(is != null) {
				properties.load(is);
				consoleUrl = properties.getProperty(CONSOLE_URL_KEY);
				env = properties.getProperty(ENV_KEY);
			}
		} catch (Exception e) {
			log.error("#AbstractClientRegisteryManager# InputStream load is error", e);
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (Exception ex) {
					//	ignore
					log.error("#AbstractClientRegisteryManager# InputStream close is error", ex);
				}
			}
		}
	}
	
	/**
	 * 	application.properties/yml 优先级是最高的
	 */
	protected AbstractClientRegisterManager(GatewayProperties gatewayProperties) throws Exception {
		if (gatewayProperties.getConsoleUrl() != null) {
			consoleUrl = gatewayProperties.getConsoleUrl();
		}

		if (gatewayProperties.getEnv() != null) {
			env = gatewayProperties.getEnv();
		}
	}

	/**
	 * 注册服务定义对象
	 */
	protected void registerServiceDefinition(ServiceDefinition serviceDefinition) throws Exception {
		HttpResponse response = HttpRequest
				.post(consoleUrl + "/serviceDefinition/addOrUpdate")
				.body(JSONUtil.toJSONString(serviceDefinition))
				.execute();
		if (response.getStatus()  == 200) {
			return;
		}
		log.error("registerServiceDefinition fail response: {}", response);
	}
	
	/**
	 * 注册服务实例方法
	 */
	protected void registerServiceInstance(ServiceInstance serviceInstance) throws Exception {
		HttpResponse response = HttpRequest
				.post(consoleUrl + "/serviceInstance/addOrUpdate")
				.body(JSONUtil.toJSONString(serviceInstance))
				.execute();
		if (response.getStatus()  == 200) {
			return;
		}
		log.error("registerServiceDefinition fail response: {}", response);
	}

}
