# --- First database schema

# --- !Ups

set ignorecase true;

create table team (
  id                        bigint not null AUTO_INCREMENT,
  name                      varchar(255) not null,
  manager_id				varchar(255) not null,
  constraint pk_team primary key (id))
;

create table device (
  id                        bigint not null AUTO_INCREMENT,
  name                      varchar(255) not null,
  version					varchar(255),
  team_id 		            bigint,
  borrowed_date             timestamp,
  borrowed_user_id			varchar(255),
  status					boolean DEFAULT true,
  constraint pk_device primary key (id))
;

create table user (
  id               		    varchar(128) not null,
  name                      varchar(128) not null,
  password                  char(65) not null,
  permission                varchar(128) not null,
  constraint pk_user primary key (id))
;

create sequence team_seq start with 1000;

create sequence device_seq start with 1000;

alter table device add constraint fk_device_team_1 foreign key (team_id) references team (id) on delete restrict on update restrict;
alter table device add constraint fk_device_user_1 foreign key (borrowed_user_id) references user (id) on delete restrict on update restrict;
create index ix_device_team_1 on device (team_id);


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists team;

drop table if exists device;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists team_seq;

drop sequence if exists device_seq;

