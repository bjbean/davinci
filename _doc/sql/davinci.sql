drop table `davinci`.`dashboard`;

create database IF NOT EXISTS `davinci`;

CREATE TABLE IF NOT EXISTS `davinci`.`dashboard` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(200) NOT NULL,
    `desc` VARCHAR(1000) NOT NULL,
    `publish` TINYINT(1) NOT NULL,
    `active` TINYINT(1) NOT NULL,
    `create_time` TIMESTAMP(6) NOT NULL,
    `create_by` BIGINT NOT NULL,
    `update_time` TIMESTAMP(6) NOT NULL,
    `update_by` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `name_UNIQUE` (`name` ASC))
ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `davinci`.`rel_dashboard_widget` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
    `dashboard_id` BIGINT NOT NULL,
    `widget_id` BIGINT NOT NULL,
    `position_x` INT NOT NULL,
    `position_y` INT NOT NULL,
    `length` INT NOT NULL,
    `width` INT NOT NULL,
    `active` TINYINT(1) NOT NULL,
    `create_time` TIMESTAMP(6) NOT NULL,
    `create_by` BIGINT NOT NULL,
    `update_time` TIMESTAMP(6) NOT NULL,
    `update_by` BIGINT NOT NULL,
    PRIMARY KEY (`id`))
ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `widget` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `widgetlib_id` bigint(20) NOT NULL,
  `bizlogic_id` bigint(20) NOT NULL,
  `name` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `olap_sql` varchar(2000) COLLATE utf8_unicode_ci DEFAULT NULL,
  `desc` varchar(1000) COLLATE utf8_unicode_ci NOT NULL,
  `trigger_type` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `trigger_params` varchar(1000) COLLATE utf8_unicode_ci NOT NULL,
  `chart_params` varchar(1000) COLLATE utf8_unicode_ci DEFAULT NULL,
  `publish` tinyint(1) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint(20) NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `update_by` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;



CREATE TABLE IF NOT EXISTS `davinci`.`widgetlib` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
    `type` VARCHAR(45) NOT NULL,
    `active` TINYINT(1) NOT NULL,
    `create_time` TIMESTAMP(6) NOT NULL,
    `create_by` BIGINT NOT NULL,
    `update_time` TIMESTAMP(6) NOT NULL,
    `update_by` BIGINT NOT NULL,
    PRIMARY KEY (`id`))
ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `source` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `connection_url` varchar(400) COLLATE utf8_unicode_ci NOT NULL,
  `desc` varchar(1000) COLLATE utf8_unicode_ci NOT NULL,
  `type` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `config` varchar(5000) COLLATE utf8_unicode_ci NOT NULL,
  `active` tinyint(1) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint(20) NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `update_by` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `bizlogic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `source_id` bigint(20) NOT NULL,
  `name` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `sql_tmpl` varchar(2000) COLLATE utf8_unicode_ci NOT NULL,
  `result_table` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `desc` varchar(1000) COLLATE utf8_unicode_ci NOT NULL,
  `active` tinyint(1) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint(20) NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `update_by` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;





CREATE TABLE IF NOT EXISTS `davinci`.`user` (
 `id` BIGINT NOT NULL AUTO_INCREMENT,
   `email` VARCHAR(200) NOT NULL,
   `password` VARCHAR(32) NOT NULL,
   `title` VARCHAR(100) NOT NULL DEFAULT '',
   `name` VARCHAR(200) NOT NULL,
   `admin` TINYINT(1) NOT NULL,
   `active` TINYINT(1) NOT NULL,
   `create_time` TIMESTAMP(6) NOT NULL,
   `create_by` BIGINT NOT NULL,
   `update_time` TIMESTAMP(6) NOT NULL,
   `update_by` BIGINT NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE INDEX `email_UNIQUE` (`email` ASC))
ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE IF NOT EXISTS `davinci`.`rel_user_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `group_id` BIGINT NOT NULL,
    `active` TINYINT(1) NOT NULL,
    `create_time` TIMESTAMP(6) NOT NULL,
    `create_by` BIGINT NOT NULL,
    `update_time` TIMESTAMP(6) NOT NULL,
    `update_by` BIGINT NOT NULL,
    PRIMARY KEY (`id`))
ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `user_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `desc` varchar(1000) DEFAULT NULL,
  `active` tinyint(1) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint(20) NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `update_by` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci


CREATE TABLE IF NOT EXISTS `davinci`.`rel_bizlogic_group` (
 `id` BIGINT NOT NULL AUTO_INCREMENT,
   `group_id` BIGINT NOT NULL,
   `bizlogic_id` BIGINT NOT NULL,
   `sql_params` VARCHAR(2000) NOT NULL,
   `active` TINYINT(1) NOT NULL,
   `create_time` TIMESTAMP(6) NOT NULL,
   `create_by` BIGINT NOT NULL,
   `update_time` TIMESTAMP(6) NOT NULL,
   `update_by` BIGINT NOT NULL,
   PRIMARY KEY (`id`))
ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE IF NOT EXISTS `davinci`.`log_sql` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sql_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `start_time` TIMESTAMP(6) NOT NULL,
    `end_time` TIMESTAMP(6) NOT NULL,
    `success` TINYINT(1) NULL,
    `error` VARCHAR(2000) NULL,
    PRIMARY KEY (`id`))
ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

