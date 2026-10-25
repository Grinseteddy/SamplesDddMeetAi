import { describe, it } from "vitest";

// Wiring — increment 0, step 9, unit 9.4: the inbound adapters through the real persistence adapters (Testcontainers PostgreSQL 16).

describe("inbound wiring — PostgreSQL 16", () => {
  it.todo("POST /help-requests stores Sunday scones step 3 in PostgreSQL with HelpRequested in the outbox -- op requestHelp wiring");
  it.todo("a raw Grandma Avatar message stores the help, its HelpProvided outbox row and the processed message -- op receiveGrandmaAvatarHelpProvided wiring");
});
