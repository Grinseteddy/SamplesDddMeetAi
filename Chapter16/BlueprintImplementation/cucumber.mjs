// Acceptance tests (ADR-CA-016): one feature file per board, steps in TypeScript through tsx.
// @todo marks a scenario that is not active yet; strict mode lets no undefined or pending step pass.
const base = {
  paths: ["features/**/*.feature"],
  import: ["src/cook-assistance/acceptance/**/*.ts"],
  strict: true,
};

export default { ...base, tags: "not @todo", format: ["progress", "json:reports/cucumber.json"] };

/** The scenarios not active yet (`cucumber-js --profile todo --dry-run`). */
export const todo = { ...base, tags: "@todo", format: ["progress"] };
