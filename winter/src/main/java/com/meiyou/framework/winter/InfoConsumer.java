package com.meiyou.framework.winter;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * info的消费者
 * Created by hxd on 16/7/6.
 */
public class InfoConsumer extends HandlerThread implements Handler.Callback {
    private Handler mHandler;
    private final static int MSG_TYPE_INFO = 1;
    private final static int MSG_TYPE_CLOSE = 2;
    private File mFile;
    private BufferedWriter mBufferedWriter;
    private int count;
    private boolean advanceMode = true;
    private boolean close = false;
    private boolean noSystemCode = false;

    public void consume(StackInfo info) {
        mHandler.dispatchMessage(Message.obtain(mHandler, MSG_TYPE_INFO, info));
    }

    public void stopConsume() {
        mHandler.dispatchMessage(Message.obtain(mHandler, MSG_TYPE_CLOSE));
    }

    public InfoConsumer(boolean noSystemCode) {
        super("info-consumer", Process.THREAD_PRIORITY_BACKGROUND);
        this.noSystemCode = noSystemCode;
        start();
        mHandler = new Handler(this.getLooper(), this);
    }

    private File createFile() {
        String dir = Environment.getExternalStorageDirectory() + "/winter/monitor";
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINESE);
        String a1 = dateformat1.format(new Date());
        File file = new File(dir + "/" + a1 + ".log");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_TYPE_INFO:
                StackInfo stackInfo = (StackInfo) msg.obj;
                if (advanceMode) {
                    //if (StackInfo.hasUsefulCodes(stackInfo.elements)) {
                        saveFile(stackInfo);
                    //}
                } else {
                    saveFile(stackInfo);
                }
                break;
            case MSG_TYPE_CLOSE:
                try {
                    if (mBufferedWriter != null && !close) {
                        mBufferedWriter.flush();
                        mBufferedWriter.close();
                        close = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return false;
    }

    private void checkInit() throws IOException {
        if (mFile == null) {
            mFile = createFile();
            mBufferedWriter = new BufferedWriter(new FileWriter(mFile));
        }
    }

    private void saveFile(StackInfo stackInfo) {
        try {
            checkInit();

            mBufferedWriter.write("tag:" + String.valueOf(stackInfo.getTag()));
            mBufferedWriter.newLine();
            mBufferedWriter.write("type:" + String.valueOf(stackInfo.getTypeString()));
            mBufferedWriter.newLine();
            mBufferedWriter.write("time:" + String.valueOf(stackInfo.getCurrentTime()) + " ms");
            mBufferedWriter.newLine();
            mBufferedWriter.write(" sample delta:" + String.valueOf(stackInfo.getSampleDelta()) + "ms");
            mBufferedWriter.newLine();
            mBufferedWriter.write(stackInfo.getStackTraceString(this.noSystemCode));
            mBufferedWriter.newLine();
            count++;
            if (count >= 8) {
                mBufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
