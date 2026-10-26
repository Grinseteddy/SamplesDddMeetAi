import { describe, expect, it } from "vitest";
import { loadMessageSchemas } from "./message-schemas.js";

// Messaging — ADR-CA-011: the validators come from the approved AsyncAPI spec; an operation it lacks is a startup error.

describe("message schemas", () => {
  it("an operation the AsyncAPI spec does not have: loading its schemas rejects, naming it -- ADR-CA-011 (AP0005 coverage)", async () => {
    await expect(loadMessageSchemas("receiveSomethingNobodySends")).rejects.toThrow(
      "the AsyncAPI spec has no message for receiveSomethingNobodySends",
    );
  });
});
