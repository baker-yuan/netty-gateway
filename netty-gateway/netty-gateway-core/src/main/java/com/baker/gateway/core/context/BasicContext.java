package com.baker.gateway.core.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.baker.gateway.common.util.CollectionUtils;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerContext;

/**
 * 基础上下文实现类
 */
public abstract class BasicContext implements Context {

	// 请求转换协议 http -> grpc/double...
	protected final String protocol;

	// netty context
	protected final ChannelHandlerContext nettyCtx;

	// 是否保持连接
	protected final boolean keepAlive;
	
	// 上下文的status标识
	protected volatile int status = RUNNING;
	
	// 保存所有的上下文参数集合
	protected final Map<AttributeKey<?>, Object> attributes = new HashMap<>();
	
	// 在请求过程中出现异常则设置异常对象
	protected Throwable throwable;
	
	// 定义是否已经释放请求资源
	protected final AtomicBoolean requestReleased = new AtomicBoolean(false);
	
	// 存放回调函数的集合
	protected List<Consumer<Context>> completedCallbacks = null;
	
	/**
	 * 	SR(Server[netty-gateway-Core] Received):	服务器接收到网络请求
	 * 	SS(Server[netty-gateway-Core] Send):		服务器写回请求
	 * 	RS(Route Send):						客户端发送请求
	 * 	RR(Route Received): 				客户端收到请求
	 */
	protected long SRTime;
	protected long SSTime;
	protected long RSTime;
	protected long RRTime;
	
	
	public BasicContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
		this.protocol = protocol;
		this.nettyCtx = nettyCtx;
		this.keepAlive = keepAlive;
	}
	
	@Override
	public String getProtocol() {
		return this.protocol;
	}
	
	@Override
	public ChannelHandlerContext getNettyCtx() {
		return this.nettyCtx;
	}
	
	@Override
	public boolean isKeepAlive() {
		return this.keepAlive;
	}
	
	@Override
	public void runned() {
		status = RUNNING;
	}
	
	@Override
	public void writtened(){
		status = WRITTEN;
	}

	@Override
	public void completed(){
		status = COMPLETED;
	}
	
	@Override
	public void terminated(){
		status = TERMINATED;
	}
	
	@Override
	public boolean isRunning(){
		return status == RUNNING;
	}
	
	@Override
	public boolean isWrittened(){
		return status == WRITTEN;
	}
	
	@Override
	public boolean isCompleted(){
		return status == COMPLETED;
	}
	
	@Override
	public boolean isTerminated(){
		return status == TERMINATED;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(AttributeKey<T> key) {
		return (T) attributes.get(key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T putAttribute(AttributeKey<T> key, T value) {
		return (T) attributes.put(key, value);
	}
	
	@Override
	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	@Override
	public Throwable getThrowable() {
		return this.throwable;
	}
	
	@Override
	public void releaseRequest() {
		this.requestReleased.compareAndSet(false, true);
	}
	
	@Override
	public void completedCallback(Consumer<Context> consumer) {
		if (this.completedCallbacks == null) {
			this.completedCallbacks = Lists.newArrayList();
		}
		this.completedCallbacks.add(consumer);
	}
	
	@Override
	public void invokeCompletedCallback() {
		if (CollectionUtils.isEmpty(this.completedCallbacks)) {
			return;
		}
		this.completedCallbacks.forEach(call -> call.accept(this));
	}
	
	@Override
    public long getSRTime() {
		return SRTime;
	}
	
	@Override
	public void setSRTime(long SRTime) {
		this.SRTime = SRTime;
	}
	
	@Override
	public long getSSTime() {
		return SSTime;
	}
	
	@Override
	public void setSSTime(long SSTime) {
		this.SSTime = SSTime;
	}
	
	@Override
	public long getRSTime() {
		return RSTime;
	}
	
	@Override
	public void setRSTime(long RSTime) {
		this.RSTime = RSTime;
	}
	
	@Override
	public long getRRTime() {
		return this.RRTime;
	}
	
	@Override
	public void setRRTime(long RRTime) {
		this.RRTime = RRTime;
	}
	
}
