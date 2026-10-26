import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    include: ["src/**/*.spec.ts"],
    // Load runs take minutes: they run on the pull request only (vitest.load.config.ts, ADR-CA-018, QS-02).
    exclude: ["**/node_modules/**", "src/cook-assistance/quality/performance.spec.ts"],
    // Integration tests start a PostgreSQL container per file (ADR-CA-016).
    hookTimeout: 120_000,
  },
});
