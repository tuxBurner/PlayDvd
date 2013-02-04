# --- !Ups
ALTER TABLE  `movie` ADD  `imdb_id` VARCHAR( 25 ) NULL,
                     ADD  `grabber_id` VARCHAR( 25 ) NULL,
                     ADD  `grabber_type` VARCHAR( 15 ) default 'NONE' not null,
                     ADD constraint ck_movie_grabber_type check (grabber_type in ('THETVDB','TMDB','NONE'));
CREATE index ix_movie_grabber_idtype on movie (grabber_id,grabber_type);



# --- !Downs
ALTER TABLE `movie` DROP `imdb_id`,
                   DROP `grabber_id`,
                   DROP `grabber_type`;
DROP index ix_movie_grabber_idtype on movie;
