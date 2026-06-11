alter table users add column allow_recommendation boolean not null default true;
alter table users add column allow_stranger_messages boolean not null default true;
alter table users add column allow_favorites boolean not null default true;
alter table users add column notify_messages boolean not null default true;
alter table users add column notify_interactions boolean not null default true;
alter table users add column notify_publish boolean not null default true;
alter table users add column notify_system boolean not null default true;
