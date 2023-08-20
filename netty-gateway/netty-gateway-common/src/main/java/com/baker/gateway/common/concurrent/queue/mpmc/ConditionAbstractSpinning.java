package com.baker.gateway.common.concurrent.queue.mpmc;

/**
 * 阻塞的自旋锁抽象类
 */
public abstract class ConditionAbstractSpinning implements Condition {

	/**
	 * on spinning waiting breaking on test and expires > timeNow
     *
	 * @see Condition#awaitNanos(long)
	 */
    @Override
    public void awaitNanos(final long timeout) throws InterruptedException {
        long timeNow = System.nanoTime();
        final long expires = timeNow+timeout;

        final Thread t = Thread.currentThread();

        while(test() && expires > timeNow && !t.isInterrupted()) {
            timeNow = System.nanoTime();
            Condition.onSpinWait();
        }

        if(t.isInterrupted()) {
            throw new InterruptedException();
        }
    }

    /**
     * on spinning waiting breaking on test
     *
     * @see Condition#await()
     */
    @Override
    public void await() throws InterruptedException {
        final Thread t = Thread.currentThread();

        while(test() && !t.isInterrupted()) {
            Condition.onSpinWait();
        }

        if(t.isInterrupted()) {
            throw new InterruptedException();
        }
    }

    @Override
    public void signal() {

    }
}
