-- A notification is created from exactly one event: source_message_id is the messageId of the
-- consumed message and unique, so a redelivered event creates no second notification.
-- Rows are never removed: deleting is per Receiver (notification_receiver.deleted_at), and the
-- row keeps the event's messageId so that a late redelivery cannot bring a deleted notification back.
create table notification (
    notification_id   uuid primary key,
    source_message_id uuid not null unique,
    title             text not null check (length(title) between 1 and 200),
    text              text not null check (length(text) between 1 and 2000),
    link              text not null,
    created_at        timestamptz not null
);

-- One row per Receiver (a Cook id): their own read status and their own deletion.
create table notification_receiver (
    notification_id uuid not null references notification (notification_id),
    receiver        uuid not null,
    status          text not null check (status in ('NEW', 'READ')),
    deleted_at      timestamptz,
    primary key (notification_id, receiver)
);

create index notification_receiver_receiver_idx on notification_receiver (receiver) where deleted_at is null;
