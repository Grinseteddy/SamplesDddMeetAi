import { type Kysely, sql, type Transaction } from "kysely";
import type { Clock } from "../../../application/ports/clock.js";
import { type OutboxRecord, type OutgoingMessage, toOutgoingMessage, UnmappedEvent } from "./outbound-messages.js";
import type { SendCheck } from "./send-check.js";

/** A claimed outbox row: the record the mapper reads, plus the row id the relay marks. */
interface OutboxRow extends OutboxRecord {
  readonly id: string;
  readonly attempts: number;
}

/** ADR-CA-004 · exponential backoff from 1 s: the n-th failure waits 2^(n-1) seconds. */
const FIRST_BACKOFF_MS = 1_000;

/** ADR-CA-004 · after the 5th failed attempt the row is dead-lettered and not retried. */
const MAX_ATTEMPTS = 5;

/** ADR-CA-004 (amended, inspection m-10) · rows per claim round: bounds the transaction, its locks and a rollback. */
const CLAIM_BATCH = 50;

/** A cap on claim rounds per run, so one run always ends. */
const MAX_ROUNDS = 100;

/** Sends one message; resolves only when the broker confirmed it (ADR-CA-004). */
export interface Publisher {
  publish(message: OutgoingMessage): Promise<void>;
}

/** A row the relay gave up on: reported for the metric and the alert (ADR-CA-004). */
export interface DeadLetteredRow {
  readonly messageId: string;
  readonly eventType: string;
  readonly lastError: string;
}

export interface OutboxRelayPorts {
  readonly db: Kysely<unknown>;
  readonly publisher: Publisher;
  readonly clock: Clock;
  readonly check: SendCheck;
  readonly publicBaseUrl: string;
  readonly reportDeadLetter: (row: DeadLetteredRow) => void;
}

const DEAD_LETTER = "dead-letter";

/** What happened to one claimed row (ADR-CA-004). */
type Outcome =
  | { readonly kind: "published" }
  | { readonly kind: "retry"; readonly reason: string }
  | { readonly kind: typeof DEAD_LETTER; readonly reason: string; readonly attempted: boolean };

function deadLetter(reason: string, attempted: boolean): Outcome {
  return { kind: DEAD_LETTER, reason, attempted };
}

/** One relay run: its transaction and the instant it acts at. */
interface Run {
  readonly tx: Transaction<unknown>;
  readonly now: Date;
}

// ADR-CA-004 · the only place that publishes: from the outbox, in order per aggregate, at least once
export class OutboxRelay {
  constructor(private readonly ports: OutboxRelayPorts) {}

  /** Claim rounds until nothing is due (an aggregate's next row goes out once its earlier one is done). */
  async runOnce(): Promise<void> {
    for (let round = 0; round < MAX_ROUNDS; round += 1) {
      if ((await this.claimRound()) === 0) {
        return;
      }
    }
  }

  /** One transaction: claim the oldest due row of each aggregate, publish, record; returns how many were claimed. */
  private async claimRound(): Promise<number> {
    const now = this.ports.clock.now();
    const deadLettered: DeadLetteredRow[] = [];
    let claimed = 0;
    await this.ports.db.transaction().execute(async (tx) => {
      const rows = await sql<OutboxRow>`
        select id::text, message_id::text as "messageId", aggregate_type as "aggregateType", aggregate_id::text as "aggregateId",
               event_type as "eventType", payload, occurred_at as "occurredAt", attempts
        from cook_assistance.outbox o
        where o.published_at is null and o.dead_lettered_at is null and o.next_attempt_at <= ${now}
          -- order per aggregate: only its oldest pending row; a waiting failed row blocks the later ones
          and not exists (
            select 1 from cook_assistance.outbox earlier
            where earlier.aggregate_id = o.aggregate_id and earlier.id < o.id
              and earlier.published_at is null and earlier.dead_lettered_at is null)
        order by o.id
        limit ${CLAIM_BATCH}
        -- several relays at once (QS-14): each locks what it claims, the others skip it instead of waiting
        for update of o skip locked`.execute(tx);
      claimed = rows.rows.length;
      for (const row of rows.rows) {
        const outcome = await this.outcomeOf(row);
        await record({ tx, now }, row, outcome);
        if (outcome.kind === DEAD_LETTER) {
          deadLettered.push({ messageId: row.messageId, eventType: row.eventType, lastError: outcome.reason });
        }
      }
    });
    // reported only once the dead letters are committed
    deadLettered.forEach((row) => this.ports.reportDeadLetter(row));
    return claimed;
  }

  /** Map, check and publish one row — and say what happened, without touching the outbox. */
  private async outcomeOf(row: OutboxRow): Promise<Outcome> {
    let message: OutgoingMessage;
    try {
      message = toOutgoingMessage(row, this.ports.publicBaseUrl);
    } catch (error) {
      // G5 · an event type nobody decided to publish is never sent; the batch goes on
      if (error instanceof UnmappedEvent) {
        return deadLetter(error.message, false);
      }
      throw error;
    }
    // ADR-CA-011 · a message that breaks the spec cannot be fixed by retrying it
    if (!this.ports.check(message)) {
      return deadLetter("the message breaks the AsyncAPI spec", false);
    }
    try {
      await this.ports.publisher.publish(message);
      return { kind: "published" };
    } catch (error) {
      const reason = (error as Error).message;
      return row.attempts + 1 >= MAX_ATTEMPTS ? deadLetter(reason, true) : { kind: "retry", reason };
    }
  }
}

/** A dead letter after a failed publish counts as an attempt; one the relay refused before sending does not. */
function attemptsAdded(outcome: { readonly attempted: boolean }): number {
  return outcome.attempted ? 1 : 0;
}

/** One UPDATE per outcome; the row's other columns stay as they are. */
async function record({ tx, now }: Run, row: OutboxRow, outcome: Outcome): Promise<void> {
  switch (outcome.kind) {
    case "published":
      await sql`update cook_assistance.outbox set published_at = ${now} where id = ${row.id}`.execute(tx);
      return;
    case "retry": {
      const nextAttemptAt = new Date(now.getTime() + FIRST_BACKOFF_MS * 2 ** row.attempts);
      await sql`
        update cook_assistance.outbox set attempts = attempts + 1, last_error = ${outcome.reason}, next_attempt_at = ${nextAttemptAt}
        where id = ${row.id}`.execute(tx);
      return;
    }
    case DEAD_LETTER:
      await sql`
        update cook_assistance.outbox
        set attempts = attempts + ${attemptsAdded(outcome)}, last_error = ${outcome.reason}, dead_lettered_at = ${now}
        where id = ${row.id}`.execute(tx);
      return;
  }
}
