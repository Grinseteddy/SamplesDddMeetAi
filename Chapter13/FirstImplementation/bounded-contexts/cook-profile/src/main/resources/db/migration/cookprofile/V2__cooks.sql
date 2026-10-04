-- A cook's id is the cookId of the registered user (claim of the access token), not generated here.
-- Deregistering deletes the row (the contract's DELETE is permanent).
create table cook (
    cook_id      uuid primary key,
    email        varchar(254) not null check (length(email) > 0),
    name         varchar(100) not null check (length(name) > 0),
    given_name   varchar(100) not null check (length(given_name) > 0),
    member_since date not null,
    status       varchar(16) not null check (status in ('ACTIVE', 'INACTIVE', 'PREMIUM'))
);

-- One email address belongs to at most one cook, regardless of case.
create unique index cook_email_uq on cook (lower(email));
create index cook_name_idx on cook (name);
