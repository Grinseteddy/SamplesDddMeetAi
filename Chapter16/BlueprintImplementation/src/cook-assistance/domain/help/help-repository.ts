import type { Help, HelpId } from "./help.js";

/** Where helps are kept (port; implemented by an adapter in adapter/out/persistence). */
export interface HelpRepository {
  /** The help with this id, or undefined if there is none. */
  findById(id: HelpId): Promise<Help | undefined>;

  /** Keeps the help and what it announced; throws ConcurrencyConflict if it changed since it was loaded. */
  save(help: Help): Promise<void>;
}
