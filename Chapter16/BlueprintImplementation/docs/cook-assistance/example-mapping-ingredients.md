# Example Mapping: Help with an ingredient (Cook Assistance)

EXACT Coding `example-mapping` session, 1:1 interview with the domain expert, 2026-10-24.
Seeded with board B3 (Ingredients) and US-CA-02. The expert confirmed that rules 3, 4, 6, 8, 9 and
10 of `example-mapping-preparation-step.md` apply unchanged (with an ingredient instead of a step).

## Story (yellow)

As a Cook, I want to ask the community and/or Grandma Avatar for help with one ingredient of a recipe,
so that I can cook even without that ingredient.

## Rules (blue)

### Rule 1: A request names one recipe and exactly one ingredient of it
### Rule 2: The ingredient must exist in the recipe (Recipe Catalog)
### Rule 3: The cook addresses the community and/or Grandma Avatar — at least one, never a Chef (as Preparation step R3)
### Rule 4: Only an addressee may answer (as R4)
### Rule 5: An answer is for the same recipe and the same ingredient as the request
### Rule 6: An answer contains 1 to 3 substitutes
### Rule 7: The system cancels a REQUESTED request 24 h after it was requested, or 24 h after the last rejection (as R7)
### Rule 8: The cook's question is required (as R8)
### Rule 9: When the cook accepts a help, the request is closed (as R9)
### Rule 10: The cook can reject a help; it becomes rejected and the 24 h clock restarts (as R10)

## Examples (green)

### For Rule 1
- Cook asks for a substitute for "oil" in "Sunday scones", addressed to the community and Grandma Avatar -> request created

### For Rule 2
- Cook asks about an ingredient that is not in "Sunday scones" -> rejected

### For Rule 6
- In "Sunday scones" the wheat flour is substituted by a melange of rye flour and wheat pastry flour (2 substitutes) -> help recorded
- An answer with 0 substitutes -> rejected, not recorded as help
- An answer with 4 substitutes -> rejected, not recorded as help

### For Rule 7 (the expert: "the timeout rules are the same as before")
- Derived from the Preparation step examples with 24 h: requested 10:00, no answer -> cancelled at 10:00 the next day

### For Rules 3, 4, 5, 8, 9, 10
- As in the Preparation step map, with an ingredient instead of a step.

## Questions (red)

No open questions - all uncertainties were resolved.

## New Stories (sliced out)

- A community cook answers an ingredient request
