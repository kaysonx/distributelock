package me.qspeng.distributelock.lock;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.entity.DistributeLock;
import me.qspeng.distributelock.lock.persist.DistributeLockDAO;
import me.qspeng.distributelock.watchdog.WatchDog;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
public class LockProvider {
    private final DistributeLockDAO distributeLockDAO;
    private final ExecutorService executorService;

    public LockProvider(DistributeLockDAO distributeLockDAO, ExecutorService executorService) {
        this.distributeLockDAO = distributeLockDAO;
        this.executorService = executorService;
    }

    //互斥锁，unlock 或者 超时后可获得
    public synchronized boolean lock(String serviceName, String lockKey, String owner, int expireSeconds) {
        if (notTimeoutOrNotExistLock(serviceName, lockKey, owner)) {
            return false;
        }
        try {
            Optional<DistributeLock> existTimeoutLock = distributeLockDAO.findByServiceKeyAndLockKeyAndOwner(serviceName, lockKey, owner);
            if (existTimeoutLock.isPresent()) {
                _renewLock(existTimeoutLock.get());
            } else {
                distributeLockDAO.addLock(serviceName, lockKey, owner, expireSeconds);
            }
        } catch (Exception e) {
            log.info("Duplicate lock added with:", e);
            return false;
        }
        return true;
    }

    public void renewLock(String serviceName, String lockKey, String owner) {
        distributeLockDAO.findByServiceKeyAndLockKeyAndOwner(serviceName, lockKey, owner).ifPresent(this::_renewLock);
    }

    private void _renewLock(DistributeLock existLock) {
        existLock.setRenewalTime(new Date());
        distributeLockDAO.save(existLock);
    }

    //自动续约
    public boolean lockWithWatchDog(String serviceName, String lockKey, String owner, int expireSeconds) {
        if (this.lock(serviceName, lockKey, owner, expireSeconds)) {
            log.info("Will add watch dog to renew lock...");
            executorService.submit(new WatchDog(this, Thread.currentThread(), serviceName, lockKey, owner));
            return true;
        }
        return false;
    }

    //可以重复调用
    public void unlock(String serviceName, String lockKey, String owner) {
        distributeLockDAO.findByServiceKeyAndLockKeyAndOwner(serviceName, lockKey, owner).ifPresent(lock -> distributeLockDAO.deleteById(lock.getId()));
    }

    public boolean notTimeoutOrNotExistLock(String serviceName, String lockKey, String owner) {
        return distributeLockDAO.exists(serviceName, lockKey, owner) == 1;
    }

    public boolean exist(String serviceName, String lockKey, String owner) {
        return distributeLockDAO.findByServiceKeyAndLockKeyAndOwner(serviceName, lockKey, owner).isPresent();
    }

    //For Scheduling
    public int releaseAllTimeoutLock() {
        List<DistributeLock> timeoutLocks = distributeLockDAO.findAllTimeoutLocks();
        distributeLockDAO.deleteAll(timeoutLocks);
        return timeoutLocks.size();
    }

    //For Test Using
    public void releaseAllLock() {
        distributeLockDAO.deleteAll();
    }

    //For Test Using
    public List<DistributeLock> getAllLocks() {
        return distributeLockDAO.findAll();
    }
}
