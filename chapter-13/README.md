# Chapter 13 — Domain Model Co-creation with AI

**Blueprint step:** Step 11 — Define domain model

## Context

Chapter 13 shows how AI identifies Aggregate candidates, distinguishes Entities from Value Objects, and surfaces invariant candidates — with the team validating every boundary and every rule.

Starting from the bounded-context-level EventStorming output and the Visual Glossary, the AI proposed:
- **Cook** as an Entity (identity survives state changes; a Cook is not interchangeable with another Cook)
- **Ingredient** as a Value Object (defined entirely by its name, quantity, and unit; two Ingredients with the same properties are equal)
- **ShoppingItem** as a Value Object (a projection of an Ingredient at order time)
- **Recipe** as an Aggregate Root (owns the Ingredients; enforces all recipe invariants)

Three invariants were confirmed by the team:
1. A Recipe must have at least one Ingredient.
2. A Recipe's title must be non-empty and no longer than 120 characters.
3. An Ingredient's quantity must be positive.

Two further invariants proposed by AI were rejected after domain-expert review.

## Samples

```
src/main/java/com/cookwithus/domain/
├── cook/
│   ├── Cook.java          — Cook Entity (Aggregate Root of the Registration context)
│   └── CookId.java        — Cook identity Value Object
└── recipe/
    ├── Recipe.java        — Recipe Aggregate Root (Sharing context)
    ├── RecipeId.java      — Recipe identity Value Object
    ├── Ingredient.java    — Ingredient Value Object
    └── ShoppingItem.java  — ShoppingItem Value Object (used in Ordering context)
```

## AI Prompt Used

> You are a DDD expert.
> I will provide the EventStorming output and Visual Glossary for the Sharing bounded context of CookWithUs.
> Identify Aggregate candidates and distinguish Entities from Value Objects.
> For each Aggregate, list all candidate invariants.
> Present your reasoning so the team can challenge each boundary and each rule.
> Use Java 21 with records for Value Objects and classes for Entities.
