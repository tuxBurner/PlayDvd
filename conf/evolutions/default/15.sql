# --- !Ups
ALTER TABLE  `user` ADD  `has_gravatar` tinyint(1) default 0 not null;

# --- !Downs
ALTER TABLE `user` DROP `has_gravatar`