alter table users add column email varchar(160);
alter table users add column password_hash varchar(128);

update users
set email = lower(coalesce(nullif(phone, ''), id) || '@legacy.local')
where email is null;

alter table users alter column phone drop not null;
alter table users alter column email set not null;

create unique index ux_users_email on users (email);
