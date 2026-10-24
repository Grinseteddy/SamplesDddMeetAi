// Test support: a real PostgreSQL 16 per test file (ADR-CA-016). Test code only.
import { PostgreSqlContainer, type StartedPostgreSqlContainer } from "@testcontainers/postgresql";
import { Kysely, PostgresDialect } from "kysely";
import pg from "pg";

export interface TestDatabase {
  readonly db: Kysely<any>; // tests inspect the catalog freely
  stop(): Promise<void>;
}

export async function startPostgres(): Promise<TestDatabase> {
  const container: StartedPostgreSqlContainer = await new PostgreSqlContainer("postgres:16-alpine").start();
  const db = new Kysely<any>({
    dialect: new PostgresDialect({ pool: new pg.Pool({ connectionString: container.getConnectionUri(), max: 4 }) }),
  });
  return {
    db,
    async stop() {
      await db.destroy();
      await container.stop();
    },
  };
}
