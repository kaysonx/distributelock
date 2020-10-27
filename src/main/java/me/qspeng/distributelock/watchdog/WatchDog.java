package me.qspeng.distributelock.watchdog;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.LockProvider;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class WatchDog implements Runnable {
    private final LockProvider lockProvider;
    private final String serviceName;
    private final String lockKey;
    private final String owner;
    private final int refreshInterval = 1;

    private final Thread businessThread;

    public WatchDog(LockProvider lockProvider, Thread thread, String serviceName, String lockKey, String owner) {
        this.lockProvider = lockProvider;
        this.serviceName = serviceName;
        this.lockKey = lockKey;
        this.owner = owner;
        this.businessThread = thread;
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!businessThread.isAlive() || !lockProvider.exist(serviceName, lockKey, owner)) {
                    log.info("Lock for {} is released, will shutdown self...", lockKey);
                    timer.cancel();
                } else {
                    log.info("Will renew lock now...");
                    lockProvider.renewLock(serviceName, lockKey, owner);
                }
            }
        }, 0, refreshInterval * 1000);
    }
}
