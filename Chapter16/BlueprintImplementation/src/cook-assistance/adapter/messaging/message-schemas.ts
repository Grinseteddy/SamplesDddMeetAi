import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { Parser } from "@asyncapi/parser";
import AjvModule, { type ValidateFunction } from "ajv";
import addFormatsModule from "ajv-formats";

const ASYNCAPI_SPEC = fileURLToPath(new URL("../../../../contracts/asyncapi/cook-assistance.asyncapi.yaml", import.meta.url));

// CommonJS packages: in an ES module the class or function may sit on `.default`.
const Ajv = (AjvModule as unknown as { default?: typeof AjvModule }).default ?? AjvModule;
const addFormats = (addFormatsModule as unknown as { default?: typeof addFormatsModule }).default ?? addFormatsModule;

/** The validators of one operation's message (receive or send), compiled from the approved AsyncAPI spec (ADR-CA-011). */
export interface MessageSchemas {
  readonly headers: ValidateFunction;
  readonly payload: ValidateFunction;
}

export async function loadMessageSchemas(operationId: string): Promise<MessageSchemas> {
  const { document } = await new Parser().parse(await readFile(ASYNCAPI_SPEC, "utf8"));
  const message = document?.operations().get(operationId)?.messages().all()[0];
  if (message === undefined) {
    throw new Error(`the AsyncAPI spec has no message for ${operationId}`);
  }
  // strict: false — the parser adds x-parser-* keywords; ajv-formats checks uuid, uri, date-time
  const ajv = new Ajv({ strict: false, allErrors: true });
  addFormats(ajv);
  return { headers: ajv.compile(message.headers()?.json() ?? {}), payload: ajv.compile(message.payload()?.json() ?? {}) };
}
