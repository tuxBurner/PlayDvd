# --- !Ups
ALTER TABLE dvd_attribute
ADD CONSTRAINT ck_dvd_attribute_attribute_type check (attribute_type in ('BOX','COLLECTION','RATING','COPY_TYPE','AUDIO_TYPE'))

# --- !Downs
ALTER TABLE dvd_attribute
ADD CONSTRAINT ck_dvd_attribute_attribute_type check (attribute_type in ('BOX','COLLECTION','RATING','COPY_TYPE'))