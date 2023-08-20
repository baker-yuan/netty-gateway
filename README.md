

# 一、测试

## 1.0、查看数据

### 服务

```bash
curl --location --request GET 'http://127.0.0.1:9005/serviceDefinition/getList?prefixPath=netty-gateway-dev' \
--header 'Cookie: uid=1'
```

![test-etcd-service](./doc/img/test-etcd-service.png)



### 实例

```bash
curl --location --request GET 'http://127.0.0.1:9005/serviceInstance/getList?prefixPath=netty-gateway-dev&uniqueId=hello:1.0.0' \
--header 'Cookie: uid=1'
```

![test-decd-instance](./doc/img/test-etcd-instance.png)



### 规则

```bash
curl --location --request GET 'http://127.0.0.1:9005/rule/getList?prefixPath=netty-gateway-dev' \
--header 'Cookie: uid=1'
```

![test-etcd-rule](./doc/img/test-etcd-rule.png)





### 调用方法列表

```bash
curl --location --request GET 'http://127.0.0.1:9005/serviceInvoker/getListByUniqueId?prefixPath=netty-gateway-dev&uniqueId=hello:1.0.0' \
--header 'Cookie: uid=1'
```





## 1.1、添加规则

```bash
curl --location --request POST 'http://127.0.0.1:9005/rule/add' \
--header 'uniqueId: hello:1.0.0' \
--header 'Cookie: uid=1; uid=1' \
--header 'Content-Type: application/json' \
--data-raw '{
    "filterConfigs": [
        {
            "config": "{\"loggable\":\"true\"}",
            "id": "defaultErrorFilter"
        },
        {
            "config": "{\"loggable\":\"true\",\"loadBalanceStrategy\":\"RANDOM\"}",
            "id": "loadBalancePreFilter"
        },
        {
            "config": "{\"loggable\":\"true\"}",
            "id": "httpRouteFilter"
        },
        {
            "config": "{\"loggable\":\"true\",\"timeout\":\"4000\"}",
            "id": "timeoutPreFilter"
        }
    ],
    "id": "1",
    "name": "测试",
    "order": 1,
    "protocol": "http",
    "prefixPath": "netty-gateway-dev"
}'
```



## 1.2、服务绑定规则

```bash
curl --location --request GET 'http://127.0.0.1:9005/serviceInvoker/bindingRuleId?uniqueId=hello:1.0.0&invokerPath=/testGet&ruleId=1&prefixPath=netty-gateway-dev' \
--header 'uniqueId: hello:1.0.0' \
--header 'Cookie: uid=1; uid=1; uid=1' \
--header 'Content-Type: application/json' \
--data-raw ''
```



## 1.3、测试下游服务

```bash
curl --location --request GET 'http://127.0.0.1:8083/testGet'
```



## 1.4、测试网关

```bash
curl --location --request GET 'http://127.0.0.1:8888/testGet' \
--header 'uniqueId: hello:1.0.0' \
--header 'Cookie: uid=1'
```

