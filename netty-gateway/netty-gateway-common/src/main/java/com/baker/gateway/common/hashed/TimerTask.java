package com.baker.gateway.common.hashed;

public interface TimerTask {

    void run(Timeout timeout) throws Exception;

}