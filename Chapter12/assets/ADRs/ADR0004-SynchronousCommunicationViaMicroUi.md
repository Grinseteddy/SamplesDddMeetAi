ADR0004

# Synchronous Communication per UI

### Adopted 2026-09-10

### Annegret Junker, Architect

## Decision
**We will use synchronous communication for User Interfaces.**

## Context

Most of the Bounded Contexts need user interactions that should be done synchronously via REST API.

## Options considered

### Synchronous via REST API

We will use REST APIs for synchronous communication with UI.

Advantages: It is supported by principles (see [AP0006](./LarderArchitecturalPrinciples.md/#ap0006-synchronous-communication-for-uis))
Advantages: Skills are available to implement REST APIs.

### Synchronous via GraphQL

We will use GraphQL for user interfaces.

Advantages: It is supported by principles (see [AP0006](./LarderArchitecturalPrinciples.md/#ap0006-synchronous-communication-for-uis))
Disadvantages: It contradicts a fine-grained control of access rights (see [AP0008](./LarderArchitecturalPrinciples.md/#ap0008-fine-grained-access-rights)).

### Using of asynchronous communication with WebHooks

We use asynchronous communication via WebHooks for UI.

Disadvantage: It contradicts [AP0006](./LarderArchitecturalPrinciples.md/#ap0006-synchronous-communication-for-uis).
Disadvantage: It requires high efforts on server and client side.


## Consequences

| Consequence    | Synchronous REST | Synchronous GraphQL | Asynchronous WebHooks |
|----------------|------------------|---------------------|-----------------------|
| Implementation | ☑️ Small effort  | ☑️ Small effort     | ‼️ High effort        |
| Security       | ☑️ Fine grained  | ‼️ Coarse grained   | ☑️ Fine grained       |

## Advice

See meeting protocol
