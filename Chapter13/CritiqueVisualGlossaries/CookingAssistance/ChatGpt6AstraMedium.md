# Prompt 2026-10-04

/devils-advocate
Critique the Visual Glossaries of Help and Help Request in the Bounded Context Cooking Assistance.

![](../../assets/HelpRequest.jpg)
![](../../assets/Help.jpg)

# Answer

**My strongest objection: the glossaries look precise, but several rules still allow incompatible interpretations.** Two teams could implement these diagrams faithfully and produce different behavior.

I reviewed both **Help Request** and **Help**. These are challenges to the model as shown; some may already be resolved elsewhere.

1. **The request permits missing information that its answer requires.**

   In *Help Request*, `howToStep` is `0..1`, with “Only for type Preparation Step Explanation.” That prohibits it for other types but does not clearly require it for this type. Yet the corresponding answer requires exactly one step, which must match the request. Similarly, an ingredient-substitution request permits zero ingredients, while its answer requires one or more substitutes.

   **Why this matters:** a valid request can become impossible to answer without inventing missing information.

   **What would resolve it:** explicit conditional cardinalities: a preparation-step request requires exactly one step; a substitution request requires at least one ingredient. Also specify that the step and ingredients belong to the referenced recipe.

2. **Provider preference, eligibility, and exclusivity are mixed together.**

   `preferred Provider` permits one or two choices. The Chef annotations say both “only for menu proposal” and “provided exclusively.” Does that mean:

    - Chef answers are restricted to menu proposals?
    - Menu proposals can receive only Chef answers?
    - Choosing Chef excludes all other providers?

   These are different rules. A request selecting both Chef and Community exposes the ambiguity immediately.

   In *Help*, provider type is mandatory but provider identity is optional. The rule allowing cooks to answer their own requests, except Chef requests, needs an identifiable respondent to be enforceable.

   **What would resolve it:** separate preferred provider types, eligible provider types, and the actual responder. State precisely when identity is required and what “exclusively” excludes.

3. **The answer variants need one unmistakable exclusivity rule.**

   *Help* requires one `answer`, but each of its four branches is labeled `oneOf – 0..1`. This appears intended to mean exactly one variant overall, but the notation needs to say that explicitly. Answer type also appears both on the answer and within each variant.

   **Why this matters:** without a clear rule, the structure can be read as allowing no payload, multiple payloads, or a payload whose type disagrees with its parent.

   **What would resolve it:** “Exactly one answer variant must exist; its type must match the request type.” Consider deriving the answer type from the variant instead of recording it repeatedly.

4. **“Answered” describes a response arriving, not necessarily help succeeding.**

   The request has `Open` and `Answered`, with “At least one Help must answer it.” A useless or incomplete response could therefore make a request answered. The diagram also does not specify whether the first Help automatically changes the status.

   **Pressure test:** someone posts “Stay calm,” but the cook still needs a replacement dish. Is the request finished?

   **What would resolve it:** define whether `Answered` means “has a response” or “requester’s need is resolved.” Add further states only if the business needs that distinction. Clarify what happens if the only answer is withdrawn.

5. **Separate aggregates rely on shared facts whose stability is unspecified.**

   *Help* and *Help Request* are separate aggregates, but answer type, recipe, step, and ingredient must match the request. Help also repeats the requester reference.

   **Pressure test:** a cook edits the request’s recipe or type after an answer arrives. Is the existing answer now invalid?

   **What would resolve it:** decide whether relevant request fields become immutable, answers target a request revision, or edits require another explicit policy. Define whether the repeated requester is a historical snapshot or a value that must always remain synchronized.

6. **Some vocabulary hides consequential domain choices.**

   | Visible modeling choice | Challenge |
      |---|---|
   | `meal` means breakfast/lunch/dinner and also something referring to a recipe | Are these really one concept? `Meal occasion` and `Dish` would distinguish them. |
   | Course `step` permits equal values for parallel serving | Is this a serving order or a preparation step? |
   | One substitute ingredient per substitute | Can replacing one ingredient require a combination of ingredients? How are alternatives distinguished from combinations? |
   | Unit includes Cup, Piece, and Pinch | What makes quantities interpretable—measurement convention, ingredient identity, piece size? |
   | Menu has `1..10` courses | Is ten a domain limit, a product policy, or an arbitrary implementation limit? |

   These deserve short definitions and contrasting examples. UUID examples establish reference shape, but do little to explain meaning.

**I would test the revised glossaries against three cases:** a substitution request with no ingredient selected; a menu request preferring both Chef and Community; and a request edited after receiving an answer. If domain experts can independently agree on what is permitted and what happens next, my main concerns would be substantially reduced.

# Have you not seen the oneOf with the discriminator answerType?

Yes—you’re right. I saw the oneOf branches and answerType, but my critique failed to credit their combined meaning: exactly one answer variant, selected by answerType. My suggestion that the model permits zero or multiple variants was misplaced.
Repeating answerType within each variant also makes sense as a discriminator with a fixed value per variant; it isn’t inherently problematic duplication.
I withdraw point 3. The separate issue in point 1 remains: whether a request requires the step or ingredients that its selected answer variant needs.