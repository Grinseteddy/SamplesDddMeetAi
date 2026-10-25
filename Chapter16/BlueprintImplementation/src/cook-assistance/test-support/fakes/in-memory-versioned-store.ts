import { ConcurrencyConflict } from "../../domain/shared/concurrency-conflict.js";

interface VersionedState {
  readonly id: string;
  readonly version: number;
}

// ADR-CA-005 · compare-and-set over state snapshots — the fakes' counterpart of writeVersioned.
export class InMemoryVersionedStore<State extends VersionedState> {
  private readonly stored = new Map<string, State>();

  constructor(private readonly aggregateName: string) {}

  find(id: string): State | undefined {
    return this.stored.get(id);
  }

  /** Stores the snapshot as the loaded version + 1; `state.version` is the version it was loaded with. */
  write(state: State): void {
    const storedVersion = this.stored.get(state.id)?.version ?? 0;
    if (storedVersion !== state.version) {
      throw new ConcurrencyConflict(this.aggregateName, state.id);
    }
    this.stored.set(state.id, { ...state, version: state.version + 1 });
  }
}
