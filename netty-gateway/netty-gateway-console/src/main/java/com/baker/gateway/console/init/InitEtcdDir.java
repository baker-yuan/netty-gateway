package com.baker.gateway.console.init;

import com.baker.gateway.common.constants.BasicConst;
import com.baker.gateway.common.util.ServiceLoader;
import com.baker.gateway.discovery.api.RegistryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import com.baker.gateway.discovery.api.Registry;

// https://github.com/baker-yuan/netty-gateway/blob/3b0376df665959c325db7d5e8f217059528ef353/netty-gateway/netty-gateway-client/src/main/java/com/baker/gateway/client/core/AbstractClientRegisterManager.java
public class InitEtcdDir implements CommandLineRunner {

    @Value("${gateway.console.registryAddress}")
    private String registryAddress;
    @Value("${gateway.console.namespace}")
    private String namespace;

    @Value("${gateway.console.env}")
    private String env;

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
     * 注册服务接口
     */
    private RegistryService registryService;


    public InitEtcdDir() throws Exception {
        //	1. 初始化加载注册中心对象
        ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
        RegistryService registryService = serviceLoader.iterator().next();
        registryService.initialized(registryAddress);
        this.registryService = registryService;

        //	2. 注册构建顶级目录结构
        generatorStructPath(Registry.PATH + namespace + BasicConst.BAR_SEPARATOR + env);
    }

    /**
     * 注册顶级结构目录路径，只需要构建一次即可
     */
    private void generatorStructPath(String path) throws Exception {
        /**
         * 	/netty-gateway-dev
         * 		/services
         * 			/serviceA  ==> ServiceDefinition
         * 			/serviceB
         * 		/instances
         * 			/serviceA/192.168.11.100:port	 ==> ServiceInstance
         * 			/serviceB/192.168.11.102:port
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

    @Override
    public void run(String... args) throws Exception {

    }
}
