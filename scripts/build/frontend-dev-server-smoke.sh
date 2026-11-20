#!/usr/bin/env bash
# SER-142: Angular dev-server HTTP smoke with bounded process-group teardown (CI + local).
set -euo pipefail

readonly SMOKE_HOST="${SMOKE_HOST:-127.0.0.1}"
readonly SMOKE_PORT="${SMOKE_PORT:-4200}"
readonly SMOKE_URL="http://${SMOKE_HOST}:${SMOKE_PORT}/"
readonly WAIT_SECONDS="${WAIT_SECONDS:-90}"

_SERVER_PGID=""

_cleanup() {
  if [[ -z "${_SERVER_PGID}" ]]; then
    return 0
  fi
  if kill -0 "-${_SERVER_PGID}" 2>/dev/null; then
    kill -TERM "-${_SERVER_PGID}" 2>/dev/null || true
    local i
    for i in $(seq 1 20); do
      if ! kill -0 "-${_SERVER_PGID}" 2>/dev/null; then
        break
      fi
      sleep 0.25
    done
    if kill -0 "-${_SERVER_PGID}" 2>/dev/null; then
      kill -KILL "-${_SERVER_PGID}" 2>/dev/null || true
    fi
  fi
  _SERVER_PGID=""
}

_verify_port_released() {
  if command -v ss >/dev/null 2>&1; then
    if ss -ltn 2>/dev/null | grep -qE ":${SMOKE_PORT}[[:space:]]"; then
      echo "V0_FRONTEND_DEV_SERVER_SMOKE_FAIL: port ${SMOKE_PORT} still listening after cleanup" >&2
      return 1
    fi
  elif command -v lsof >/dev/null 2>&1; then
    if lsof -iTCP:"${SMOKE_PORT}" -sTCP:LISTEN -t >/dev/null 2>&1; then
      echo "V0_FRONTEND_DEV_SERVER_SMOKE_FAIL: port ${SMOKE_PORT} still listening after cleanup" >&2
      return 1
    fi
  fi
  return 0
}

trap _cleanup EXIT

export NG_CLI_ANALYTICS="${NG_CLI_ANALYTICS:-false}"

setsid pnpm --dir frontend/app start >/tmp/v0-ng-serve-smoke.log 2>&1 &
_SERVER_PGID=$!

_smoke_passed=false
for _ in $(seq 1 "${WAIT_SECONDS}"); do
  if curl -sf "${SMOKE_URL}" >/dev/null; then
    echo "V0_FRONTEND_DEV_SERVER_SMOKE_PASS"
    _smoke_passed=true
    break
  fi
  sleep 1
done

if [[ "${_smoke_passed}" != "true" ]]; then
  echo "V0_FRONTEND_DEV_SERVER_SMOKE_FAIL: no HTTP 200 from dev server (see /tmp/v0-ng-serve-smoke.log)" >&2
  exit 1
fi

_cleanup
trap - EXIT
_verify_port_released