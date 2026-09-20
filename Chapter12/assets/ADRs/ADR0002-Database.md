ADR0002

# Using One Database Instance

### Adopted 2026-09-10

### Annegret Junker, Architect

## Decision
**We will use one database instance for Larder.**

## Context

The Bounded Contexts modules in the modular monolith need databases.

## Options considered

### One database instance with schemas per Bounded Context

The modules use one database instance with decoupled schemas.
Each module gets its own database user who can only access the belonging schema.

Advantages: Easy to maintain and to operate (see [AP0001](./LarderArchitecturalPrinciples.md/#ap0001monolith-before-microservices) )
Disadvantages: Requires discipline for the teams to respect the boundaries (see [AP0001](./LarderArchitecturalPrinciples.md/#ap0001monolith-before-microservices))

### One database instance per Bounded Contexts

We will use a database instance per Bounded Contexts.

Advantage: The teams can develop and maintain the services completely independent of each other (contradicts (see [AP0001](./LarderArchitecturalPrinciples.md/#ap0001monolith-before-microservices))).

### One database for all

We use one database for all Bounded Contexts.

Disadvantage: The Domain Models are easily (contradicts (see [AP0001](./LarderArchitecturalPrinciples.md/#ap0001monolith-before-microservices))).


## Consequences

| Consequence    | Modular Monolith   | Microservices   | Monolith        |
|----------------|--------------------|-----------------|-----------------|
| Implementation | ⚠️️More discipline | ☑️ Small effort  | ☑️ Small effort |
| Maintenance    | ⚠️️Easier          | ☑️ Small effort | ☑️ Secure       |
| Operation      | ☑️ Secure          | ‼️High effort   | ‼️Insecure      |
| Enhancements   | ☑️ Small effort    | ☑️ Small effort | ‼️Insecure      |

## Advice

See meeting protocol
