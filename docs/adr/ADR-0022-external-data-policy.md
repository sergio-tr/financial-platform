# ADR-0022: External data policy

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Every workspace integration follows explicit external-data policy; default is LOCAL_ONLY.

## Rationale

Slack/Notion/email are useful but should not receive sensitive financial data implicitly.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

User must explicitly opt into broader publication.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
