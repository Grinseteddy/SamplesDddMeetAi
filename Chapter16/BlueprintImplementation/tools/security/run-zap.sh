#!/usr/bin/env bash
# Dynamic scan (ADR-CA-010, step 14): OWASP ZAP's API scan of the running test service against the approved OpenAPI
# spec, with a COOK token and the version header on every request. The active scan waits 1.1 s per request, so the
# API's 60 requests per minute and cook (ADR-CA-010) are not what the scan measures (one thread); the active scan stops
# after 20 minutes. Results in reports/security/.
set -euo pipefail
cd "$(dirname "$0")/../.."
ZAP=zaproxy/zap-stable:20261008
PORT="${CONTRACT_PORT:-8099}"
OUT=reports/security
mkdir -p "$OUT"
rm -f reports/contract-token.txt
CONTRACT_PORT="$PORT" node --import tsx src/cook-assistance/contract/serve-for-contract.ts > "$OUT/zap-service.log" 2>&1 &
SERVICE_PID=$!
trap 'kill "$SERVICE_PID" 2>/dev/null || true; wait "$SERVICE_PID" 2>/dev/null || true' EXIT
for _ in $(seq 1 180); do
  grep -q "^ready" "$OUT/zap-service.log" && break
  kill -0 "$SERVICE_PID" 2>/dev/null || { cat "$OUT/zap-service.log"; exit 1; }
  sleep 1
done
grep -q "^ready" "$OUT/zap-service.log" || { echo "the service did not start"; exit 1; }

# the spec's server is the production URL; ZAP scans this copy, pointed at the test instance
sed "s#https://larder.org/cooking-assistance#http://host.docker.internal:${PORT}/cooking-assistance#g" \
  contracts/openapi/cook-assistance.openapi.yaml > "$OUT/openapi-for-zap.yaml"

REPLACER="-config replacer.full_list(0).description=version -config replacer.full_list(0).enabled=true"
REPLACER="$REPLACER -config replacer.full_list(0).matchtype=REQ_HEADER -config replacer.full_list(0).matchstr=version"
REPLACER="$REPLACER -config replacer.full_list(0).regex=false -config replacer.full_list(0).replacement=2.0.0"
docker run --rm --add-host=host.docker.internal:host-gateway -v "$PWD/$OUT:/zap/wrk:rw" \
  -e ZAP_AUTH_HEADER=Authorization -e ZAP_AUTH_HEADER_VALUE="Bearer $(cat reports/contract-token.txt)" \
  "$ZAP" zap-api-scan.py -t /zap/wrk/openapi-for-zap.yaml -f openapi \
  -r zap-report.html -J zap-report.json -z "$REPLACER -config scanner.delayInMs=1100 -config scanner.threadPerHost=1 -config scanner.maxScanDurationInMins=20" -I
