// ADR-CA-018 · pre-commit on the staged files of this service only: format, lint, type check. Never the whole suite.
export default {
  "src/**/*.ts": (files) => [
    `prettier --write ${files.join(" ")}`,
    // spec and generated files are outside ESLint's scope (eslint.config.js); only errors refuse the commit
    `eslint --no-warn-ignored ${files.join(" ")}`,
    "tsc --noEmit", // tsc checks the project, not single files
  ],
  "{tools/**/*.{mjs,js},*.{js,ts,json}}": (files) => `prettier --write ${files.join(" ")}`,
};
