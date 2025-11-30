package com.multibank.candle.service.storage;

import java.util.concurrent.locks.Lock;

public interface LockManager {
    
    Lock getLock(String key);
    
    void removeLock(String key);

}

