#!/usr/bin/env bash
# Contract run, OpenAPI (step 12, unit 12.4): Schemathesis against the running service, limited by name to the
# operations served so far (planned ones are excluded by name, never by a broad filter).
set -euo pipefail
cd "$(dirname "$0")/../.."
PORT="${CONTRACT_PORT:-8099}"
SERVED=(requestHelp getHelpRequestById listHelpRequests getHelpById listHelpsForHelpRequest)
mkdir -p reports
rm -f reports/contract-token.txt
CONTRACT_PORT="$PORT" node --import tsx src/cook-assistance/contract/serve-for-contract.ts > reports/contract-service.log 2>&1 &
SERVICE_PID=$!
trap 'kill "$SERVICE_PID" 2>/dev/null || true; wait "$SERVICE_PID" 2>/dev/null || true' EXIT
for _ in $(seq 1 180); do
  grep -q "^ready" reports/contract-service.log && break
  kill -0 "$SERVICE_PID" 2>/dev/null || { cat reports/contract-service.log; exit 1; }
  sleep 1
done
grep -q "^ready" reports/contract-service.log || { echo "the service did not start"; exit 1; }
INCLUDE=()
for operation in "${SERVED[@]}"; do INCLUDE+=(--include-operation-id "$operation"); done
tools/contract/.venv/bin/st --config-file tools/contract/schemathesis.toml run contracts/openapi/cook-assistance.openapi.yaml \
  --url "http://127.0.0.1:${PORT}/cooking-assistance" \
  --header "Authorization: Bearer $(cat reports/contract-token.txt)" \
  "${INCLUDE[@]}" \
  --seed 20261025 --max-examples 20 --rate-limit 55/m \
  --report junit --report-dir reports
