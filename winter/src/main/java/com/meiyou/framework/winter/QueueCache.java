package com.meiyou.framework.winter;

import java.util.LinkedHashMap;

/**
 * 简单的缓存实现
 * Created by hxd on 16/7/6.
 */
public class QueueCache<K, V> extends LinkedHashMap<K, V> {
    private long mMaxLength;

    public QueueCache(long maxLength) {
        super();
        this.mMaxLength = maxLength;
    }

    @Override
    public V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {
        return this.size() > mMaxLength;
    }
}
