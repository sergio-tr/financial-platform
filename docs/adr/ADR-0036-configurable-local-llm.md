# ADR-0036: Configurable local LLM

Status: **ACCEPTED**  
Date: **2026-09-23**

## Context

This decision is part of Architecture Baseline v1.0 for the local financial platform.

## Decision

Do not hard-code a single Ollama model in architecture; select concrete chat/embedding models using local benchmarked profiles.

## Rationale

Model quality/speed depends on hardware and evolves faster than application architecture.

## Consequences and constraints

Implementations must conform to this decision and must not hide an incompatible design behind an adapter or temporary shortcut. Tests and architecture checks should enforce the decision where practical.

## Revisit trigger

Ports and logical model profiles remain stable.

## Supersession

Any incompatible change requires a new ADR that explicitly supersedes this record.
