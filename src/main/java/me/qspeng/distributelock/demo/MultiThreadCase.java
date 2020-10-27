package me.qspeng.distributelock.demo;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.LockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class MultiThreadCase {
    @Value("${service.key}")
    private String serviceKey;
    private final LockProvider lockProvider;

    public MultiThreadCase(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    //只有1个线程能拿到
    public void run() throws InterruptedException {
        String lockKey = "mock-key";
        String owner = "mock-owner";
        CountDownLatch countDownLatch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                boolean lock = lockProvider.lock(serviceKey, lockKey, owner, 5);
                log.info("{} get lock result {}", Thread.currentThread().getName(), lock);
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
    }
}
