#!/usr/bin/env bash
# Phase 0 test bench: exercises the contracts through Prism mocks.
#   1. every API answers with a contract-valid response and rejects calls without a token
#   2. contract rules (invariants) are enforced on requests
#   3. the examples in the contracts satisfy their own invariants
#   4. the rescue flow across Bounded Contexts (context map Q1-Q3) can be played
# Starts the mocks if they are not running and stops the ones it started.
# Prism runs with --errors: a response that violates its own contract fails with 500.
set -uo pipefail
source "$(dirname "$0")/common.sh"

passed=0; failed=0
COOK=f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074
RECIPE=7cf09822-77a1-46bb-812f-b7852bca0913
HELP_REQUEST=23a8eeed-35f6-460b-892e-7bb458a8fded
HELP=a9caf90d-00b4-4a66-8184-7d02152e8d6a

# call <expected status> <method> <api> <path> [json body] [extra curl args...]; body -> $BODY
call() {
    local expected=$1 method=$2 api=$3 path=$4 body=${5:-}
    shift 5 2>/dev/null || shift $#
    local url="http://localhost:$(port_of "$api")$path" args=("$@") status
    [ ${#args[@]} -eq 0 ] && args=("${AUTH_HEADERS[@]}")
    if [ -n "$body" ]; then
        status=$(curl -s -o "$MOCK_DIR/body" -w '%{http_code}' -X "$method" "$url" "${args[@]}" -d "$body")
    else
        status=$(curl -s -o "$MOCK_DIR/body" -w '%{http_code}' -X "$method" "$url" "${args[@]}")
    fi
    BODY=$(cat "$MOCK_DIR/body")
    if [ "$status" = "$expected" ]; then
        passed=$((passed + 1)); echo "  ok    $status $method $api$path"
    else
        failed=$((failed + 1)); echo "  FAIL  $status (expected $expected) $method $api$path"
        echo "$BODY" | head -c 400 | sed 's/^/        /'; echo
    fi
}

# check <description> <python expression over 'body'>
check() {
    if BODY="$BODY" python3 -c "import json,os,sys; body=json.loads(os.environ['BODY']); sys.exit(0 if ($2) else 1)" 2>/dev/null; then
        passed=$((passed + 1)); echo "  ok    $1"
    else
        failed=$((failed + 1)); echo "  FAIL  $1"
    fi
}

started_here=0
mkdir -p "$MOCK_DIR"
if ! is_listening "$(port_of cooking-assistance)"; then
    "$(dirname "$0")/mock-apis.sh" start >/dev/null
    started_here=1
fi

echo "== 1. Every API answers and is protected"
call 200 GET recipe-catalog /recipes
call 200 GET meal-planning /meal-plans
call 200 GET meal-preparation "/preparations/$HELP_REQUEST"
call 200 GET cooking-assistance /help-requests
call 200 GET cooking-assistance /helps
call 200 GET cook-profile /cooks
call 200 GET consent-management "/consents?subject=$COOK"
call 200 GET media "/images?businessObjectId=$HELP_REQUEST&businessObjectType=helpRequest"
call 200 GET notifications /notifications
call 200 GET sharing /thanks
call 401 GET cooking-assistance /help-requests "" -H "version: 1.0.0"
call 400 GET cooking-assistance /help-requests "" -H "Authorization: Bearer phase0"

echo "== 2. Contract rules on requests"
call 400 POST cooking-assistance /help-requests \
    '{"title":"Need a chef","type":"INGREDIENT_SUBSTITUTE","description":"d","recipe":"'$RECIPE'","ingredients":["'$RECIPE'"],"preferredProvider":["CHEF"]}'
call 400 POST cooking-assistance /help-requests \
    '{"title":"Which step?","type":"PREPARATION_STEP_EXPLANATION","description":"d","preferredProvider":["COMMUNITY"]}'
call 400 POST cooking-assistance /help-requests \
    '{"title":"Burnt","type":"STEPS_TO_MITIGATE_CATASTROPHE","description":"d","ingredients":["'$RECIPE'"],"preferredProvider":["COMMUNITY"]}'

echo "== 3. Examples satisfy the invariants"
call 200 GET cooking-assistance /help-requests
check "help request examples: howToStep only for PREPARATION_STEP_EXPLANATION" \
    "all('howToStep' not in r or r['type']=='PREPARATION_STEP_EXPLANATION' for r in body)"
check "help request examples: ingredients only for INGREDIENT_SUBSTITUTE" \
    "all('ingredients' not in r or r['type']=='INGREDIENT_SUBSTITUTE' for r in body)"
check "help request examples: CHEF only alone and only for MENU_PROPOSAL" \
    "all('CHEF' not in r['preferredProvider'] or (r['type']=='MENU_PROPOSAL' and len(r['preferredProvider'])==1) for r in body)"

echo "== 4. Rescue flow across Bounded Contexts"
call 200 GET recipe-catalog "/recipes/$RECIPE"
call 201 POST meal-preparation /preparations '{"recipe":"'$RECIPE'"}'
call 201 POST cooking-assistance /help-requests \
    '{"title":"Burning Catastrophe","type":"STEPS_TO_MITIGATE_CATASTROPHE","description":"Scones are burned","recipe":"'$RECIPE'","preferredProvider":["GRANDMA_AVATAR","COMMUNITY"]}'
call 200 GET cooking-assistance "/helps/$HELP"
check "help answers a help request of the same type" \
    "body['answer']['answerType']=='STEPS_TO_MITIGATE_CATASTROPHE' and body['helpRequest']=='$HELP_REQUEST'"
call 200 GET media "/images?businessObjectId=$HELP_REQUEST&businessObjectType=helpRequest"
call 200 GET consent-management "/consents?subject=$COOK"
call 201 POST sharing /thanks \
    '{"helpId":"'$HELP'","thanksText":"Grandma saved my scones","pictures":"https://larder.org/media/images/b009a5d1-0205-4b0c-af82-822229cf243a","recipients":[{"type":"GrandmaAvatar"}]}'
call 400 POST sharing /thanks \
    '{"thanksText":"Thanks without a help","pictures":"https://larder.org/media/images/b009a5d1-0205-4b0c-af82-822229cf243a"}'

[ $started_here -eq 1 ] && "$(dirname "$0")/mock-apis.sh" stop >/dev/null

echo
echo "passed: $passed  failed: $failed"
[ $failed -eq 0 ]
