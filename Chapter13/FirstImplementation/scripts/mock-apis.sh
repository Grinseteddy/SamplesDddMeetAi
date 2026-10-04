#!/usr/bin/env bash
# Starts, stops or lists one Prism mock per OpenAPI contract.
#   scripts/mock-apis.sh start    start all mocks in the background
#   scripts/mock-apis.sh stop     stop all mocks
#   scripts/mock-apis.sh status   show which mocks are listening
# Logs: target/mocks/<api>.log
set -uo pipefail
source "$(dirname "$0")/common.sh"

start() {
    mkdir -p "$MOCK_DIR"
    local entry name port
    # Install Prism once up front; parallel first-time npx installs race each other.
    npx -y "$PRISM" --version >/dev/null 2>&1 || { echo "Cannot install $PRISM"; exit 1; }
    for entry in $APIS; do
        name=$(api_name "$entry"); port=$(api_port "$entry")
        if is_listening "$port"; then
            echo "  running  $name  http://localhost:$port"
            continue
        fi
        npx -y "$PRISM" mock --errors "$CONTRACTS/openapi/$name.openapi.yaml" -p "$port" \
            > "$MOCK_DIR/$name.log" 2>&1 &
        echo $! > "$MOCK_DIR/$name.pid"
    done
    echo "Waiting for mocks ..."
    for entry in $APIS; do
        name=$(api_name "$entry"); port=$(api_port "$entry")
        for _ in $(seq 1 90); do is_listening "$port" && break; sleep 1; done
        if is_listening "$port"; then
            echo "  up       $name  http://localhost:$port"
        else
            echo "  FAILED   $name  (see $MOCK_DIR/$name.log)"
        fi
    done
}

stop() {
    local pidfile
    for pidfile in "$MOCK_DIR"/*.pid; do
        [ -e "$pidfile" ] || continue
        # npx starts prism as a child process; stop the whole group of children
        pkill -P "$(cat "$pidfile")" 2>/dev/null
        kill "$(cat "$pidfile")" 2>/dev/null
        rm -f "$pidfile"
    done
    echo "Mocks stopped."
}

status() {
    local entry
    for entry in $APIS; do
        if is_listening "$(api_port "$entry")"; then s="up  "; else s="down"; fi
        echo "  $s  $(api_name "$entry")  http://localhost:$(api_port "$entry")"
    done
}

case "${1:-}" in
    start) start ;;
    stop) stop ;;
    status) status ;;
    *) echo "usage: $0 start|stop|status"; exit 2 ;;
esac
