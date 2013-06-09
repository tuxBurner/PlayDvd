# --- !Ups
alter table bookmark add constraint fk_bookmark_copy_1 foreign key (copy_id) references dvd (id) on delete restrict on update restrict;

# --- !Downs