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

    public void consume(StackInfo info) {
        mHandler.dispatchMessage(Message.obtain(mHandler, MSG_TYPE_INFO, info));
    }

    public void stopConsume() {
        mHandler.dispatchMessage(Message.obtain(mHandler, MSG_TYPE_CLOSE));
    }

    public InfoConsumer() {
        super("info-cunsumer", Process.THREAD_PRIORITY_FOREGROUND);
        start();
        mHandler = new Handler(this.getLooper(), this);
    }

    private File createFile() {
        String dir = Environment.getExternalStorageDirectory() + "/winter/monitor";
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss", Locale.CHINESE);
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
                    if (hasUsefulCodes(stackInfo.elements)) {
                        saveFile(stackInfo);
                    }
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
            mBufferedWriter.write("sample time:" + String.valueOf(stackInfo.mSampleTime) + "ms");
            mBufferedWriter.newLine();
            mBufferedWriter.write(" check time:" + String.valueOf(stackInfo.mCheckTime) + "ms");
            mBufferedWriter.newLine();
            mBufferedWriter.write(" cost  time:" + String.valueOf(stackInfo.mCheckTime - stackInfo.mSampleTime) + "ms");
            mBufferedWriter.newLine();
            mBufferedWriter.write("time delta:" + String.valueOf(stackInfo.mCheckTime) + "ms");
            mBufferedWriter.newLine();
            mBufferedWriter.write(stackInfo.getStackTraceString());
            mBufferedWriter.newLine();
            count++;
            if (count >= 5) {
                mBufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean hasUsefulCodes(StackTraceElement[] elements) {
        if (elements == null) {
            return false;
        }
        for (StackTraceElement element : elements) {
            String str = element.toString();
            if (!(startsWith(str, "android.")
                    || startsWith(str, "java.")
                    || startsWith(str, "dalvik.")
                    || startsWith(str, "com.android."))) {
                return true;
            }
        }
        return false;
    }


    public static boolean startsWith(String str, String prefix) {
        return startsWith(str, prefix, false);
    }

    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return startsWith(str, prefix, true);
    }

    private static boolean startsWith(String str, String prefix, boolean ignoreCase) {
        return str != null && prefix != null ? (prefix.length() > str.length() ? false : str.regionMatches(ignoreCase, 0, prefix, 0, prefix.length())) : str == null && prefix == null;
    }

}
