# ADR-0001: Modular monolith

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Use a Spring Boot modular monolith with Spring Modulith; maintain explicit bounded-context boundaries rather than microservices.

## Rationale

Avoid distributed-system overhead while preserving modularity and enforceable boundaries.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Revisit only if independently deployable scaling/availability requirements are demonstrated.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
