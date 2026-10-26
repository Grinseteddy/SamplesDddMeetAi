import { execFile } from "node:child_process";
import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import { diff } from "@asyncapi/diff";
import { Parser } from "@asyncapi/parser";
import { describe, expect, it } from "vitest";

// Compatibility — increment 0, step 12, unit 12.5b (QS-03): within major 2, a spec change must not break a client.
// Baseline: contracts/released/2.0.0 (the approved r5 specs). oasdiff 1.33.0 (Docker) and @asyncapi/diff 0.5.0.

const CONTRACTS = fileURLToPath(new URL("../../../contracts", import.meta.url));
const OASDIFF = "tufin/oasdiff:v1.33.0";

async function parsedAsyncApi(path: string): Promise<Record<string, unknown>> {
  const { document } = await new Parser().parse(await readFile(`${CONTRACTS}/${path}`, "utf8"));
  return document?.json() as Record<string, unknown>;
}

describe("spec compatibility against the released 2.0.0 -- QS-03", () => {
  it("OpenAPI: no breaking change, potentially breaking ones included (oasdiff --fail-on WARN) -- QS-03", async () => {
    // oasdiff exits non-zero on a finding, which rejects the promise; its output is the finding
    await promisify(execFile)("docker", [
      "run",
      "--rm",
      "-v",
      `${CONTRACTS}:/specs:ro`,
      OASDIFF,
      "breaking",
      "/specs/released/2.0.0/cook-assistance.openapi.yaml",
      "/specs/openapi/cook-assistance.openapi.yaml",
      "--fail-on",
      "WARN",
    ]);
  }, 120_000);

  it("AsyncAPI: no breaking change (@asyncapi/diff) -- QS-03", async () => {
    const released = await parsedAsyncApi("released/2.0.0/cook-assistance.asyncapi.yaml");
    const current = await parsedAsyncApi("asyncapi/cook-assistance.asyncapi.yaml");

    expect(diff(released, current).breaking()).toEqual([]);
  });
});
