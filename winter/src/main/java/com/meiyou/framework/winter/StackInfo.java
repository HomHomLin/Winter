package com.meiyou.framework.winter;

/**
 * 堆栈信息
 * Created by hxd on 16/7/6.
 */
public class StackInfo {
    StackTraceElement[] elements;
    long currentTime;
    long delta;

    public StackTraceElement[] getElements() {
        return elements;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getDelta() {
        return delta;
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
        currentTime = builder.currentTime;
        delta = builder.delta;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {
        private StackTraceElement[] elements;
        private long currentTime;
        private long delta;

        private Builder() {
        }

        public Builder elements(StackTraceElement[] val) {
            elements = val;
            return this;
        }

        public Builder currentTime(long val) {
            currentTime = val;
            return this;
        }

        public Builder delta(long val) {
            delta = val;
            return this;
        }

        public StackInfo build() {
            return new StackInfo(this);
        }
    }
}
