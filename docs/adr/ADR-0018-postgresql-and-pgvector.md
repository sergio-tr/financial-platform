# ADR-0018: PostgreSQL and pgvector

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use PostgreSQL as primary datastore and pgvector for local RAG embeddings; avoid a separate vector DB initially.

## Rationale

Reduces operational complexity while covering relational, event metadata and vector retrieval needs.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Separate datastore only after measured need.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
