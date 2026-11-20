#!/usr/bin/env bash
# SER-142: regression harness — setsid process groups must tear down descendants.
set -euo pipefail

_pg=""
_child=""

_cleanup() {
  if [[ -n "${_pg}" ]] && kill -0 "-${_pg}" 2>/dev/null; then
    kill -TERM "-${_pg}" 2>/dev/null || true
    sleep 0.5
    kill -KILL "-${_pg}" 2>/dev/null || true
  fi
}
trap _cleanup EXIT

setsid bash -c 'sleep 600 & wait' &
_pg=$!
sleep 0.3
_child=$(pgrep -P "$_pg" 2>/dev/null | head -1 || true)

if [[ -z "${_child}" ]]; then
  echo "PROCESS_GROUP_CLEANUP_FAIL: expected child under setsid leader" >&2
  exit 1
fi

kill -TERM "-${_pg}" 2>/dev/null || true
for _ in $(seq 1 20); do
  if ! kill -0 "${_child}" 2>/dev/null; then
    echo "PROCESS_GROUP_CLEANUP_PASS"
    _pg=""
    trap - EXIT
    exit 0
  fi
  sleep 0.1
done

echo "PROCESS_GROUP_CLEANUP_FAIL: child ${_child} still alive after TERM on PGID ${_pg}" >&2
exit 1