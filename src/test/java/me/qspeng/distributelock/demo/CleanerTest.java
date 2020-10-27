package me.qspeng.distributelock.demo;


import me.qspeng.distributelock.lock.LockProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CleanerTest {
    @Autowired
    private LockProvider lockProvider;

    @Test
    void should_insert_lock_clear_it_automatically_after_timeout() throws InterruptedException {
        lockProvider.releaseAllLock();
        lockProvider.lock("mock_svc_1", "mock_key_1", "jerrs", 2);
        lockProvider.lock("mock_svc_2", "mock_key_2", "jerrs", 2);
        lockProvider.lock("mock_svc_3", "mock_key_3", "jerrs", 2);
        Assertions.assertEquals(3, lockProvider.getAllLocks().size());
        Thread.sleep(15 * 1000);
        Assertions.assertEquals(0, lockProvider.getAllLocks().size());
    }
}
