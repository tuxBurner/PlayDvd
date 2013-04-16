# --- !Ups
ALTER TABLE  `dvd` ADD  `additional_info` VARCHAR( 255 ) NULL


# --- !Downs
ALTER TABLE `dvd` DROP `additional_info`