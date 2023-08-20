package com.baker.gateway.common.util;

import junit.framework.TestCase;

public class NetUtilsTest extends TestCase {

    public void testGetLocalIp() {
        String localIp = NetUtils.getLocalIp();
        System.out.println(localIp);
    }
}