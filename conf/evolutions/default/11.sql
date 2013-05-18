# --- !Ups
create table movie_image (
  id                        bigint auto_increment not null,
  size                      varchar(22) not null,
  type                      varchar(8) not null,
  store_type                varchar(5) not null,
  movie_id                  bigint,
  constraint ck_movie_image_size check (size in ('ORIGINAL','SMALL','SMALL_LIST_VIEW','GRABBER_POSTER_SMALL','GRABBER_BACKDROP_SMALL','SELECT2','TINY','BACKCKDROP_POPUP_SIZE')),
  constraint ck_movie_image_type check (type in ('POSTER','BACKDROP')),
  constraint ck_movie_image_store_type check (store_type in ('LOCAL','S3')),
  constraint pk_movie_image primary key (id))
;

alter table movie_image add constraint fk_movie_image_movie_8 foreign key (movie_id) references movie (id) on delete restrict on update restrict;
create index ix_movie_image_movie_8 on movie_image (movie_id);

# --- !Downs
SET FOREIGN_KEY_CHECKS=0;

drop table movie_image;

SET FOREIGN_KEY_CHECKS=1;