create table if not exists topics (
    id varchar(64) primary key,
    name varchar(80) not null unique,
    description varchar(500),
    post_count int not null default 0,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists comment_mentions (
    id varchar(64) primary key,
    comment_id varchar(64) not null,
    user_id varchar(64) not null,
    created_at timestamp not null,
    unique (comment_id, user_id)
);

create table if not exists comment_topics (
    id varchar(64) primary key,
    comment_id varchar(64) not null,
    topic_id varchar(64) not null,
    created_at timestamp not null,
    unique (comment_id, topic_id)
);

create table if not exists sticker_packs (
    id varchar(64) primary key,
    name varchar(80) not null,
    sort_order int not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists stickers (
    id varchar(64) primary key,
    pack_id varchar(64) not null,
    name varchar(80) not null,
    emoji_text varchar(80),
    image_url varchar(500),
    sort_order int not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists comment_stickers (
    id varchar(64) primary key,
    comment_id varchar(64) not null,
    sticker_id varchar(64) not null,
    created_at timestamp not null,
    unique (comment_id, sticker_id)
);

create index if not exists idx_topics_name on topics (name);
create index if not exists idx_comment_mentions_comment on comment_mentions (comment_id);
create index if not exists idx_comment_topics_comment on comment_topics (comment_id);
create index if not exists idx_comment_stickers_comment on comment_stickers (comment_id);
create index if not exists idx_stickers_pack on stickers (pack_id, sort_order);
