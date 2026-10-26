import { type Kysely, sql } from "kysely";

// QS-16 (unit 12.5d) · one year of data, by bulk SQL: the one help request and the one help the service has stored are
// the templates, cloned into state, outbox (published) and both views with new ids, requesters and times — in
// milliseconds, as a rebuild reads them back through Date. Run on an otherwise empty schema; afterwards there are
// `count` requests with one help each.

const YEAR_SECONDS = 365 * 24 * 3600;

/** Clones the stored help request and help until there are `count` of each, the requesters spread over `cookIds`. */
export async function seedOneYearOfData(db: Kysely<any>, cookIds: readonly string[], count: number): Promise<void> {
  await db.transaction().execute(async (tx) => {
    await sql`
      create temporary table seed on commit drop as
      select n,
             gen_random_uuid() as request_id,
             gen_random_uuid() as help_id,
             (${cookIds as string[]}::uuid[])[1 + n % ${cookIds.length}] as requester,
             date_trunc('milliseconds', now() - make_interval(secs => ${YEAR_SECONDS}::float * n / ${count})) as requested_at
      from generate_series(1, ${count - 1}) as n`.execute(tx);
    await sql`
      insert into cook_assistance.help_request
      select s.request_id, s.requester, t.title, t.topic, t.question, t.recipe_id, t.how_to_step_id, t.addressees,
             t.status, s.requested_at + (t.answer_deadline - v.created_at), t.version
      from seed s, cook_assistance.help_request t join cook_assistance.help_request_view v on v.id = t.id`.execute(tx);
    await sql`
      insert into cook_assistance.help
      select s.help_id, s.request_id, s.requester, t.answer_title, t.provider_type, t.provider_id, t.answer, t.status, t.version
      from seed s, cook_assistance.help t`.execute(tx);
    await sql`
      insert into cook_assistance.outbox (aggregate_type, aggregate_id, event_type, payload, occurred_at, published_at, message_id)
      select o.aggregate_type,
             case o.event_type when 'HelpRequested' then s.request_id else s.help_id end,
             o.event_type,
             case o.event_type
               when 'HelpRequested' then o.payload || jsonb_build_object('helpRequestId', s.request_id, 'requester', s.requester,
                 'answerDeadline', to_jsonb(s.requested_at + ((o.payload->>'answerDeadline')::timestamptz - o.occurred_at)))
               else o.payload || jsonb_build_object('helpId', s.help_id, 'helpRequestId', s.request_id, 'helpRequester', s.requester)
             end,
             case o.event_type when 'HelpRequested' then s.requested_at else s.requested_at + interval '1 minute' end,
             now(),
             gen_random_uuid()
      from seed s, cook_assistance.outbox o
      order by s.n, o.id`.execute(tx);
    await sql`
      insert into cook_assistance.help_request_view
      select s.request_id, s.requester, t.title, t.topic, t.description, t.recipe_id, t.how_to_step_id, t.addressees, t.status,
             s.requested_at + (t.answer_deadline - t.created_at), s.requested_at, s.requested_at
      from seed s, cook_assistance.help_request_view t`.execute(tx);
    await sql`
      insert into cook_assistance.help_view
      select s.help_id, s.request_id, s.requester, t.answer_title, t.provider_type, t.provider_id, t.answer, t.status,
             s.requested_at + interval '1 minute', s.requested_at + interval '1 minute'
      from seed s, cook_assistance.help_view t`.execute(tx);
  });
}

/** A fingerprint of both views' contents, to compare the seeded views with a rebuild from the outbox. */
export async function fingerprintOfViews(db: Kysely<any>): Promise<string> {
  const { rows } = await sql<{ fingerprint: string }>`
    select md5(
      (select string_agg(v::text, '|' order by v.id) from cook_assistance.help_request_view v) ||
      (select string_agg(v::text, '|' order by v.id) from cook_assistance.help_view v)) as fingerprint`.execute(db);
  return rows[0]?.fingerprint as string;
}
