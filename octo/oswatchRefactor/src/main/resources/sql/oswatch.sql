CREATE DATABASE inf_oswatch
  DEFAULT CHARSET utf8;
USE inf_oswatch;

CREATE TABLE oswatch_monitor_policy (
  id                      BIGINT(20)     NOT NULL  AUTO_INCREMENT,
  #   monitorPolicyiId BIGINT(20)   NOT  NULL  DEFAULT 0,
  appkey                   VARCHAR(128)  NOT NULL  DEFAULT '',
  idc                      VARCHAR(128),
  env                      INT           NOT NULL  DEFAULT 0
  COMMENT 'env：PROD(3), STAGE(2), TEST(1)',
  gteType                  INT           NOT NULL  DEFAULT 0
  COMMENT '0(大于) 1(小于)',
  watchPeriod              INT           NOT NULL  DEFAULT 0,
  monitor_type             INT           NOT NULL
  COMMENT 'CPU(0), MEM(1), QPS(2), TP50(3), TP90(4), TP95(5), TP99(6)',
  monitorValue DOUBLE      NOT NULL,
  span_name                VARCHAR(256)
  COMMENT 'for qps and metrics',
  responseUrl              VARCHAR(512)   NOT NULL  DEFAULT ''
  COMMENT 'response URL',
  provider_count_switch    INT(11)        NOT NULL DEFAULT 0
  COMMENT   '计算appkey Proivder有效节点开关： = 0 表示by ip＋port  = 1 表示by ip',
  PRIMARY KEY (id)
)
  ENGINE = INNODB
  DEFAULT CHARSET = utf8
  COMMENT = 'monitor policy';
