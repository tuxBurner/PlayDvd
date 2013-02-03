# --- !Ups
ALTER TABLE  `user` ADD  `password_reset_token` VARCHAR( 255 ) NULL


# --- !Downs
ALTER TABLE `user` DROP `password_reset_token`