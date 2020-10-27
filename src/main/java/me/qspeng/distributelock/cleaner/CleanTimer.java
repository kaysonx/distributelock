package me.qspeng.distributelock.cleaner;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.LockProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CleanTimer {
    private final LockProvider lockProvider;

    public CleanTimer(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
        log.info("Scheduled timout lock cleaner is started....");
    }

    @Scheduled(fixedRate = 10 * 1000, initialDelay = 20 * 1000)
    public void cleanTimoutLock() {
        log.info("Will clean all the timeout lock...");
        int timeoutLocks = lockProvider.releaseAllTimeoutLock();
        log.info("After clean, delete {} locks", timeoutLocks);
    }
}
