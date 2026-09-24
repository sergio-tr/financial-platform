# Technology Baseline

Verification date: 2026-09-23.

## Backend

- Java 25 LTS.
- Spring Boot 4.1.x; current verified patch at baseline time: 4.1.1.
- Spring Modulith 2.1.x; current verified stable line at baseline time: 2.1.1.
- Spring AI 2.0.x; GA line designed for Spring Boot 4.0/4.1 and Spring Framework 7.
- Spring Security / Spring Data / Spring WebSocket versions are managed through the Spring Boot dependency baseline unless a documented exception is required.
- Liquibase for schema evolution.

## Frontend

- Angular 22.x, active support line at baseline time.
- TypeScript 6.0-compatible line as required by Angular 22.
- RxJS supported by Angular 22.
- Angular Signals for feature state, RxJS for event/async composition.

## Data and runtime

- PostgreSQL 18.x; current verified minor at baseline time: 18.6.
- pgvector compatible release selected and pinned during bootstrap.
- Temporal self-hosted, exact compatible release pinned during bootstrap.
- Ollama installed locally; exact model profiles are runtime configuration.
- Caddy as initial reverse proxy/local TLS endpoint.

## Version policy

Major/minor baselines are architectural. Exact patches are locked in build/Compose files during Phase 1 and upgraded through dependency-maintenance PRs after test validation. Never use floating `latest` tags in reproducible runtime definitions.
