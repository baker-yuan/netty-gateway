package com.baker.gateway.common.config;

import com.alibaba.fastjson.JSONObject;
import com.baker.gateway.common.constants.GatewayProtocol;
import com.baker.gateway.common.util.FastJsonConvertUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServiceDefinitionDeserializer extends StdDeserializer<ServiceDefinition> {
    public ServiceDefinitionDeserializer() {
        super(ServiceInvoker.class);
    }

    @Override
    public ServiceDefinition deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        //	填充serviceDefinition
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setServiceId(node.get("serviceId").asText(""));
        serviceDefinition.setBasePath(node.get("basePath").asText(""));
        serviceDefinition.setProtocol(node.get("protocol").asText(""));
        serviceDefinition.setVersion(node.get("version").asText(""));
        serviceDefinition.setEnable(node.get("enable").asBoolean(true));





        JsonNode invokerMapNode = node.get("invokerMap");
        Map<String, ServiceInvoker> invokerMap = new HashMap<>();
        switch (serviceDefinition.getProtocol()) {
            case GatewayProtocol.HTTP:
                for (Iterator<Map.Entry<String, JsonNode>> it = invokerMapNode.fields(); it.hasNext(); ) {
                    Map.Entry<String, JsonNode> field = it.next();
                    String path = field.getKey();
                    JsonNode value = field.getValue();
                    HttpServiceInvoker httpServiceInvoker = new HttpServiceInvoker();
                    httpServiceInvoker.setInvokerPath(value.get("invokerPath").asText(""));
                    httpServiceInvoker.setRuleId(value.get("ruleId").asInt(0));
                    httpServiceInvoker.setTimeout(value.get("timeout").asInt(3000));
                    invokerMap.put(path, httpServiceInvoker);
                }
                break;
            case GatewayProtocol.DUBBO:
                for (Iterator<Map.Entry<String, JsonNode>> it = invokerMapNode.fields(); it.hasNext(); ) {
                    Map.Entry<String, JsonNode> field = it.next();
                    String path = field.getKey();
                    JsonNode value = field.getValue();

                    DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
                    dubboServiceInvoker.setInvokerPath(value.get("invokerPath").asText(""));
                    dubboServiceInvoker.setRuleId(value.get("ruleId").asInt(0));
                    dubboServiceInvoker.setTimeout(value.get("timeout").asInt(3000));


                    invokerMap.put(path, dubboServiceInvoker);
                }
                break;
            default:
                break;
        }

        serviceDefinition.setInvokerMap(invokerMap);
        return serviceDefinition;
    }


}