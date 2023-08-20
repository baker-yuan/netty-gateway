package com.baker.gateway.common.concurrent.queue.mpmc;

import java.util.concurrent.locks.ReentrantLock;

/**
 * use java sync to signal
 */
abstract class ConditionAbstract implements Condition {

    private final ReentrantLock queueLock = new ReentrantLock();

    private final java.util.concurrent.locks.Condition condition = queueLock.newCondition();

    /**
     * wake me when the condition is satisfied, or timeout
     *
     * @see Condition#awaitNanos(long)
     */
    @Override
    public void awaitNanos(final long timeout) throws InterruptedException {
        long remaining = timeout;
        queueLock.lock();
        try {
        	//	如果当前队列已经满了
            while(test() && remaining > 0) {
                remaining = condition.awaitNanos(remaining);
            }
        }
        finally {
            queueLock.unlock();
        }
    }

    /**
     * wake if signal is called, or wait indefinitely
     *
     * @see Condition#await()
     */
    @Override
    public void await() throws InterruptedException {
        queueLock.lock();
        try {
            while(test()) {
                condition.await();
            }
        }
        finally {
            queueLock.unlock();
        }
    }

    /**
     * tell threads waiting on condition to wake up
     *
     * @see Condition#signal()
     */
    @Override
    public void signal() {
        queueLock.lock();
        try {
            condition.signalAll();
        }
        finally {
            queueLock.unlock();
        }

    }

}