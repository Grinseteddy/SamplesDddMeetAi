// QS-01 (maintainability · modularity, ADR-CA-002): the hexagonal layout as rules — the domain and the application
// import no framework, HTTP, database or broker type; adapters depend inward only. Production code only: specs, test
// support, acceptance and contract code may reach anything.
const TESTS = "\\.spec\\.ts$|/test-support/|/acceptance/|/contract/";

module.exports = {
  forbidden: [
    {
      name: "domain-depends-on-the-domain-only",
      comment: "The domain imports nothing outside the domain — no npm package, no application, no adapter (QS-01).",
      severity: "error",
      from: { path: "^src/cook-assistance/domain", pathNot: TESTS },
      to: { pathNot: "^src/cook-assistance/domain" },
    },
    {
      name: "application-depends-on-domain-and-application-only",
      comment: "Use cases and query ports import the domain and their own ports — no adapter, framework or generated type.",
      severity: "error",
      from: { path: "^src/cook-assistance/application", pathNot: TESTS },
      to: { pathNot: "^src/cook-assistance/(domain|application)" },
    },
    {
      name: "adapters-do-not-depend-on-the-composition-root",
      comment: "Adapters point inward; only the composition root (composition.ts, service.ts, main.ts) wires them.",
      severity: "error",
      from: { path: "^src/cook-assistance/adapter", pathNot: TESTS },
      to: { path: "^src/(cook-assistance/(composition|service)|main)\\.ts$" },
    },
    {
      name: "inbound-adapters-do-not-depend-on-outbound-adapters",
      comment:
        "Adapters depend inward only (ADR-CA-002): what inbound and outbound adapters share lives in adapter/messaging, " +
        "which imports neither side.",
      severity: "error",
      from: { path: "^src/cook-assistance/adapter/in/", pathNot: TESTS },
      to: { path: "^src/cook-assistance/adapter/out/" },
    },
    {
      name: "outbound-adapters-do-not-depend-on-inbound-adapters",
      comment: "The other direction of the rule above.",
      severity: "error",
      from: { path: "^src/cook-assistance/adapter/out/", pathNot: TESTS },
      to: { path: "^src/cook-assistance/adapter/in/" },
    },
    {
      name: "shared-messaging-imports-neither-side",
      comment: "adapter/messaging is what both sides share; importing either side would open an in→out path (ADR-CA-002, N-3).",
      severity: "error",
      from: { path: "^src/cook-assistance/adapter/messaging/", pathNot: TESTS },
      to: { path: "^src/cook-assistance/adapter/(in|out)/" },
    },
    {
      name: "not-to-unresolvable",
      comment: "An import the cruiser cannot resolve is invisible to every rule above — so it is an error itself.",
      severity: "error",
      from: {},
      to: { couldNotResolve: true },
    },
    {
      name: "no-circular",
      comment:
        "Runtime cycles only: types that belong together (an aggregate and its event union) may name each other through " +
        "`import type`, which leaves no cycle at runtime.",
      severity: "error",
      from: { pathNot: TESTS },
      to: { circular: true, viaOnly: { dependencyTypesNot: ["type-only"] } },
    },
  ],
  options: {
    // generated code stays in the graph as leaves, so a domain or application import of it fails (ADR-CA-011)
    doNotFollow: { path: ["node_modules", "^src/generated"] },
    tsConfig: { fileName: "tsconfig.json" },
    tsPreCompilationDeps: true,
    // resolve package subpath exports (kysely/migration) the way Node and TypeScript do
    enhancedResolveOptions: { exportsFields: ["exports"], conditionNames: ["import", "require", "node", "types", "default"] },
  },
};
