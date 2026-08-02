# Prompt 2026-08-02

Check the Visual Glossary against the provided schema. Use the provided skill. Mark where the Visual Glossary might need to be adapted.

![Visual Glossary](../assets/VisualGlossaryGrandma.jpg)
[Help Schema](../code/Help.yaml)

# Answer

I'll run the consistency check with the skill — parsing the schema first, then diffing it against the glossary.No mechanical defects beyond one minor `ORPHAN` finding — the file parses clean, so the disagreements below are genuine domain disagreements rather than the schema contradicting itself. Here's the transcription I diffed against, then the report.**Verdict: aligned with drift**, tilting toward diverged on the answer side. 16 findings — 3 blocking, 8 significant, 5 minor. Eleven of them resolve as *the glossary moves*, which matches the usual prior: the schema is newer and carries decisions nobody went back to draw.

The three the picture most needs adapting for:

- **The glossary subtypes only half the model.** It draws three `is-a` edges into `Help Request`, then models the three answer variants as `Help —contains→ 0..*`. The schema has a full mirror family (`HelpIngredients` / `HelpStep` / `HelpCatastrophe`, discriminated). Composition and specialization are different claims — one says a Help could carry substitutes *and* mitigation steps, the other says it's exactly one kind. This is invisible from either side alone.
- **"Help" means two things in the picture.** `Help with Ingredients` and `Help for Meal Preparation Step` hang off **Help Request**, not Help. Read it cold and you attach them to the wrong parent. The third sibling, `Meal Preparation Catastrophe`, doesn't say "help" at all, so the family is already inconsistent internally.
- **`Ingredient` exists only as a role.** The glossary names `Ingredient Substitute` but never the base noun — while the schema uses one `Ingredient` type in three roles (the shortage, the substitutes, the rescue addition) with a description that still names only the first.

Two blocking findings run the other way, where the schema is what should move: `recipeStepNumber` is a positional reference (`"2."`) into a recipe that can be edited underneath it, and `MitigationStep.ingredient` is required — so "turn the oven down" is unrepresentable.

Worth knowing: the file is **mechanically clean**. No broken `$ref`, no `required:` parsed as null, no example failing its own pattern, dots escaped in both patterns. That's unusual, and it means everything above is a real domain disagreement rather than the schema contradicting itself.

Two things would sharpen this if you can answer them: **is the schema shipped**, and **does the picture have a context legend** I couldn't see? The single sticky colour left me guessing whether `Meal` and `Recipe` are a neighbouring context — that answer changes F8 and F13 from findings into allowlist entries.

[Conformance report](./conformance-report-opus5.md)