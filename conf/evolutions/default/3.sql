# --- !Ups
ALTER TABLE  `user` ADD  `default_copy_type` VARCHAR( 255 ) NULL


# --- !Downs
ALTER TABLE `user` DROP `default_copy_type`