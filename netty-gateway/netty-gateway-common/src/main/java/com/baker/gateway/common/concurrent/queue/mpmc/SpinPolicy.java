package com.baker.gateway.common.concurrent.queue.mpmc;

public enum SpinPolicy {
    WAITING,
    BLOCKING,
    SPINNING;
}