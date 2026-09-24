# ADR-0006: Workspace tenancy

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Workspace is the ownership/authorization boundary; resources belong to a workspace rather than directly to a user.

## Rationale

Supports future shared/family/read-only use without domain redesign.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Initial deployments may still create one workspace per user.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
