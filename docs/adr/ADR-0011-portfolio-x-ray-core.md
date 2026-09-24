# ADR-0011: Portfolio X-Ray core

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Portfolio X-Ray is a dedicated core bounded context from the first domain model.

## Rationale

Look-through, overlap, concentration, data coverage and freshness are primary product capabilities.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

It is not implemented as an ad-hoc dashboard calculator.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
