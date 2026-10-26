# Cook Assistance — rules collected before asking (step 3a)

2026-10-24. Sources: the domain knowledge graph (`dkg:Rule` in context Cook Assistance) and the glossary
relationships with cardinalities (Visual Glossary v1/v2, Help model, Help Request model v1/v2).
Nothing here is merged yet — that happens in 3d.

## A. Rules in the graph (28)

| # | Rule (verbatim) | Source | Note |
|---|---|---|---|
| C01 | Chef can review a dinner plan of Grandma Avartar. | board B1 | → B1-S02 |
| C02 | Chef needs to be selected | board B1 | |
| C03 | Chef can only be asked when cook is premium user. | board B1 | same meaning as C04 |
| C04 | Only premium users allowed to ask Chef | board B1 | |
| C05 | Chef is not allowed - only community or Grandma | boards B2, B3, B4 | |
| C06 | Help Meal plan can be provided by Chef, community, or Grandma | boards B1–B4 | Menu only (F4) |
| C07 | Cook can accept the answer | boards B1–B4 | → B1-S05 |
| C08 | Cook can cancel their request when answer takes too long | boards B1–B4 | → B1-S06 |
| C09 | System cancels request when answer takes too long | boards B1–B4 | → B1-S07, F6 thresholds |
| C10 | Cook cannot answer their own request | boards B1–B4 | **contradicts C23** |
| C11 | Help is provided for one ingredient in one recipe | board B3 | |
| C12 | Help is provided for one step in one recipe | boards B2, B4 | |
| C13 | Recipe and ingredient in recipe must be provided | board B3 | |
| C14 | Recipe and step in recipe must be provided | boards B2, B4 | **B4 part contradicts HR model** ("howTo Step only for Preparation Step Explanation") |
| C15 | Only requester is allowed to (PATCH, DELETE /help-requests/{id}) | API canvas (sync) | |
| C16 | Only in status open (DELETE /help-requests/{id}) | API canvas (sync) | |
| C17 | If help provider is Chef only original requester can see the help | API canvas (sync) | = C19 |
| C18 | Only provider is allowed to (PATCH, DELETE /helps/{id}) | API canvas (sync) | |
| C19 | Chef help is visible only to the original requester | earlier OpenAPI 1.0.0, Help model | = C17 |
| C20 | A help request can be deleted only by its requester and only while OPEN | earlier OpenAPI 1.0.0 | = C15 + C16 |
| C21 | Answered help requests can be still answered | Help Request model v2 | matches step 2 decision |
| C22 | At least one Help must be accepted | Help Request model v2 | to close (B1-S08)? |
| C23 | A cook can answer their own request - except for Chef requests | Help model | **contradicts C10** |
| C24 | Must be the same as request type | Help model | Help type = request discriminator |
| C25 | Same howTo as in request | Help model | ≈ C12 |
| C26 | Same ingredientId as in request | Help model | ≈ C11 |
| C27 | Same recipe as in request | Help model | ≈ C11, C12 |
| C28 | different courses can have the same step when served in parallel | Help model | Meal Preparation concern? |

## B. Glossary cardinalities — candidate invariants (drawn)

| # | Relationship | Cardinality | Source | Kind |
|---|---|---|---|---|
| G01 | Help *for* Help Request | 1 | Glossary v1, v2 | cardinality |
| G02 | Help Request *contains* preferred Provider | 1..2 | HR model v1, v2 | cardinality — **contradicts G03** |
| G03 | Help Request *at* Community **and** *at* Grandma Avatar | 1 each | Glossary v1, v2 | cardinality |
| G04 | Help Request *belongs* Meal | 1 | Glossary v1, v2 | cardinality |
| G05 | Help Request *contains* Picture | 0..10 (glossary v2) / 0..* (glossary v1, HR model v2) | | cardinality — **contradiction** |
| G06 | Help Request *contains* howTo Step | 0..1 | HR model | cardinality |
| G07 | Help Request *contains* recipe | 0..1 | HR model | cardinality |
| G08 | Help Request *contains* ingredients | 0..* | HR model | cardinality — vs C11 "one ingredient" |
| G09 | Help *contains* Menu proposal | 1..3 | Glossary v2 | cardinality (Menu) |
| G10 | Help *contains* Ingredient Substitute | 1..3 (v2) / 0..* (v1) | Glossary | cardinality (Ingredients) |
| G11 | Help *contains* Preparation Step Explanation | 1 (v2) / 0..* (v1) | Glossary | cardinality (Preparation step) |
| G12 | Help *contains* Steps to Mitigate Catastrophe | 1..10 (v2) / 0..* (v1) | Glossary | cardinality (Catastrophe) |
| G13 | Help *contains* Picture | 0..10 (v2) / 0..* (v1) | Glossary | cardinality |
| G14 | Help *contains* help provider | 0..1, help provider type 1 | Help model | cardinality |

Derived (not drawn): none taken.
