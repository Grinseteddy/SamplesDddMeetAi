/** What the poller drives: one relay run. */
export interface Runnable {
  runOnce(): Promise<void>;
}

// ADR-CA-004 · runs the relay at an interval (500 ms in production) and lets a running pass finish on shutdown
export class RelayPoller {
  constructor(
    private readonly relay: Runnable,
    private readonly intervalMs: number,
    private readonly onFailure: (error: unknown) => void, // ADR-CA-004 · a failed run is reported, never swallowed
  ) {}

  private timer: ReturnType<typeof setInterval> | undefined;
  private running: Promise<void> | undefined;
  private stopped = false;

  start(): void {
    this.timer = setInterval(() => this.tick(), this.intervalMs);
  }

  /** Stops the interval and waits until a running pass has finished. */
  async stop(): Promise<void> {
    this.stopped = true;
    clearInterval(this.timer);
    await this.running;
  }

  /** Passes never overlap in one instance: a tick while one runs is skipped. */
  private tick(): void {
    if (this.running !== undefined || this.stopped) {
      return;
    }
    this.running = this.relay
      .runOnce()
      .catch((error: unknown) => this.onFailure(error))
      .finally(() => {
        this.running = undefined;
      });
  }
}
