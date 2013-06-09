# --- !Ups
create table viewed_copy (
  id                        bigint auto_increment not null,
  copy_id                   bigint not null,
  user_id                   bigint not null,
  date                      bigint not null,
constraint pk_viewed_copy primary key (id));

alter table viewed_copy add constraint fk_viewed_copy_copy_1 foreign key (copy_id) references dvd (id) on delete restrict on update restrict;
create index ix_viewed_copy_1 on viewed_copy (copy_id);
alter table viewed_copy add constraint fk_viewed_copy_user_1 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_viewed_copy_1 on viewed_copy (user_id);

# --- !Downs
SET FOREIGN_KEY_CHECKS=0;
drop table viewed_copy;
SET FOREIGN_KEY_CHECKS=1;