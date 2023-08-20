package com.baker.gateway.discovery.api;

import java.util.List;

import com.baker.gateway.common.util.Pair;

/**
 * 注册接口
 */
public interface Registry {

	/**
	 * 	/services: 是要存储所有的服务定义信息的：ServiceDefinition (永久存储)
	 */
	String SERVICE_PREFIX = "/services";
	
	/**
	 * 	/instances: 是要存储所有的服务实例信息的： ServiceInstance (加载时存储)
	 */
	String INSTANCE_PREFIX = "/instances";
	
	/**
	 * 	/rules: 是要存储所有的规则信息的：Rule (永久存储)
	 */
	String RULE_PREFIX = "/rules";
	
	/**
	 * 	/gateway: 这个是要存储所有的网关本身自注册信息的 netty-gateway-core(网关服务本身，加载时存储)
	 */
	String GATEWAY_PREFIX = "/gateway";

	/**
	 * 路径分隔符
	 */
	String PATH = "/";
	
	/**
	 * 注册一个路径如果不存在
	 */
	void registerPathIfNotExists(String path, String value, boolean isPersistent) throws Exception;
	
	/**
	 * 注册一个临时节点
	 */
	long registerEphemeralNode(String key, String value) throws Exception;
	
	/**
	 * 注册一个永久节点
	 */
	void registerPersistentNode(String key, String value) throws Exception;
	
	/**
	 * 通过一个前缀路径，获取一堆对应的数据信息
	 */
	List<Pair<String, String>> getListByPrefixKey(String prefix) throws Exception;
	
	/**
	 * 通过一个key查询对应键值对对象
	 */
	Pair<String, String> getByKey(String key) throws Exception;
	
	/**
	 * 根据一个key键，判断是否存在
	 */
	boolean isExistKey(String key) throws Exception;
	
	/**
	 * 根据key删除
	 */
	void deleteByKey(String key);
	
	/**
	 * 关闭服务
	 */
	void close();
	
}
