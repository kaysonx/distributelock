package me.qspeng.distributelock.demo;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.LockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class LockTimeoutCase {
    @Value("${service.key}")
    private String serviceKey;
    private final LockProvider lockProvider;

    public LockTimeoutCase(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    //超时后锁自动释放 后续操作可以拿到锁
    public void run() throws InterruptedException {
        String lockKey = "mock-key-" + UUID.randomUUID().toString();
        String owner = Thread.currentThread().getName();
        boolean lock = lockProvider.lock(serviceKey, lockKey, owner, 3);
        log.info("First time get lock result {}", lock);
        Thread.sleep(5 * 1000);
        boolean lockAfterTimeout = lockProvider.lock(serviceKey, lockKey, owner, 10);
        log.info("After timeout get lock result {}", lockAfterTimeout);
    }
}
