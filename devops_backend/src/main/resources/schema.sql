drop table if exists user;
CREATE TABLE "user" (
  user_id bigserial primary key,
  username text not null,
  email text not null,
  pw_hash text not null
);

drop table if exists follower;
create table follower (
  who_id integer,
  whom_id integer
);

drop table if exists message;
create table message (
  message_id bigserial primary key,
  author_id integer not null,
  text text not null,
  pub_date integer,
  flagged integer
);
