# Chapter 15 — From Example Mapping to Exact Coding

**Blueprint step:** Step 10 — Define test cases

## Context

Chapter 15 introduces the **EXACT** workflow (Example-Guided, AI-Collaborative & Test-Driven), co-authored by Ferdinand Ade and Marco Emrich of codecentric AG.
It shows how Example Mapping generates structured test cases that are then turned into executable tests through a disciplined cycle:

1. Write a failing test derived from an example
2. Prompt the AI with the failing test and the domain model
3. Review the AI-generated implementation
4. Make the human decision: accept, reject, or refine
5. Refactor

The examples below are from the Recipe publication and revision scenarios, including the co-authorship edge case that revealed a model gap during Example Mapping: *what happens when a Cook tries to revise a Recipe they did not author?*

## Samples

```
src/test/java/com/cookwithus/sharing/
└── RecipeSharingTest.java   — JUnit 5 test class covering publication and revision scenarios
```

## Running the Tests

The tests depend on the domain model from Chapter 13.
With Maven:
```bash
mvn test -pl chapter-15
```

## AI Prompt Used (EXACT workflow — step 2)

> I have a failing test: [paste failing test method].
> The domain model is: [paste Recipe.java and Ingredient.java].
> Implement only what is needed to make this test pass.
> Do not add behaviour that is not required by the test.
> Explain each decision you make.
