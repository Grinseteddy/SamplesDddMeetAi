# Example Mapping: Help in a catastrophe (Cook Assistance)

EXACT Coding `example-mapping` session, 1:1 interview with the domain expert, 2026-10-24.
Seeded with board B4 (Catastrophe) and US-CA-04. The expert confirmed that rules 3–6 and 8–10 of
`example-mapping-preparation-step.md` apply (timeout 10 min), and decided that a step is **optional**
for a catastrophe (the Help Request model wins over the board sticky "Recipe and step in recipe must
be provided").

## Story (yellow)

As a Cook, I want to ask the community and/or Grandma Avatar for urgent help when something goes
wrong at one step of a recipe, so that I can still save my meal.

## Rules (blue)

### Rule 1: A request names one recipe; a step is optional — a named step must exist in the recipe
### Rule 2: The cook's question is required; the request may carry 0 to 10 pictures
Pictures are uploaded in the Media bounded context; Cook Assistance only knows their URLs.
### Rule 3: Addressed to the community and/or Grandma Avatar — at least one, never a Chef; only addressees may answer
### Rule 4: An answer is free text for the same recipe (and the same step, if the request named one)
### Rule 5: A cancelled or closed request cannot be answered
### Rule 6: The system cancels a REQUESTED request 10 min after it was requested, or 10 min after the last rejection
### Rule 7: Accepting a help closes the request; rejecting it marks the help rejected and restarts the 10 min clock

## Examples (green)

### For Rule 1
- "Sunday scones" are burned; the cook asks without naming a step -> request created

### For Rule 2
- The cook asks in free text and adds pictures of the burned scones -> request created
- Only a question, no pictures -> request created
- 10 pictures -> accepted
- 11 pictures -> rejected

### For Rule 4
- Grandma Avatar answers in time: cut out the burned parts, butter the scones and serve them with orange marmalade -> help recorded

### For Rule 6 (the expert: "the timeout rules are the same as before")
- Derived with 10 min: requested 10:00, no answer -> still open at 10:09, cancelled at 10:10

### For Rules 3, 5, 7
- As in the Preparation step map.

## Questions (red)

No open questions - all uncertainties were resolved.

## New Stories (sliced out)

- Pictures from Media (enabler E-01): uploading pictures and resolving their URLs belongs to the Media context
- A community cook answers a catastrophe request
