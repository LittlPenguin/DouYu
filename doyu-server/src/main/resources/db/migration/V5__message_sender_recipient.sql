alter table messages add column if not exists sender_id varchar(64);
alter table messages add column if not exists recipient_id varchar(64);

create index if not exists idx_messages_conversation_created_at on messages (conversation_id, created_at);
create index if not exists idx_messages_conversation_sender on messages (conversation_id, sender_id);
