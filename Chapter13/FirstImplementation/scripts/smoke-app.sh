#!/usr/bin/env bash
# Phase 1 check of the running application (infra/docker-compose.yml + larder-app on :8080):
#   1. every Bounded Context has its own schema with Flyway history, and cannot read another schema
#   2. the RabbitMQ topology matches the AsyncAPI contracts
#   3. every API rejects calls without a token
#   4. phase 2 contexts work end to end: Consent Management, Cook Profile, Recipe Catalog, Media
#   5. phase 3 contexts work across contexts: Meal Planning and Meal Preparation read Recipe Catalog
#   6. phase 4 works asynchronously: help request -> Grandma Avatar -> Cooking Assistance -> Notification
#   7. phase 5 closes the rescue story: thanks with a picture, mentioning a cook only with consent
# Re-runnable: everything the script creates it deletes again (meal preparations have no delete
# operation in the contract and remain).
set -uo pipefail
source "$(dirname "$0")/common.sh"

APP=${LARDER_APP_URL:-http://localhost:8080}
ISSUER=${LARDER_ISSUER_URI:-http://localhost:8180/realms/larder}
COOK=f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074
SCHEMAS="consentmanagement cookingassistance cookprofile grandmaavatar mealplanning mealpreparation media notification recipecatalog sharing"
compose() { docker compose -f "$ROOT/infra/docker-compose.yml" "$@"; }
psql_as() { compose exec -T -e PGPASSWORD="$1" postgres psql -h localhost -U "$1" -d larder -tAc "$2" 2>&1; }

passed=0; failed=0
expect() { # expect <description> <expected> <actual>
    if [ "$2" = "$3" ]; then passed=$((passed + 1)); echo "  ok    $1"
    else failed=$((failed + 1)); echo "  FAIL  $1 (expected '$2', got '$3')"; fi
}

curl -sf "$APP/actuator/health" >/dev/null || { echo "larder-app is not running on $APP"; exit 1; }

echo "== 1. One schema and one user per Bounded Context"
for schema in $SCHEMAS; do
    expect "$schema migrated by its own user" "1" \
        "$(psql_as "$schema" "select count(*) > 0 from $schema.flyway_schema_history" | sed 's/t/1/')"
done
foreign=$(psql_as recipecatalog "select 1 from sharing.flyway_schema_history")
[[ "$foreign" == *"permission denied"* ]] && foreign=denied
expect "recipecatalog cannot read schema sharing" "denied" "$foreign"

echo "== 2. RabbitMQ topology from the AsyncAPI contracts"
bindings=$(compose exec -T rabbitmq rabbitmqctl list_bindings source_name destination_name routing_key -q 2>/dev/null)
for b in "cooking-assistance notifications.help-requested cooking-assistance.help.requested" \
         "cooking-assistance notifications.help-provided cooking-assistance.help.provided" \
         "cooking-assistance grandma-avatar.help-requested cooking-assistance.help.requested" \
         "grandma-avatar cooking-assistance.grandma-avatar-help-provided grandma-avatar.help.provided"; do
    set -- $b
    expect "$1 -[$3]-> $2" "yes" "$(echo "$bindings" | awk -v s="$1" -v d="$2" -v k="$3" '$1==s && $2==d && $3==k {f=1} END {print f ? "yes" : "no"}')"
done

echo "== 3. REST APIs are protected"
TOKEN=$(curl -s -X POST "$ISSUER/protocol/openid-connect/token" -d grant_type=password -d client_id=larder-dev \
    -d username=cook -d password=cook | python3 -c "import json,sys; print(json.load(sys.stdin)['access_token'])")
expect "token carries the cookId claim" "$COOK" "$(echo "$TOKEN" | cut -d. -f2 | python3 -c \
    "import sys,base64,json; s=sys.stdin.read().strip(); print(json.loads(base64.urlsafe_b64decode(s+'='*(-len(s)%4)))['cookId'])")"
for path in /recipe-catalog/recipes /cook-profile/cooks "/consent-management/consents?subject=$COOK" \
            "/media/images?businessObjectId=$COOK&businessObjectType=helpRequest" \
            /meal-planning/meal-plans /cooking-assistance/help-requests /cooking-assistance/helps \
            /notifications/notifications /sharing/thanks; do
    expect "GET $path without token" "401" "$(curl -s -o /dev/null -w '%{http_code}' "$APP$path" -H 'version: 1.0.0')"
done

# api <expected status> <method> <path> [json body]  -> response body in $BODY, Location in $LOCATION
api() {
    local out headers status
    out=$(mktemp); headers=$(mktemp)
    status=$(curl -s -o "$out" -D "$headers" -w '%{http_code}' -X "$2" "$APP$3" -H 'version: 1.0.0' \
        -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' ${4:+-d "$4"})
    BODY=$(cat "$out"); LOCATION=$(grep -i '^location:' "$headers" | tr -d '\r' | cut -d' ' -f2)
    rm -f "$out" "$headers"
    expect "$2 $3" "$1" "$status"
    [ "$status" = "$1" ] || echo "        $(echo "$BODY" | head -c 300)"
}
json() { echo "$BODY" | python3 -c "import json,sys; body=json.load(sys.stdin); print($1)" 2>/dev/null; }
id_of() { echo "${1##*/}"; }

echo "== 4a. Consent Management"
MENTION=3c6e2b7a-9d41-4f0b-8e5a-2b7c9d1e4f60
api 200 GET /consent-management/consents/consent-texts
expect "two consent texts are seeded" "2" "$(json 'len(body)')"
api 201 POST /consent-management/consents '{"subject":"'$COOK'","consentTextId":"'$MENTION'"}'
CONSENT=$(id_of "$LOCATION")
api 200 GET "/consent-management/consents?subject=$COOK"
expect "the new consent is listed and in force" "True" "$(json "any(c['consentId']=='$CONSENT' and c.get('revokedAt') is None for c in body)")"
api 403 POST /consent-management/consents '{"subject":"39a7aed5-2e50-48c8-8aa6-f3afa9f03f74","consentTextId":"'$MENTION'"}'
api 400 POST /consent-management/consents '{"subject":"'$COOK'","consentTextId":"'$COOK'"}'
api 204 DELETE "/consent-management/consents/$CONSENT"
api 200 GET "/consent-management/consents/$CONSENT"
expect "a revoked consent is kept with revokedAt" "True" "$(json "body.get('revokedAt') is not None")"
api 400 DELETE "/consent-management/consents/$CONSENT"

echo "== 4b. Cook Profile"
curl -s -o /dev/null -X DELETE "$APP/cook-profile/cooks/$COOK" -H 'version: 1.0.0' -H "Authorization: Bearer $TOKEN"
api 201 POST /cook-profile/cooks '{"email":"cook@larder.test","name":"Cook","givenName":"Test"}'
expect "the cook is registered under the token's cookId" "$COOK" "$(id_of "$LOCATION")"
api 400 POST /cook-profile/cooks '{"email":"cook@larder.test","name":"Cook","givenName":"Test"}'
api 200 PATCH "/cook-profile/cooks/$COOK" '{"status":"premium"}'
api 200 GET "/cook-profile/cooks/$COOK"
expect "the change is stored" "premium" "$(json "body['status']")"
api 400 GET "/cook-profile/cooks?email=no-address"
api 204 DELETE "/cook-profile/cooks/$COOK"
api 404 GET "/cook-profile/cooks/$COOK"

echo "== 4c. Recipe Catalog"
api 201 POST /recipe-catalog/recipes '{"name":"Scones for Sunday","preparationTime":"00:45","servings":6,"meal":"BREAKFAST","diet":"VEGETARIAN",
  "ingredients":[{"name":"Flour","value":500,"unit":"GRAM"},{"name":"Buttermilk","value":250,"unit":"MILLILITER"}],
  "howToSteps":[{"sequenceNumber":1,"description":"Mix flour and buttermilk"},{"sequenceNumber":2,"description":"Bake at 220 degrees for 12 minutes"}]}'
RECIPE=$(id_of "$LOCATION")
api 200 GET "/recipe-catalog/recipes/$RECIPE"
expect "the recipe has two steps in order" "[1, 2]" "$(json "[s['sequenceNumber'] for s in body['howToSteps']]")"
api 200 GET "/recipe-catalog/recipes?ingredients=flour&ingredients=buttermilk&diet=VEGETARIAN"
expect "search by ingredients and diet finds it" "True" "$(json "any(r['recipeId']=='$RECIPE' for r in body)")"
api 400 POST "/recipe-catalog/recipes/$RECIPE/how-to-steps" '{"sequenceNumber":2,"description":"Duplicate step number"}'
api 400 POST /recipe-catalog/recipes '{"name":"No ingredients","preparationTime":"00:10","servings":1,"meal":"LUNCH","diet":"NORMAL","ingredients":[],"howToSteps":[{"sequenceNumber":1,"description":"x"}]}'
api 204 DELETE "/recipe-catalog/recipes/$RECIPE"
api 404 GET "/recipe-catalog/recipes/$RECIPE"

echo "== 4d. Media (bytes in the S3 bucket, metadata in the schema)"
PNG=iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==
HELP_REQUEST=23a8eeed-35f6-460b-892e-7bb458a8fded
api 201 POST /media/images '{"media":"'$PNG'","links":[{"type":"helpRequest","url":"https://larder.org/cooking-assistance/help-requests/'$HELP_REQUEST'"}]}'
MEDIA=$(id_of "$LOCATION")
api 200 GET "/media/images/$MEDIA"
expect "the image comes back unchanged from the bucket" "$PNG" "$(json "body['media']")"
api 200 GET "/media/images?businessObjectId=$HELP_REQUEST&businessObjectType=helpRequest"
expect "the image is found via its help request" "True" "$(json "any(m['mediaId']=='$MEDIA' for m in body)")"
api 400 POST /media/images '{"media":"bm90IGFuIGltYWdl"}'
api 204 DELETE "/media/images/$MEDIA"
api 404 GET "/media/images/$MEDIA"

echo "== 5a. Meal Planning reads recipes from Recipe Catalog"
recipe() { # recipe <name> <diet> -> id in $RECIPE_ID
    api 201 POST /recipe-catalog/recipes '{"name":"'"$1"'","preparationTime":"00:30","servings":4,"meal":"DINNER","diet":"'$2'",
      "ingredients":[{"name":"Potatoes","value":1,"unit":"KILOGRAM"}],
      "howToSteps":[{"sequenceNumber":1,"description":"Peel"},{"sequenceNumber":2,"description":"Boil"},{"sequenceNumber":3,"description":"Serve"}]}'
    RECIPE_ID=$(id_of "$LOCATION")
}
recipe "Vegan potato soup" VEGAN; SOUP=$RECIPE_ID
recipe "Potato gratin" VEGETARIAN; GRATIN=$RECIPE_ID
api 201 POST /meal-planning/meal-plans '{}'
PLAN=$(id_of "$LOCATION")
api 200 PATCH "/meal-planning/meal-plans/$PLAN" '{"occasion":"Smoke test dinner","servings":6,"meal":"dinner",
  "courses":[{"step":1,"meal":{"recipe":"'$SOUP'"}},{"step":2,"meal":{"recipe":"'$GRATIN'"}}]}'
api 200 GET "/meal-planning/meal-plans/$PLAN"
expect "the plan has both courses" "2" "$(json "len(body['courses'])")"
api 200 GET "/meal-planning/meal-plans?diet=VEGETARIAN&occasion=smoke%20test%20dinner"
expect "vegan + vegetarian courses: the plan suits VEGETARIAN" "True" "$(json "any(p['mealPlanId']=='$PLAN' for p in body)")"
api 200 GET "/meal-planning/meal-plans?diet=VEGAN&diet=NORMAL"
expect "the most restrictive diet (VEGAN) is searched: not found" "False" "$(json "any(p['mealPlanId']=='$PLAN' for p in body)")"
api 400 PATCH "/meal-planning/meal-plans/$PLAN" '{"courses":[{"step":1,"meal":{"recipe":"'$COOK'"}}]}'
expect "an unknown recipe is rejected by asking Recipe Catalog" "UNKNOWN_RECIPE" "$(json "body['code']")"
api 400 PATCH "/meal-planning/meal-plans/$PLAN" '{"courses":[]}'
api 204 DELETE "/meal-planning/meal-plans/$PLAN"

echo "== 5b. Meal Preparation steps through a snapshot of the recipe"
api 200 GET "/recipe-catalog/recipes/$SOUP"
STEP1=$(json "body['howToSteps'][0]['howToStepId']"); STEP3=$(json "body['howToSteps'][2]['howToStepId']")
api 201 POST /meal-preparation/preparations '{"recipe":"'$SOUP'"}'
PREP=$(id_of "$LOCATION")
api 200 GET "/meal-preparation/preparations/$PREP"
expect "the preparation starts at step 1" "1" "$(json "body['currentStep']['sequenceNumber']")"
api 200 PATCH "/meal-preparation/preparations/$PREP/how-to-steps/next?stepId=$STEP1"
expect "next moves to step 2" "2" "$(json "body['sequenceNumber']")"; STEP2=$(json "body['howToStepId']")
api 400 PATCH "/meal-preparation/preparations/$PREP/how-to-steps/next?stepId=$STEP1"
expect "a stale step id is rejected" "STEP_NOT_CURRENT" "$(json "body['code']")"
api 204 DELETE "/recipe-catalog/recipes/$SOUP/how-to-steps/$STEP3"
api 200 PATCH "/meal-preparation/preparations/$PREP/how-to-steps/next?stepId=$STEP2"
expect "the snapshot still has step 3 after the recipe lost it" "3" "$(json "body['sequenceNumber']")"
api 400 PATCH "/meal-preparation/preparations/$PREP/how-to-steps/next?stepId=$STEP3"
expect "no step after the last one" "LAST_STEP_REACHED" "$(json "body['code']")"
api 200 PATCH "/meal-preparation/preparations/$PREP/how-to-steps/previous?stepId=$STEP3"
expect "previous moves back to step 2" "2" "$(json "body['sequenceNumber']")"
api 200 GET "/meal-preparation/preparations/$PREP/how-to-steps/$STEP1"
api 400 POST /meal-preparation/preparations '{"recipe":"'$COOK'"}'
expect "an unknown recipe cannot be prepared" "UNKNOWN_RECIPE" "$(json "body['code']")"
api 204 DELETE "/recipe-catalog/recipes/$SOUP"
api 204 DELETE "/recipe-catalog/recipes/$GRATIN"

echo "== 6. Asynchronous help journey over RabbitMQ"
# eventually <description> <expected> <path> <python expression>: polls until the expression yields the expected value
eventually() {
    local actual="" i
    for i in $(seq 1 40); do
        actual=$(curl -s "$APP$3" -H 'version: 1.0.0' -H "Authorization: Bearer $TOKEN" \
            | python3 -c "import json,sys; body=json.load(sys.stdin); print($4)" 2>/dev/null)
        [ "$actual" = "$2" ] && break
        sleep 0.25
    done
    expect "$1" "$2" "$actual"
}
api 201 POST /cooking-assistance/help-requests '{"title":"Burning Catastrophe","type":"STEPS_TO_MITIGATE_CATASTROPHE",
  "description":"Scones are burned and mother in law is coming in 30 minutes","recipe":"7cf09822-77a1-46bb-812f-b7852bca0913",
  "preferredProvider":["GRANDMA_AVATAR","COMMUNITY"]}'
REQUEST=$(id_of "$LOCATION")
eventually "Grandma answered: the request is ANSWERED (HelpRequested -> Grandma -> HelpProvided)" "ANSWERED" \
    "/cooking-assistance/help-requests/$REQUEST" "body['status']"
api 200 GET "/cooking-assistance/helps?helpRequestId=$REQUEST"
expect "exactly one help, from the Grandma Avatar" "1 GRANDMA_AVATAR" "$(json "str(len(body)) + ' ' + body[0]['helpProviderType']")"
HELP=$(json "body[0]['helpId']")
expect "Grandma's advice for burnt scones" "Stay calm" "$(json "body[0]['answerTitle']")"
eventually "Notification informed the cook (HelpProvided -> Notification)" "True" "/notifications/notifications" \
    "any(n['link'].endswith('/cooking-assistance/helps/$HELP') and n['status']=='NEW' for n in body['notifications'])"
NOTIFICATION=$(curl -s "$APP/notifications/notifications" -H 'version: 1.0.0' -H "Authorization: Bearer $TOKEN" | python3 -c \
    "import json,sys; print([n['notificationId'] for n in json.load(sys.stdin)['notifications'] if n['link'].endswith('/$HELP')][0])")
api 200 PUT "/notifications/notifications/$NOTIFICATION/status" '{"status":"READ"}'
api 204 DELETE "/notifications/notifications/$NOTIFICATION"
api 404 GET "/notifications/notifications/$NOTIFICATION"
api 400 DELETE "/cooking-assistance/help-requests/$REQUEST"
expect "an answered request cannot be withdrawn" "True" "$(json "body['code'] is not None")"

api 201 POST /cooking-assistance/help-requests '{"title":"Dinner for my in-laws","type":"MENU_PROPOSAL",
  "description":"Six guests on Saturday","preferredProvider":["CHEF"]}'
CHEF_REQUEST=$(id_of "$LOCATION")
sleep 2
api 200 GET "/cooking-assistance/help-requests/$CHEF_REQUEST"
expect "a chef-only request is left to the chef - Grandma stays out" "OPEN" "$(json "body['status']")"
api 204 DELETE "/cooking-assistance/help-requests/$CHEF_REQUEST"
api 400 POST /cooking-assistance/help-requests '{"title":"Chef for a catastrophe","type":"STEPS_TO_MITIGATE_CATASTROPHE",
  "description":"d","preferredProvider":["CHEF"]}'

api 201 POST /cooking-assistance/help-requests '{"title":"Fold in?","type":"PREPARATION_STEP_EXPLANATION",
  "description":"What does fold in mean?","recipe":"7cf09822-77a1-46bb-812f-b7852bca0913",
  "howToStep":"65610dee-fb83-4341-a730-26a4a99a621a","preferredProvider":["COMMUNITY"]}'
COMMUNITY_REQUEST=$(id_of "$LOCATION")
api 201 POST /cooking-assistance/helps '{"helpRequest":"'$COMMUNITY_REQUEST'","answerTitle":"Gently!","helpProviderType":"COMMUNITY",
  "answer":{"answerType":"PREPARATION_STEP_EXPLANATION","recipe":"7cf09822-77a1-46bb-812f-b7852bca0913",
  "howToStep":"65610dee-fb83-4341-a730-26a4a99a621a","description":"Lift the dough over the filling with a spatula."}}'
COMMUNITY_HELP=$(id_of "$LOCATION")
eventually "a community help over REST also reaches Notification" "True" "/notifications/notifications" \
    "any(n['link'].endswith('/$COMMUNITY_HELP') for n in body['notifications'])"

echo "== 7. Sharing: thanks for the help, guarded by consents"
PHOTOS=5f8d8a1c-515d-4eae-a6b1-0a0313edfc31
# Precondition: the cook has neither photo nor mention consent in force (e.g. given earlier in the UI)
for consent in $(curl -s "$APP/consent-management/consents?subject=$COOK" -H 'version: 1.0.0' -H "Authorization: Bearer $TOKEN" \
        | python3 -c "import json,sys; print(' '.join(c['consentId'] for c in json.load(sys.stdin) if 'revokedAt' not in c))"); do
    curl -s -o /dev/null -X DELETE "$APP/consent-management/consents/$consent" -H 'version: 1.0.0' -H "Authorization: Bearer $TOKEN"
done
api 201 POST /media/images '{"media":"'$PNG'","links":[{"type":"helpRequest","url":"https://larder.org/cooking-assistance/help-requests/'$REQUEST'"}]}'
PICTURE=$LOCATION; PICTURE_ID=$(id_of "$LOCATION")
THANKS_FOR_GRANDMA='{"helpId":"'$HELP'","recipients":[{"type":"GrandmaAvatar"}],"thanksText":"Grandma saved my scones!","pictures":"'$PICTURE'"}'
api 400 POST /sharing/thanks "$THANKS_FOR_GRANDMA"
expect "a picture in public thanks needs the giver's photo consent" "PICTURE_WITHOUT_CONSENT" "$(json "body['code']")"
api 201 POST /consent-management/consents '{"subject":"'$COOK'","consentTextId":"'$PHOTOS'"}'
PHOTO_CONSENT=$(id_of "$LOCATION")
api 201 POST /sharing/thanks "$THANKS_FOR_GRANDMA"
GRANDMA_THANKS=$(id_of "$LOCATION")
api 400 POST /sharing/thanks "$THANKS_FOR_GRANDMA"
expect "one thanks per help" "THANKS_ALREADY_GIVEN" "$(json "body['code']")"

api 400 POST /sharing/thanks '{"helpId":"'$COMMUNITY_HELP'","recipients":[{"type":"GrandmaAvatar"}],"thanksText":"Thanks!","pictures":"'$PICTURE'"}'
expect "the recipients must be who actually helped" "RECIPIENT_NOT_HELPER" "$(json "body['code']")"
THANKS_FOR_COOK='{"helpId":"'$COMMUNITY_HELP'","recipients":[{"type":"Cook","cooks":["'$COOK'"]}],"thanksText":"Thanks for the folding tip!","pictures":"'$PICTURE'"}'
api 400 POST /sharing/thanks "$THANKS_FOR_COOK"
expect "a cook may be mentioned only with their consent" "MENTION_WITHOUT_CONSENT" "$(json "body['code']")"
api 201 POST /consent-management/consents '{"subject":"'$COOK'","consentTextId":"'$MENTION'"}'
MENTION_CONSENT=$(id_of "$LOCATION")
api 201 POST /sharing/thanks "$THANKS_FOR_COOK"
COOK_THANKS=$(id_of "$LOCATION")
api 200 GET "/sharing/thanks?recipient=$COOK"
expect "the mentioned cook finds the thanks" "True" "$(json "any(t['thanksId']=='$COOK_THANKS' for t in body['thanks'])")"
api 200 GET "/sharing/thanks?giver=$COOK"
expect "the giver finds the thanks to Grandma" "True" "$(json "any(t['thanksId']=='$GRANDMA_THANKS' for t in body['thanks'])")"
expect "the giver finds the thanks to the cook" "True" "$(json "any(t['thanksId']=='$COOK_THANKS' for t in body['thanks'])")"
api 200 PATCH "/sharing/thanks/$GRANDMA_THANKS" '{"thanksText":"Grandma saved my scones - and my mother-in-law loved them!"}'
api 200 GET "/sharing/thanks/$GRANDMA_THANKS"
expect "the giver changed the text" "True" "$(json "'loved them' in body['thanksText']")"
api 400 POST /sharing/thanks '{"helpId":"'$COOK'","thanksText":"For nothing","pictures":"'$PICTURE'"}'
expect "thanks need an existing help" "UNKNOWN_HELP" "$(json "body['code']")"

api 204 DELETE "/sharing/thanks/$GRANDMA_THANKS"
api 204 DELETE "/sharing/thanks/$COOK_THANKS"
api 204 DELETE "/consent-management/consents/$PHOTO_CONSENT"
api 204 DELETE "/consent-management/consents/$MENTION_CONSENT"
api 204 DELETE "/media/images/$PICTURE_ID"

dlq=$(compose exec -T rabbitmq rabbitmqctl list_queues name messages -q 2>/dev/null | awk '$1 ~ /\.dlq$/ {s+=$2} END {print s+0}')
expect "no message ended up in a dead-letter queue" "0" "$dlq"

echo
echo "passed: $passed  failed: $failed"
[ $failed -eq 0 ]
