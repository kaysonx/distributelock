package me.qspeng.distributelock.lock;

import lombok.extern.slf4j.Slf4j;
import me.qspeng.distributelock.lock.entity.DistributeLock;
import me.qspeng.distributelock.lock.persist.DistributeLockDAO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LockProvider {
    private final DistributeLockDAO distributeLockDAO;

    public LockProvider(DistributeLockDAO distributeLockDAO) {
        this.distributeLockDAO = distributeLockDAO;
    }

    //互斥 - 重复调用，抛出异常
    public boolean lock(String serviceName, String lockKey, String owner, int expireSeconds) {
        if (existLock(serviceName, lockKey, owner)) {
            return false;
        }

        Optional<DistributeLock> existTimeoutLock = distributeLockDAO.findByServiceKeyAndLockKeyAndOwner(serviceName, lockKey, owner);
        if (existTimeoutLock.isPresent()) {
            renewLock(existTimeoutLock.get());
        } else {
            distributeLockDAO.addLock(serviceName, lockKey, owner, expireSeconds);
        }
        return true;
    }

    private void renewLock(DistributeLock existLock) {
        existLock.setRenewalTime(new Date());
        distributeLockDAO.save(existLock);
    }

    //自动续约
    public void lockWithWatchDog(String serviceName, String lockKey, String owner, int expireSeconds) {

    }

    //可以重复调用
    public void unlock(String serviceName, String lockKey, String owner) {
        Optional<DistributeLock> lockOptional = distributeLockDAO.findByServiceKeyAndLockKeyAndOwner(serviceName, lockKey, owner);
        lockOptional.ifPresent(lock -> distributeLockDAO.deleteById(lock.getId()));
    }

    //所有没有过期的锁
    private boolean existLock(String serviceName, String lockKey, String owner) {
        final int existLock = distributeLockDAO.exists(serviceName, lockKey, owner);
        return existLock == 1;
    }

    //For Test Clean
    public void releaseAllLock() {
        distributeLockDAO.deleteAll();
    }

    public int releaseAllTimeoutLock() {
        List<DistributeLock> timeoutLocks = distributeLockDAO.findAllTimeoutLocks();
        distributeLockDAO.deleteAll(timeoutLocks);
        return timeoutLocks.size();
    }
}
