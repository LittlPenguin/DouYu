create table if not exists notifications (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    type varchar(32) not null,
    title varchar(160) not null,
    content varchar(2000) not null,
    read_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null
);

insert into notifications (id, user_id, type, title, content, read_at, created_at, updated_at)
select m.id, m.user_id, m.type, m.title, m.content, m.read_at, m.created_at, m.updated_at
from messages m
where m.type <> 'PRIVATE'
  and not exists (
      select 1
      from notifications n
      where n.id = m.id
  );

create index if not exists idx_notifications_user_created_at on notifications (user_id, created_at);

alter table if exists users drop column if exists allow_stranger_messages;
alter table if exists users drop column if exists notify_messages;

drop table if exists messages;
drop table if exists conversations;
drop table if exists auth_tokens;
drop table if exists refresh_tokens;
drop table if exists reward_ledgers;
drop table if exists checkin_records;
drop table if exists reward_accounts;
drop table if exists reports;
drop table if exists moderation_records;
drop table if exists admin_operation_logs;
drop table if exists admin_users;

drop table if exists ai_usage;
drop table if exists refunds;
drop table if exists payments;
drop table if exists pattern_assets;
drop table if exists pattern_jobs;
