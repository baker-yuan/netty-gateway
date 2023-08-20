package com.baker.gateway.core.netty.processor.filter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.baker.gateway.core.context.Context;

import lombok.extern.slf4j.Slf4j;

/**
 * 抽象的过滤器工厂
 */
@Slf4j
public abstract class AbstractProcessorFilterFactory implements ProcessorFilterFactory {

	/**
	 * pre + route + post
	 */
	public DefaultProcessorFilterChain defaultProcessorFilterChain = new DefaultProcessorFilterChain("defaultProcessorFilterChain"); 
	
	/**
	 * error + post
	 */
	public DefaultProcessorFilterChain errorProcessorFilterChain = new DefaultProcessorFilterChain("errorProcessorFilterChain"); 

	/**
	 * 根据过滤器类型获取filter集合 key=processorFilterType
	 */
	public Map<String , Map<String, ProcessorFilter<Context>>> processorFilterTypeMap = new LinkedHashMap<>();
	
	/**
	 * 根据过滤器id获取对应的Filter key=filterId
	 */
	public Map<String , ProcessorFilter<Context>> processorFilterIdMap = new LinkedHashMap<>();
	
	/**
	 * 构建过滤器链条
	 *
	 * @see ProcessorFilterFactory#buildFilterChain(ProcessorFilterType, java.util.List)
	 */
	@Override
	public void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters) throws Exception {
		switch (filterType) {
			case PRE:
			case ROUTE:
				addFilterForChain(defaultProcessorFilterChain, filters);
				break;
			case ERROR:
				addFilterForChain(errorProcessorFilterChain, filters);
				break;
			case POST:	
				addFilterForChain(defaultProcessorFilterChain, filters);
				addFilterForChain(errorProcessorFilterChain, filters);
				break;
			default:
				throw new RuntimeException("ProcessorFilterType is not supported !");
			}
	}
	
	private void addFilterForChain(DefaultProcessorFilterChain processorFilterChain, List<ProcessorFilter<Context>> filters) throws Exception {
		for(ProcessorFilter<Context> processorFilter : filters) {
			processorFilter.init();
			doBuilder(processorFilterChain, processorFilter);
		}
	}

	/**
	 * 添加过滤器到指定的filterChain
	 */
	private void doBuilder(DefaultProcessorFilterChain processorFilterChain, ProcessorFilter<Context> processorFilter) {
		log.info("filterChain: {}, the scanner filter is : {}", processorFilterChain.getId(), processorFilter.getClass().getName());
		Filter annotation = processorFilter.getClass().getAnnotation(Filter.class);
		
		if(annotation != null) {
			//	构建过滤器链条，添加filter
			processorFilterChain.addLast((AbstractLinkedProcessorFilter<Context>)processorFilter);
			
			//	映射到过滤器集合
			String filterId = annotation.id();
			if(filterId == null || filterId.length() < 1) {
				filterId = processorFilter.getClass().getName();
			}

			String code = annotation.value().getCode();
			Map<String, ProcessorFilter<Context>> filterMap = processorFilterTypeMap.get(code);
			if(filterMap == null) {
				filterMap = new LinkedHashMap<>();
			}
			filterMap.put(filterId, processorFilter);
			
			//	type
			processorFilterTypeMap.put(code, filterMap);
			//	id
			processorFilterIdMap.put(filterId, processorFilter);
		}

	}

	@Override
	public <T> T getFilter(Class<T> t) {
		Filter annotation = t.getAnnotation(Filter.class);
		if(annotation != null) {
			String filterId = annotation.id();
			if(filterId == null || filterId.length() < 1) {
				filterId = t.getName();
			}
			return this.getFilter(filterId);
		}
		return null;
	}
	

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getFilter(String filterId) {
		ProcessorFilter<Context> filter = null;
		if(!processorFilterIdMap.isEmpty()) {
			filter = processorFilterIdMap.get(filterId);
		}
		return (T)filter;
	}
	
}
