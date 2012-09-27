# --- !Ups
ALTER TABLE  `dvd` ADD  `ean_nr` VARCHAR( 255 ) NOT NULL ,
ADD INDEX (  `ean_nr` )

# --- !Downs
ALTER TABLE `dvd` DROP `ean_nr`