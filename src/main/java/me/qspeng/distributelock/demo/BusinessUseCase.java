package me.qspeng.distributelock.demo;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.LockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class BusinessUseCase {
    @Value("${service.key}")
    private String serviceKey;
    private final LockProvider lockProvider;

    public BusinessUseCase(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    public void happyPath() {
        String lockKey = "mock-key-" + UUID.randomUUID().toString();
        String owner = Thread.currentThread().getName();
        boolean lockResult = lockProvider.lock(serviceKey, lockKey, owner, 3);
        if (lockResult) {
            try {
                log.info("Mock some business working...");
                log.info("Mock some rpc invoke now....");
                log.info("Mock some business working...");
            } catch (Exception e) {
                log.error("Exception occurred with: ", e);
                log.info("Do some exception handle....");
            } finally {
                lockProvider.unlock(serviceKey, lockKey, owner);
            }
        } else {
            log.error("This request is ongoing now, not permit for reentrant...");
        }
    }

    public void errorPath() {
        String lockKey = "mock-key-" + UUID.randomUUID().toString();
        String owner = Thread.currentThread().getName();
        boolean lockResult = lockProvider.lock(serviceKey, lockKey, owner, 3);
        if (lockResult) {
            try {
                log.info("Mock some business working...");
                log.info("Mock some rpc invoke now....");
                log.info("Mock some business working...");
                throw new RuntimeException("Ex..");
            } catch (Exception e) {
                log.error("Exception occurred with: ", e);
                log.info("Do some exception handle....");
            } finally {
                lockProvider.unlock(serviceKey, lockKey, owner);
            }
        } else {
            log.error("This request is ongoing now, not permit for reentrant...");
        }
    }

}
