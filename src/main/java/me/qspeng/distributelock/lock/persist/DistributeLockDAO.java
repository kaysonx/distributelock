package me.qspeng.distributelock.lock.persist;

import me.qspeng.distributelock.lock.entity.DistributeLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DistributeLockDAO extends JpaRepository<DistributeLock, Integer> {

    @Query(value = "select count(id) from distributed_lock dl where dl.service_key = :serviceKey and dl.lock_key = :lockKey and dl.owner = :owner and dl.expire_seconds > TIMESTAMPDIFF(SECOND, dl.renewal_time, NOW())", nativeQuery = true)
    int exists(@Param("serviceKey") String serviceKey, @Param("lockKey") String lockKey, @Param("owner") String owner);


    @Query(value = "select dl.id, dl.service_key, dl.lock_key, dl.owner, dl.expire_seconds, dl.create_time, dl.renewal_time from distributed_lock dl where dl.expire_seconds < TIMESTAMPDIFF(SECOND, dl.renewal_time, NOW())", nativeQuery = true)
    List<DistributeLock> findAllTimeoutLocks();


    default DistributeLock addLock(String serviceKey, String lockKey, String owner, int expireSeconds) {
        DistributeLock lock = new DistributeLock(serviceKey, lockKey, owner, expireSeconds);
        return save(lock);
    }

    @Query(value = "delete from distributed_lock dl where dl.service_key = :serviceKey and dl.lock_key = :lockKey and dl.owner = :owner", nativeQuery = true)
    int deleteLock(@Param("serviceKey") String serviceKey, @Param("lockKey") String lockKey, @Param("owner") String owner);

    Optional<DistributeLock> findByServiceKeyAndLockKeyAndOwner(String serviceName, String lockKey, String owner);
}
