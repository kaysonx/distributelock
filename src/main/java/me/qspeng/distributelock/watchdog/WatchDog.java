package me.qspeng.distributelock.watchdog;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.LockProvider;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class WatchDog implements Runnable {
    private final static int refreshGap = 10;

    private final LockProvider lockProvider;
    private final String serviceName;
    private final String lockKey;
    private final String owner;
    private final int refreshInterval;

    private final Thread businessThread;

    public WatchDog(LockProvider lockProvider, Thread thread, String serviceName, String lockKey, String owner, int expireSeconds) {
        this.lockProvider = lockProvider;
        this.serviceName = serviceName;
        this.lockKey = lockKey;
        this.owner = owner;
        this.refreshInterval = Math.max(5, expireSeconds - refreshGap);
        this.businessThread = thread;
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!businessThread.isAlive() || !lockProvider.existLock(serviceName, lockKey, owner)) {
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
