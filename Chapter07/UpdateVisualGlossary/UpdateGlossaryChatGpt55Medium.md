# Prompt 2026-08-02

Update the provided glossary based on the provided schema. Use the provided skill.

![](../assets/VisualGlossaryGrandma.jpg)
[Schema](../code/Help.yaml)

[Skill](../Skills/VisualGlossaryUpdateSkill/SKILL.md)
[Derivation rules](../Skills/VisualGlossaryUpdateSkill/references/derivation-rules.md)
[Rendering](../Skills/VisualGlossaryUpdateSkill/references/rendering.md)
[Worked example](../Skills/VisualGlossaryUpdateSkill/references/worked-example.md)
[Example](../Skills/VisualGlossaryUpdateSkill/references/example/help-glossary.yaml)

# Answer

## Changelog

The glossary was updated using the schema-to-glossary derivation rules: identifiers became relationships, arrays became cardinalities, `allOf` inheritance became `is-a`, and referenced Recipe concepts were separated by context.

Key changes include:

* Removed **Meal**, **Substitute**, **Ingredient Substitute**, and **Preparation Step Explanation** because the schema no longer models them as independent concepts.
* Reshaped the three request and response variants into subtype families under **Help Request** and **Help**.
* Added **Question**, **Help Text**, **Thank Text**, **Help Provider**, **Mitigation Step**, and the Ingredient attributes.
* Converted UUID references such as `cook`, `helpRequestIdentifier`, and `help` into business-facing relationships.
* Represented picture collections as `0..10 Picture`.
* Represented `Help.helpProviderType` as an optional `0..1 Help Provider`, with Grandma Avatar and Community as its values.
* Preserved **Cook**, **Recipe**, and **Step** as domain concepts rather than implementation fields.

The geometry checker completed with **Geometry clean**, following the supplied rendering model and layout guidance.

### What the redraw exposed

**Step Help** adds no business content beyond the base **Help** type, so its continued status as a distinct concept may need review.

**Community** is now only an enum value in the schema. The earlier `Community —contains→ Cook` business relationship cannot be derived from the current schema and should be confirmed separately. This is consistent with the diagnostic issue described in the supplied worked example.

## Deliverables

[Updated glossary — SVG](./UpdatedGlossaryChatGpt/help-glossary-updated.svg)

[Updated glossary — PNG preview](./UpdatedGlossaryChatGpt/help-glossary-updated.png)

[Updated editable model — YAML](./UpdatedGlossaryChatGpt/help-glossary-updated.yaml)

[Full changelog](./UpdatedGlossaryChatGpt/help-glossary-changelog.md)

