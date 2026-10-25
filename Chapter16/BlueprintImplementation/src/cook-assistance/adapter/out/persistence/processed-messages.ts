import type { Kysely } from "kysely";
import {
  MessageAlreadyProcessed,
  type MessageId,
  type ProcessedMessages,
} from "../../../application/ports/processed-messages.js";
import { isUniqueViolation } from "./postgres-errors.js";

interface ProcessedMessageRow {
  message_id: string;
}

const PROCESSED_MESSAGE_TABLE = "cook_assistance.processed_message";

interface CookAssistanceTables {
  [PROCESSED_MESSAGE_TABLE]: ProcessedMessageRow;
}

export class KyselyProcessedMessages implements ProcessedMessages {
  private readonly db: Kysely<CookAssistanceTables>;

  constructor(db: Kysely<unknown>) {
    this.db = db as Kysely<CookAssistanceTables>;
  }

  async hasBeenProcessed(messageId: MessageId): Promise<boolean> {
    const row = await this.db
      .selectFrom(PROCESSED_MESSAGE_TABLE)
      .select("message_id")
      .where("message_id", "=", messageId)
      .executeTakeFirst();
    return row !== undefined;
  }

  async record(messageId: MessageId): Promise<void> {
    try {
      await this.db.insertInto(PROCESSED_MESSAGE_TABLE).values({ message_id: messageId }).execute();
    } catch (error) {
      // constraint translation: the message id is the primary key, so a unique violation means "already processed"
      if (isUniqueViolation(error)) {
        throw new MessageAlreadyProcessed(messageId);
      }
      throw error;
    }
  }
}
