package me.qspeng.distributelock.demo;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.LockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class WatchDogCase {
    @Value("${service.key}")
    private String serviceKey;
    private final LockProvider lockProvider;

    public WatchDogCase(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    public void run() throws InterruptedException {
        String lockKey = "mock-key-" + UUID.randomUUID().toString();
        String owner = Thread.currentThread().getName();
        boolean lockResult = lockProvider.lockWithWatchDog(serviceKey, lockKey, owner, 1);
        log.info("First time get lock result {}", lockResult);
        Thread.sleep(8 * 1000);
        boolean lockResultAfterTimeout = lockProvider.lock(serviceKey, lockKey, owner, 10);
        log.info("After timeout get lock result {}", lockResultAfterTimeout);
    }

    public void runWithUnlock() throws InterruptedException {
        String lockKey = "mock-key-" + UUID.randomUUID().toString();
        String owner = Thread.currentThread().getName();
        boolean lockResult = lockProvider.lockWithWatchDog(serviceKey, lockKey, owner, 1);
        log.info("First time get lock result {}", lockResult);
        Thread.sleep(2 * 1000);
        lockProvider.unlock(serviceKey, lockKey, owner);
        Thread.sleep(8 * 1000);
        boolean lockResultAfterTimeout = lockProvider.lock(serviceKey, lockKey, owner, 10);
        log.info("After timeout get lock result {}", lockResultAfterTimeout);
    }
}
