package com.baker.test.dubbo.service;

public interface HelloService {

	User sayHelloUser(String name);

    User sayHelloUser(User user, String name);

    User sayHelloUser(User user);

}