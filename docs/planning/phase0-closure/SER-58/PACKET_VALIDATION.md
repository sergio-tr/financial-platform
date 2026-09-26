# SER-58 packet validation — pre-execution gate

## Authority checked
- Linear issue SER-58
- Document: SER-58 — Contract-generation spike execution packet candidate v1
- Architecture Refinement Iteration 19 (LOCKED BASELINE EXTENSION) UUID a827c378-fc80-4e0b-9823-977d32570df7

## Packet rule (literal)
> Exact AsyncAPI CLI, Ajv, NetworkNT/Jackson and oasdiff versions are captured from the accepted R19/... compatibility authority when the packet is finalized; if that authority does not contain an exact version, packet validation returns BLOCKED_DECISION rather than choosing latest.

## Versions present in I19
| Tool | I19 statement | Exact pin? |
|------|---------------|------------|
| @asyncapi/modelina | Not in I19 body; SER-58 packet pins 5.10.1 | packet-only |
| AsyncAPI CLI | "pinned @asyncapi/cli" | NO exact version |
| Ajv Draft-07 | "pinned Ajv Draft-07" | NO exact version |
| NetworkNT json-schema-validator | "3.x (Jackson 3 line), exact reviewed patch pinned at bootstrap" | NO exact patch |
| Jackson 3 | "Jackson 3 line" | NO exact version |
| oasdiff | "pinned oasdiff 1.x" | NO exact version |

## Namespace contradiction (first-party)
| Authority | First-party alias / URN |
|-----------|-------------------------|
| Mission / SER-58 packet | `@sergiotr/contracts/*`, `io.sergiotr.financialplatform.contract.generated...` |
| I19 LOCKED (I19-03, I19-10) | `urn:gafi:schema:...`, `@gafi/contracts/*` |

Choosing either alias without adjudication would invent or override an accepted authority.

## Execution decision
Per packet failure condition and non-invention rule: **do not select floating/latest versions**, **do not silently substitute**, **do not build a custom generator**.

SER58_RESULT=BLOCKED_DECISION

## Cases recorded NOT_RUN (not PASS)
- Draft-07 schema authorship
- local refs resolution
- Modelina generation Java/TS
- byte-for-byte double generation
- Java compile JDK25
- TypeScript compile
- NetworkNT/Jackson runtime validation
- Ajv Draft-07 runtime validation
- good/bad/unknown/enum/decimal fixtures
- no partial state mutation proof
- compatibility/breaking matrix
- oasdiff / AsyncAPI diff CI consumption

## Toolchain available (informational only; unused for version selection)
- Node 24.21.0
- pnpm 12.7.0
- Temurin JDK 25.0.4+7 at /tmp/gafi-spikes/toolchains/jdk-25.0.4+7
- Docker: unavailable (packet preferred container digests therefore also NOT_RUN)
