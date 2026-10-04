#!/usr/bin/env bash
# Lints all OpenAPI and validates all AsyncAPI contracts. Exit code != 0 on any error.
set -uo pipefail
source "$(dirname "$0")/common.sh"

failed=0

echo "== OpenAPI (Redocly)"
files=()
for entry in $APIS; do files+=("$CONTRACTS/openapi/$(api_name "$entry").openapi.yaml"); done
if output=$(npx -y "$REDOCLY" lint --format=summary "${files[@]}" 2>&1); then
    for f in "${files[@]}"; do echo "  ok    $(basename "$f")"; done
else
    echo "$output" | sed 's/^/        /' | tail -40
    failed=1
fi

echo "== AsyncAPI (AsyncAPI CLI, incl. referenced documents)"
for name in $ASYNC_APIS; do
    file="$CONTRACTS/asyncapi/$name.asyncapi.yaml"
    output=$(cd "$CONTRACTS/asyncapi" && SUPPRESS_NO_CONFIG_WARNING=1 npx -y "$ASYNCAPI" validate "$(basename "$file")" 2>&1)
    if echo "$output" | grep -q "is valid"; then
        echo "  ok    $(basename "$file")"
    else
        echo "  FAIL  $(basename "$file")"
        echo "$output" | grep -vE 'WARNING|DeprecationWarning|trace-deprecation|analytics|^\s*$' | sed 's/^/        /' | tail -20
        failed=1
    fi
done

exit $failed
