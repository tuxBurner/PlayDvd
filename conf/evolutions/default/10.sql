# --- !Ups
create table bookmark (
  id                        bigint auto_increment not null,
  copy_id                   bigint,
  date                      bigint not null,
constraint pk_bookmark primary key (id))
;

--alter table mark_to_view add constraint fk_mark_to_view_1 foreign key (copy_id) references dvd (id) on delete restrict on update restrict;
create index ix_bookmark_1 on bookmark (copy_id);

# --- !Downs
DROP index ix_bookmark_1 on bookmark;
drop table bookmark;