package com.baker.gateway.etcd.core.test;

import java.nio.charset.Charset;

import org.junit.Test;

import com.baker.gateway.etcd.api.EtcdClient;
import com.baker.gateway.etcd.core.EtcdClientImpl;

import io.etcd.jetcd.KeyValue;

public class EtcdImplTest {

	@Test
	public void test() throws Exception {
		
		String registryAddress = "http://192.168.11.114:2379,http://192.168.11.115:2379,http://192.168.11.116:2379";
		
		EtcdClient etcdClient = new EtcdClientImpl(registryAddress, true);
		
		System.err.println("etcdClient: " + etcdClient);
		
		KeyValue keyValue = etcdClient.getKey("/test/key6");
		
		System.err.println("key: " + keyValue.getKey().toString(Charset.defaultCharset()) + ", value: " + keyValue.getValue().toString(Charset.defaultCharset()));
	}
	
	
}
