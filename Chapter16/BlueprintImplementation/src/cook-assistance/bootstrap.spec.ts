import { describe, it } from "vitest";

// Bootstrap — increment 0, step 10, unit 10.4: poller, configuration and one run end to end (real PostgreSQL and RabbitMQ).

describe("bootstrap", () => {
  it.todo("the poller runs the relay every 500 ms and waits for the running pass on shutdown -- ADR-CA-004");
  it.todo("configuration from the environment (DATABASE_URL, AMQP_URL, OIDC_ISSUER, OIDC_AUDIENCE, PUBLIC_BASE_URL, PORT); a missing variable stops the start with a named error -- ADR-CA-010");
  it.todo("end to end: POST /help-requests, one relay run → exactly the promised message on help.requested, no pending rows left -- B2-S01 wiring");
});
