package com.baker.gateway.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.baker.gateway.common.util.PropertiesUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 网关配置信息加载类
 *
 * 网关配置加载规则，高的优先级会覆盖掉低的优先级，
 * 优先级顺序如下
 * 运行参数(最高) ->  jvm参数  -> 环境变量  -> 配置文件  -> 内部GatewayConfig对象的默认属性值(最低);
 */
@Slf4j
public class GatewayConfigLoader {
    /**
     * 环境变量
     */
    private final static String CONFIG_ENV_PREFIEX = "GATEWAY_";
    /**
     * JVM参数
     */
    private final static String CONFIG_JVM_PREFIEX = "gateway.";
    /**
     * 配置文件
     */
    private final static String CONFIG_FILE = "gateway.properties";

    /**
     * 单例
     */
    private final static GatewayConfigLoader INSTANCE = new GatewayConfigLoader();
    /**
     * 配置信息存储
     */
    private GatewayConfig gatewayConfig = new GatewayConfig();

    private GatewayConfigLoader() {
    }

    public static GatewayConfigLoader getInstance() {
        return INSTANCE;
    }

    public static GatewayConfig getGatewayConfig() {
        return INSTANCE.gatewayConfig;
    }


    /**
     * 加载逻辑
     */
    public GatewayConfig load(String[] args) {
        //	1. 配置文件
        {
            InputStream is = GatewayConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (is != null) {
                Properties properties = new Properties();
                try {
                    properties.load(is);
                    PropertiesUtils.properties2Object(properties, gatewayConfig);
                } catch (IOException e) {
                    //	warn
                    log.warn("#GatewayConfigLoader# load config file: {} is error", CONFIG_FILE, e);
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        //	ignore
                    }
                }
            }
        }

        //	2. 环境变量
        {
            Map<String, String> env = System.getenv();
            Properties properties = new Properties();
            properties.putAll(env);
            PropertiesUtils.properties2Object(properties, gatewayConfig, CONFIG_ENV_PREFIEX);
        }

        //	3. jvm参数
        {
            Properties properties = System.getProperties();
            PropertiesUtils.properties2Object(properties, gatewayConfig, CONFIG_JVM_PREFIEX);
        }

        //	4. 运行参数: --xxx=xxx --enable=true --port=1234
        {
            if (args != null && args.length > 0) {
                Properties properties = new Properties();
                for (String arg : args) {
                    if (arg.startsWith("--") && arg.contains("=")) {
                        properties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
                    }
                }
                PropertiesUtils.properties2Object(properties, gatewayConfig);
            }
        }

        return gatewayConfig;
    }

}
