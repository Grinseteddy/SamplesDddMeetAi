import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    include: ["src/**/*.spec.ts"],
    // Integration tests start a PostgreSQL container per file (ADR-CA-016).
    hookTimeout: 120_000,
  },
});
