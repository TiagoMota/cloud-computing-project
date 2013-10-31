SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `cloudocr_db` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `cloudocr_db` ;

-- -----------------------------------------------------
-- Table `cloudocr_db`.`JobStatus`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cloudocr_db`.`JobStatus` ;

CREATE TABLE IF NOT EXISTS `cloudocr_db`.`JobStatus` (
  `jobstatus` TINYINT NOT NULL,
  `jobstatus_name` VARCHAR(31) NOT NULL,
  UNIQUE INDEX `statusname_UNIQUE` (`jobstatus_name` ASC),
  PRIMARY KEY (`jobstatus`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cloudocr_db`.`Job`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cloudocr_db`.`Job` ;

CREATE TABLE IF NOT EXISTS `cloudocr_db`.`Job` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `filename` VARCHAR(255) NOT NULL,
  `filesize` BIGINT NOT NULL,
  `priority` TINYINT NOT NULL DEFAULT 0,
  `num_failures` TINYINT NOT NULL DEFAULT 0,
  `submission_time` DATETIME NOT NULL,
  `jobstatus` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  INDEX `FK_Job_JobStatus_idx` (`jobstatus` ASC),
  INDEX `IDX_jobsearch` (`priority` ASC, `submission_time` ASC),
  CONSTRAINT `FK_Job_JobStatus`
    FOREIGN KEY (`jobstatus`)
    REFERENCES `cloudocr_db`.`JobStatus` (`jobstatus`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cloudocr_db`.`Assignment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cloudocr_db`.`Assignment` ;

CREATE TABLE IF NOT EXISTS `cloudocr_db`.`Assignment` (
  `job_id` INT NOT NULL,
  `worker_instanceid` VARCHAR(15) NOT NULL,
  `order` INT NOT NULL DEFAULT 0,
  INDEX `FK_Assignment_Job_idx` (`job_id` ASC),
  UNIQUE INDEX `UNIQUE_ASSIGNMENT` (`worker_instanceid` ASC, `job_id` ASC),
  CONSTRAINT `FK_Assignment_Job`
    FOREIGN KEY (`job_id`)
    REFERENCES `cloudocr_db`.`Job` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `cloudocr_db`.`JobStatus`
-- -----------------------------------------------------
START TRANSACTION;
USE `cloudocr_db`;
INSERT INTO `cloudocr_db`.`JobStatus` (`jobstatus`, `jobstatus_name`) VALUES (1, 'submitted');
INSERT INTO `cloudocr_db`.`JobStatus` (`jobstatus`, `jobstatus_name`) VALUES (2, 'completed');
INSERT INTO `cloudocr_db`.`JobStatus` (`jobstatus`, `jobstatus_name`) VALUES (3, 'failed');

COMMIT;

