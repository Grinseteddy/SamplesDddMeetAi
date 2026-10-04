-- Metadata of the uploaded media. The image bytes live in the S3 bucket under the media id
-- (ADR0001); a row here exists only while its object exists (object written first, row
-- deleted first), so the only possible leftover is an orphaned object in the bucket.
create table media (
    media_id     uuid primary key,
    uploader     uuid not null,
    content_type text not null check (content_type in ('image/jpeg', 'image/png', 'image/gif', 'image/webp')),
    size_bytes   bigint not null check (size_bytes > 0),
    uploaded_at  timestamptz not null
);

-- The business objects a media is used in; immutable after the upload.
-- business_object_id is the last path segment of url, kept for the lookup by business object.
create table media_link (
    media_id           uuid not null references media (media_id) on delete cascade,
    position           int not null,
    link_type          text not null check (link_type in ('RECIPE', 'THANKS', 'HELP_REQUEST', 'HELP')),
    url                text not null check (length(url) <= 2048),
    business_object_id uuid not null,
    primary key (media_id, position),
    unique (media_id, link_type, url)
);

create index media_link_business_object_idx on media_link (business_object_id, link_type);
