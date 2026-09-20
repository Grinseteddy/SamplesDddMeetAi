ADR0001

# Using Monolith

### Adopted 2026-09-10

### Annegret Junker, Architect

## Decision
**We will implement Larder as Monolith.**

## Context

We defined Bounded Contexts of the Larder application.
Those Bounded Contexts are modules in our application.

## Options considered

### Modular monolith

We define Larder as modular monolith with the corresponding modules:
- Recipe Catalog
- Meal Planning
- Cook Profile (as ACL to the external used IAM)
- Grandma Avatar AI (as ACL to the external used AI)
- Notification
- Cooking Assistance
- Media (as specific implementation to an external used bucket)
- Consent Management (as ACL to an external used Consent Management)
- Meal Preparation
- Sharing

The modules use one database instance with decoupled schemas.
Each module gets its own database user who can only access the belonging schema.

Advantages: Easy to maintain and to operate (see [AP0001](./LarderArchitecturalPrinciples.md/#ap0001monolith-before-microservices) )
Disadvantages: Requires discipline for the teams to respect the boundaries (see [AP0001](./LarderArchitecturalPrinciples.md/#ap0001monolith-before-microservices))

### Microservices

We will use full-fledged microservices.
For each Bounded Context, a microservice is established.

Advantage: The teams can develop and maintain the services completely independent of each other (contradicts (see [AP0001](./LarderArchitecturalPrinciples.md/#ap0001monolith-before-microservices))).

### Monolith

We implement Larder as monolith.

Advantage: Easy to maintain and to generate (contradicts (see [AP0001](./LarderArchitecturalPrinciples.md/#ap0001monolith-before-microservices))).
Disadvantage: Difficult to enhance in future


## Consequences

| Consequence    | Modular Monolith | Microservices   | Monolith        |
|----------------|------------------|-----------------|-----------------|
| Implementation | ⚠️️Higher effort | ‼️High effort   | ☑️ Small effort |
| Maintenance    | ⚠️️Higher effort | ☑️ Small effort | ‼️High effort   |
| Operation      | ☑️ Small effort  | ‼️High effort   | ☑️ Small effort |
| Enhancements   | ☑️ Small effort  | ☑️ Small effort | ‼️High effort   |

## Advice

See meeting protocol
