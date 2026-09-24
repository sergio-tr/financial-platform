# ADR-0002: Runtime baseline

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use Java 25 LTS, Spring Boot 4.1.x, Spring Modulith 2.1.x-compatible baseline and Angular 22.x. Pin exact patch versions during repository bootstrap.

## Rationale

Current supported baselines provide long-lived Java support and modern Spring/Angular capabilities.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Patch/minor upgrades may occur without new ADR if compatibility and tests remain valid; major changes require review.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
