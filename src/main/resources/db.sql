create database if not exists shortlink character set 'utf8mb4';

use shortlink;

CREATE TABLE `short_url` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `tag` varchar(10) NOT NULL COMMENT 'tag',
  `source_url` varchar(256) NOT NULL COMMENT 'source url',
  PRIMARY KEY (`id`),
  UNIQUE KEY `indx_tag` (`tag`) USING HASH COMMENT 'tag index'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

