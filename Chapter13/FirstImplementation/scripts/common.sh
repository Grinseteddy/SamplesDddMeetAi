# Shared settings for the contract scripts. Source it, do not run it.
# Works with the bash 3.2 that ships with macOS (no associative arrays).

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CONTRACTS="$ROOT/contracts"
MOCK_DIR="$ROOT/target/mocks"

# Pinned tool versions, run via npx (Node.js 18+ required)
REDOCLY="@redocly/cli@2.57.0"
ASYNCAPI="@asyncapi/cli@4.1.1"
PRISM="@stoplight/prism-cli@5.14.2"

# <contract file name>:<mock port> - one Prism mock per Bounded Context with a REST API
APIS="
recipe-catalog:4010
meal-planning:4011
meal-preparation:4012
cooking-assistance:4013
cook-profile:4014
consent-management:4015
media:4016
notifications:4017
sharing:4018
"

ASYNC_APIS="cooking-assistance grandma-avatar notifications"

api_name() { echo "${1%%:*}"; }
api_port() { echo "${1##*:}"; }

port_of() {
    local entry
    for entry in $APIS; do
        if [ "$(api_name "$entry")" = "$1" ]; then api_port "$entry"; return; fi
    done
    echo "unknown API $1" >&2
    return 1
}

is_listening() { curl -s -o /dev/null "http://localhost:$1/" 2>/dev/null; }

# Mocks do not verify tokens, they only check that one is present.
AUTH_HEADERS=(-H "Authorization: Bearer phase0" -H "version: 1.0.0" -H "Content-Type: application/json")
