-- Consent texts are maintained only via the backend, i.e. by migrations like V3.
create table consent_text (
    consent_text_id uuid primary key,
    text            text not null check (length(text) > 0)
);

-- A consent keeps the wording it was given for (consent_text), and is never deleted:
-- revoking sets revoked_at.
create table consent (
    consent_id      uuid primary key,
    subject         uuid not null,
    consent_text_id uuid not null references consent_text (consent_text_id),
    consent_text    text not null,
    given_at        timestamptz not null,
    revoked_at      timestamptz,
    check (revoked_at is null or revoked_at >= given_at)
);

create index consent_subject_idx on consent (subject);
