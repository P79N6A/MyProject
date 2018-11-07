CREATE DATABASE msgp
  DEFAULT CHARSET utf8;
USE msgp;

CREATE TABLE IF NOT EXISTS trace_log (
  id            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  trace_id      VARCHAR(64)  NOT NULL DEFAULT ''
  COMMENT '请求ID',
  span_id       VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '调用链ID',
  span_name     VARCHAR(255) NOT NULL DEFAULT ''
  COMMENT '请求标识',
  local_appkey  VARCHAR(128) NOT NULL DEFAULT ''
  COMMENT '本地appkey',
  local_host    VARCHAR(128) NOT NULL DEFAULT ''
  COMMENT '本地节点hostname/ip',
  local_port    INT(11)      NOT NULL DEFAULT 0
  COMMENT '本地实例侦听端口',
  remote_appkey VARCHAR(128) NOT NULL DEFAULT ''
  COMMENT '远程appkey',
  remote_host   VARCHAR(128) NOT NULL DEFAULT ''
  COMMENT '远程节点ip或域名',
  remote_port   INT(11)      NOT NULL DEFAULT 0
  COMMENT '远程端口',
  start         BIGINT(20)   NOT NULL DEFAULT 0
  COMMENT '时间戳，unixtime，毫秒级',
  cost          INT(11)      NOT NULL DEFAULT 0
  COMMENT '耗时，毫秒',
  source        INT(11)      NOT NULL DEFAULT 0,
  status        INT(11)      NOT NULL DEFAULT 0,
  cnt           INT(11)      NOT NULL DEFAULT 1,
  debug         INT(11)      NOT NULL DEFAULT 1,
  extend        VARCHAR(255) NOT NULL DEFAULT ''
  COMMENT 'extend',
  PRIMARY KEY (id),
  KEY idx_trace (trace_id),
  KEY idx_time (START)
)
  ENGINE =INNODB
  DEFAULT CHARSET =utf8
  COMMENT ='调用链原始日志';

/* 存储sg_agent日志信息 */
CREATE TABLE IF NOT EXISTS sg_agent_log (
  `id`          BIGINT(20)    	NOT NULL AUTO_INCREMENT,
  `appkey`      VARCHAR(128)  	NOT NULL DEFAULT '',
  `time`        BIGINT(22)   	  NOT NULL DEFAULT 0,
  `level`    	  INT(11)  	      NOT NULL DEFAULT 0,
  `content`	    text(65535)     NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_time` (`time`))
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8;

CREATE TABLE IF NOT EXISTS octo_log (
  `id`       BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `appkey`   VARCHAR(128) NOT NULL DEFAULT '',
  `time`     BIGINT(22)   NOT NULL DEFAULT 0,
  `level`    INT(11)      NOT NULL DEFAULT 0,
  `category` VARCHAR(128) NOT NULL DEFAULT '',
  `content`  TEXT(65535)  NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_time` (`time`))
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8;
