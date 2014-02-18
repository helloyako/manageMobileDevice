# --- Sample dataset

# --- !Ups

insert into team (name,manager_id) values ('JSBJ','helloyako');


insert into device (name,version,team_id,borrowed_date,borrowed_user_id,status) values ('iPhone 5','6.1.4',1,null,null,true);

insert into user (id,name,password,permission) values ('admin','관리자','6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b','admin');

# --- !Downs

delete from device;
delete from team;
delete from user;	