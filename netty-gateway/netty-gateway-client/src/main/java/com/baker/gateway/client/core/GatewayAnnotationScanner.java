package com.baker.gateway.client.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.ServiceBean;

import com.baker.gateway.client.GatewayInvoker;
import com.baker.gateway.client.GatewayProtocol;
import com.baker.gateway.client.GatewayService;
import com.baker.gateway.client.support.dubbo.DubboConstants;
import com.baker.gateway.common.config.DubboServiceInvoker;
import com.baker.gateway.common.config.HttpServiceInvoker;
import com.baker.gateway.common.config.ServiceDefinition;
import com.baker.gateway.common.config.ServiceInvoker;
import com.baker.gateway.common.constants.BasicConst;

/**
 * 注解扫描类，用于扫描所有的用户定义的 @GatewayService 和 @GatewayInvoker
 */
public class GatewayAnnotationScanner {

    private GatewayAnnotationScanner() {
    }

    private static class SingletonHolder {
        static final GatewayAnnotationScanner INSTANCE = new GatewayAnnotationScanner();
    }

    public static GatewayAnnotationScanner getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 扫描传入的Bean对象，最终返回一个ServiceDefinition
     *
     * @param args 额外的参数选项，注册dubbo时需要使用ServiceBean
     * @return ServiceDefinition
     */
    public synchronized ServiceDefinition scanbuilder(Object bean, Object... args) {
        Class<?> clazz = bean.getClass();

        // 是否有GatewayService
        boolean isPresent = clazz.isAnnotationPresent(GatewayService.class);
        if (!isPresent) {
            return null;
        }

        GatewayService gatewayService = clazz.getAnnotation(GatewayService.class);
        String serviceId = gatewayService.serviceId();
        GatewayProtocol protocol = gatewayService.protocol();
        String basePath = gatewayService.basePath();
        String version = gatewayService.version();

        ServiceDefinition serviceDefinition = new ServiceDefinition();
        Map<String, ServiceInvoker> invokerMap = new HashMap<>();

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            GatewayInvoker gatewayInvoker = method.getAnnotation(GatewayInvoker.class);
            if (gatewayInvoker == null) {
                continue;
            }
            String path = gatewayInvoker.path();

            if (!path.startsWith(basePath)) {
                throw new IllegalArgumentException(String.format("url冲突，%s.%s GatewayInvoker#path 必须以 %s 开头", clazz.getName(), method.getName(), basePath));
            }

            switch (protocol) {
                case HTTP:
                    HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path, bean, method);
                    invokerMap.put(path, httpServiceInvoker);
                    break;
                case DUBBO:
                    ServiceBean<?> serviceBean = (ServiceBean<?>) args[0];
                    DubboServiceInvoker dubboServiceInvoker = createDubboServiceInvoker(path, serviceBean, method);
                    //	dubbo version reset for serviceDefinition version
                    String dubboVersion = dubboServiceInvoker.getVersion();
                    if (!StringUtils.isBlank(dubboVersion)) {
                        version = dubboVersion;
                    }
                    invokerMap.put(path, dubboServiceInvoker);
                    break;
                default:
                    break;
            }
        }
        //	设置属性
        serviceDefinition.setServiceId(serviceId);
        serviceDefinition.setBasePath(basePath);
        serviceDefinition.setVersion(version);
        serviceDefinition.setProtocol(protocol.getCode());
        serviceDefinition.setBasePath(basePath);
        serviceDefinition.setEnable(true);
        serviceDefinition.setInvokerMap(invokerMap);
        return serviceDefinition;
    }

    /**
     * 构建HttpServiceInvoker对象
     */
    private HttpServiceInvoker createHttpServiceInvoker(String path, Object bean, Method method) {
        HttpServiceInvoker httpServiceInvoker = new HttpServiceInvoker();
        httpServiceInvoker.setInvokerPath(path);
        return httpServiceInvoker;
    }

    /**
     * 构建DubboServiceInvoker对象
     */
    private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
        DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
        dubboServiceInvoker.setInvokerPath(path);

        String methodName = method.getName();
        String registerAddress = serviceBean.getRegistry().getAddress();
        String interfaceClass = serviceBean.getInterface();

        dubboServiceInvoker.setRegisterAddress(registerAddress);
        dubboServiceInvoker.setMethodName(methodName);
        dubboServiceInvoker.setInterfaceClass(interfaceClass);

        String[] parameterTypes = new String[method.getParameterCount()];
        Class<?>[] classes = method.getParameterTypes();
        for (int i = 0; i < classes.length; i++) {
            parameterTypes[i] = classes[i].getName();
        }
        dubboServiceInvoker.setParameterTypes(parameterTypes);

        Integer serviceTimeout = serviceBean.getTimeout();
        if (serviceTimeout == null || serviceTimeout == 0) {
            ProviderConfig providerConfig = serviceBean.getProvider();
            if (providerConfig != null) {
                Integer providerTimeout = providerConfig.getTimeout();
                if (providerTimeout == null || providerTimeout == 0) {
					serviceTimeout = DubboConstants.DUBBO_TIMEOUT;
                } else {
					serviceTimeout = providerTimeout;
                }
            }
        }
        dubboServiceInvoker.setTimeout(serviceTimeout);

        String dubboVersion = serviceBean.getVersion();
        dubboServiceInvoker.setVersion(dubboVersion);

        return dubboServiceInvoker;
    }

}
