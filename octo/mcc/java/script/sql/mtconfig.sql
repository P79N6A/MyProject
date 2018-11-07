# TODO 实现app限定功能后去掉
# CREATE TABLE app (
#   `id` int(11) NOT NULL AUTO_INCREMENT,
#   `appkey` varchar(64) COLLATE utf8_bin NOT NULL COMMENT 'appkey',
#   `secret` varchar(64) COLLATE utf8_bin NOT NULL COMMENT 'secret',
#   `create_time` datetime DEFAULT NULL COMMENT '添加时间',
#   `update_time` datetime DEFAULT NULL COMMENT '修改时间',
#   PRIMARY KEY (`id`),
#   UNIQUE KEY `idx_client` (`appkey`)
# ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='API认证的clientId和secret';