package com.baker.gateway.core.netty.processor.filter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.baker.gateway.common.util.ServiceLoader;
import com.baker.gateway.core.context.Context;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认过滤器工厂实现类
 */
@Slf4j
public class DefaultProcessorFilterFactory extends AbstractProcessorFilterFactory {

    private static class SingletonHolder {
        private static final DefaultProcessorFilterFactory INSTANCE = new DefaultProcessorFilterFactory();
    }

    public static DefaultProcessorFilterFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 构造方法：加载所有的ProcessorFilter子类的实现
     */
    @SuppressWarnings("unchecked")
    private DefaultProcessorFilterFactory() {
        //	SPI方式加载filter的集合 key=filterType value=filter集合
        Map<String, List<ProcessorFilter<Context>>> filterMap = Maps.newLinkedHashMap();

        //	通过ServiceLoader加载
        @SuppressWarnings("rawtypes")
        ServiceLoader<ProcessorFilter> serviceLoader = ServiceLoader.load(ProcessorFilter.class);

        for (ProcessorFilter<Context> filter : serviceLoader) {
            Filter annotation = filter.getClass().getAnnotation(Filter.class);
            if (annotation != null) {
                String filterType = annotation.value().getCode();
                List<ProcessorFilter<Context>> filterList = filterMap.get(filterType);
                if (filterList == null) {
                    filterList = new ArrayList<>();
                }
                filterList.add(filter);
                filterMap.put(filterType, filterList);
            }
        }

        //	java基础：枚举类循环也是有顺序的
        for (ProcessorFilterType filterType : ProcessorFilterType.values()) {
            List<ProcessorFilter<Context>> filterList = filterMap.get(filterType.getCode());
            if (filterList == null || filterList.isEmpty()) {
                continue;
            }
            // 根据Filter#order正序
            filterList.sort(Comparator.comparingInt(f -> f.getClass().getAnnotation(Filter.class).order()));
            try {
                super.buildFilterChain(filterType, filterList);
            } catch (Exception e) {
                //	ignore
                log.error("#DefaultProcessorFilterFactory.buildFilterChain# 网关过滤器加载异常, 异常信息为：{}!", e.getMessage(), e);
            }
        }

    }

    /**
     * 正常过滤器链条执行：pre + route + post
     *
     * @see ProcessorFilterFactory#doFilterChain(Context)
     */
    @Override
    public void doFilterChain(Context ctx) {
        try {
            defaultProcessorFilterChain.entry(ctx);
        } catch (Throwable e) {
            log.error("#DefaultProcessorFilterFactory.doFilterChain# ERROR MESSAGE: {}", e.getMessage(), e);
            //	设置异常
            ctx.setThrowable(e);

            //	执行doFilterChain显示抛出异常时，Context上下文的生命周期为，Context.TERMINATED
            if (ctx.isTerminated()) {
                // 恢复异常，走错误过滤器链条
                ctx.runned();
            }

            //	执行异常处理的过滤器链条
            doErrorFilterChain(ctx);
        }
    }

    /**
     * 异常过滤器链条执行：error + post
     *
     * @see ProcessorFilterFactory#doErrorFilterChain(Context)
     */
    @Override
    public void doErrorFilterChain(Context ctx) {
        try {
            errorProcessorFilterChain.entry(ctx);
        } catch (Throwable e) {
            log.error("#DefaultProcessorFilterFactory.doErrorFilterChain# ERROR MESSAGE: {}", e.getMessage(), e);
        }
    }

}
