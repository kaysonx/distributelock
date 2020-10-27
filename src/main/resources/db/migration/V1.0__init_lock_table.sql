CREATE TABLE `distributed_lock`
(
    `id`             int         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `service_key`    varchar(64) NOT NULL COMMENT 'Service Key',
    `lock_key`       varchar(64) NOT NULL COMMENT 'Lock Key',
    `owner`          char(36)    NOT NULL COMMENT 'Owner',
    `expire_seconds` int         NOT NULL COMMENT 'Timeout/Second',
    `create_time`    timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `u_key_service_key_lock_key_owner` (`service_key`, `lock_key`, `owner`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Distribute Lock'