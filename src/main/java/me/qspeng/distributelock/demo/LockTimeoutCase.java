package me.qspeng.distributelock.demo;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.LockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LockTimeoutCase {
    @Value("${service.key}")
    private String serviceKey;
    private final LockProvider lockProvider;

    public LockTimeoutCase(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    public void run() throws InterruptedException {
        String lockKey = "mock-key";
        String owner = Thread.currentThread().getName();
        boolean lockResult = lockProvider.lock(serviceKey, lockKey, owner, 3);
        log.info("First time get lock result {}", lockResult);
        Thread.sleep(5 * 1000);
        boolean lockResultAfterTimeout = lockProvider.lock(serviceKey, lockKey, owner, 10);
        log.info("After timeout get lock result {}", lockResultAfterTimeout);
    }
}
