package com.meiyou.framework.winter;

/**
 * 堆栈信息
 * Created by hxd on 16/7/6.
 */
public class StackInfo {
    StackTraceElement[] elements;
    long mCheckTime;// 检测是否丢帧的时刻
    long delta;
    long mSampleTime;//采样 时间

    public StackTraceElement[] getElements() {
        return elements;
    }

    public long getCheckTime() {
        return mCheckTime;
    }

    public long getDelta() {
        return delta;
    }

    public long getSampleTime() {
        return mSampleTime;
    }

    public void setCheckTime(long checkTime) {
        mCheckTime = checkTime;
    }

    public StackInfo setSampleTime(long sampleTime) {
        this.mSampleTime = sampleTime;
        return this;
    }

    public String getStackTraceString() {
        if (elements != null) {
            StringBuilder builder = new StringBuilder();
            for (StackTraceElement element : elements) {
                builder.append(element.toString()).append("\n");
            }
            return builder.toString();
        }
        return "";
    }

    private StackInfo(Builder builder) {
        elements = builder.elements;
        mCheckTime = builder.mCheckTime;
        delta = builder.delta;
        setSampleTime(builder.mSampleTime);
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {
        private StackTraceElement[] elements;
        private long mCheckTime;
        private long delta;
        private long mSampleTime;

        private Builder() {
        }

        public Builder elements(StackTraceElement[] val) {
            elements = val;
            return this;
        }

        public Builder checkTime(long val) {
            mCheckTime = val;
            return this;
        }

        public Builder delta(long val) {
            delta = val;
            return this;
        }

        public Builder sampleTime(long val) {
            mSampleTime = val;
            return this;
        }

        public StackInfo build() {
            return new StackInfo(this);
        }

    }
}
