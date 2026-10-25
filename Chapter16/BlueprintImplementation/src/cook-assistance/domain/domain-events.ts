import type { HelpRequestEvent } from "./help-request/help-request-events.js";
import type { HelpEvent } from "./help/help-events.js";

/** Every event the aggregates of Cook Assistance raise; adapters that decide per event type check against it. */
export type DomainEvent = HelpRequestEvent | HelpEvent;
