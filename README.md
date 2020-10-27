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
