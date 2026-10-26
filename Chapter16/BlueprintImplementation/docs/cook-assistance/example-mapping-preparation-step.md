# Example Mapping: Help for a preparation step (Cook Assistance)

EXACT Coding `example-mapping` session, 1:1 interview with the domain expert, 2026-10-24.
Seeded with board B2 (Preparation step) and US-CA-03; every rule and example below was stated or
confirmed by the domain expert.

## Story (yellow)

As a Cook, I want to ask the community and/or Grandma Avatar for help with one preparation step of a
recipe, and receive Grandma Avatar's answer, so that I get help in preparing my meal.

## Rules (blue)

### Rule 1: A request names one recipe and exactly one step of it
Missing recipe or step → the request is rejected.

### Rule 2: The step must exist in the recipe
Cook Assistance asks Recipe Catalog for the recipe's steps.

### Rule 3: The cook addresses the community and/or Grandma Avatar — at least one, never a Chef
Chefs are only allowed for Meal plan.

### Rule 4: Only an addressee may answer
An answer from a provider the cook did not address is refused; nothing is recorded.

### Rule 5: An answer is for the same recipe and the same step as the request
Another step means another request (one step per request).

### Rule 6: A cancelled or closed request cannot be answered
The answer is refused; nothing is recorded.

### Rule 7: The system cancels a REQUESTED request 5 h after it was requested, or 5 h after the last rejection
A pending answer (neither accepted nor rejected) does not protect the request.

### Rule 8: The cook's question is required

### Rule 9: When the cook accepts a help, the request is closed

### Rule 10: The cook can reject a help
The help becomes rejected and cannot be accepted later; the request's 5 h clock restarts at the rejection.

## Examples (green)

### For Rule 1
- Cook asks about "Sunday scones", step 3 "Let dough rest in a warm place", question "What means 'warm'?" -> request created
- Cook asks about "Sunday scones" without a step -> rejected: name the step; no request created

### For Rule 2
- "Sunday scones" has 8 steps; the cook asks about step 9 -> rejected; no request created

### For Rule 3
- Sunday scones request addressed to the community and Grandma Avatar -> request created
- Cook selects Chef A -> refused: Chefs are only allowed for Meal plan
- Cook selects no addressee -> rejected

### For Rule 4
- Request addressed to Grandma Avatar only; a community cook answers -> refused, nothing recorded

### For Rule 5
- Grandma Avatar answers about Sunday scones step 3 -> help recorded for the request
- An answer about another step -> refused (another step needs another request)

### For Rule 6
- An answer to a cancelled or a closed request -> refused, nothing recorded

### For Rule 7
- Requested 10:00, no answer -> still open at 14:59; cancelled by the system at 15:00
- Requested 10:00, Grandma Avatar answers 12:00, cook neither accepts nor rejects -> cancelled at 15:00
- Requested 10:00, Grandma Avatar answers 12:00, cook rejects 13:00, no further answer -> cancelled at 18:00

### For Rule 8
- Request without a question -> rejected

### For Rule 9
- Grandma Avatar's answer about Sunday scones step 3 is accepted by the cook -> request closed

### For Rule 10
- Cook rejects the 12:00 answer at 13:00 -> help rejected (cannot be accepted later); the request's deadline moves to 18:00

### Without a rule card
- Several open requests of one cook for the same recipe and step are allowed.

## Questions (red)

No open questions - all uncertainties were resolved.

## New Stories (sliced out)

- A community cook answers a preparation-step request
- Help with ingredients (separate session)
- Help in a catastrophe (separate session)
- Help Meal plan — already mapped in US-CA-01 (`Chapter15/EXACTCodingSample/src/help-meal-plan-example-mapping.md`)
