package com.baker.gateway.common.constants;

/**
 * 网关缓冲区辅助类
 */
public interface GatewayBufferHelper {

	String FLUSHER = "FLUSHER";
	
	String MPMC = "MPMC";
	
	static boolean isMpmc(String bufferType) {
		return MPMC.equals(bufferType);
	}
	
	static boolean isFlusher(String bufferType) {
		return FLUSHER.equals(bufferType);
	}
	
}
