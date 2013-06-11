# --- !Ups
ALTER TABLE  `user` ADD  `rss_auth_key` VARCHAR( 255 ) NULL;
CREATE INDEX user_password_reset_token_IDX ON `user` (password_reset_token);
CREATE INDEX user_rss_auth_key_IDX ON `user` (rss_auth_key);



# --- !Downs
ALTER TABLE `user` DROP INDEX user_password_reset_token_IDX;
ALTER TABLE `user` DROP INDEX user_rss_auth_key_IDX;
ALTER TABLE `user` DROP `rss_auth_key`