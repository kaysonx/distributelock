package me.qspeng.distributelock;

import me.qspeng.distributelock.lock.LockProvider;
import me.qspeng.distributelock.lock.persist.DistributeLockDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DistributelockApplicationTests {
    @Autowired
    private LockProvider lockProvider;

    @Test
    void should_insert_lock_correctly() {
        lockProvider.lock("mock_svc", "mock_key", "jerrs", 5);
        System.out.println("Finished...");
    }
}
