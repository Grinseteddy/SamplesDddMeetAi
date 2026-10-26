import { defineConfig } from "vitest/config";

// Load runs with k6 (ADR-CA-016): on the pull request only, outside the verify build (ADR-CA-018, QS-02).
export default defineConfig({
  test: {
    include: ["src/cook-assistance/quality/performance.spec.ts"],
    hookTimeout: 180_000,
    testTimeout: 300_000,
  },
});
