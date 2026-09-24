# ADR-0010: Capability-based connectors

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Design connector SPI using segregated capability interfaces and capability negotiation.

## Rationale

Providers expose heterogeneous data; a god interface would couple the platform to the weakest/common denominator.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Capabilities are explicitly versioned.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
