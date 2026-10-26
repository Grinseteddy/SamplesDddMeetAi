import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    include: ["src/**/*.spec.ts"],
    // Load runs take minutes: they run on the pull request only (vitest.load.config.ts, ADR-CA-018, QS-02).
    exclude: ["**/node_modules/**", "src/cook-assistance/quality/performance.spec.ts"],
    // Integration tests start a PostgreSQL container per file (ADR-CA-016).
    hookTimeout: 120_000,
    // ADR-CA-020 · AP0005 measured: line and branch coverage of the hand-written service code, ≥ 95 % (`--coverage`)
    coverage: {
      provider: "v8",
      include: ["src/cook-assistance/**/*.ts"],
      exclude: [
        "**/*.spec.ts",
        "**/test-support/**",
        "src/cook-assistance/acceptance/**",
        "src/cook-assistance/contract/**",
        "src/cook-assistance/quality/**",
      ],
      reporter: ["text-summary", "json-summary"],
      thresholds: { lines: 95, branches: 95 },
    },
  },
});
