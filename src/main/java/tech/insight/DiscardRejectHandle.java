package tech.insight;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class DiscardRejectHandle implements RejectHandle {
    @Override
    public void reject(Runnable rejectCommand, MyThreadPool threadPool) {
        threadPool.blockingQueue.poll();
        threadPool.execute(rejectCommand);
    }
}
