package tech.insight;

/**
 * @author gongxuanzhangmelt@gmail.com
 * 拒绝策略 - 丢弃任务
 **/
public class DiscardRejectHandle implements RejectHandle {
    @Override
    public void reject(Runnable rejectCommand, MyThreadPool threadPool) {
        threadPool.blockingQueue.poll();
        threadPool.execute(rejectCommand);
    }
}
