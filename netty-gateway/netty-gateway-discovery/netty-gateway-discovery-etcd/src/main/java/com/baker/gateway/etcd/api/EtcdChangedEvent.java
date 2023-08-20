package com.baker.gateway.etcd.api;

import io.etcd.jetcd.KeyValue;


public class EtcdChangedEvent {

	public static enum Type {
        PUT,
        DELETE,
        UNRECOGNIZED;
    }

    /**
     * 老值
     */
	private final KeyValue prevKeyValue;
    /**
     * 新值
     */
	private final KeyValue curtKeyValue;

    private final Type type;

    public EtcdChangedEvent(KeyValue prevKeyValue, KeyValue curtKeyValue, Type type) {
    	this.prevKeyValue = prevKeyValue;
        this.curtKeyValue = curtKeyValue;
        this.type = type;
    }

    public KeyValue getCurtKeyValue() {
		return curtKeyValue;
	}

	public KeyValue getPrevKeyValue() {
		return prevKeyValue;
	}

	public Type getType() {
        return type;
    }
    
}