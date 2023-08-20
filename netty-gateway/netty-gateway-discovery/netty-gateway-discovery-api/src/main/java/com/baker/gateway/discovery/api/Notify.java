package com.baker.gateway.discovery.api;

/**
 * 监听服务接口
 */
public interface Notify {
	/**
	 * 添加或者更新的方法
	 */
	void put(String key, String value) throws Exception;
	
	/**
	 * 删除方法
	 */
	void delete(String key) throws Exception;
}
