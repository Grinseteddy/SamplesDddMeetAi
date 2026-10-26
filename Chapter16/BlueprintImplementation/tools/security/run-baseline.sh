#!/usr/bin/env bash
# Security baseline (ADR-CA-010, step 14): dependency audit, secret scan and static analysis, pinned, results in
# reports/security/. Each tool's exit status is kept; the script fails if any tool reports a finding.
set -uo pipefail
cd "$(dirname "$0")/../.."
OUT=reports/security
mkdir -p "$OUT"
GITLEAKS=zricethezav/gitleaks:v8.30.1
SEMGREP=semgrep/semgrep:1.180.0
OSV=ghcr.io/google/osv-scanner:v2.6.0
status=0

echo "npm audit (service and generators)"
# ADR-CA-021 · exactly one accepted advisory; any other high or critical one fails
ACCEPTED="GHSA-q7qp-rjg7-q26m"
npm audit --json > "$OUT/npm-audit.json" || true
npm --prefix tools/codegen audit --json > "$OUT/npm-audit-codegen.json" || true
node -e '
  const fs = require("node:fs");
  const accepted = new Set(process.argv[1].split(","));
  const open = process.argv.slice(2).flatMap((file) =>
    Object.values(JSON.parse(fs.readFileSync(file, "utf8")).vulnerabilities ?? {})
      .filter((v) => ["high", "critical"].includes(v.severity))
      .flatMap((v) => v.via.filter((via) => typeof via === "object").map((via) => via.url.split("/").pop()))
      .filter((id) => !accepted.has(id)));
  if (open.length > 0) { console.error("npm audit: not accepted", [...new Set(open)]); process.exit(1); }
' "$ACCEPTED" "$OUT/npm-audit.json" "$OUT/npm-audit-codegen.json" || status=1

echo "OSV-Scanner (the three lockfiles: service, generators, contract tools)"
docker run --rm -v "$PWD:/src:ro" "$OSV" scan source --format json --config /src/tools/security/osv-scanner.toml \
  --lockfile /src/package-lock.json --lockfile /src/tools/codegen/package-lock.json \
  --lockfile requirements.txt:/src/tools/contract/requirements.lock > "$OUT/osv-scanner.json" || status=1

echo "gitleaks (every commit that touched the service folder; local test tokens in reports/ are never committed)"
REPO_ROOT="$(git rev-parse --show-toplevel)"
docker run --rm -v "$REPO_ROOT:/repo:ro" -v "$PWD/$OUT:/out" -e GIT_CONFIG_COUNT=1 -e GIT_CONFIG_KEY_0=safe.directory \
  -e GIT_CONFIG_VALUE_0=/repo "$GITLEAKS" git /repo --log-opts="--all -- Chapter16/BlueprintImplementation" \
  --no-banner --redact --report-format json --report-path /out/gitleaks.json --log-level warn || status=1

echo "semgrep (p/typescript, p/owasp-top-ten)"
docker run --rm -v "$PWD:/src:ro" -v "$PWD/$OUT:/out" "$SEMGREP" semgrep scan \
  --config p/typescript --config p/owasp-top-ten --metrics off --error --quiet \
  --exclude node_modules --exclude src/generated --exclude tools/codegen/node_modules --exclude tools/contract/.venv \
  --json --output /out/semgrep.json /src/src /src/tools || status=1

exit "$status"
