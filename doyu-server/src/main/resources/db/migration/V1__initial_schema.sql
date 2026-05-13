create table users (
    id varchar(64) primary key,
    phone varchar(64) not null unique,
    nickname varchar(80) not null,
    avatar_file_id varchar(64),
    bio varchar(500),
    age_group varchar(32) not null,
    is_minor boolean not null,
    real_name_status varchar(32) not null,
    account_status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table auth_tokens (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    refresh_token_hash varchar(128) not null unique,
    revoked boolean not null,
    expires_at timestamp not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table admin_users (
    id varchar(64) primary key,
    username varchar(80) not null unique,
    password_hash varchar(128) not null,
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table follows (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    target_user_id varchar(64) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    unique (user_id, target_user_id)
);

create table file_assets (
    id varchar(64) primary key,
    owner_id varchar(64) not null,
    usage varchar(32) not null,
    storage_key varchar(255) not null unique,
    mime_type varchar(120) not null,
    size_bytes bigint not null,
    width int,
    height int,
    audit_status varchar(32) not null,
    public_url varchar(500),
    created_at timestamp not null,
    updated_at timestamp not null
);

create table posts (
    id varchar(64) primary key,
    author_id varchar(64) not null,
    title varchar(120),
    content varchar(3000) not null,
    media_file_ids varchar(1000),
    topic_ids varchar(1000),
    linked_pattern_id varchar(64),
    status varchar(32) not null,
    like_count int not null,
    favorite_count int not null,
    comment_count int not null,
    pinned boolean not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table comments (
    id varchar(64) primary key,
    post_id varchar(64) not null,
    author_id varchar(64) not null,
    parent_id varchar(64),
    content varchar(1000) not null,
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table likes (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    target_type varchar(32) not null,
    target_id varchar(64) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    unique (user_id, target_type, target_id)
);

create table favorites (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    target_type varchar(32) not null,
    target_id varchar(64) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    unique (user_id, target_type, target_id)
);

create table pattern_jobs (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    input_file_id varchar(64) not null,
    bead_size varchar(32) not null,
    target_size varchar(64) not null,
    difficulty varchar(32) not null,
    palette_id varchar(64) not null,
    style varchar(32) not null,
    status varchar(32) not null,
    failure_reason varchar(500),
    pattern_id varchar(64),
    retryable boolean,
    quota_refunded boolean,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table pattern_assets (
    id varchar(64) primary key,
    job_id varchar(64) not null,
    owner_id varchar(64) not null,
    preview_file_id varchar(64) not null,
    grid_file_id varchar(64) not null,
    color_map_file_id varchar(64) not null,
    pdf_file_id varchar(64),
    bead_size varchar(32) not null,
    width_cells int not null,
    height_cells int not null,
    total_beads int not null,
    materials_json text not null,
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table products (
    id varchar(64) primary key,
    type varchar(32) not null,
    seller_id varchar(64),
    title varchar(160) not null,
    description varchar(3000),
    category_id varchar(64),
    status varchar(32) not null,
    audit_status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table skus (
    id varchar(64) primary key,
    product_id varchar(64) not null,
    spec_name varchar(120) not null,
    price_cent int not null,
    stock int not null,
    locked_stock int not null,
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table cart_items (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    sku_id varchar(64) not null,
    quantity int not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    unique (user_id, sku_id)
);

create table orders (
    id varchar(64) primary key,
    buyer_id varchar(64) not null,
    seller_type varchar(32) not null,
    seller_id varchar(64),
    order_type varchar(32) not null,
    status varchar(32) not null,
    total_amount_cent int not null,
    payable_amount_cent int not null,
    address_snapshot text not null,
    expires_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table order_items (
    id varchar(64) primary key,
    order_id varchar(64) not null,
    sku_id varchar(64) not null,
    product_id varchar(64) not null,
    quantity int not null,
    price_cent int not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table payments (
    id varchar(64) primary key,
    order_id varchar(64) not null,
    channel varchar(32) not null,
    status varchar(32) not null,
    amount_cent int not null,
    channel_trade_no varchar(128),
    paid_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table refunds (
    id varchar(64) primary key,
    order_id varchar(64) not null,
    payment_id varchar(64) not null,
    amount_cent int not null,
    reason varchar(500) not null,
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table conversations (
    id varchar(64) primary key,
    user_a_id varchar(64) not null,
    user_b_id varchar(64) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table messages (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    conversation_id varchar(64),
    type varchar(32) not null,
    title varchar(160) not null,
    content varchar(2000) not null,
    read_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table reward_accounts (
    id varchar(64) primary key,
    user_id varchar(64) not null unique,
    points int not null,
    experience int not null,
    level_code varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table checkin_records (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    checkin_date date not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    unique (user_id, checkin_date)
);

create table reward_ledgers (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    type varchar(32) not null,
    amount int not null,
    reason varchar(120) not null,
    related_id varchar(64),
    created_at timestamp not null,
    updated_at timestamp not null
);

create table reports (
    id varchar(64) primary key,
    reporter_id varchar(64) not null,
    target_type varchar(32) not null,
    target_id varchar(64) not null,
    reason varchar(120) not null,
    description varchar(1000),
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table moderation_records (
    id varchar(64) primary key,
    target_type varchar(32) not null,
    target_id varchar(64) not null,
    result varchar(32) not null,
    reason varchar(500),
    operator_type varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table admin_operation_logs (
    id varchar(64) primary key,
    admin_id varchar(64) not null,
    action varchar(80) not null,
    target_type varchar(32) not null,
    target_id varchar(64) not null,
    before_state varchar(120),
    after_state varchar(120),
    reason varchar(500),
    created_at timestamp not null,
    updated_at timestamp not null
);

create table idempotency_records (
    id varchar(64) primary key,
    user_id varchar(64),
    idempotency_key varchar(160) not null,
    operation varchar(64) not null,
    request_hash varchar(128) not null,
    response_body text not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    unique (user_id, idempotency_key, operation)
);
