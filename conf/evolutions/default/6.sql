# --- !Ups
ALTER TABLE  `dvd` ADD  `asin_nr` VARCHAR( 255 ) NULL ,
ADD INDEX (  `asin_nr` )

# --- !Downs
ALTER TABLE `dvd` DROP `asin_nr`