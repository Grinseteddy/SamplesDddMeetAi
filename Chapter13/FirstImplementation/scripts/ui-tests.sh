#!/usr/bin/env bash
# End-to-end UI tests (Playwright) against the RUNNING application:
#   AppShell + micro-UIs + APIs on http://localhost:8080 (larder-app), Keycloak on http://localhost:8180.
# Start infra/docker-compose.yml and larder-app first. The tests sign in as the local test user of
# infra/keycloak/larder-realm.json and create (and mostly remove) their own data.
#
#   scripts/ui-tests.sh                      # whole suite
#   scripts/ui-tests.sh tests/02-rescue-story.spec.ts --headed   # arguments go to `playwright test`
#
# Environment: LARDER_APP_URL, LARDER_ISSUER_URI, LARDER_CLIENT_ID, LARDER_TEST_USER, LARDER_TEST_PASSWORD.
# Report: ui-tests/playwright-report (npx playwright show-report, from ui-tests/).
set -euo pipefail
source "$(dirname "$0")/common.sh"

APP=${LARDER_APP_URL:-http://localhost:8080}
ISSUER=${LARDER_ISSUER_URI:-http://localhost:8180/realms/larder}
curl -sf -m 10 -o /dev/null "$APP/app-shell/shell.js" || { echo "Larder is not running on $APP"; exit 1; }
curl -sf -m 10 -o /dev/null "$ISSUER/.well-known/openid-configuration" || { echo "Keycloak is not running on $ISSUER"; exit 1; }

cd "$ROOT/ui-tests"
npm ci --no-audit --no-fund
npx playwright install chromium
npx playwright test "$@"
