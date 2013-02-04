# --- !Ups
ALTER TABLE  `movie` ADD  `imdb_id` VARCHAR( 25 ) NULL,
                     ADD  `grabber_id` VARCHAR( 25 ) NULL,
                     ADD  `grabber_type` VARCHAR( 15 ) default 'NONE' not null,
                     ADD constraint ck_movie_grabber_type check (grabber_type in ('THETVDB','TMDB','NONE'))


# --- !Downs
ALTER TABLE `user` DROP `imdb_id`,
                   DROP `grabber_id`,
                   DROP `grabber_type`