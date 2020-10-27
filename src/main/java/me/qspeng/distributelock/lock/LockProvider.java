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

    //互斥 - 重复调用，抛出异常
    public boolean lock(String serviceName, String lockKey, String owner, int expireSeconds) {
        if (existLock(serviceName, lockKey, owner)) {
            return false;
        }

        Optional<DistributeLock> existTimeoutLock = distributeLockDAO.findByServiceKeyAndLockKeyAndOwner(serviceName, lockKey, owner);
        if (existTimeoutLock.isPresent()) {
            _renewLock(existTimeoutLock.get());
        } else {
            distributeLockDAO.addLock(serviceName, lockKey, owner, expireSeconds);
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
            executorService.submit(new WatchDog(this, Thread.currentThread(), serviceName, lockKey, owner, expireSeconds));
            return true;
        }
        return false;
    }

    //可以重复调用
    public void unlock(String serviceName, String lockKey, String owner) {
        Optional<DistributeLock> lockOptional = distributeLockDAO.findByServiceKeyAndLockKeyAndOwner(serviceName, lockKey, owner);
        lockOptional.ifPresent(lock -> distributeLockDAO.deleteById(lock.getId()));
    }

    //没有过期的锁
    public boolean existLock(String serviceName, String lockKey, String owner) {
        final int existLock = distributeLockDAO.exists(serviceName, lockKey, owner);
        return existLock == 1;
    }

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
