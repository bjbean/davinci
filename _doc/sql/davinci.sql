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
ENGINE = InnoDB;

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
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `davinci`.`widget` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
    `widgetlib_id` BIGINT NOT NULL,
    `bizlogic_id` BIGINT NULL,
    `name` VARCHAR(200) NOT NULL,
    `desc` VARCHAR(1000) NOT NULL,
    `trigger_type` VARCHAR(45) NOT NULL,
    `trigger_params` VARCHAR(1000) NOT NULL,
    `publish` TINYINT(1) NOT NULL,
    `active` TINYINT(1) NOT NULL,
    `create_time` TIMESTAMP(6) NOT NULL,
    `create_by` BIGINT NOT NULL,
    `update_time` TIMESTAMP(6) NOT NULL,
    `update_by` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `name_UNIQUE` (`name` ASC))
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `davinci`.`widgetlib` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
    `type` VARCHAR(45) NOT NULL,
    `active` TINYINT(1) NOT NULL,
    `create_time` TIMESTAMP(6) NOT NULL,
    `create_by` BIGINT NOT NULL,
    `update_time` TIMESTAMP(6) NOT NULL,
    `update_by` BIGINT NOT NULL,
    PRIMARY KEY (`id`))
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `davinci`.`source` (
 `id` BIGINT NOT NULL AUTO_INCREMENT,
   `name` VARCHAR(200) NOT NULL,
   `desc` VARCHAR(1000) NOT NULL,
   `type` VARCHAR(45) NOT NULL,
   `config` VARCHAR(5000) NOT NULL,
   `active` TINYINT(1) NOT NULL,
   `create_time` TIMESTAMP(6) NOT NULL,
   `create_by` BIGINT NOT NULL,
   `update_time` TIMESTAMP(6) NOT NULL,
   `update_by` BIGINT NOT NULL,
   PRIMARY KEY (`id`))
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `davinci`.`bizlogic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
    `source_id` BIGINT NOT NULL,
    `name` VARCHAR(200) NOT NULL,
    `desc` VARCHAR(1000) NOT NULL,
    `active` TINYINT(1) NOT NULL,
    `create_time` TIMESTAMP(6) NOT NULL,
    `create_by` BIGINT NOT NULL,
    `update_time` TIMESTAMP(6) NOT NULL,
    `update_by` BIGINT NOT NULL,
    PRIMARY KEY (`id`))
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `davinci`.`sql` (
 `id` BIGINT NOT NULL AUTO_INCREMENT,
   `bizlogic_id` BIGINT NOT NULL,
   `name` VARCHAR(200) NOT NULL,
   `sql_type` VARCHAR(45) NOT NULL,
   `sql_tmpl` VARCHAR(2000) NOT NULL,
   `sql_order` INT NOT NULL,
   `desc` VARCHAR(1000) NOT NULL,
   `active` TINYINT(1) NOT NULL,
   `create_time` TIMESTAMP(6) NOT NULL,
   `create_by` BIGINT NOT NULL,
   `update_time` TIMESTAMP(6) NOT NULL,
   `update_by` BIGINT NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE INDEX `name_UNIQUE` (`name` ASC))
ENGINE = InnoDB;


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
ENGINE = InnoDB;


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
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `davinci`.`group` (
 `id` BIGINT NOT NULL AUTO_INCREMENT,
   `name` VARCHAR(200) NOT NULL,
   `desc` VARCHAR(1000) NOT NULL,
   `active` TINYINT(1) NOT NULL,
   `create_time` TIMESTAMP(6) NOT NULL,
   `create_by` BIGINT NOT NULL,
   `update_time` TIMESTAMP(6) NOT NULL,
   `update_by` BIGINT NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE INDEX `name_UNIQUE` (`name` ASC))
ENGINE = InnoDB;


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
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `davinci`.`log_sql` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sql_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `start_time` TIMESTAMP(6) NOT NULL,
    `end_time` TIMESTAMP(6) NOT NULL,
    `success` TINYINT(1) NULL,
    `error` VARCHAR(2000) NULL,
    PRIMARY KEY (`id`))
ENGINE = InnoDB;

