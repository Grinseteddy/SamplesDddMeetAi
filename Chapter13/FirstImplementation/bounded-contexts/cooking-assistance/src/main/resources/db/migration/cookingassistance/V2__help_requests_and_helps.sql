-- A help request refers to recipe, how-to step and ingredients of Recipe Catalog by id only (no
-- foreign keys across Bounded Contexts). The type-dependent rules are checked by the aggregate.
create table help_request (
    help_request_id uuid primary key,
    requester       uuid        not null,
    title           text        not null check (length(title) between 1 and 200),
    type            text        not null,
    description     text        not null check (length(description) between 1 and 2000),
    recipe_id       uuid,
    how_to_step_id  uuid,
    status          text        not null check (status in ('OPEN', 'ANSWERED')),
    created_at      timestamptz not null,
    updated_at      timestamptz not null
);

create index help_request_requester_idx on help_request (requester);
create index help_request_status_idx on help_request (status);

create table help_request_ingredient (
    help_request_id uuid    not null references help_request (help_request_id) on delete cascade,
    ingredient_id   uuid    not null,
    position        integer not null,
    primary key (help_request_id, ingredient_id)
);

create table help_request_preferred_provider (
    help_request_id uuid    not null references help_request (help_request_id) on delete cascade,
    provider_type   text    not null check (provider_type in ('CHEF', 'GRANDMA_AVATAR', 'COMMUNITY')),
    position        integer not null,
    primary key (help_request_id, provider_type)
);

-- A help keeps its answer as one JSON document (one of four kinds, selected by answer_type).
-- No cascade: a request can only be deleted while it is Open, i.e. without helps.
-- help_id is the idempotency key for the Grandma Avatar's helps (a redelivered help is not stored twice).
create table help (
    help_id         uuid primary key,
    help_request_id uuid        not null references help_request (help_request_id),
    help_requester  uuid        not null,
    answer_title    text        not null check (length(answer_title) between 1 and 200),
    provider_type   text        not null check (provider_type in ('CHEF', 'GRANDMA_AVATAR', 'COMMUNITY')),
    help_provider   uuid,
    answer_type     text        not null,
    answer          jsonb       not null,
    created_at      timestamptz not null,
    updated_at      timestamptz not null,
    check ((provider_type = 'GRANDMA_AVATAR') = (help_provider is null))
);

create index help_help_request_idx on help (help_request_id);
create index help_help_provider_idx on help (help_provider);

-- Transactional outbox (org.larder.platform.messaging.Outbox): HelpRequested / HelpProvided are written
-- in the transaction of the change and published by the OutboxRelay afterwards.
create table outbox (
    message_id     uuid primary key,
    exchange       text not null,
    routing_key    text not null,
    message_type   text not null,
    correlation_id uuid not null,
    source         text not null,
    payload        text not null,
    created_at     timestamptz not null default now()
);
