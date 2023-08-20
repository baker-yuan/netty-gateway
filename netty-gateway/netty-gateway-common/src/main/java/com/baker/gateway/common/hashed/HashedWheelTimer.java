package com.baker.gateway.common.hashed;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;

/**
 * 时间轮
 */
@Slf4j
public class HashedWheelTimer implements Timer {

    private static final char PACKAGE_SEPARATOR_CHAR = '.';

    // 	实例数 静态变量共享
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    
    //	
    private static final AtomicBoolean WARNED_TOO_MANY_INSTANCES = new AtomicBoolean();
    
    //	实例数最多 64
    private static final int INSTANCE_COUNT_LIMIT = 64;
    
    
    private static final ResourceLeakDetector<HashedWheelTimer> leakDetector = ResourceLeakDetectorFactory.instance()
            .newResourceLeakDetector(HashedWheelTimer.class, 1);
    
    // 	netty 资源泄露探测
    private static final AtomicIntegerFieldUpdater<HashedWheelTimer> WORKER_STATE_UPDATER =
            AtomicIntegerFieldUpdater
            .newUpdater(HashedWheelTimer.class, "workerState");

    private final ResourceLeakTracker<HashedWheelTimer> leak;
    
    // 	工作线程核心实现RUN方法
    private final Worker worker = new Worker();
    
    // 	工作线程引用
    private final Thread workerThread;

    // 	工作线程状态：初始化
    public static final int WORKER_STATE_INIT = 0;
    //	工作线程状态：已启动
    public static final int WORKER_STATE_STARTED = 1;
    //	工作线程状态：已关闭
    public static final int WORKER_STATE_SHUTDOWN = 2;
    
    // 	工作线程状态volatile 变量保存内存可见性，配合 AtomicIntegerFieldUpdater 原子字段更新类进行 CAS 操作使用
    @SuppressWarnings({ "unused"})
    private volatile int workerState; // 0 - init, 1 - started, 2 - shut down

    //	时钟走一格的时间间隔，刻度
    private final long tickDuration;
    
    // 	wheel一个时间轮，其实就是一个环形数组，数组中的每个元素代表的就是未来的某些时间片段上需要执行的定时任务的集合；
    //	这里需要注意的就是不是某个时间而是某些时间：因为比方说时间轮上的大小是10，时间间隔是1s，那么1s、11s的要执行的定时任务都会在index为1的格子上
    private final HashedWheelBucket[] wheel;
    
    //	掩码：用于位运算计算索引位置 获取HashedWheelBucket
    private final int mask;
    
    // 	闭锁 用于同步启动的操作
    private final CountDownLatch startTimeInitialized = new CountDownLatch(1);
    
    //	执行任务mpsc队列：HashedWheelTimeout时间轮超时任务封装，无界队列
    private final Queue<HashedWheelTimeout> timeouts = PlatformDependent.newMpscQueue();
    
    // 	已经取消mpsc队列：用于清理资源、回收
    private final Queue<HashedWheelTimeout> cancelledTimeouts = PlatformDependent.newMpscQueue();
    
    //	目前执行任务数
    private final AtomicLong pendingTimeouts = new AtomicLong(0);
    
    // 	最大执行任务数
    private final long maxPendingTimeouts;
    
    // 	开始时间 volatile 变量保存内存可见性
    private volatile long startTime;

    public HashedWheelTimer() {
        this(Executors.defaultThreadFactory());
    }

    public HashedWheelTimer(long tickDuration, TimeUnit unit) {
        this(Executors.defaultThreadFactory(), 
        		tickDuration,
        		unit);
    }

    public HashedWheelTimer(long tickDuration, TimeUnit unit, 
    		int ticksPerWheel) {
        this(Executors.defaultThreadFactory(), 
        		tickDuration, 
        		unit, 
        		ticksPerWheel);
    }

    public HashedWheelTimer(ThreadFactory threadFactory) {
        this(threadFactory,
        		100,
        		TimeUnit.MILLISECONDS);
    }

    public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, 
    		TimeUnit unit) {
        this(threadFactory, tickDuration, unit, 512);
    }

    public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, 
    		TimeUnit unit, int ticksPerWheel) {
        this(threadFactory,
        		tickDuration,
        		unit, 
        		ticksPerWheel,
        		true);
    }

    public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, 
    		int ticksPerWheel, boolean leakDetection) {  
        this(threadFactory,
        		tickDuration, 
        		unit, 
        		ticksPerWheel, 
        		leakDetection,
        		-1);
    }

    /**
     * <B>构造方法</B>HashedWheelTimer<BR>
     * @param threadFactory
     * @param tickDuration		单个时间间隔（刻度）
     * @param unit				时间单位
     * @param ticksPerWheel		槽位数（格子数量）
     * @param leakDetection
     * @param maxPendingTimeouts
     */
    public HashedWheelTimer(
            ThreadFactory threadFactory,
            long tickDuration, TimeUnit unit, int ticksPerWheel, 
            boolean leakDetection,
            long maxPendingTimeouts) {

    	//	前置检查
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (tickDuration <= 0) {
            throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);
        }
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }

        // 	Normalize ticksPerWheel to power of two and initialize the wheel.
        // 	创建时钟格子 也就是槽位
        wheel = createWheel(ticksPerWheel);
        //	power of 2 的掩码
        mask = wheel.length - 1;

        // 	Convert tickDuration to nanos.	转换时间间隔到纳秒
        this.tickDuration = unit.toNanos(tickDuration);

        // 	Prevent overflow. 防止溢出，每一格的时间大于等于long最大值/格子数时候
        if (this.tickDuration >= Long.MAX_VALUE / wheel.length) {
            throw new IllegalArgumentException(String.format(
                    "tickDuration: %d (expected: 0 < tickDuration in nanos < %d",
                    tickDuration, Long.MAX_VALUE / wheel.length));
        }
        //	创建worker线程
        workerThread = threadFactory.newThread(worker);

        // 	泄露检测
        leak = leakDetection || !workerThread.isDaemon() ? leakDetector.track(this) : null;

        //	设置最大等待任务数，默认-1
        this.maxPendingTimeouts = maxPendingTimeouts;

        // 	限制timer的实例数，避免过多的timer线程反而影响性能
        if (INSTANCE_COUNTER.incrementAndGet() > INSTANCE_COUNT_LIMIT &&
            WARNED_TOO_MANY_INSTANCES.compareAndSet(false, true)) {
            reportTooManyInstances();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            // This object is going to be GCed and it is assumed the ship has sailed to do a proper shutdown. If
            // we have not yet shutdown then we want to make sure we decrement the active instance count.
            if (WORKER_STATE_UPDATER.getAndSet(this, WORKER_STATE_SHUTDOWN) != WORKER_STATE_SHUTDOWN) {
                INSTANCE_COUNTER.decrementAndGet();
            }
        }
    }

    private static HashedWheelBucket[] createWheel(int ticksPerWheel) {
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException(
                    "ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }
        if (ticksPerWheel > 1073741824) {
            throw new IllegalArgumentException(
                    "ticksPerWheel may not be greater than 2^30: " + ticksPerWheel);
        }

        //	槽位效验：时钟格子数必须是2的n次方, 因为获取槽位或者时间格子位置是使用位运算,位运算& 比取模mod效率高
        ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);
        HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
        for (int i = 0; i < wheel.length; i ++) {
            wheel[i] = new HashedWheelBucket();
        }
        return wheel;
    }

    private static int normalizeTicksPerWheel(int ticksPerWheel) {
        int normalizedTicksPerWheel = 1;
        while (normalizedTicksPerWheel < ticksPerWheel) {
            normalizedTicksPerWheel <<= 1;
        }
        return normalizedTicksPerWheel;
    }

    /**
     * Starts the background thread explicitly.  The background thread will
     * start automatically on demand even if you did not call this method.
     *
     * @throws IllegalStateException if this timer has been
     *                               {@linkplain #stop() stopped} already
     */
    public void start() {
    	// 	获取当前工作线程状态，开启时间轮
        switch (WORKER_STATE_UPDATER.get(this)) {
            case WORKER_STATE_INIT:
                if (WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_INIT, WORKER_STATE_STARTED)) {
                    workerThread.start();
                }
                break;
            case WORKER_STATE_STARTED:
                break;
            case WORKER_STATE_SHUTDOWN:
                throw new IllegalStateException("cannot be started once stopped");
            default:
                throw new Error("Invalid WorkerState");
        }

        // Wait until the startTime is initialized by the worker.
        while (startTime == 0) {
            try {
            	//	同步等待workerThread线程启动成功
                startTimeInitialized.await();
            } catch (InterruptedException ignore) {
                // Ignore - it will be ready very soon.
            }
        }
    }

    @Override
    public Set<Timeout> stop() {
    	// 	如果当前线程为工作线程，不允许操作
        if (Thread.currentThread() == workerThread) {
            throw new IllegalStateException(
                    HashedWheelTimer.class.getSimpleName() +
                            ".stop() cannot be called from " +
                            TimerTask.class.getSimpleName());
        }

        // 	CAS 设置成为关闭状态 不成功时候
        if (!WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_STARTED, WORKER_STATE_SHUTDOWN)) {
            // workerState can be 0 or 2 at this moment - let it always be 2.
            if (WORKER_STATE_UPDATER.getAndSet(this, WORKER_STATE_SHUTDOWN) != WORKER_STATE_SHUTDOWN) {
                INSTANCE_COUNTER.decrementAndGet();
                //	清空引用
                if (leak != null) {
                    boolean closed = leak.close(this);
                    assert closed;
                }
            }

            return Collections.emptySet();
        }

        try {
            boolean interrupted = false;
            while (workerThread.isAlive()) {
            	// 	中断 work 线程
                workerThread.interrupt();
                try {
                    workerThread.join(100);
                } catch (InterruptedException ignored) {
                    interrupted = true;
                }
            }

            if (interrupted) {
            	// 	如果 work 线程出现中断异常，当前线程进行中断操作
                Thread.currentThread().interrupt();
            }
        } finally {
        	// 	减少实例数
            INSTANCE_COUNTER.decrementAndGet();
            //	清空引用
            if (leak != null) {
                boolean closed = leak.close(this);
                assert closed;
            }
        }
        //	将没有处理完的任务返回
        return worker.unprocessedTimeouts();
    }

    /**
     * 添加一个延迟任务
     *
     * @see Timer#newTimeout(TimerTask, long, java.util.concurrent.TimeUnit)
     */
    @Override
    public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }

        // 	正在执行任务数 + 1
        long pendingTimeoutsCount = pendingTimeouts.incrementAndGet();

        if (maxPendingTimeouts > 0 && pendingTimeoutsCount > maxPendingTimeouts) {
            pendingTimeouts.decrementAndGet();
            throw new RejectedExecutionException("Number of pending timeouts ("
                + pendingTimeoutsCount + ") is greater than or equal to maximum allowed pending "
                + "timeouts (" + maxPendingTimeouts + ")");
        }

        //	启动方法
        start();

        // Add the timeout to the timeout queue which will be processed on the next tick.
        // During processing all the queued HashedWheelTimeouts will be added to the correct HashedWheelBucket.
        //	计算截止时间：(currentTime + delayTime) - startTime 
        long deadline = System.nanoTime() + unit.toNanos(delay) - startTime;

        // Guard against overflow.
        if (delay > 0 && deadline < 0) {
            deadline = Long.MAX_VALUE;
        }
        //	新增时间轮任务
        HashedWheelTimeout timeout = new HashedWheelTimeout(this, task, deadline);
        //	将执行任务入队列
        timeouts.add(timeout);
        return timeout;
    }

    /**
     * Returns the number of pending timeouts of this {@link Timer}.
     */
    public long pendingTimeouts() {
        return pendingTimeouts.get();
    }

    private static void reportTooManyInstances() {
        String resourceType = simpleClassName(HashedWheelTimer.class);
        log.error("You are creating too many " + resourceType + " instances. " +
                resourceType + " is a shared resource that must be reused across the JVM," +
                "so that only a few instances are created.");
    }
    
    /**
     * The shortcut to {@link #simpleClassName(Class) simpleClassName(o.getClass())}.
     */
    public static String simpleClassName(Object o) {
        if (o == null) {
            return "null_object";
        } else {
            return simpleClassName(o.getClass());
        }
    }

    /**
     * Generates a simplified name from a {@link Class}.  Similar to {@link Class#getSimpleName()}, but it works fine
     * with anonymous classes.
     */
    public static String simpleClassName(Class<?> clazz) {
        String className = ObjectUtil.checkNotNull(clazz, "clazz").getName();
        final int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (lastDotIdx > -1) {
            return className.substring(lastDotIdx + 1);
        }
        return className;
    }

    /**
     * 工作任务：单线程用于处理所有的定时任务，它会在每个tick执行一个bucket中所有的定时任务，以及一些其他的操作
     * ，意味着定时任务不能有较大的阻塞和耗时，不然就会影响定时任务执行的准时性和有效性
     */
    private final class Worker implements Runnable {
    	
    	//	未完成集合
        private final Set<Timeout> unprocessedTimeouts = new HashSet<Timeout>();

        //	转过格子的次数
        //	tick: 工作线程当前运行的tick数，每一个tick代表worker线程当前的一次工作时间
        private long tick;

        @Override
        public void run() {
            // Initialize the startTime.
            startTime = System.nanoTime();
            if (startTime == 0) {
                // We use 0 as an indicator for the uninitialized value here, so make sure it's not 0 when initialized.
                startTime = 1;
            }

            // Notify the other threads waiting for the initialization at start().
            startTimeInitialized.countDown();

            do {
            	// 	waitForNextTick 方法主要是计算下次tick的时间，然后sleep到下次tick，返回当前时间
                final long deadline = waitForNextTick();
                // 	大于0说明休眠时间已经结束（转完这个格子）
                if (deadline > 0) {
                	// 	 获取下一个bucket的index，即当前tick mod mask
                    int idx = (int) (tick & mask);
                    // 	处理掉已取消的任务
                    processCancelledTasks();
                    //	获取当前要处理的bucket
                    HashedWheelBucket bucket = wheel[idx];
                    // 	将待处理的任务移动到它该去的bucket去
                    transferTimeoutsToBuckets();
                    // 	处理掉当前bucket的所有到期定时任务，传递当前时间
                    bucket.expireTimeouts(deadline);
                    // 	转动格子数 + 1
                    tick++;
                }
            } while (WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == WORKER_STATE_STARTED);

            // 	Fill the unprocessedTimeouts so we can return them from stop() method.
            // 	开始收集所有槽位里面未完成任务：遍历所有的bucket，将还没来得及处理的任务全部清理到unprocessedTimeouts中
            for (HashedWheelBucket bucket: wheel) {
                bucket.clearTimeouts(unprocessedTimeouts);
            }
            // 	遍历所有待处理并且还没取消的任务，添加到unprocessedTimeouts中
            for (;;) {
                HashedWheelTimeout timeout = timeouts.poll();
                if (timeout == null) {
                    break;
                }
                if (!timeout.isCancelled()) {
                    unprocessedTimeouts.add(timeout);
                }
            }
            // 	处理已经取消的任务
            processCancelledTasks();
        }

        /**
         * 将要处理的任务移动到对应的bucket位置上去
         */
        private void transferTimeoutsToBuckets() {
        	//	最多一次转移100000个待分发定时任务到它们对应的bucket内，不然如果有一个线程一直添加定时任务就能让工作线程活生生饿死
            // 	transfer only max. 100000 timeouts per tick to prevent a thread to stale the workerThread when it just
            // 	adds new timeouts in a loop.
        	//	从timeouts最多取出100000个任务（如果有10万个）
            for (int i = 0; i < 100000; i++) {
                HashedWheelTimeout timeout = timeouts.poll();
                if (timeout == null) {
                    // all processed
                    break;
                }
                if (timeout.state() == HashedWheelTimeout.ST_CANCELLED) {
                    // Was cancelled in the meantime.
                    continue;
                }
                // 	计算要经过多少个格子(刻度)才能执行到这个任务（返回格子数量）
                long calculated = timeout.deadline / tickDuration;
                // 	计算执行到这个任务还要经过多少圈（多少个周期，不包含当前周期）
                timeout.remainingRounds = (calculated - tick) / wheel.length;
                // 	如果这个任务我们取晚了，那就让他在这个周期执行
                final long ticks = Math.max(calculated, tick); // Ensure we don't schedule for past.
                // 	位运算取索引位：计算当前任务应该方到的bucket的位置
                int stopIndex = (int) (ticks & mask);
                // 	获取对应的Bucket 并放入对应的Bucket
                HashedWheelBucket bucket = wheel[stopIndex];
                //	将当前任务放入到这个格子中去（链表中去）
                bucket.addTimeout(timeout);
            }
        }

        private void processCancelledTasks() {
            for (;;) {
                HashedWheelTimeout timeout = cancelledTimeouts.poll();
                if (timeout == null) {
                    // all processed
                    break;
                }
                try {
                    timeout.remove();
                } catch (Throwable t) {
                    if (log.isWarnEnabled()) {
                        log.warn("An exception was thrown while process a cancellation task", t);
                    }
                }
            }
        }

        private long waitForNextTick() {
        	
        	//	计算下一个tick的deadline
            long deadline = tickDuration * (tick + 1);
            
            // 	循环直到当前时间来到了下一个tick
            for (;;) {
            	//	计算当前时间
                final long currentTime = System.nanoTime() - startTime;
                // 	计算需要sleep的毫秒数
                long sleepTimeMs = (deadline - currentTime + 999999) / 1000000;

                if (sleepTimeMs <= 0) {
                    if (currentTime == Long.MIN_VALUE) {
                        return -Long.MAX_VALUE;
                    } else {
                        return currentTime;
                    }
                }

                // Check if we run on windows, as if thats the case we will need
                // to round the sleepTime as workaround for a bug that only affect
                // the JVM if it runs on windows.
                //
                // See https://github.com/netty/netty/issues/356
                if (PlatformDependent.isWindows()) {
                    sleepTimeMs = sleepTimeMs / 10 * 10;
                }

                // 	尝试sleep到下个tick的deadline
                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException ignored) {
                    if (WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == WORKER_STATE_SHUTDOWN) {
                        return Long.MIN_VALUE;
                    }
                }
            }
        }

        public Set<Timeout> unprocessedTimeouts() {
            return Collections.unmodifiableSet(unprocessedTimeouts);
        }
    }

    /**
     * Hashed时间轮超时类：代表一个定时任务，其中记录了自己的deadline、运行逻辑以及在bucket中需要呆满的圈数，
     * 比方说是1s和11s两个任务，他们对应的timeout中圈数就应该是0和1。 这样当遍历一个bucket中所有的timeout的时候，只要圈数为0说明就应该被执行，而其他情况就把圈数-1就好
     */
    private static final class HashedWheelTimeout implements Timeout {

    	//	任务状态
        private static final int ST_INIT = 0;		//	初始化
        private static final int ST_CANCELLED = 1;	//	取消
        private static final int ST_EXPIRED = 2;	//	过期
        
        // 	任务状态原子更新 io.netty.util.HashedWheelTimer.HashedWheelTimeout.state
        private static final AtomicIntegerFieldUpdater<HashedWheelTimeout> STATE_UPDATER =
                AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimeout.class, "state");

        // 	当前所属时间轮引用
        private final HashedWheelTimer timer;
        
        // 	任务内容
        private final TimerTask task;
        
        // 	过期截止时间
        private final long deadline;
        
        //	volatile 变量保证可见性
        private volatile int state = ST_INIT;

        // 	remainingRounds will be calculated and set by Worker.transferTimeoutsToBuckets() before the
        // 	HashedWheelTimeout will be added to the correct HashedWheelBucket.
        //	当前任务要等待的圈数
        long remainingRounds;

        // This will be used to chain timeouts in HashedWheelTimerBucket via a double-linked-list.
        // As only the workerThread will act on it there is no need for synchronization / volatile.
        HashedWheelTimeout next;
        HashedWheelTimeout prev;

        // The bucket to which the timeout was added
        HashedWheelBucket bucket;

        HashedWheelTimeout(HashedWheelTimer timer, TimerTask task, long deadline) {
            this.timer = timer;
            this.task = task;
            this.deadline = deadline;
        }

        @Override
        public Timer timer() {
            return timer;
        }

        @Override
        public TimerTask task() {
            return task;
        }

        @Override
        public boolean cancel() {
            // only update the state it will be removed from HashedWheelBucket on next tick.
            if (!compareAndSetState(ST_INIT, ST_CANCELLED)) {
                return false;
            }
            // If a task should be canceled we put this to another queue which will be processed on each tick.
            // So this means that we will have a GC latency of max. 1 tick duration which is good enough. This way
            // we can make again use of our MpscLinkedQueue and so minimize the locking / overhead as much as possible.
            timer.cancelledTimeouts.add(this);
            return true;
        }

        void remove() {
            HashedWheelBucket bucket = this.bucket;
            if (bucket != null) {
                bucket.remove(this);
            } else {
                timer.pendingTimeouts.decrementAndGet();
            }
        }

        public boolean compareAndSetState(int expected, int state) {
            return STATE_UPDATER.compareAndSet(this, expected, state);
        }

        public int state() {
            return state;
        }

        @Override
        public boolean isCancelled() {
            return state() == ST_CANCELLED;
        }

        @Override
        public boolean isExpired() {
            return state() == ST_EXPIRED;
        }

        public void expire() {
        	//	设置当前任务为过期
            if (!compareAndSetState(ST_INIT, ST_EXPIRED)) {
                return;
            }

            try {
            	//	执行具体的业务逻辑
                task.run(this);
            } catch (Throwable t) {
                if (log.isWarnEnabled()) {
                    log.warn("An exception was thrown by " + TimerTask.class.getSimpleName() + '.', t);
                }
            }
        }

        @Override
        public String toString() {
            final long currentTime = System.nanoTime();
            long remaining = deadline - currentTime + timer.startTime;

            StringBuilder buf = new StringBuilder(192)
               .append(simpleClassName(this))
               .append('(')
               .append("deadline: ");
            if (remaining > 0) {
                buf.append(remaining)
                   .append(" ns later");
            } else if (remaining < 0) {
                buf.append(-remaining)
                   .append(" ns ago");
            } else {
                buf.append("now");
            }

            if (isCancelled()) {
                buf.append(", cancelled");
            }

            return buf.append(", task: ")
                      .append(task())
                      .append(')')
                      .toString();
        }
    }

    /**
     * Bucket that stores HashedWheelTimeouts. These are stored in a linked-list like datastructure to allow easy
     * removal of HashedWheelTimeouts in the middle. Also the HashedWheelTimeout act as nodes themself and so no
     * extra object creation is needed.
     */
    private static final class HashedWheelBucket {
        // Used for the linked-list datastructure
    	
    	//	头结点
        private HashedWheelTimeout head;
        //	尾节点
        private HashedWheelTimeout tail;

        /**
         * Add {@link HashedWheelTimeout} to this bucket.
         */
        public void addTimeout(HashedWheelTimeout timeout) {
            assert timeout.bucket == null;
            timeout.bucket = this;
            if (head == null) {
                head = tail = timeout;
            } else {
            	//	将链表的尾部指向当前任务对象
                tail.next = timeout;
                //	当前任务对象的前一个节点指向尾部节点
                timeout.prev = tail;
                //	将链表的尾部指向当前任务对象
                tail = timeout;
            }
        }

        /**
         * Expire all: 将当前节点的所有过期任务取出并执行
         */
        public void expireTimeouts(long deadline) {
        	
        	//	先获取当前格子中链表的头节点
            HashedWheelTimeout timeout = head;

            // 	process all timeouts
            while (timeout != null) {
            	//	先获头节点的下一个节点，后续用于赋值给头节点
                HashedWheelTimeout next = timeout.next;
                //	如果剩余轮数小于等于0说明需要马上执行
                if (timeout.remainingRounds <= 0) {
                	 //	将它从当前链表中移除
                    next = remove(timeout);
                    //	如果到期截止时间小于等于当前时间节点
                    if (timeout.deadline <= deadline) {
                    	//	立即执行任务，并标记当前任务为过期
                        timeout.expire();
                    } else {
                        // 	The timeout was placed into a wrong slot. This should never happen.
                        throw new IllegalStateException(String.format(
                                "timeout.deadline (%d) > deadline (%d)", timeout.deadline, deadline));
                    }
                } 
                //	如果当前任务已经取消，则直接从链表中移除
                else if (timeout.isCancelled()) {
                	// 	任务已经取消，那就移除
                    next = remove(timeout);
                } else {
                	// 	否则轮数减一
                    timeout.remainingRounds --;
                }
                //	 将下一个节点赋值给time节点，用于继续链表的遍历
                timeout = next;
            }
        }

        public HashedWheelTimeout remove(HashedWheelTimeout timeout) {
            HashedWheelTimeout next = timeout.next;
            // remove timeout that was either processed or cancelled by updating the linked-list
            if (timeout.prev != null) {
                timeout.prev.next = next;
            }
            if (timeout.next != null) {
                timeout.next.prev = timeout.prev;
            }

            if (timeout == head) {
                // if timeout is also the tail we need to adjust the entry too
                if (timeout == tail) {
                    tail = null;
                    head = null;
                } else {
                    head = next;
                }
            } else if (timeout == tail) {
                // if the timeout is the tail modify the tail to be the prev node.
                tail = timeout.prev;
            }
            // null out prev, next and bucket to allow for GC.
            timeout.prev = null;
            timeout.next = null;
            timeout.bucket = null;
            timeout.timer.pendingTimeouts.decrementAndGet();
            return next;
        }

        /**
         * Clear this bucket and return all not expired / cancelled {@link Timeout}s.
         */
        public void clearTimeouts(Set<Timeout> set) {
            for (;;) {
                HashedWheelTimeout timeout = pollTimeout();
                if (timeout == null) {
                    return;
                }
                if (timeout.isExpired() || timeout.isCancelled()) {
                    continue;
                }
                set.add(timeout);
            }
        }

        private HashedWheelTimeout pollTimeout() {
            HashedWheelTimeout head = this.head;
            if (head == null) {
                return null;
            }
            HashedWheelTimeout next = head.next;
            if (next == null) {
                tail = this.head =  null;
            } else {
                this.head = next;
                next.prev = null;
            }

            // null out prev and next to allow for GC.
            head.next = null;
            head.prev = null;
            head.bucket = null;
            return head;
        }
    }
}
