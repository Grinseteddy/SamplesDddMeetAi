-- Grandma's notebook: one row per help request she has dealt with, answered or not. The primary key
-- makes her answer every request at most once although HelpRequested arrives at least once.
create table handled_help_request (
    help_request_id uuid        primary key,
    requester       uuid        not null,
    help_type       text        not null check (help_type in ('INGREDIENT_SUBSTITUTE', 'PREPARATION_STEP_EXPLANATION',
                                                             'STEPS_TO_MITIGATE_CATASTROPHE', 'MENU_PROPOSAL')),
    outcome         text        not null check (outcome in ('ANSWERED', 'NOT_FOR_GRANDMA', 'NO_ADVICE', 'ADVICE_REJECTED')),
    -- Only for answered requests: the Help published as HelpProvided (the payload itself lives in the outbox
    -- until it is relayed).
    help_id         uuid        unique,
    answer_title    text,
    handled_at      timestamptz not null,
    check ((outcome = 'ANSWERED') = (help_id is not null)),
    check ((help_id is null) = (answer_title is null))
);

-- Transactional outbox (org.larder.platform.messaging.Outbox), relayed by OutboxRelay.
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
