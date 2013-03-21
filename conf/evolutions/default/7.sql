# --- !Ups
create table copy_reservation (
  id                        bigint auto_increment not null,
  borrower_id               bigint,
  copy_id                   bigint,
  date                      bigint not null,
constraint pk_copy_reservation primary key (id))
;

alter table copy_reservation add constraint fk_copy_reservation_borrower_1 foreign key (borrower_id) references user (id) on delete restrict on update restrict;
create index ix_copy_reservation_borrower_1 on copy_reservation (borrower_id);
alter table copy_reservation add constraint fk_copy_reservation_copy_2 foreign key (copy_id) references dvd (id) on delete restrict on update restrict;
create index ix_copy_reservation_copy_2 on copy_reservation (copy_id);

# --- !Downs
drop table copy_reservation;
DROP index ix_copy_reservation_borrower_1 on copy_reservation;
DROP index ix_copy_reservation_copy_2 on copy_reservation;