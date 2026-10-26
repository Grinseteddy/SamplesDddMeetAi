// Intent coverage (step 12, unit 12.7): every rule, example, operation, channel, domain event, policy and testable
// quality scenario of the slices built so far has at least one passing, active test — or is on the planned list with
// the increment that will build it. Intents come from the approved documents, specs and the domain's event union;
// evidence from the test results (Vitest JSON, Cucumber JSON). Writes reports/intent-coverage.md; exits 1 on a gap.
//
// node tools/intent-coverage/report.mjs [--load reports/vitest-load-results.json]
import { existsSync, readFileSync, writeFileSync } from "node:fs";
import { fileURLToPath } from "node:url";

const ROOT = fileURLToPath(new URL("../..", import.meta.url));
const read = (path) => readFileSync(`${ROOT}/${path}`, "utf8");
const json = (path) => JSON.parse(read(path));

const PLANNED = json("tools/intent-coverage/planned.json");
const COVERED_BY = json("tools/intent-coverage/covered-by.json");
const loadArgument = process.argv.indexOf("--load");
const LOAD_RESULTS = loadArgument > 0 ? process.argv[loadArgument + 1] : undefined;
const LOAD_SCENARIOS = ["QS-08", "QS-09", "QS-10", "QS-16"]; // performance.spec.ts, the pull-request job (ADR-CA-018)

// ---- intents -------------------------------------------------------------------------------------------------------

/** Every invariant of the sheet: anchored to a built slice, or planned for the increment of its slice (m-1). */
function rules() {
  return read("docs/cook-assistance/04-invariants-sheet.md")
    .split("\n")
    .filter((line) => /^\| INV-\d+ \|/.test(line))
    .map((line) => line.split("|").map((cell) => cell.trim()))
    .map((cells) => ({ kind: "rule", id: cells[1], title: cells[2], tokens: [cells[1]] }));
}

/** Example Mapping "Help for a preparation step": one intent per green card, R<n> ex.<k> as the feature tags say. */
function examples() {
  const intents = [];
  let rule;
  let count = 0;
  for (const line of read("docs/cook-assistance/example-mapping-preparation-step.md").split("\n")) {
    const heading = /^### (For Rule (\d+)|Without a rule card)/.exec(line);
    if (heading) {
      rule = heading[2] === undefined ? "no-rule" : `R${heading[2]}`;
      count = 0;
    } else if (/^## /.test(line)) {
      rule = undefined;
    } else if (rule !== undefined && line.startsWith("- ")) {
      count += 1;
      const id = rule === "no-rule" ? "no-rule" : `${rule} ex.${count}`;
      intents.push({ kind: "example", id, title: line.slice(2), tokens: id.split(" ") });
    }
  }
  return intents;
}

/** Every OpenAPI operation and every AsyncAPI operation of the approved specs. */
function operations() {
  const openapi = new Set(
    [...read("contracts/openapi/cook-assistance.openapi.yaml").matchAll(/^ {6}operationId: (\w+)$/gm)].map((m) => m[1]),
  );
  const asyncapi = read("contracts/asyncapi/cook-assistance.asyncapi.yaml")
    .split(/^operations:$/m)[1]
    .split(/^\S/m)[0];
  const channels = [...asyncapi.matchAll(/^ {2}(\w+):$/gm)].map((m) => m[1]);
  return [
    ...[...openapi].map((id) => ({ kind: "operation", id, title: "OpenAPI", tokens: [id] })),
    ...channels.map((id) => ({ kind: "channel", id, title: "AsyncAPI", tokens: [id] })),
  ];
}

/** The domain's event union: every event type an aggregate raises. */
function events() {
  return ["src/cook-assistance/domain/help-request/help-request-events.ts", "src/cook-assistance/domain/help/help-events.ts"].flatMap(
    (path) => [...read(path).matchAll(/readonly type: "(\w+)"/g)].map((m) => ({ kind: "event", id: m[1], title: path, tokens: [m[1]] })),
  );
}

/** Policies of the built slices: B1-S04 is the policy "whenever Grandma Avatar answers, provide the help". */
function policies() {
  return [
    {
      kind: "policy",
      id: "ProvideHelpWhenGrandmaAvatarAnswers",
      title: "B1-S04",
      tokens: [],
      file: "provide-help-when-grandma-avatar-answers",
    },
  ];
}

/** Quality scenarios verified by test or by both (06-quality-scenarios.md, column Verify). */
function qualityScenarios() {
  return read("docs/cook-assistance/06-quality-scenarios.md")
    .split("\n")
    .filter((line) => /^\| QS-\d+ \|/.test(line))
    .map((line) => line.split("|").map((cell) => cell.trim()))
    .filter((cells) => /^(test|both)/.test(cells[5]))
    .map((cells) => ({ kind: "quality", id: cells[1], title: cells[2], tokens: [cells[1]] }));
}

// ---- evidence ------------------------------------------------------------------------------------------------------

/** Passing tests: Vitest assertions with status passed, Cucumber scenarios whose steps all passed. */
function passingTests() {
  const vitest = (path) =>
    json(path).testResults.flatMap((file) =>
      file.assertionResults
        .filter((test) => test.status === "passed")
        .map((test) => ({ name: test.fullName, file: file.name.replace(`${ROOT}/`, ""), tags: [] })),
    );
  const cucumber = json("reports/cucumber.json").flatMap((feature) =>
    feature.elements
      .filter((scenario) => scenario.steps.every((step) => step.result.status === "passed"))
      .map((scenario) => ({ name: scenario.name, file: feature.uri, tags: scenario.tags.map((tag) => tag.name.slice(1)) })),
  );
  return [...vitest("reports/vitest-results.json"), ...(LOAD_RESULTS ? vitest(LOAD_RESULTS) : []), ...cucumber];
}

/** A test's trace: the words after `--` in its name, plus its scenario tags. */
function traceOf(test) {
  const trace = test.name.includes("--") ? test.name.slice(test.name.lastIndexOf("--") + 2) : "";
  return [...trace.split(/[\s,()]+/).filter(Boolean), ...test.tags];
}

function evidenceFor(intent, tests) {
  const named = COVERED_BY[intent.id]?.tests ?? [];
  return tests.filter((test) => {
    const trace = traceOf(test);
    if (intent.kind === "event") return new RegExp(`\\b${intent.id}\\b`).test(test.name);
    if (intent.kind === "policy") return test.file.includes(intent.file);
    if (named.some((part) => test.name.includes(part))) return true;
    return intent.tokens.length > 0 && intent.tokens.every((token) => trace.includes(token));
  });
}

// ---- report --------------------------------------------------------------------------------------------------------

const tests = passingTests();
const intents = [...rules(), ...examples(), ...operations(), ...events(), ...policies(), ...qualityScenarios()];
const rows = intents.map((intent) => {
  const evidence = evidenceFor(intent, tests);
  const planned = PLANNED[intent.id];
  const loadOnly = !LOAD_RESULTS && LOAD_SCENARIOS.includes(intent.id);
  const gate = COVERED_BY[intent.id]?.gate; // a build gate, not a test: dependency-cruiser, the CI run
  // partial: tests cover what the built slices can show; the rest is planned (e.g. a timeout that needs the runner)
  const covered = planned?.partial ? "partial" : "covered";
  const status = evidence.length > 0 ? covered : gate ? "gate" : planned ? "planned" : loadOnly ? "load job" : "GAP";
  const domainLevel = evidence.some((test) => test.file.includes("/domain/"));
  return { ...intent, evidence, planned, status, domainLevel, gate };
});
const coveredButPlanned = rows.filter((row) => row.status === "covered" && row.planned);
const partial = rows.filter((row) => row.status === "partial");
const gaps = rows.filter((row) => row.status === "GAP");
const withoutDomainTest = rows.filter((row) => row.kind === "rule" && row.status === "covered" && !row.domainLevel);
const unknownPlanned = Object.keys(PLANNED).filter((id) => !intents.some((intent) => intent.id === id));

const cell = (text) => String(text).replaceAll("|", "\\|");
const lines = [
  "# Intent coverage — increment 0, walking skeleton (B2-S01, B1-S04)",
  "",
  `Generated by tools/intent-coverage/report.mjs from ${LOAD_RESULTS ? "the Vitest, load and Cucumber results" : "the Vitest and Cucumber results (load scenarios: the pull-request job)"}.`,
  `Passing tests read: ${tests.length}. Intents: ${rows.length} — covered ${rows.filter((r) => r.status === "covered").length}, partial ${partial.length}, gate ${rows.filter((r) => r.status === "gate").length}, planned ${rows.filter((r) => r.status === "planned").length}, load job ${rows.filter((r) => r.status === "load job").length}, gaps ${gaps.length}.`,
  "",
  "| Kind | Intent | Status | Tests | Planned / note |",
  "|---|---|---|---|---|",
  ...rows.map(
    (row) =>
      `| ${row.kind} | ${cell(row.id)} | ${row.status} | ${row.evidence.length} | ${cell(row.planned ? `${row.planned.increment} — ${row.planned.reason}` : (row.gate ?? COVERED_BY[row.id]?.reason ?? ""))} |`,
  ),
  "",
  `Rules covered without a domain-level test: ${withoutDomainTest.map((row) => row.id).join(", ") || "none"}.`,
  `Planned but covered (planned list out of date): ${coveredButPlanned.map((row) => row.id).join(", ") || "none"}.`,
  `Planned entries that name no intent: ${unknownPlanned.join(", ") || "none"}.`,
  `Gaps: ${gaps.map((row) => row.id).join(", ") || "none"}.`,
  "",
];
writeFileSync(`${ROOT}/reports/intent-coverage.md`, lines.join("\n"));
process.stdout.write(`${lines.slice(3, 4).join("")}\n${lines.slice(-5).join("\n")}\n`);
process.exit(gaps.length > 0 || coveredButPlanned.length > 0 || unknownPlanned.length > 0 || !existsSync(`${ROOT}/reports`) ? 1 : 0);
