#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import sys

import yaml
from jsonschema import Draft202012Validator

ROOT = Path(__file__).resolve().parents[2]
SCHEMA = ROOT / "docs/governance/task-packet.schema.json"

def canonical_hash(packet: dict) -> str:
    payload = dict(packet)
    payload["packetHash"] = ""
    raw = json.dumps(payload, sort_keys=True, separators=(",", ":"), ensure_ascii=False).encode()
    return hashlib.sha256(raw).hexdigest()

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("packet", type=Path)
    parser.add_argument("--skip-hash", action="store_true")
    args = parser.parse_args()

    packet = yaml.safe_load(args.packet.read_text())
    schema = json.loads(SCHEMA.read_text())
    errors = sorted(Draft202012Validator(schema).iter_errors(packet), key=lambda e: list(e.path))
    if errors:
        for error in errors:
            print(f"INVALID_TASK_PACKET schema {list(error.path)}: {error.message}", file=sys.stderr)
        return 2

    if not args.skip_hash:
        expected = canonical_hash(packet)
        if packet["packetHash"] != expected:
            print(f"INVALID_TASK_PACKET packetHash expected={expected} actual={packet['packetHash']}", file=sys.stderr)
            return 3

    if set(packet["allowedPaths"]) & set(packet["forbiddenPaths"]):
        print("INVALID_TASK_PACKET allowedPaths intersects forbiddenPaths", file=sys.stderr)
        return 4

    print("READY_TO_IMPLEMENT")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
