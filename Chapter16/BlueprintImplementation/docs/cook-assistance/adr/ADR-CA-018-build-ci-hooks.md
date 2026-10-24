# ADR-CA-018 — One build command, GitHub Actions, Husky hooks

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 18

## Context
Step 0 chose GitHub Actions and Conventional Commits, with the hook tool decided with the stack. QS-02
gives the build ≤ 10 min in CI (load tests excluded). The service lives in the book repository under
`Chapter16/BlueprintImplementation`.

## Decision
- **One build command** `npm run verify`, in this order: `generate` (OpenAPI/AsyncAPI types and models,
  upstream client) → `tsc --noEmit` → `eslint` (incl. `eslint-plugin-security`) → `prettier --check` →
  `depcruise` (module rules) → `vitest run` (unit + integration with Testcontainers) → acceptance
  (Cucumber-JS) → contract runs (Schemathesis, AsyncAPI validation) → spec diffs (oasdiff,
  `@asyncapi/diff`) → `npm audit` / OSV-Scanner / gitleaks / Semgrep.
- **GitHub Actions** workflow in the book repo's `.github/workflows/cook-assistance.yml`, filtered to
  `Chapter16/BlueprintImplementation/**`: job `verify` on every push and pull request; jobs `load` (k6)
  and `dast` (OWASP ZAP API scan) on pull requests; nightly job for 1000-run property tests.
- **Hooks**: Husky + lint-staged, installed at the book-repo root and scoped to
  `Chapter16/BlueprintImplementation`: pre-commit — prettier, eslint and type check on changed files,
  gitleaks on staged changes; commit-msg — commitlint (Conventional Commits). Hooks never run the
  whole suite.
- **Formatter / linter / type checker**: Prettier, ESLint (typescript-eslint, sonarjs as in the EXACT
  profile), `tsc` strict.

## Consequences
The walking skeleton writes the workflow, the hooks and the scripts and proves the pipeline green on
its pull request.

## Quality scenarios
Serves QS-02, QS-03, QS-01; supports the Definition of Done.
