# --- !Ups
ALTER TABLE  `user` ADD  `has_gravatar` tinyint(1) default 0 not null;
ALTER TABLE  `movie` ADD  `updated_date` bigint NULL;
ALTER TABLE  `movie` ADD  `imdb_rating` VARCHAR( 25 ) NULL;

# --- !Downs
ALTER TABLE `user` DROP `has_gravatar`;
ALTER TABLE `movie` DROP `updated_date`;
ALTER TABLE `movie` DROP `imdb_rating`;