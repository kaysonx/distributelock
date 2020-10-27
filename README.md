# Getting Started

### 运行环境
docker目录下可以启动本地数据库
```
docker-compose up -d
```

### 测试Case
在测试的Demo目录下

### 功能模块
lock: 使用数据库实现了分布式锁，同一业务标识互斥(比如Service Key一致，Owner一致，方法名称和参数构成的Lock Key一致)  
cleaner: 定期清理过期的Lock，在业务线程崩溃的情况下，可能无法释放锁，需要定期清理  
watchdog: 对业务线程加的锁，自动续约，直到业务线程完成手动释放锁、或者业务线程挂掉，锁过期自动释放  


### 业务使用说明

假设一下是业务方法里面的逻辑：
```Java
boolean lock = lockProvider.lock(serviceKey, lockKey, owner, expireSeconds);
if (lock) {
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
```
1. 同时调动这个方法互斥，只有获取锁才能执行业务逻辑
2. RPC请求重试没有影响，不影响锁，因为有watch dog机制，会一直持有锁
3. RPC失败/本地逻辑异常，都会到Finally释放锁
4. 业务处理超时会自动续期，知道业务处理关闭或者挂掉
5. 业务崩溃，watch dog也会关闭，等待超时后锁即可释放