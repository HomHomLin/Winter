package com.meiyou.framework.winter;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Choreographer;


/**
 * 监控绘制性能,可以排查导致丢帧的问题
 * Created by hxd on 16/7/6.
 */
public class Monitor implements Handler.Callback {
    private static String sTAG = "Monitor";

    private static long delay = 1000 / 60;
    private long lastTime = 0;
    private HandlerThread mHandlerThread;
    private Handler mMainHandler;
    Choreographer choreographer;
    Choreographer.FrameCallback callback;
    private boolean loopFlag = true;
    private QueueCache<Long, StackInfo> stackQueue;
    private InfoConsumer mInfoConsumer;

    public Monitor() {
        mHandlerThread = new TestHandlerThread("monitor",
                android.os.Process.THREAD_PRIORITY_DISPLAY);
        mMainHandler = new Handler(Looper.getMainLooper());
        stackQueue = new QueueCache<>(3);
        mInfoConsumer = new InfoConsumer();
    }

    public void start() {
        mHandlerThread.start();
    }

    public void stop() {
        loopFlag = false;
        mInfoConsumer.stopConsume();
    }

    private void statisticsStack(long time, long delta) {
        /**
         * cost < 1ms
         */
        StackTraceElement[] elements = mMainHandler.getLooper().getThread().getStackTrace();
        time = time / 1000000;
        stackQueue.put(time, StackInfo.newBuilder().
                delta(delta).
                currentTime(time).
                elements(elements)
                .build());
    }

    private void processInfo() {
        for (StackInfo stackInfo : stackQueue.values()) {
            mInfoConsumer.consume(stackInfo);
        }
        stackQueue.clear();
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    class TestHandlerThread extends HandlerThread {

        public TestHandlerThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            choreographer = Choreographer.getInstance();
            callback = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    long delta = (frameTimeNanos - lastTime) / 1000000;
                    if (lastTime != 0 && delta > delay) {
                        // LogUtils.d(sTAG, "callback after " + delta + " ms");
                        statisticsStack(frameTimeNanos, delta);
                        processInfo();
                    }
                    // everything is ok ,continue
                    lastTime = frameTimeNanos;
                    if (loopFlag) choreographer.postFrameCallback(callback);
                }
            };
            choreographer.postFrameCallback(callback);
        }

        @Override
        public void run() {
            super.run();
        }

        public TestHandlerThread(String name, int priority) {
            super(name, priority);
        }
    }
}
