CREATE database config CHARACTER SET = utf8;

use config;

CREATE TABLE `config_admin` (
  `id` INT(12) NOT NULL AUTO_INCREMENT,
  `user_id` INT(12) NOT NULL COMMENT 'UID',
  `operator_id` INT(12) NOT NULL COMMENT '操作人员uid',
  `status` INT(12) NOT NULL COMMENT '状态：0正常，128删除',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX user_id_idx (`user_id`)
)
ENGINE =InnoDB DEFAULT CHARSET =utf8 COMMENT='超级管理员';


CREATE TABLE `space_admin` (
  `id` INT(12) NOT NULL AUTO_INCREMENT,
  `user_id` INT(12) NOT NULL COMMENT 'UID',
  `space_name` VARCHAR(512) NOT NULL COMMENT '空间名',
  `operator_id` INT(12) NOT NULL COMMENT '操作人员uid',
  `status` INT(12) NOT NULL COMMENT '状态：0正常，128删除',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX user_id_idx (`user_id`),
  INDEX space_name_idx (`space_name`)
)
ENGINE =InnoDB DEFAULT CHARSET =utf8 COMMENT='空间管理员';

CREATE TABLE `client_sync_log` (
  `id` INT(12) NOT NULL AUTO_INCREMENT,
  `node` VARCHAR(512) NOT NULL COMMENT '同步请求的nodeName',
  `ip` VARCHAR(32) NOT NULL COMMENT '客户端ip',
  `pid` INT(12) NOT NULL COMMENT '客户端进程号',
  `version` BIGINT(12) NOT NULL COMMENT '同步的版本',
  `sync_time` DATETIME NOT NULL COMMENT '同步时间',
  `host` VARCHAR(128) COMMENT '客户端hostName，便于查看',
  PRIMARY KEY (`id`),
  INDEX node_idx (`node`(100))
)
ENGINE =InnoDB DEFAULT CHARSET =utf8 COMMENT='客户端同步日志';

alter table client_sync_log add index `ip_pid_node_idx`(`ip`,`pid`,`node`(100));

CREATE TABLE IF NOT EXISTS `pull_request` (
  `pr_id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `note` VARCHAR(2000) NULL COMMENT 'PR备注',
  `pr_misid` VARCHAR(12) NULL COMMENT '提PR的misID',
  `status` INT NULL DEFAULT 0 COMMENT '0：open，默认值; \n1：代表已经merge，merge后不可更改\n-1: decline',
  `appkey` VARCHAR(150) NULL COMMENT '',
  `env` INT NULL DEFAULT 3 COMMENT '1:test\n2:stage\n3:prod',
  `pr_time` DATETIME NULL COMMENT '',
  PRIMARY KEY (`pr_id`)  COMMENT '')
  ENGINE = InnoDB

CREATE TABLE IF NOT EXISTS `config`.`pull_request_detail` (
  `pr_detail_id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `pr_id` INT NULL COMMENT 'pr编号',
  `modified_key` VARCHAR(200) NULL COMMENT '',
  `old_value` VARCHAR(5000) NULL COMMENT '',
  `new_value` VARCHAR(5000) NULL COMMENT '',
  `old_comment` VARCHAR(1000) NULL COMMENT '',
  `new_comment` VARCHAR(1000) NULL COMMENT '',
  `is_deleted` TINYINT(1) NULL COMMENT '',
  PRIMARY KEY (`pr_detail_id`)  COMMENT '')
  ENGINE = InnoDB


CREATE TABLE IF NOT EXISTS `config`.`review` (
  `review_id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `pr_id` INT NULL COMMENT '',
  `reviewer_misid` VARCHAR(12) NULL COMMENT '',
  `note` VARCHAR(2000) NULL COMMENT '',
  `review_time` DATETIME NULL COMMENT '',
  `approve` INT NULL DEFAULT 0 COMMENT '1:approve\n0:默认\n-1：decline',
  PRIMARY KEY (`review_id`)  COMMENT '')
  ENGINE = InnoDB


CREATE TABLE `config_rollback` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `path` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '''/appkey/env''',
  `content` mediumtext COLLATE utf8mb4_unicode_ci COMMENT 'kv的json',
  `user` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建该版本的用户',
  `note` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '该版本的说明',
  `time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci

CREATE TABLE `config_trash` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `path` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '''/appkey/env''',
  `content` mediumtext COLLATE utf8mb4_unicode_ci COMMENT 'kv的json',
  `user` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回滚配置，移除该版本的用户',
  `note` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '该版本的说明',
  `time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;