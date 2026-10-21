# Example Mapping: US-CA-01 Help Meal plan

Source board: `BookDddMeetsAi/chapters/part5/15-From-Example-Mapping-to-Exact-Coding/assets/ExampleMappingUcCa01.jpg`, clarified in an interview with the domain expert on 2026-10-21.

## Story (yellow)

As Cook, I want to request help with my Meal plan from a Chef, so that I get help in planning and preparation of meals and dinners. This is a premium feature.

## Rules (blue)

### Rule 1: A Chef can only be asked for meal planning
Meal planning for any meal (lunch, dinner, ...) is allowed. Other kinds of help, such as substitutions, are rejected.
*Board: "Chef can only be asked for dinner planning" — clarified to cover any meal.*

### Rule 2: Only a premium Cook can ask a Chef
A non-premium Cook gets no request; the Cook is asked to upgrade and must ask again after upgrading.

### Rule 3: A Cook selects exactly one Chef by name, and only an available one
A Chef is available when they are in the same city as the Cook and free before the date of the planned meal. When no Chef qualifies, the request is rejected.
*Board: "Chef can only answer when they are available" — clarified to be a selection rule.*

### Rule 4: A Chef can review a Grandma Avatar meal plan of the Cook's own earlier request
The Grandma Avatar answer must belong to one of the Cook's own requests.

### Rule 5: The Cook can cancel an open request at any time
An already cancelled request cannot be cancelled.
*Board: "Cook can cancel their request when answer takes too long" — clarified: no waiting time.*

### Rule 6: The system cancels a Chef request that is unanswered after exactly 48 hours

## Examples (green)

### For Rule 1: Meal planning only
- Premium Cook asks Chef for help planning a lunch -> request is created
- Cook asks Chef for substitutions -> rejected: not meal planning

### For Rule 2: Premium only
- Non-premium Cook asks a Chef -> no request is created; Cook is asked to upgrade

### For Rule 3: Available Chef only
- Cook in Hamburg, dinner on Oct 30, selects Chef Anna (Hamburg, free on Oct 28) -> request is created
- Cook in Hamburg selects Chef Ben (Munich) -> rejected: Chef not local
- Cook in Hamburg, dinner on Oct 30, selects Chef Clara (Hamburg, free only on Nov 2) -> rejected: Chef not timely available
- No Chef is local and free before the meal date -> request is rejected

### For Rule 4: Review of own Grandma Avatar plan
- Cook received a Grandma Avatar answer to their own request and asks Chef Anna to review it -> review request is created
- Cook asks a Chef to review another Cook's Grandma Avatar answer -> rejected

### For Rule 5: Cook cancels
- Cook cancels an open request -> request is cancelled
- Cook cancels an already cancelled request -> rejected

### For Rule 6: System timeout after 48 h
- Request on Oct 21 10:00, unanswered on Oct 23 09:59 -> request is still open
- Request on Oct 21 10:00, no answer by Oct 23 10:00 -> request is cancelled by the system
- Request on Oct 22 15:30, unanswered on Oct 23 10:00 -> request is still open (added during EXACT Coding: the deadline depends on the request time)

## Scope note

Answering a request belongs to US-CA-08 (Provide help). Decided on 2026-10-21 during EXACT Coding: US-CA-01 knows only open and cancelled requests; the board's examples about answered requests are out of scope for this story.

## Questions (red)

No open questions - all uncertainties were resolved.

## New Stories (sliced out)

- System cancels a Community help request with no answer within two days
- System cancels a Grandma Avatar help request with no answer within two hours
