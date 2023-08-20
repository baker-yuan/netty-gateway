package com.baker.gateway.core.rolling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;

import com.baker.gateway.common.util.Pair;


/**
 * RollingNumber
 */
public class RollingNumber {

	//	获取当前实际时间类
    private static final Time ACTUAL_TIME = new ActualTime();
    //	时间戳
    private final Time time;
    //	总时间
    final int timeInMilliseconds;
    //	桶数量
    final int numberOfBuckets;
    //	一个桶的时间窗口大小
    final int bucketSizeInMillseconds;
    //	唯一标识：uniqueKey
    final String uniqueKey;

    //	BucketCircularArray: 环形列表,内部由ListState(环形)存储Buckets,通过buckets.peekLast()获取尾端内容(Bucket)
    public final BucketCircularArray buckets;
    
    //	累积聚合对象
    private final CumulativeSum cumulativeSum;
    
    public RollingNumber(int timeInMilliseconds, int numberOfBuckets, String uniqueKey, 
    		BlockingQueue<Pair<String, Long>> blockingQueue) {
        this(ACTUAL_TIME, timeInMilliseconds, numberOfBuckets, uniqueKey, blockingQueue);
    }
    
    public RollingNumber(Time time, int timeInMilliseconds, int numberOfBuckets, String uniqueKey,
    		BlockingQueue<Pair<String, Long>> blockingQueue) {
        cumulativeSum = new CumulativeSum(uniqueKey, blockingQueue);
        this.time = time;
        this.timeInMilliseconds = timeInMilliseconds;
        this.numberOfBuckets = numberOfBuckets;

        if (timeInMilliseconds % numberOfBuckets != 0) {
            throw new IllegalArgumentException("The timeInMilliseconds must divide equally into numberOfBuckets. For example 1000/10 is ok, 1000/11 is not.");
        }
        //	一个窗口的Size大小
        this.bucketSizeInMillseconds = timeInMilliseconds / numberOfBuckets;
        //	创建一个数组
        buckets = new BucketCircularArray(numberOfBuckets);
        this.uniqueKey = uniqueKey;
    }

    /**
     * 就是通过事件：获取当前窗口，Bucket -> adderForCounterType[ordinal()]
     */
    public void increment(RollingNumberEvent type) {
        getCurrentBucket().getAdder(type).increment();
    }

    public void recordRT(int rt) {
        LongAdder longAdder = getCurrentBucket().getRTBottle().computeIfAbsent(rt, k -> new LongAdder());
        longAdder.increment();
    }

    public void add(RollingNumberEvent type, long value) {
        getCurrentBucket().getAdder(type).add(value);
    }

    public void updateRollingMax(RollingNumberEvent type, long value) {
        getCurrentBucket().getMaxUpdater(type).update(value);
    }

    //	TODO:
    public void reset() {
        // 	如果我们正在执行重置操作，这意味着lastBucket的统计数据将没有机会被CumulativeSum捕获，所以让我们在这里进行操作
        Bucket lastBucket = buckets.peekLast();
        if (lastBucket != null) {
        	//	CumulativeSum进行统计
            cumulativeSum.addBucket(lastBucket);
        }

        // 	清空数组操作
        buckets.clear();
    }

    public long getCumulativeSum(RollingNumberEvent type) {
    	//	最后一个未完成的窗口值 + 已经累计的总值
        return getValueOfLatestBucket(type) + cumulativeSum.get(type);
    }

    public long getRollingSum(RollingNumberEvent type) {
        Bucket lastBucket = getCurrentBucket();
        if (lastBucket == null)
            return 0;

        long sum = 0;
        for (Bucket b : buckets) {
            sum += b.getAdder(type).sum();
        }
        return sum;
    }

    public long getValueOfLatestBucket(RollingNumberEvent type) {
        Bucket lastBucket = getCurrentBucket();
        if (lastBucket == null)
            return 0;
        return lastBucket.get(type);
    }

    public long[] getValues(RollingNumberEvent type) {
        Bucket lastBucket = getCurrentBucket();
        if (lastBucket == null)
            return new long[0];

        //	获取整个数组bucketArray:
        Bucket[] bucketArray = buckets.getArray();

        long[] values = new long[bucketArray.length];
        int i = 0;
        //	按照类型进行累计
        for (Bucket bucket : bucketArray) {
            if (type.isCounter()) {
                values[i++] = bucket.getAdder(type).sum();
            } else if (type.isMaxUpdater()) {
                values[i++] = bucket.getMaxUpdater(type).max();
            }
        }
        return values;
    }

    public long getRollingMaxValue(RollingNumberEvent type) {
    	//	获取所有桶的value值
        long[] values = getValues(type);
        if (values.length == 0) {
            return 0;
        } else {
        	//	将其排序获取最大的值
            Arrays.sort(values);
            return values[values.length - 1];
        }
    }

    //	创建新同必须要加锁, 因为是一个符合性的操作 1 向数组中添加新的桶, 还要把之前的桶里的统计数据清空(历史数据)
    private final ReentrantLock newBucketLock = new ReentrantLock();
    
    public Bucket getCurrentBucket() {
    	
    	//	获取当前时间 
        long currentTime = time.getCurrentTimeInMillis();
        
        //	从数组buckets取出来最后一个Bucket
        Bucket currentBucket = buckets.peekLast();
        //	Bucket不为空, 当前时间小于窗口开始时间 + 一个桶的时间跨度, 则直接返回
        if (currentBucket != null && currentTime < currentBucket.windowStart + this.bucketSizeInMillseconds) {
            return currentBucket;
        }

        //	如果我们没有找到当前的桶, 那么我们必须要创建一个新的同并返回
        if (newBucketLock.tryLock()) {
            try {
            	//	如果没有最后一个桶
                if (buckets.peekLast() == null) {
                    // 	创建一个新的桶, 开始时间就是当前时间
                    Bucket newBucket = new Bucket(currentTime);
                    //	添加到数组尾部返回即可
                    buckets.addLast(newBucket);
                    return newBucket;
                } 
                //	这种情况就是当我们的数组元素都放满了, 需要利用环形数组的特性, 就是交替替换桶, 重复利用
                else {
                    // 	我们进入一个循环，以便它将创建尽可能多的桶以赶上当前时间
                    for (int i = 0; i < numberOfBuckets; i++) {
                        // 	我们找到了最后一个桶
                        Bucket lastBucket = buckets.peekLast();
                        //	1. 如果当前时间小于窗口开始时间 + 一个桶的时间跨度, 则直接返回该桶即可
                        if (currentTime < lastBucket.windowStart + this.bucketSizeInMillseconds) {
                            return lastBucket;
                        } 
                        //	3. 当前时间(所经过的时间)大于整个滚动计数器，所以我们想要清除它并从头开始
                        else if (currentTime - (lastBucket.windowStart + this.bucketSizeInMillseconds) > timeInMilliseconds) {
                        	//	执行重置操作
                        	reset();
                        	//	再一次获取当前桶, 重新获取当前时间并创建新的桶
                            return getCurrentBucket();
                        }
                        //	2. 如果我们已经错过了时间窗口, 则创建一个新的就可以了
                        //	距离上一次请求, 大于当前桶时间, 小于总窗口时间; 在此循环总桶数间追赶匹配的桶位
                        else { 
                        	//	新窗口的开始时间为上一个窗口的开始时间加上窗口的大小
                            buckets.addLast(new Bucket(lastBucket.windowStart + this.bucketSizeInMillseconds));
                            //	累计到cumulativeSum
                            cumulativeSum.addBucket(lastBucket);
                        }
                    }
                    // 	返回最新的一个桶
                    return buckets.peekLast();
                }
            } finally {
                newBucketLock.unlock();
            }
        }
        //	如果我们没有获得锁，一定是另一个线程创建下一个桶, 这里我们直接获取即可
        else {
        	//	直接返回最后一个桶
            currentBucket = buckets.peekLast();
            if (currentBucket != null) {
                return currentBucket;
            } else {
            	//	少数情况下，多个线程竞相创建第一个桶; 稍等一下，然后在另一个线程创建桶时使用递归
                try {
                    Thread.sleep(5);
                } catch (Exception e) {
                    // ignore
                }
                return getCurrentBucket();
            }
        }
    }

    public static interface Time {
        long getCurrentTimeInMillis();
    }

    private static class ActualTime implements Time {

        @Override
        public long getCurrentTimeInMillis() {
            return System.currentTimeMillis();
        }

    }

    public static class Bucket {
    	//	窗口开始时间
        final long windowStart;
        //	事件集合数组, 累积增量  	//		一个事件计数器就是一个：LongAdder
        final LongAdder[] adderForCounterType;
        //	事件集合数组, 累积更新
        final LongMaxUpdater[] updaterForCounterType;
        // key is RT, make the same RT merge
        final ConcurrentHashMap<Integer, LongAdder> rtBottle;

        Bucket(long startTime) {
            this.windowStart = startTime;

            // 	初始化事件集合数组
            adderForCounterType = new LongAdder[RollingNumberEvent.values().length];
            //	为计数器类型初始化 key: 类型, value: LongAdder
            for (RollingNumberEvent type : RollingNumberEvent.values()) {
                if (type.isCounter()) {
                    adderForCounterType[type.ordinal()] = new LongAdder();
                }
            }
            updaterForCounterType = new LongMaxUpdater[RollingNumberEvent.values().length];
            for (RollingNumberEvent type : RollingNumberEvent.values()) {
                if (type.isMaxUpdater()) {
                    updaterForCounterType[type.ordinal()] = new LongMaxUpdater();
                    // initialize to 0 otherwise it is Long.MIN_VALUE
                    updaterForCounterType[type.ordinal()].update(0);
                }
            }
            rtBottle = new ConcurrentHashMap<>();
        }

        long get(RollingNumberEvent type) {
            if (type.isCounter()) {
                return adderForCounterType[type.ordinal()].sum();
            }
            if (type.isMaxUpdater()) {
                return updaterForCounterType[type.ordinal()].max();
            }
            throw new IllegalStateException("Unknown type of event: " + type.name());
        }

        LongAdder getAdder(RollingNumberEvent type) {
            if (!type.isCounter()) {
                throw new IllegalStateException("Type is not a Counter: " + type.name());
            }
            return adderForCounterType[type.ordinal()];
        }

        LongMaxUpdater getMaxUpdater(RollingNumberEvent type) {
            if (!type.isMaxUpdater()) {
                throw new IllegalStateException("Type is not a MaxUpdater: " + type.name());
            }
            return updaterForCounterType[type.ordinal()];
        }

        //	返回Map集合rtBottle
        ConcurrentHashMap<Integer, LongAdder> getRTBottle() {
            return rtBottle;
        }

        long getWindowStart() {
            return windowStart;
        }

    }

    public static class CumulativeSum {
        //	事件集合数组, 累积增量
        final LongAdder[] adderForCounterType;
        //	事件集合数组, 累积更新
        final LongMaxUpdater[] updaterForCounterType;
        
        final String uniqueKey;
        
        final BlockingQueue<Pair<String, Long>> blockingQueue;
        
        CumulativeSum(String uniqueKey, BlockingQueue<Pair<String, Long>> blockingQueue) {

            // 	初始化事件集合数组
            this.uniqueKey = uniqueKey;
            this.blockingQueue = blockingQueue;
            
            adderForCounterType = new LongAdder[RollingNumberEvent.values().length];
            for (RollingNumberEvent type : RollingNumberEvent.values()) {
                if (type.isCounter()) {
                    adderForCounterType[type.ordinal()] = new LongAdder();
                }
            }
            updaterForCounterType = new LongMaxUpdater[RollingNumberEvent.values().length];
            for (RollingNumberEvent type : RollingNumberEvent.values()) {
                if (type.isMaxUpdater()) {
                    updaterForCounterType[type.ordinal()] = new LongMaxUpdater();
                    // initialize to 0 otherwise it is Long.MIN_VALUE
                    updaterForCounterType[type.ordinal()].update(0);
                }
            }
        }

        public void addBucket(Bucket lastBucket) {
            //	循环所有的事件类型
            for (RollingNumberEvent type : RollingNumberEvent.values()) {
                if (type.isCounter()) {
                	//	获取最后一个桶的统计值, 计数到对应的类型匹配的LongAdder
                    long sum = lastBucket.getAdder(type).sum();
                    getAdder(type).add(sum);
                    if (sum != 0) {
                    	System.err.println("========>>> QPS: " + sum);
                    	//	每次上报数据的时机
                    	if(blockingQueue != null) {
                    		blockingQueue.add(new Pair<String, Long>(type.name(), sum));
                    	}
                    }
                }
                if (type.isMaxUpdater()) {
                	//	获取最后一个桶的统计值, 计数到对应的类型匹配的LongMaxUpdater
                    long max = lastBucket.getMaxUpdater(type).max();
                    getMaxUpdater(type).update(max);
                    if (max != 0) {
                    	//	每次上报数据的时机
                    	if(blockingQueue != null) {
                    		blockingQueue.add(new Pair<String, Long>(type.name(), max));
                    	}
                    }
                }
            }
            String rtValue = rtBottleToString(lastBucket.getRTBottle());
            if (StringUtils.isNotEmpty(rtValue)) {
            	//	每次上报rt数据的时机
            }
        }
        
        private String rtBottleToString(ConcurrentHashMap<Integer, LongAdder> bottle) {
            if (bottle == null || bottle.size() == 0) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            bottle.forEach((key, value) -> sb.append(key).append(":").append(value.longValue()).append(","));
            if ((",").equals(sb.substring(sb.length() - 1, sb.length()))) {
                sb.delete(sb.length() - 1, sb.length());
            }
            return sb.toString();
        }

        long get(RollingNumberEvent type) {
            if (type.isCounter()) {
                return adderForCounterType[type.ordinal()].sum();
            }
            if (type.isMaxUpdater()) {
                return updaterForCounterType[type.ordinal()].max();
            }
            throw new IllegalStateException("Unknown type of event: " + type.name());
        }

        LongAdder getAdder(RollingNumberEvent type) {
            if (!type.isCounter()) {
                throw new IllegalStateException("Type is not a Counter: " + type.name());
            }
            return adderForCounterType[type.ordinal()];
        }

        LongMaxUpdater getMaxUpdater(RollingNumberEvent type) {
            if (!type.isMaxUpdater()) {
                throw new IllegalStateException("Type is not a MaxUpdater: " + type.name());
            }
            return updaterForCounterType[type.ordinal()];
        }

    }

    public static class BucketCircularArray implements Iterable<Bucket> {
    	
    	//	原子引用
        private final AtomicReference<ListState> state;
        //	数组长度 = numBuckets + 1
        private final int dataLength; // we don't resize, we always stay the same, so remember this
        //	桶数量
        private final int numBuckets;
        
        BucketCircularArray(int size) {
        	//	创建环形数组
            AtomicReferenceArray<Bucket> _buckets = new AtomicReferenceArray<>(size + 1); // + 1 as extra room for the add/remove;
            //	赋值引用
            state = new AtomicReference<>(new ListState(_buckets, 0, 0));
            //	数组长度
            dataLength = _buckets.length();
            //	桶数量 = 6
            numBuckets = size;
        }

        private class ListState {
        	
        	//	AtomicReferenceArray: 元素内容为Bucket
            private final AtomicReferenceArray<Bucket> data;
            
            //	长度
            private final int size;
            
            //	头结点索引
            private final int head;
            
            //	尾节点索引
            private final int tail;

            private ListState(AtomicReferenceArray<Bucket> data, int head, int tail) {
                this.head = head;
                this.tail = tail;
                if (head == 0 && tail == 0) {
                    size = 0;
                } 
                else {
                    this.size = (tail + dataLength - head) % dataLength;
                }
                this.data = data;
            }

            public Bucket tail() {
                if (size == 0) {
                    return null;
                } else {
                    // we want to get the last item, so size()-1
                    return data.get(convert(size - 1));
                }
            }

            private Bucket[] getArray() {
                ArrayList<Bucket> array = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    array.add(data.get(convert(i)));
                }
                return array.toArray(new Bucket[0]);
            }

            private ListState incrementTail() {
            	//	如果达到桶最大值
                if (size == numBuckets) {
                    //	头索引+1，尾索引+1
                    return new ListState(data, (head + 1) % dataLength, (tail + 1) % dataLength);
                } else {
                    //	头索引不变，尾索引+1
                    return new ListState(data, head, (tail + 1) % dataLength);
                }
            }

            //	创建一个新的环形数组ListState, 也就是AtomicReferenceArray
            public ListState clear() {
                return new ListState(new AtomicReferenceArray<>(dataLength), 0, 0);
            }

            //	添加Bucket
            public ListState addBucket(Bucket b) {
            	//	先添加Bucket到数组中  size = 0 1 2 3 4 5 6
                data.set(tail, b);
                //	再自增尾节点索引
                return incrementTail();
            }

            // The convert() method takes a logical index (as if head was
            // always 0) and calculates the index within elementData
            private int convert(int index) {
                return (index + head) % dataLength;
            }
        }

        public void clear() {
            while (true) {
            	//	获取当前的ListState
                ListState current = state.get();
                //	创建一个新的ListState
                ListState newState = current.clear();
                //	引用替换
                if (state.compareAndSet(current, newState)) {
                    return;
                }
            }
        }

        //	返回不可变Bucket数组迭代器
        public Iterator<Bucket> iterator() {
            return Collections.unmodifiableList(Arrays.asList(getArray())).iterator();
        }

        public void addLast(Bucket o) {
        	//	获取当前ListState
            ListState currentState = state.get();
            //	添加一个Bucket addBucket
            ListState newState = currentState.addBucket(o);

            //	单线程操作：原子更新引用保证ListState是正确的
            if (state.compareAndSet(currentState, newState)) {
                // we succeeded
                return;
            } else {
                //	我们失败了，因为其他人正在添加或删除
                //	后续处理不是再次尝试和风险多个addLast并发(不应该是这样的情况)
                //	我们将返回并让另一个线程'win'，在下一次调用getCurrentBucket将修复问题
                return;
            }
        }

        public Bucket getLast() {
            return peekLast();
        }

        public int size() {
            // the size can also be worked out each time as:
            // return (tail + data.length() - head) % data.length();
            return state.get().size;
        }

        public Bucket peekLast() {
            return state.get().tail();
        }

        private Bucket[] getArray() {
            return state.get().getArray();
        }

    }

}