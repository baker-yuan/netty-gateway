package com.baker.gateway.common.concurrent.queue.mpmc;

/**
 * Linux Intel CacheLine Size 64
 */
public class Contended {

    public static final int CACHE_LINE = Integer.getInteger("Intel.CacheLineSize", 64); // bytes

}
