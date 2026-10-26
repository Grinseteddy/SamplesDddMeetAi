import type { Delivery } from "../../adapter/messaging/delivery.js";

/** Records how a consumer settled a message (ack, dead letter, redelivery) instead of talking to the broker. */
export class RecordingDelivery implements Delivery {
  readonly settled: string[] = [];

  ack(): void {
    this.settled.push("ack");
  }

  deadLetter(): void {
    this.settled.push("dead-letter");
  }

  retry(): void {
    this.settled.push("retry");
  }
}
