# ADR-0030: Initial infrastructure exclusions

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Do not introduce Kafka, RabbitMQ, Redis or Kubernetes initially.

## Rationale

Current scale and topology do not justify their operational cost.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

A future ADR may add one after demonstrated requirements.

## Supersession

**ADR-0037** partially supersedes this record for **RabbitMQ used exclusively as the private STOMP broker relay** (SER-38 / REQ-REALTIME-002). Kafka, Redis, Kubernetes, and other RabbitMQ roles remain governed by ADR-0030 until a future ADR changes them.

Any other incompatible change requires a new ADR that explicitly supersedes this record.
