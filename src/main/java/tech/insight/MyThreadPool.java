package tech.insight;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class MyThreadPool {

    /**
     * 核心线程数
     */
    private final int corePoolSize;
    /**
     * 最大线程数
     */
    private final int maxSize;
    /**
     * 超时数
     */
    private final int timeout;
    /**
     * 时间单位
     */
    private final TimeUnit timeUnit;
    /**
     * 任务 阻塞队列
     */
    public final BlockingQueue<Runnable> blockingQueue;
    /**
     * 拒绝策略
     */
    private final RejectHandle rejectHandle;

    public MyThreadPool(int corePoolSize, int maxSize, int timeout, TimeUnit timeUnit,
                        BlockingQueue<Runnable> blockingQueue, RejectHandle rejectHandle) {
        this.corePoolSize = corePoolSize;
        this.maxSize = maxSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.blockingQueue = blockingQueue;
        this.rejectHandle = rejectHandle;
    }

    /**
     * 核心线程数组
     */
    List<Thread> coreList = new ArrayList<>();

    /**
     * 辅助线程数组
     */
    List<Thread> supportList = new ArrayList<>();

    /**
     * 添加任务
     * @param command
     */
    void execute(Runnable command) {
        if (coreList.size() < corePoolSize) {
            Thread thread = new CoreThread(command);
            coreList.add(thread);
            thread.start();
            return;
        }
        /**
         * 使用add方法添加元素时,队列满了的话会抛出 IllegalStateException
         * 使用offer方法添加元素时, 队列满了的话会返回false
         */
        if (blockingQueue.offer(command)) {
            return;
        }
        if (coreList.size() + supportList.size() < maxSize) {
            Thread thread = new SupportThread(command);
            supportList.add(thread);
            thread.start();
            return;
        }
        if (!blockingQueue.offer(command)) {
            rejectHandle.reject(command, this);
        }
    }

    /**
     * 核心线程类
     * 线程会一直执行任务
     */
    class CoreThread extends Thread {

        private final Runnable firstTask;

        public CoreThread(Runnable firstTask) {
            this.firstTask = firstTask;
        }

        @Override
        public void run() {
            firstTask.run();
            while (true) {
                try {
                    Runnable command = blockingQueue.take();
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 辅助线程类
     * 线程会执行任务,超时后结束
     */
    class SupportThread extends Thread {
        private final Runnable firstTask;

        public SupportThread(Runnable firstTask) {
            this.firstTask = firstTask;
        }

        @Override
        public void run() {
            firstTask.run();
            while (true) {
                try {
                    // 获取任务
                    Runnable command = blockingQueue.poll(timeout, timeUnit);
                    if (command == null) {
                        break;
                    }
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(Thread.currentThread().getName() + "线程结束了！");
            supportList.remove(Thread.currentThread());
        }
    }
}
