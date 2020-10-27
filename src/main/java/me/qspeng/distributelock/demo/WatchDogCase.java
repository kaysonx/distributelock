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

    //第二次依旧能拿到，因为有续约
    public void run() throws InterruptedException {
        String lockKey = "mock-key-" + UUID.randomUUID().toString();
        String owner = Thread.currentThread().getName();
        boolean lock = lockProvider.lockWithWatchDog(serviceKey, lockKey, owner, 3);

        log.info("First time get lock result {}", lock);
        assert (lock);
        Thread.sleep(5 * 1000);

        boolean lockAfterTimeout = lockProvider.lock(serviceKey, lockKey, owner, 10);
        log.info("After timeout get lock result {}", lockAfterTimeout);
        assert (!lockAfterTimeout);
    }

    //第二次拿不到锁，因为中途方法执行完毕，手动释放Lock
    public void runWithUnlock() throws InterruptedException {
        String lockKey = "mock-key-" + UUID.randomUUID().toString();
        String owner = Thread.currentThread().getName();
        boolean lock = lockProvider.lockWithWatchDog(serviceKey, lockKey, owner, 1);

        log.info("First time get lock result {}", lock);
        assert (lock);
        Thread.sleep(2 * 1000);

        lockProvider.unlock(serviceKey, lockKey, owner);
        Thread.sleep(8 * 1000);

        boolean lockAfterTimeout = lockProvider.lock(serviceKey, lockKey, owner, 10);
        log.info("After timeout get lock result {}", lockAfterTimeout);
        assert (lockAfterTimeout);
    }
}
