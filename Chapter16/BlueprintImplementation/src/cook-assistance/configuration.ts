/** What the service needs from its environment to start (ADR-CA-010: configuration from the environment, no secrets in code). */
export interface Configuration {
  readonly databaseUrl: string;
  readonly amqpUrl: string;
  readonly oidcIssuer: string;
  readonly oidcAudience: string;
  readonly publicBaseUrl: string;
  readonly port: number;
  /** Recipe Catalog's server URL (ADR-CA-015). */
  readonly recipeCatalogUrl: string;
}

/** A variable the service cannot start without is not set. */
export class MissingConfiguration extends Error {
  constructor(readonly variable: string) {
    super(`${variable} is not set`);
    this.name = "MissingConfiguration";
  }
}

/** A variable is set, but not to a value the service can use. */
export class InvalidConfiguration extends Error {
  constructor(readonly variable: string) {
    super(`${variable} is not valid`);
    this.name = "InvalidConfiguration";
  }
}

type Environment = Readonly<Record<string, string | undefined>>;

function required(env: Environment, name: string): string {
  const value = env[name];
  if (value === undefined || value === "") {
    throw new MissingConfiguration(name);
  }
  return value;
}

/** A TCP port: an integer from 1 to 65535. */
function portOf(value: string): number {
  const port = Number(value);
  if (!Number.isInteger(port) || port < 1 || port > 65_535) {
    throw new InvalidConfiguration("PORT");
  }
  return port;
}

export function loadConfiguration(env: Environment): Configuration {
  return {
    databaseUrl: required(env, "DATABASE_URL"),
    amqpUrl: required(env, "AMQP_URL"),
    oidcIssuer: required(env, "OIDC_ISSUER"),
    oidcAudience: required(env, "OIDC_AUDIENCE"),
    publicBaseUrl: required(env, "PUBLIC_BASE_URL"),
    port: portOf(required(env, "PORT")),
    recipeCatalogUrl: required(env, "RECIPE_CATALOG_URL"),
  };
}
