package com.meiyou.framework.winter;

/**
 * 堆栈信息
 * Created by hxd on 16/7/6.
 */
public class StackInfo {
    StackTraceElement[] elements;
    long mSampleDelta;//采样周期
    long mCurrentTime;
    int type;
    final public static int TYPE_SAMPLE = 0;
    final public static int TYPE_CHECK = 1;
    String mTag;

    public StackInfo(StackTraceElement[] elements, long currentTime, int type, long sampleDelta) {
        this.elements = elements;
        this.mCurrentTime = currentTime;
        this.type = type;
        this.mSampleDelta = sampleDelta;
    }

    public StackTraceElement[] getElements() {
        return elements;
    }

    public void setElements(StackTraceElement[] elements) {
        this.elements = elements;
    }

    public long getSampleDelta() {
        return mSampleDelta;
    }

    public void setSampleDelta(long sampleDelta) {
        mSampleDelta = sampleDelta;
    }

    public long getCurrentTime() {
        return mCurrentTime;
    }

    public void setCurrentTime(long currentTime) {
        mCurrentTime = currentTime;
    }

    public int getType() {
        return type;
    }

    public String getTypeString() {
        return type == TYPE_SAMPLE ? "sample" : "check";
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public String getStackTraceString(boolean noSystemCode) {
        if (elements != null) {
            StringBuilder builder = new StringBuilder();
            for (StackTraceElement element : elements) {
                if (!noSystemCode) {
                    builder.append(element.toString()).append("\n");
                } else {
                    if (!isSystemMethod(element.toString())) {
                        builder.append(element.toString()).append("\n");
                    }
                }

            }
            return builder.toString();
        }
        return "";
    }

    public static boolean hasUsefulCodes(StackTraceElement[] elements) {
        if (elements == null) {
            return false;
        }
        for (StackTraceElement element : elements) {
            String str = element.toString();
            if (isSystemMethod(str)) return true;
        }
        return false;
    }

    private static boolean isSystemMethod(String str) {
        if ((startsWith(str, "android.")
                || startsWith(str, "java.")
                || startsWith(str, "dalvik.")
                || startsWith(str, "com.android."))) {
            return true;
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
