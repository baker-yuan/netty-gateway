package com.baker.gateway.core.netty.processor.filter;

import com.baker.gateway.core.context.Context;
import com.baker.gateway.core.netty.processor.cache.DefaultCacheManager;
import org.apache.commons.lang3.StringUtils;

import com.github.benmanes.caffeine.cache.Cache;
import com.baker.gateway.common.config.Rule.FilterConfig;
import com.baker.gateway.common.constants.BasicConst;
import com.baker.gateway.common.util.JSONUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 抽象的Filter，用于真正的Filter进行继承的
 */
@Slf4j
public abstract class AbstractEntryProcessorFilter<FilterConfigClass> extends AbstractLinkedProcessorFilter<Context> {
	/**
	 * 自定义过滤器@Filter信息
	 */
	protected Filter filterAnnotation;
	/**
	 * 缓存，过滤器配置
	 */
	protected Cache<String, FilterConfigClass> cache;
	/**
	 * 过滤器class类型，用于反序列化
	 */
	protected final Class<FilterConfigClass> filterConfigClass;


	public AbstractEntryProcessorFilter(Class<FilterConfigClass> filterConfigClass) {
		this.filterAnnotation = this.getClass().getAnnotation(Filter.class);
		this.filterConfigClass = filterConfigClass;
		this.cache = DefaultCacheManager.getInstance().create(DefaultCacheManager.FILTER_CONFIG_CACHE_ID);
	}
	
	@Override
	public boolean check(Context ctx) {
		// 规则里面是否配置了该过滤器
		return ctx.getRule().hashId(filterAnnotation.id());
	}
	
	@Override
	public void transformEntry(Context ctx, Object... args) throws Throwable {
		FilterConfigClass filterConfigClass = dynamicLoadCache(ctx, args);
		super.transformEntry(ctx, filterConfigClass);
	}

	/**
	 * 动态加载缓存：每一个过滤器的具体配置规则
	 */
	private FilterConfigClass dynamicLoadCache(Context ctx, Object[] args) {
		// 通过上下文对象拿到规则，再通过规则获取到指定filterId的FilterConfig
		FilterConfig filterConfig = ctx.getRule().getFilterConfig(filterAnnotation.id());
		
		// 定义一个cacheKey
		String ruleId = ctx.getRule().getId();
		String cacheKey = ruleId + BasicConst.DOLLAR_SEPARATOR + filterAnnotation.id();
		
		FilterConfigClass fcc = cache.getIfPresent(cacheKey);
		if(fcc == null) {
			if(filterConfig != null && StringUtils.isNotEmpty(filterConfig.getConfig())) {
				String configStr = filterConfig.getConfig();
				try {
					fcc = JSONUtil.parse(configStr, filterConfigClass);
					cache.put(cacheKey, fcc);					
				} catch (Exception e) {
					log.error("#AbstractEntryProcessorFilter# dynamicLoadCache filterId: {}, config parse error: {}",
							filterAnnotation.id(),
							configStr,
							e);
				}
			}
		}
		return fcc;
	}


}
