import { createRemoteJWKSet } from "jose";
import { JoseTokenVerifier } from "./cook-assistance/adapter/in/http/token-verifier.js";
import { NoTokenYet } from "./cook-assistance/adapter/out/recipe-catalog/no-token-yet.js";
import { SystemClock } from "./cook-assistance/adapter/out/system/system-clock.js";
import { loadConfiguration } from "./cook-assistance/configuration.js";
import { type ServiceLogEvent, startService } from "./cook-assistance/service.js";

// The process entry (step 10): configuration from the environment, Keycloak's keys, the walking skeleton.
// Not unit-tested itself — startService is covered end to end in bootstrap.spec.ts; a start smoke test is step 12's.

const clock = new SystemClock();

/** One JSON line per event on stdout (ADR-CA-010: structured, names and IDs only). */
function log(event: ServiceLogEvent): void {
  process.stdout.write(`${JSON.stringify({ at: clock.now().toISOString(), ...event })}\n`);
}

async function main(): Promise<void> {
  const configuration = loadConfiguration(process.env);
  // ADR-CA-008 · Keycloak's signing keys, fetched and cached by jose
  const tokenVerifier = new JoseTokenVerifier({
    issuer: configuration.oidcIssuer,
    audience: configuration.oidcAudience,
    keys: createRemoteJWKSet(new URL(`${configuration.oidcIssuer}/protocol/openid-connect/certs`)),
  });
  // ADR-CA-015 (amended) · no client-credentials provider yet: Recipe Catalog lookups answer 503 until steps 13/14
  log({ event: "recipe-catalog-token-missing", until: "client-credentials provider (ADR-CA-015, steps 13/14)" });
  const service = await startService(configuration, { tokenVerifier, recipeCatalogTokens: new NoTokenYet(), log });
  log({ event: "service-started", url: service.url });

  const stop = async (signal: string): Promise<void> => {
    log({ event: "service-stopping", signal });
    await service.stop();
    process.exit(0);
  };
  process.once("SIGTERM", () => void stop("SIGTERM"));
  process.once("SIGINT", () => void stop("SIGINT"));
}

main().catch((error: unknown) => {
  log({ event: "service-start-failed", error: String(error) });
  process.exit(1);
});
