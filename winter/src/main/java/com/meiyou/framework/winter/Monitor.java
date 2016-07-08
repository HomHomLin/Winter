package com.meiyou.framework.winter;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.view.Choreographer;


/**
 * 监控绘制性能,可以排查导致丢帧的问题
 * Created by hxd on 16/7/6.
 */
public class Monitor implements Handler.Callback {
    private static String sTAG = "Monitor";
    private int threshold = 1;
    private long delay = 1000 / 60;
    private long lastDoFrameTime = 0;
    private long lastSampleTime = 0;
    //private HandlerThread mHandlerThread;
    private Handler mMainHandler;
    private Handler mCheckerHandler;
    private Choreographer mSampleChoreographer;
    private Choreographer.FrameCallback mSampleCallback;
    private Choreographer mMainChoreographer;
    private Choreographer.FrameCallback mMainCallback;
    private boolean mLoopFlag = true;
    private QueueCache<Long, StackInfo> stackQueue;
    private InfoConsumer mInfoConsumer;
    private static final int MSG_TYPE_LOOP = 0;
    private boolean inited = false;
    final private Boolean[] lock = new Boolean[]{false};
    private boolean noSystemCode = false;

    public static Monitor getInstance() {
        return Holder.sMonitor;
    }

    static class Holder {
        static Monitor sMonitor = new Monitor();
    }

    private Monitor() {
    }

    public Monitor init(int threshold,boolean noSystemCode) {
        if (new Handler().getLooper() != Looper.getMainLooper()) {
            throw new RuntimeException(" must init in Main thread !");
        }
        if (inited) {
            return this;
        }

        if (threshold <= 0) {
            threshold = 1;
        }
        this.threshold = threshold;
        this.delay = delay * threshold;
        HandlerThread mHandlerThread = new CheckerHandlerThread("monitor",
                Process.THREAD_PRIORITY_FOREGROUND);
        mHandlerThread.start();
        mCheckerHandler = new Handler(mHandlerThread.getLooper(), this);
        mMainHandler = new Handler(Looper.getMainLooper());
        stackQueue = new QueueCache<>(threshold + 1);
        mInfoConsumer = new InfoConsumer(noSystemCode);
        mMainChoreographer = Choreographer.getInstance();
        mMainCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(final long frameTimeNanos) {
                final long delta = (frameTimeNanos - lastDoFrameTime) / 1000000;
                if (lastDoFrameTime != 0 && delta > delay) {
                    //LogUtils.d("monitor", "main doFrame " + delta);
                    final long lastTime = lastDoFrameTime;
                    // 单线程操作 queue
                    mCheckerHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            statisticsStack(frameTimeNanos / 1000000, delta, StackInfo.TYPE_CHECK);
                            processInfo(lastTime / 1000000, frameTimeNanos / 1000000);
                        }
                    });

                }
                lastDoFrameTime = frameTimeNanos;
                if (mLoopFlag) {
                    mMainChoreographer.postFrameCallback(mMainCallback);
                }
            }
        };
        inited = true;
        return this;
    }

    public void start() {
        while (!lock[0]) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mSampleChoreographer.postFrameCallback(mSampleCallback);
        mMainChoreographer.postFrameCallback(mMainCallback);
    }

    public void stop() {
        mLoopFlag = false;
        mInfoConsumer.stopConsume();
    }

    /**
     * @param time  ms
     * @param delta ms
     */
    private void statisticsStack(final long time, long delta, int type) {
        /**
         * cost < 1ms
         */
        StackTraceElement[] elements = mMainHandler.getLooper().getThread().getStackTrace();

        stackQueue.put(time, new StackInfo(elements, time, type, delta));
    }

    /**
     */
    private void processInfo(final long checkLastTime, final long checkNowTime) {
        for (StackInfo stackInfo : stackQueue.values()) {
            if (stackInfo.getCurrentTime() > checkLastTime
                    && stackInfo.getCurrentTime() <= checkNowTime) {
                stackInfo.setTag("tag-" + checkNowTime);
                mInfoConsumer.consume(stackInfo);
            }

        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_TYPE_LOOP:

                break;
        }
        return false;
    }

    class CheckerHandlerThread extends HandlerThread {
        @Override
        protected void onLooperPrepared() {
            mSampleChoreographer = Choreographer.getInstance();
            mSampleCallback = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(final long frameTimeNanos) {
                    final long delta = (frameTimeNanos - lastSampleTime) / 1000000;
                    if (lastSampleTime != 0 && delta > delay) {
                        //采样周期不准了,目前丢弃吧
                        //LogUtils.w("monitor", "sample doFrame " + delta);
//                        mCheckerHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                processInfo(lastSampleTime / 1000000,frameTimeNanos / 1000000);
//                            }
//                        });
                    } else {
                        mCheckerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                statisticsStack(frameTimeNanos / 1000000, delta, StackInfo.TYPE_SAMPLE);
                            }
                        });
                    }
                    lastSampleTime = frameTimeNanos;
                    if (mLoopFlag) {
                        mSampleChoreographer.postFrameCallback(mSampleCallback);
                    }
                }
            };

            lock[0] = true;

        }

        public CheckerHandlerThread(String name, int priority) {
            super(name, priority);
        }
    }

}