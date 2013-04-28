# --- !Ups
create table bookmark (
  id                        bigint auto_increment not null,
  copy_id                   bigint,
  date                      bigint not null,
constraint pk_bookmark primary key (id))
;

create index ix_bookmark_1 on bookmark (copy_id);

# --- !Downs
DROP index ix_bookmark_1 on bookmark;
drop table bookmark;