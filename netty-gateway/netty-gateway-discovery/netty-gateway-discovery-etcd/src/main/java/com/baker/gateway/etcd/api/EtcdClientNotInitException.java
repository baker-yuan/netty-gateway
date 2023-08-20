package com.baker.gateway.etcd.api;


public class EtcdClientNotInitException extends RuntimeException {

	private static final long serialVersionUID = -617743243793838282L;
	
	public EtcdClientNotInitException() {
		super();
	}
	
	public EtcdClientNotInitException(String message) {
		super(message);
	}

}
