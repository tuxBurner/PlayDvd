# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table dvd (
  id                        bigint auto_increment not null,
  title                     varchar(255),
  has_poster                tinyint(1) default 0,
  has_backdrop              tinyint(1) default 0,
  description               longtext,
  year                      integer not null,
  runtime                   integer,
  created_date              bigint not null,
  owner_id                  bigint,
  borrower_id               bigint,
  borrower_name             varchar(255),
  hull_nr                   integer,
  constraint pk_dvd primary key (id))
;

create table dvd_attibute (
  pk                        bigint auto_increment not null,
  attribute_type            varchar(10) not null,
  value                     varchar(255),
  constraint ck_dvd_attibute_attribute_type check (attribute_type in ('ACTOR','GENRE','DIRECTOR','BOX','COLLECTION')),
  constraint pk_dvd_attibute primary key (pk))
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


create table dvd_attibute_dvd (
  dvd_attibute_pk                bigint not null,
  dvd_id                         bigint not null,
  constraint pk_dvd_attibute_dvd primary key (dvd_attibute_pk, dvd_id))
;
alter table dvd add constraint fk_dvd_owner_1 foreign key (owner_id) references user (id) on delete restrict on update restrict;
create index ix_dvd_owner_1 on dvd (owner_id);
alter table dvd add constraint fk_dvd_borrower_2 foreign key (borrower_id) references user (id) on delete restrict on update restrict;
create index ix_dvd_borrower_2 on dvd (borrower_id);



alter table dvd_attibute_dvd add constraint fk_dvd_attibute_dvd_dvd_attib_01 foreign key (dvd_attibute_pk) references dvd_attibute (pk) on delete restrict on update restrict;

alter table dvd_attibute_dvd add constraint fk_dvd_attibute_dvd_dvd_02 foreign key (dvd_id) references dvd (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table dvd;

drop table dvd_attibute_dvd;

drop table dvd_attibute;

drop table setting;

drop table user;

SET FOREIGN_KEY_CHECKS=1;

