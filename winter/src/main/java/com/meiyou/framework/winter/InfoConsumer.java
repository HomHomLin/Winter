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

/**
 * info的消费者
 * Created by hxd on 16/7/6.
 */
public class InfoConsumer extends HandlerThread implements Handler.Callback {
    private Handler mHandler;
    private final static int MSG_TYPE_INFO = 1;
    private File mFile;
    private BufferedWriter mBufferedWriter;
    private int count;
    private boolean advanceMode = true;

    public void consume(StackInfo info) {
        mHandler.dispatchMessage(Message.obtain(mHandler, MSG_TYPE_INFO, info));
    }

    public void stopConsume() {
        try {
            if (mBufferedWriter == null) {
                return;
            }
            mBufferedWriter.flush();
            mBufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InfoConsumer() {
        super("info-cunsumer", Process.THREAD_PRIORITY_FOREGROUND);
        start();
        mHandler = new Handler(this.getLooper(),this);
    }

    private File createFile() {
        String dir = Environment.getExternalStorageDirectory() + "/winter/monitor";
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File file = new File(dir + "/" + System.currentTimeMillis() + ".log");
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
            mBufferedWriter.write("current time:"+String.valueOf(stackInfo.currentTime) + "ms");
            mBufferedWriter.newLine();
            mBufferedWriter.write("doFrame cost:"+String.valueOf(stackInfo.delta)+ "ms");
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
