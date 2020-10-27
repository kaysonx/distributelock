package me.qspeng.distributelock.demo;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.LockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NormalCase {
    @Value("${service.key}")
    private String serviceKey;
    private final LockProvider lockProvider;

    public NormalCase(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    public void run() {
        String lockKey = "mock-key";
        String owner = Thread.currentThread().getName();
        for (int i = 0; i < 5; i++) {
            boolean lockResult = lockProvider.lock(serviceKey, lockKey, owner, 60);
            log.info("Iteration with {} and lock result {}", i, lockResult);
        }
        lockProvider.releaseAllLock();
    }

}
