import tseslint from "typescript-eslint";
import sonarjs from "eslint-plugin-sonarjs";
import security from "eslint-plugin-security";

// Measurements and smells for the end-refactor pass. cognitive-complexity has
// threshold 0, so every branching function reports its score in the message
// text: those findings are measurements, not smells. Everything else is a smell.
export default [
  { ignores: ["**/*.spec.ts", "src/generated/**"] }, // generated code is never edited, so never linted (ADR-CA-011)
  tseslint.configs.base,
  // ADR-CA-010 · static security rules (as warnings, triaged in the security review, step 14)
  { files: ["src/**/*.ts"], ...security.configs.recommended },
  {
    files: ["src/**/*.ts"],
    plugins: { sonarjs },
    rules: {
      "sonarjs/cognitive-complexity": ["warn", 0],
      complexity: "warn",
      "max-depth": "warn",
      "max-lines-per-function": "warn",
      "max-params": "warn",
      "sonarjs/no-duplicate-string": "warn",
      "sonarjs/no-collapsible-if": "warn",
      "sonarjs/no-nested-switch": "warn",
      "sonarjs/no-identical-functions": "warn",
    },
  },
  {
    // Cucumber passes one argument per placeholder of the step's text, plus `this` for the world: the step's wording
    // decides the count, not the design — max-params stays on everywhere else.
    files: ["src/cook-assistance/acceptance/steps/**/*.ts"],
    rules: { "max-params": "off" },
  },
];
