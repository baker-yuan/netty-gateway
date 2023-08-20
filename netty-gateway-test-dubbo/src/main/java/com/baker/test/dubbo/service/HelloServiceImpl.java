package com.baker.test.dubbo.service;

import com.baker.gateway.client.GatewayInvoker;
import com.baker.gateway.client.GatewayService;
import com.baker.gateway.client.GatewayProtocol;
import org.apache.dubbo.config.annotation.Service;


@GatewayService(serviceId = "say", patternPath = "/say*/**", protocol = GatewayProtocol.DUBBO)
@Service(timeout = 3000)
public class HelloServiceImpl implements HelloService {

    private volatile int count;

    @GatewayInvoker(path = "/sayHelloUser/a")
    @Override
    public User sayHelloUser(User user) {
        //System.err.println("1 sayHelloUser(User user): "
        //			+ FastJsonConvertUtil.convertObjectToJSON(user));
        return user;
    }

    @GatewayInvoker(path = "/sayHelloUser/b")
    @Override
    public User sayHelloUser(User user, String name) {
        user.setName(name);
        //System.err.println("2 sayHelloUser(User user, String name): "
        //		+ FastJsonConvertUtil.convertObjectToJSON(user));
        return user;
    }

    @GatewayInvoker(path = "/sayHelloUser/c")
    @Override
    public User sayHelloUser(String name) {
        User user = new User();
        user.setName(name);

        count++;
        if (count >= 100000) {
            System.err.println("<------ sayHelloUser.testParam收到请求, name:" + name + " ------>");
            count = 0;
        }
        //System.err.println("3 sayHelloUser(String name): "
        //		+ FastJsonConvertUtil.convertObjectToJSON(user));
        return user;
    }

}
