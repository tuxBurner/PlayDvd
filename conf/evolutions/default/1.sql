# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table dvd (
  id                        bigint auto_increment not null,
  owner_id                  bigint,
  borrower_id               bigint,
  borrow_date               bigint,
  borrower_name             varchar(255),
  hull_nr                   integer,
  movie_id                  bigint,
  created_date              bigint not null,
  constraint pk_dvd primary key (id))
;

create table dvd_attibute (
  pk                        bigint auto_increment not null,
  attribute_type            varchar(10) not null,
  value                     varchar(255),
  constraint ck_dvd_attibute_attribute_type check (attribute_type in ('ACTOR','GENRE','DIRECTOR','BOX','COLLECTION')),
  constraint pk_dvd_attibute primary key (pk))
;

create table movie (
  id                        bigint auto_increment not null,
  title                     varchar(255),
  has_poster                tinyint(1) default 0,
  has_backdrop              tinyint(1) default 0,
  description               longtext,
  year                      integer not null,
  runtime                   integer,
  constraint pk_movie primary key (id))
;

create table setting (
  id                        bigint auto_increment not null,
  bundle                    varchar(255) not null,
  ke_y                      varchar(255) not null,
  value                     varchar(255),
  constraint pk_setting primary key (id))
;

create table user (
  id                        bigint auto_increment not null,
  user_name                 varchar(255),
  password                  varchar(255),
  email                     varchar(255),
  constraint uq_user_user_name unique (user_name),
  constraint pk_user primary key (id))
;


create table dvd_attibute_movie (
  dvd_attibute_pk                bigint not null,
  movie_id                       bigint not null,
  constraint pk_dvd_attibute_movie primary key (dvd_attibute_pk, movie_id))
;
alter table dvd add constraint fk_dvd_owner_1 foreign key (owner_id) references user (id) on delete restrict on update restrict;
create index ix_dvd_owner_1 on dvd (owner_id);
alter table dvd add constraint fk_dvd_borrower_2 foreign key (borrower_id) references user (id) on delete restrict on update restrict;
create index ix_dvd_borrower_2 on dvd (borrower_id);
alter table dvd add constraint fk_dvd_movie_3 foreign key (movie_id) references movie (id) on delete restrict on update restrict;
create index ix_dvd_movie_3 on dvd (movie_id);



alter table dvd_attibute_movie add constraint fk_dvd_attibute_movie_dvd_att_01 foreign key (dvd_attibute_pk) references dvd_attibute (pk) on delete restrict on update restrict;

alter table dvd_attibute_movie add constraint fk_dvd_attibute_movie_movie_02 foreign key (movie_id) references movie (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table dvd;

drop table dvd_attibute;

drop table dvd_attibute_movie;

drop table movie;

drop table setting;

drop table user;

SET FOREIGN_KEY_CHECKS=1;

