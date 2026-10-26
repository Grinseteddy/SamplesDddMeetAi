# ADR-CA-001 — TypeScript, NestJS and Vitest for Cook Assistance

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 1

## Context
Cook Assistance is implemented with EXACT Coding (Predictive TDD), which ships stack profiles for
TypeScript + Vitest and Java + JUnit 5 + Maven. The Larder modular monolith of Chapter 13 is Java 21 +
Spring Boot + Maven (ADR0001 "Using Monolith", principle AP0001 "Monolith before Microservices"). The
Chapter 15 EXACT sample of the same context is TypeScript + Vitest.

## Decision
Cook Assistance is written in **TypeScript** (strict) on **Node.js 22 LTS**, with **NestJS** as the
application framework, **Vitest** as the test runner (EXACT TypeScript profile) and **npm** as the build
tool.

## Consequences
- The EXACT TypeScript + Vitest profile applies unchanged; the Chapter 15 sample's conventions carry over.
- **Overrides ADR0001 and principle AP0001 (stated, not implied):** Cook Assistance cannot be a module
  of the Java Larder monolith; it is a **separate deployable**. Whether this override is accepted for the
  whole of Larder or only as an experiment for this context is decided in question 2 and recorded in
  the decision graph as an acknowledged override.
- QS-01 (module rules) is checked with dependency-cruiser instead of ArchUnit; QS-02 (build ≤ 10 min)
  is unaffected.
- Code generation (question 11) must use TypeScript tooling (no Java server generators).
- Principles honored: AP0003 (DDD), AP0005 Automatic Testing.

## Quality scenarios
Serves QS-01, QS-02 (maintainability, rank 1); trades off the deployment simplicity of ADR0001.
