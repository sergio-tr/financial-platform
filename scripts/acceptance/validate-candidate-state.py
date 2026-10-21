#!/usr/bin/env python3
from __future__ import annotations
import argparse
import json
from pathlib import Path
import sys
import yaml
from jsonschema import Draft202012Validator

ROOT = Path(__file__).resolve().parents[2]
SCHEMA = ROOT / "docs/delivery/candidate-state.schema.json"

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("state", type=Path)
    args = parser.parse_args()
    data = yaml.safe_load(args.state.read_text())
    schema = json.loads(SCHEMA.read_text())
    errors = list(Draft202012Validator(schema).iter_errors(data))
    if errors:
        for error in errors:
            print(error.message, file=sys.stderr)
        return 2
    print("CANDIDATE_STATE_PASS")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
