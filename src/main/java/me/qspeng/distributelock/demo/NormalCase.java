package me.qspeng.distributelock.demo;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.LockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class NormalCase {
    @Value("${service.key}")
    private String serviceKey;
    private final LockProvider lockProvider;

    public NormalCase(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    //只会有一个能拿到锁
    public void run() {
        String lockKey = "mock-key-" + UUID.randomUUID().toString();
        String owner = Thread.currentThread().getName();
        for (int i = 0; i < 5; i++) {
            boolean lock = lockProvider.lock(serviceKey, lockKey, owner, 60);
            log.info("Iteration with {} and lock result {}", i, lock);
        }
        lockProvider.releaseAllLock();
    }

}
