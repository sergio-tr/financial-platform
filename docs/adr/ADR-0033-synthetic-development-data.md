# ADR-0033: Synthetic development data

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Real financial data is prohibited in Git, cloud-agent context and test fixtures; use synthetic datasets.

## Rationale

Protects sensitive information and keeps automated development safe.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Local runtime may use real data outside source-controlled/agent-visible paths.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
