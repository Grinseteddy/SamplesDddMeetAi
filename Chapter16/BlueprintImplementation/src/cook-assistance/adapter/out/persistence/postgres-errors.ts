/** PostgreSQL SQLSTATE 23505: a unique or primary-key constraint refused the row. */
export function isUniqueViolation(error: unknown): boolean {
  return (error as { code?: string }).code === "23505";
}
