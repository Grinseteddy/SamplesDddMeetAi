import { v7 } from "uuid";
import type { IdGenerator } from "../../../application/ports/id-generator.js";

// ADR-CA-007 · the production id generator: time-ordered UUIDv7.
export class UuidV7Generator implements IdGenerator {
  next(): string {
    return v7();
  }
}
