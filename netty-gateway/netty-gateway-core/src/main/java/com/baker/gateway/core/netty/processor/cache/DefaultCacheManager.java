package com.baker.gateway.core.netty.processor.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.dubbo.rpc.service.GenericService;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;


public class DefaultCacheManager {

	private DefaultCacheManager() {
	}
	
	public static final String FILTER_CONFIG_CACHE_ID = "filterConfigCache";

	/**
	 * 这个是全局的缓存，双层缓存
	 */
	private final ConcurrentHashMap<String, Cache<String, ?>> cacheMap = new ConcurrentHashMap<>();

	private static class SingletonHolder {
		private static final DefaultCacheManager INSTANCE = new DefaultCacheManager();
	}
	
	public static DefaultCacheManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * 根据一个全局的缓存ID 创建一个Caffeine缓存对象
	 */
	@SuppressWarnings("unchecked")
	public <V> Cache<String, V> create(String cacheId) {
		Cache<String, V> cache = Caffeine.newBuilder().build();
		cacheMap.put(cacheId, cache);
		return (Cache<String, V>) cacheMap.get(cacheId);
	}
	
	/**
	 * 根据cacheId 和对应的真正Caffeine缓存key 删除一个Caffeine缓存对象
	 */
	public <V> void remove(String cacheId, String key) {
		@SuppressWarnings("unchecked")
		Cache<String, V> cache = (Cache<String, V>) cacheMap.get(cacheId);
		if(cache != null) {
			cache.invalidate(key);
		}
	}
	
	/**
	 * 根据全局的缓存id 删除这个Caffeine缓存对象
	 */
	public <V> void remove(String cacheId) {
		@SuppressWarnings("unchecked")
		Cache<String, V> cache = (Cache<String, V>) cacheMap.get(cacheId);
		if(cache != null) {
			cache.invalidateAll();
		}
	}
	/**
	 * 清空所有的缓存
	 */
	public void cleanAll() {
		cacheMap.values().forEach(Cache::invalidateAll);
	}
	

    public static Cache<String, GenericService> createForDubboGenericService() {
        return Caffeine.newBuilder().build();
    }

	
}
