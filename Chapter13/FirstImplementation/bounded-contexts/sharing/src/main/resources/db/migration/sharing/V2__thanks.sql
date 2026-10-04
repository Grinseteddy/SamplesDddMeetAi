-- Thanks for exactly one help (Cooking Assistance); at most one thanks per help.
-- picture keeps the link as given; picture_media_id is the image id parsed from it (Media).
create table thanks (
    thanks_id        uuid primary key,
    giver            uuid not null,
    help_id          uuid not null unique,
    thanks_text      text not null check (length(thanks_text) between 1 and 2000),
    picture          text not null,
    picture_media_id uuid not null,
    created_at       timestamptz not null,
    updated_at       timestamptz not null,
    version          bigint not null default 0,
    check (updated_at >= created_at)
);

create index thanks_giver_idx on thanks (giver);
create index thanks_created_at_idx on thanks (created_at desc);

-- The recipients of one thanks, in the order the giver named them; each type at most once.
create table thanks_recipient (
    recipient_id uuid primary key,
    thanks_id    uuid not null references thanks (thanks_id) on delete cascade,
    position     integer not null,
    type         text not null check (type in ('COOK', 'CHEF', 'GRANDMA_AVATAR')),
    chef_name    text check (chef_name is null or (type = 'CHEF' and length(chef_name) between 1 and 200)),
    unique (thanks_id, position),
    unique (thanks_id, type)
);

-- The cooks a recipient of type COOK mentions; thanks_id is repeated for the "mentioned cook" filter.
create table thanks_recipient_cook (
    recipient_id uuid not null references thanks_recipient (recipient_id) on delete cascade,
    thanks_id    uuid not null references thanks (thanks_id) on delete cascade,
    position     integer not null,
    cook         uuid not null,
    primary key (recipient_id, cook),
    unique (recipient_id, position)
);

create index thanks_recipient_cook_cook_idx on thanks_recipient_cook (cook);
