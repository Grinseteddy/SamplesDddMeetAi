# Prompt 2026-10-04

/devils-advocate

Critique the Visual Glossary of the Cook Profile

![](../../../Chapter12/assets/ApiProductCanvas/CookProfileCookVisualGlossary.jpg)

# Answer

I recovered and read the image. **My strongest objection is that `status` appears to mix two independent concepts: account activity and membership tier.** Can a cook be both active and premium? Can a premium cook become inactive while retaining their subscription? If so, the current single-status model cannot represent those cases.

Here are the challenges I would raise, in priority order:

| What the glossary shows | Why I would challenge it | What would resolve the concern |
|---|---|---|
| Exactly one `status`: `active`, `inActive`, or `premium` | This appears to force activity and membership into mutually exclusive choices. | Test the combinations with domain experts. If independent, separate `AccountStatus` and `MembershipTier`. |
| A `Cook` aggregate containing identity, contact, membership date, and status | These fields could describe almost any account. What makes this a **Cook**, and what belongs specifically to **Cook Profile**? | Define Cook in one sentence and clarify which facts this context owns versus receives from account or membership management. |
| `name` → “Joe”; `givenName` → “Doe” | The examples suggest a naming mismatch. Is `name` a display name, full name, or family name? | Use explicit terms and consistent examples, such as `givenName: Joe`, `familyName: Doe`, or a single `displayName` if that meets the need. |
| Every property has cardinality `1` | This asserts that no Cook can exist without every field. Does that hold during registration, imports, or incomplete profile setup? | Walk through profile creation and identify when each field becomes mandatory. |
| An invariant legend, but no actual invariants | Calling Cook an aggregate leaves its consistency rules unexplained. What must always hold together? | Add confirmed business rules and their scope. For example, whether `memberSince` can change, or what permits activation. Do not invent rules merely to fill the diagram. |
| `memberSince` → a date | “Member” of what, and since which event: registration, verification, or paid membership? What happens after rejoining? | Name the triggering event and decide whether a date or timestamp is required. |

Two notation details also weaken the shared vocabulary:

- `active`, `inActive`, and `premium` use the same value-object color as `status`. That makes allowed values look like separate modeled concepts. Label them explicitly as allowed values, and clarify whether the list is exhaustive.
- The email example is literally “email.” A realistic example such as `joe@example.com` would help; a short definition should also distinguish contact email from login email, if relevant.

**The failure scenario I would test first:** Joe becomes premium, deactivates his account, and later returns. Ask the team to represent every step using this glossary without losing information. If they cannot, the status model needs revision.

I would address status semantics, context ownership, and name definitions before adding more fields. Those changes would do the most to prevent different readers from building different systems from the same diagram.