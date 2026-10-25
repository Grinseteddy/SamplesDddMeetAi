import { type Kysely, sql } from "kysely";

// Test support: empties every table of the schema cook_assistance between tests — the tables later migrations add
// included, so no test keeps rows a list forgot (step 11: the read models). Kysely's migration bookkeeping stays.
export async function resetSchema(db: Kysely<any>): Promise<void> {
  const tables = await sql<{ tablename: string }>`
    select tablename from pg_tables where schemaname = 'cook_assistance' and tablename not like 'kysely_migration%'`.execute(db);
  await sql`truncate ${sql.join(tables.rows.map(({ tablename }) => sql.table(`cook_assistance.${tablename}`)))}`.execute(db);
}
