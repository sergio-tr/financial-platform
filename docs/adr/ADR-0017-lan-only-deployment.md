# ADR-0017: LAN-only deployment

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Expose the platform only on the local network through HTTPS/WSS reverse proxy; no inbound WAN forwarding.

## Rationale

Financial data sensitivity and user requirement favor a private local deployment.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Outbound provider access is allowed; remote access requires a future ADR.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
