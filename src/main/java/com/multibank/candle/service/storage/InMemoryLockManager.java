package com.multibank.candle.service.storage;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Primary
public class InMemoryLockManager implements LockManager {
    
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    
    @Override
    public Lock getLock(String key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock());
    }
    
    @Override
    public void removeLock(String key) {
        locks.remove(key);
    }

}
