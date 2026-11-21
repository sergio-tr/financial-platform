"""Shared field-privacy catalog validation (SER-160 / SER-161 / SER-109)."""

from __future__ import annotations

import json
from pathlib import Path

import yaml
from jsonschema import Draft202012Validator

ROOT = Path(__file__).resolve().parents[2]
SCHEMA_PATH = ROOT / "docs/security/field-privacy-catalog.schema.json"
CLASS_POLICY_PATH = ROOT / "docs/security/data-classification.yaml"

SECRET_ALLOW = frozenset({"secretvault_protected_ipc_scoped_lease", "application_heap_transient"})

COLLABORATION_SINKS = frozenset(
    {
        "git_repository",
        "ci_artifact",
        "slack",
        "linear",
        "figma",
        "collaboration_tool",
    }
)

SECRET_MANDATORY_DENY = frozenset(
    {
        "rabbitmq",
        "outbox",
        "replay_buffer",
        "temporal",
        "ai_inference",
        "rag_index",
        "telemetry",
        "git_repository",
        "ci_artifact",
        "slack",
        "linear",
        "figma",
    }
)

SECURITY_SENSITIVE_MANDATORY_DENY = frozenset({"ai_inference", "rag_index"})

PERSONAL_FINANCIAL_MANDATORY_DENY = frozenset(
    {
        "external_export",
        "cloud_saas",
        "git_repository",
        "ci_artifact",
        "slack",
        "linear",
        "figma",
        "collaboration_tool",
    }
)

SESSION_BAG_FORBIDDEN_CLASSES = frozenset({"SECRET", "SECURITY_SENSITIVE"})
SESSION_BAG_EXCEPTION_FIELD_IDS = frozenset(
    {
        "platform_session.spring_session_attributes.attribute_bytes",
        "platform_session.forbidden.credentials_in_attribute_bag",
    }
)

FINANCE_PUBLIC_FORBIDDEN_SUBSTRINGS = (
    "finance.position",
    "finance.holding",
    "finance.account",
    "finance.transaction",
    "finance.snapshot",
    "finance.portfolio",
    "finance.performance",
    "finance.xray",
    "finance.import.manual",
    "finance.import.provider_payload",
)

FINANCE_CREDENTIAL_MARKERS = ("credential", "client_secret", "api_token", "oauth_refresh")


def load_yaml(path: Path) -> dict:
    return yaml.safe_load(path.read_text())


def validate_schema(catalog: dict, schema: dict) -> list[str]:
    errors: list[str] = []
    validator = Draft202012Validator(schema)
    for err in sorted(validator.iter_errors(catalog), key=lambda e: list(e.path)):
        loc = ".".join(str(p) for p in err.path) or "(root)"
        errors.append(f"SCHEMA {loc}: {err.message}")
    return errors


def sink_vocabulary(catalog: dict) -> set[str]:
    return set(catalog.get("canonicalSinkVocabulary") or [])


def validate_retention(ret: dict, prefix: str) -> list[str]:
    errors: list[str] = []
    status = ret.get("status")
    if status == "REF" and not ret.get("policyRef"):
        errors.append(f"{prefix} retention REF requires policyRef")
    if status == "BLOCKED_DECISION" and not ret.get("blockedReason"):
        errors.append(f"{prefix} BLOCKED_DECISION retention requires blockedReason")
    if status not in ("REF", "BLOCKED_DECISION"):
        errors.append(f"{prefix} retention status missing or invalid")
    return errors


def validate_finance_slice_rules(catalog: dict) -> list[str]:
    errors: list[str] = []
    slice_id = str(catalog.get("sliceId") or "")
    if not slice_id.startswith("SER-161"):
        return errors

    for idx, field in enumerate(catalog.get("fields") or []):
        prefix = f"fields[{idx}]"
        fid = field.get("fieldId") or ""
        data_class = field.get("dataClass")

        if data_class == "PUBLIC" and any(sub in fid for sub in FINANCE_PUBLIC_FORBIDDEN_SUBSTRINGS):
            errors.append(f"{prefix} FINANCIAL field {fid} must not be PUBLIC")

        lower = fid.lower()
        if any(m in lower for m in FINANCE_CREDENTIAL_MARKERS) and data_class != "SECRET":
            errors.append(f"{prefix} provider credential field {fid} must be SECRET")

        family = field.get("fieldFamily") or ""
        if family == "provider_credential" and data_class != "SECRET":
            errors.append(f"{prefix} fieldFamily provider_credential requires SECRET")

        if fid.startswith("finance.import") and "retention" not in field:
            errors.append(f"{prefix} importer field requires retention block")

    return errors


def validate_semantics(catalog: dict) -> list[str]:
    errors: list[str] = []
    vocab = sink_vocabulary(catalog)
    if not vocab:
        errors.append("FIELD_PRIVACY missing canonicalSinkVocabulary")

    seen_ids: set[str] = set()
    for idx, field in enumerate(catalog.get("fields") or []):
        prefix = f"fields[{idx}]"
        fid = field.get("fieldId")
        if fid in seen_ids:
            errors.append(f"{prefix} duplicate fieldId={fid}")
        seen_ids.add(fid)

        owner = field.get("owner")
        if not owner or owner.strip() == "":
            errors.append(f"{prefix} missing owner")
        if "PENDING (SER-109)" in (owner or ""):
            errors.append(f"{prefix} field owner must not fake global SER-109 closure")

        errors.extend(validate_retention(field.get("retention") or {}, prefix))

        mat = field.get("materializationStatus")
        binding = field.get("physicalBinding") or {}
        if mat == "BLOCKED_DECISION" and not binding.get("blockedGapId"):
            errors.append(f"{prefix} BLOCKED_DECISION requires physicalBinding.blockedGapId")
        if mat == "NORMATIVE_PLANNED" and not binding.get("postgresObjectKey"):
            if fid not in SESSION_BAG_EXCEPTION_FIELD_IDS:
                errors.append(f"{prefix} NORMATIVE_PLANNED requires postgresObjectKey")

        sinks = field.get("sinks") or {}
        allow = set(sinks.get("allow") or [])
        deny = set(sinks.get("deny") or [])
        unknown_allow = allow - vocab
        unknown_deny = deny - vocab
        if unknown_allow:
            errors.append(f"{prefix} unknown allow sinks={sorted(unknown_allow)}")
        if unknown_deny:
            errors.append(f"{prefix} unknown deny sinks={sorted(unknown_deny)}")
        overlap = allow & deny
        if overlap:
            errors.append(f"{prefix} sink allow/deny overlap={sorted(overlap)}")

        data_class = field.get("dataClass")
        if data_class == "SECRET":
            if allow and not allow.issubset(SECRET_ALLOW):
                errors.append(
                    f"{prefix} SECRET allow must be subset of {sorted(SECRET_ALLOW)} actual={sorted(allow)}"
                )
            if not SECRET_MANDATORY_DENY.issubset(deny):
                missing = sorted(SECRET_MANDATORY_DENY - deny)
                errors.append(f"{prefix} SECRET missing mandatory deny sinks={missing}")
        elif data_class == "SECURITY_SENSITIVE":
            if not SECURITY_SENSITIVE_MANDATORY_DENY.issubset(deny):
                missing = sorted(SECURITY_SENSITIVE_MANDATORY_DENY - deny)
                errors.append(f"{prefix} SECURITY_SENSITIVE missing deny AI/RAG={missing}")
        elif data_class in ("PERSONAL", "FINANCIAL_SENSITIVE"):
            if not PERSONAL_FINANCIAL_MANDATORY_DENY.issubset(deny):
                missing = sorted(PERSONAL_FINANCIAL_MANDATORY_DENY - deny)
                errors.append(f"{prefix} {data_class} missing collaboration/export deny={missing}")
            if data_class == "FINANCIAL_SENSITIVE":
                if "telemetry" in allow:
                    errors.append(f"{prefix} FINANCIAL_SENSITIVE must not allow telemetry sink")
                forbidden_allow = allow & SECURITY_SENSITIVE_MANDATORY_DENY
                if forbidden_allow:
                    errors.append(f"{prefix} FINANCIAL_SENSITIVE must not allow AI/RAG sinks={sorted(forbidden_allow)}")

        for row in field.get("storageSurfaces") or []:
            if (
                row.get("surface") == "spring_session_attributes_jsonb"
                and row.get("permitted")
                and data_class in SESSION_BAG_FORBIDDEN_CLASSES
                and fid not in SESSION_BAG_EXCEPTION_FIELD_IDS
            ):
                errors.append(
                    f"{prefix} {data_class} forbidden in spring_session_attributes_jsonb"
                )

        if (
            field.get("fieldId") == "identity.credential.raw_password"
            and "postgresql_encrypted_at_rest" in allow
        ):
            errors.append(f"{prefix} raw_password must not allow postgresql_encrypted_at_rest")

    errors.extend(validate_finance_slice_rules(catalog))

    if not CLASS_POLICY_PATH.is_file():
        errors.append("FIELD_PRIVACY missing_class_policy=data-classification.yaml")
    else:
        policy = load_yaml(CLASS_POLICY_PATH)
        if policy.get("defaultPolicy") != "deny":
            errors.append("FIELD_PRIVACY class_policy defaultPolicy must be deny")

    return errors


def validate_catalog_file(path: Path, schema: dict | None = None) -> tuple[bool, list[str]]:
    if schema is None:
        schema = json.loads(SCHEMA_PATH.read_text())
    catalog = load_yaml(path)
    errors = validate_schema(catalog, schema)
    if not errors:
        errors.extend(validate_semantics(catalog))
    return (not errors, errors)