package com.baker.gateway.core.helper;

import java.util.concurrent.CompletableFuture;

import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

/**
 * 异步的http辅助类：NettyHttpClient
 */
public class AsyncHttpHelper {

	private static final class SingletonHolder {
		private static final AsyncHttpHelper INSTANCE = new AsyncHttpHelper();
	}
	
	private AsyncHttpHelper() {
		
	}
	
	public static AsyncHttpHelper getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * 发送请求
	 */
	private AsyncHttpClient asyncHttpClient;
	
	public void initialized(AsyncHttpClient asyncHttpClient) {
		this.asyncHttpClient = asyncHttpClient;
	}
	
	public CompletableFuture<Response> executeRequest(Request request) {
		ListenableFuture<Response> future = asyncHttpClient.executeRequest(request);
		return future.toCompletableFuture();
	}
	
	public <T> CompletableFuture<T> executeRequest(Request request, AsyncHandler<T> handler) {
		ListenableFuture<T> future = asyncHttpClient.executeRequest(request, handler);
		return future.toCompletableFuture();
	}
	
}
