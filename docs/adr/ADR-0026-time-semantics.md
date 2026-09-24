# ADR-0026: Time semantics

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Technical events use UTC Instant; financial/business dates use LocalDate; workspace configuration owns ZoneId.

## Rationale

Separates global event ordering from calendar-domain semantics and handles DST correctly.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Workflow schedules resolve against workspace timezone.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
