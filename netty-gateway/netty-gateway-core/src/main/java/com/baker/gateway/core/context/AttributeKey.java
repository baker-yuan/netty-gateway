package com.baker.gateway.core.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.common.config.ServiceInvoker;
import com.baker.gateway.common.enums.LoadBalanceStrategy;

/**
 * 属性上下文的抽象类，在其内部进行实现
 */
public abstract class AttributeKey<T> {

    private static final Map<String, AttributeKey<?>> namedMap = new HashMap<>();

	/**
	 *
	 */
    public static final AttributeKey<ServiceInvoker> HTTP_INVOKER = create(ServiceInvoker.class);

	/**
	 *
	 */
	public static final AttributeKey<ServiceInvoker> DUBBO_INVOKER = create(ServiceInvoker.class);

    /**
     * 存储所有服务实例的列表信息，负载均衡使用
     */
    public static final AttributeKey<Set<ServiceInstance>> MATCH_INSTANCES = create(Set.class);

    /**
     * 负载均衡选中的实例信息
     */
    public static final AttributeKey<ServiceInstance> LOAD_INSTANCE = create(ServiceInstance.class);

    /**
     * dubbo负载均衡的策略使用
     */
    public static final AttributeKey<LoadBalanceStrategy> DUBBO_LOAD_BALANCE_STRATEGY = create(LoadBalanceStrategy.class);

    /**
     * Dubbo请求附加参数透传
     */
    public static final AttributeKey<Map<String, String>> DUBBO_ATTACHMENT = create(Map.class);


    static {
        namedMap.put("HTTP_INVOKER", HTTP_INVOKER);
        namedMap.put("DUBBO_INVOKER", DUBBO_INVOKER);
        namedMap.put("MATCH_INSTANCES", MATCH_INSTANCES);
        namedMap.put("LOAD_INSTANCE", LOAD_INSTANCE);
        namedMap.put("DUBBO_LOAD_BALANCE_STRATEGY", DUBBO_LOAD_BALANCE_STRATEGY);
        namedMap.put("DUBBO_ATTACHMENT", DUBBO_ATTACHMENT);
    }

    public static AttributeKey<?> valueOf(String name) {
        return namedMap.get(name);
    }

    /**
     * 给我一个对象，转成对应的class类型
     *
     * @param value 真实的数据对象值
     */
    public abstract T cast(Object value);

    /**
     * 对外暴露创建AttributeKey
     *
     * @param valueClass 给我的对应的泛型类
     * @return AttributeKey -> SimpleAttributeKey
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> AttributeKey<T> create(final Class<? super T> valueClass) {
        return new SimpleAttributeKey(valueClass);
    }

    /**
     * 简单的属性Key转换类
     */
    public static class SimpleAttributeKey<T> extends AttributeKey<T> {

        private final Class<T> valueClass;

        SimpleAttributeKey(final Class<T> valueClass) {
            this.valueClass = valueClass;
        }

        @Override
        public T cast(Object value) {
            return valueClass.cast(value);
        }

        @Override
        public String toString() {
            if (valueClass != null) {
				return getClass().getName() + "<" + valueClass.getName() + ">";
            }
            return super.toString();
        }

    }

}
