import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { Parser } from "@asyncapi/parser";
import { loadMessageSchemas, type MessageSchemas } from "../../in/messaging/message-schemas.js";
import type { OutgoingMessage } from "./outbound-messages.js";

const ASYNCAPI_SPEC = fileURLToPath(new URL("../../../../../contracts/asyncapi/cook-assistance.asyncapi.yaml", import.meta.url));

/** Whether a message is what the spec's send operation on its routing key promises (ADR-CA-011). */
export type SendCheck = (message: OutgoingMessage) => boolean;

export async function loadSendCheck(): Promise<SendCheck> {
  const { document } = await new Parser().parse(await readFile(ASYNCAPI_SPEC, "utf8"));
  const schemasByRoutingKey = new Map<string, MessageSchemas>();
  for (const operation of document?.operations().filterBySend() ?? []) {
    schemasByRoutingKey.set(operation.channels().all()[0]?.address() ?? "", await loadMessageSchemas(operation.id() ?? ""));
  }
  return (message) => {
    const schemas = schemasByRoutingKey.get(message.routingKey);
    return schemas !== undefined && schemas.headers(message.headers) && schemas.payload(message.payload);
  };
}
