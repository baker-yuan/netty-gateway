package com.baker.gateway.client.core;

import java.io.InputStream;
import java.util.Properties;

import com.baker.gateway.client.core.autoconfigure.GatewayProperties;
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


	public static final String REGISTER_ADDRESS_KEY = "registryAddress";
	public static final String NAMESPACE_KEY = "namespace";
	public static final String ENV_KEY = "env";
	/**
	 * etcd注册中心地址
	 */
	protected static String registryAddress;
	/**
	 * etcd注册命名空间
	 */
	protected static String namespace;
	/**
	 * 环境属性
	 */
	protected static String env;


	/**
	 * 跟路径
	 */
	protected static String superPath;
	/**
	 * 存储所有的服务定义信息的 ServiceDefinition
	 */
	protected static String servicesPath;
	/**
	 * 存储所有的服务实例信息的 ServiceInstance
	 */
	protected static String instancesPath;
	/**
	 * 存储所有的规则信息的 Rule
	 */
	protected static String rulesPath;

	/**
	 * 是否注册过
	 */
	protected volatile boolean whetherStart = false;

	/**
	 * 注册服务接口
	 */
	private RegistryService registryService;


	//	静态代码块读取gateway.properties配置文件
	static {
		InputStream is;
		is = AbstractClientRegisterManager.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH);
		try {
			if(is != null) {
				properties.load(is);
				registryAddress = properties.getProperty(REGISTER_ADDRESS_KEY);
				namespace = properties.getProperty(NAMESPACE_KEY);
				env = properties.getProperty(ENV_KEY);
				if(StringUtils.isBlank(registryAddress)) {
					String errorMessage = "Gateway网关注册配置地址不能为空";
					log.error(errorMessage);
					throw new RuntimeException(errorMessage);
				}
				if(StringUtils.isBlank(namespace)) {
					namespace = GatewayProperties.GATEWAY_PREFIX;
				}
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
		//	1. 初始化加载配置信息
		if(gatewayProperties.getRegistryAddress() != null) {
			registryAddress = gatewayProperties.getRegistryAddress();
			namespace = gatewayProperties.getNamespace();
			if(StringUtils.isBlank(namespace)) {
				namespace = GatewayProperties.GATEWAY_PREFIX;
			}
			env = gatewayProperties.getEnv();
		}
		
		//	2. 初始化加载注册中心对象
		ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
		RegistryService registryService = serviceLoader.iterator().next();
		registryService.initialized(gatewayProperties.getRegistryAddress());
		this.registryService = registryService;
		// for(RegistryService registryService : serviceLoader) {
		// 	registryService.initialized(gatewayProperties.getRegistryAddress());
		// 	this.registryService = registryService;
		// }
		
		//	3. 注册构建顶级目录结构
		generatorStructPath(Registry.PATH + namespace + BasicConst.BAR_SEPARATOR + env);
	}
	
	/**
	 * 注册顶级结构目录路径，只需要构建一次即可
	 */
	private void generatorStructPath(String path) throws Exception {
		/**
		 * 	/netty-gateway-dev
		 * 		/services
		 * 			/serviceA:1.0.0  ==> ServiceDefinition
		 * 			/serviceA:2.0.0
		 * 			/serviceB:1.0.0
		 * 		/instances
		 * 			/serviceA:1.0.0/192.168.11.100:port	 ==> ServiceInstance
		 * 			/serviceA:1.0.0/192.168.11.101:port
		 * 			/serviceB:1.0.0/192.168.11.102:port
		 * 			/serviceA:2.0.0/192.168.11.103:port
		 * 		/rules
		 * 			/ruleId1	==>	Rule
		 * 			/ruleId2
		 * 		/gateway
		 */
		superPath = path;
		registryService.registerPathIfNotExists(superPath, "", true);
		registryService.registerPathIfNotExists(servicesPath = superPath + Registry.SERVICE_PREFIX, "", true);
		registryService.registerPathIfNotExists(instancesPath = superPath + Registry.INSTANCE_PREFIX, "", true);
		registryService.registerPathIfNotExists(rulesPath = superPath + Registry.RULE_PREFIX, "", true);
	}

	/**
	 * 注册服务定义对象
	 */
	protected void registerServiceDefinition(ServiceDefinition serviceDefinition) throws Exception {
		String key = servicesPath 
				+ Registry.PATH
				+ serviceDefinition.getUniqueId();
		if(!registryService.isExistKey(key)) {
			String value = FastJsonConvertUtil.convertObjectToJSON(serviceDefinition);
			registryService.registerPathIfNotExists(key, value, true);
		}
	}
	
	/**
	 * 注册服务实例方法
	 */
	protected void registerServiceInstance(ServiceInstance serviceInstance) throws Exception {
		String key = instancesPath
				+ Registry.PATH
				+ serviceInstance.getUniqueId()
				+ Registry.PATH
				+ serviceInstance.getServiceInstanceId();
		if(!registryService.isExistKey(key)) {
			String value = FastJsonConvertUtil.convertObjectToJSON(serviceInstance);
			registryService.registerPathIfNotExists(key, value, false);
		}
	}

	public static String getRegistryAddress() {
		return registryAddress;
	}

	public static String getNamespace() {
		return namespace;
	}

	public static String getEnv() {
		return env;
	}

	public static String getSuperPath() {
		return superPath;
	}

	public static String getServicesPath() {
		return servicesPath;
	}

	public static String getInstancesPath() {
		return instancesPath;
	}

	public static String getRulesPath() {
		return rulesPath;
	}

}
