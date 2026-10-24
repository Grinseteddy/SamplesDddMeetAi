# Cook Assistance — invariants sheet (step 3d)

2026-10-24. Merged from `04a-rules-collected.md` (graph, glossary), `04b-invariants-from-boards.md`
(boards B1–B4), the Example Mappings `example-mapping-preparation-step.md`,
`example-mapping-ingredients.md`, `example-mapping-catastrophe.md` (2026-10-24) and US-CA-01 Help Meal
plan (`Chapter15/EXACTCodingSample/src/help-meal-plan-example-mapping.md`, 2026-10-21).

Source codes: **G** graph rule · **GL** glossary cardinality · **BD** board sticky · **EM-P / EM-I /
EM-K / EM-M** Example Mapping Preparation step / Ingredients / Catastrophe / Menu (US-CA-01) ·
**D** user decision in steps 1–2. Kind: *quantitative* · *cardinality* · *other* — quantitative and
cardinality rules get a property test in step 6. Every rule is anchored to a slice; none is "not anchored".

Aggregates: **HR** = Help request, **H** = Help.

## Group B1 — Meal plan board (incl. the merged slices B1-S03…S10, which all four topics use)

### Request rules (Menu, B1-S01, B1-S02)

| ID | Rule | Kind | Agg · command · slice | Rejection | Sources |
|---|---|---|---|---|---|
| INV-01 | A Chef can only be asked for meal planning (any meal) | other | HR · Request help meal plan · B1-S01 | "Chefs answer meal-planning requests only." | BD C05/C06, D F4, EM-M R1 |
| INV-02 | Only a premium cook can ask a Chef | other | HR · Request help meal plan · B1-S01 | no request; cook asked to upgrade | BD C03/C04, EM-M R2 — **cross-context: premium owned by Cook Profile (policy, see §5)** |
| INV-03 | A cook selects exactly one Chef by name, and only an available one (same city, free before the planned meal's date) | cardinality | HR · Request help meal plan · B1-S01 | "Chef not local" / "Chef not timely available" / "no Chef available" | BD C02, EM-M R3 — **cross-context: Chef location and calendar (see §5)** |
| INV-04 | A Chef review can be requested only for a Grandma Avatar answer to one of the cook's own requests | other | HR · Request Chef review · B1-S02 | "Not your request." | BD C01, EM-M R4 |

Scenarios (EM-M): premium cook asks Chef for lunch planning → created · asks Chef for substitutions →
rejected · non-premium cook → no request, upgrade · Hamburg cook, dinner 30 Oct, Chef Anna (Hamburg,
free 28 Oct) → created · Chef Ben (Munich) → rejected · Chef Clara (free only 2 Nov) → rejected · no
Chef local and free → rejected · review own Grandma Avatar answer → created · review another cook's →
rejected.

### Lifecycle and answer rules (all topics; merged slices)

| ID | Rule | Kind | Agg · command · slice | Rejection | Sources |
|---|---|---|---|---|---|
| INV-05 | Cancel, close, restart deadline and Chef review are accepted only while the request is REQUESTED; CLOSED and CANCELLED are terminal | other | HR · all non-creating commands · B1-S02, S06, S07, S08, S10 | "already cancelled / closed" | BD state sticky, D E2, EM-M R5 |
| INV-06 | Only the requester may cancel their request, at any time while it is REQUESTED | other | HR · Cancel help request · B1-S06 | "Only the cook who asked can cancel." | BD C08 (clarified by EM-M R5: no waiting time), G C15/C20 |
| INV-07 | The system cancels a REQUESTED request when its answer deadline passes: deadline = requested-at — or the last rejection — + timeout (Preparation step 5 h, Ingredients 24 h, Catastrophe 10 min, Menu 48 h); a pending answer does not protect it | **quantitative** | HR · Cancel help request (time) · B1-S07 | — (no-op before the deadline) | BD C09, D F6, EM-P R7, EM-I R7, EM-K R6, EM-M R6 |
| INV-08 | A rejection restarts the request's answer deadline | **quantitative** | HR · Restart answer deadline (policy on Help rejected) · B1-S10 | — | EM-P R10 |
| INV-09 | Only an addressee may answer; a Chef only a Menu request that selected this Chef | other | H · Provide Help · B1-S03, B1-S04 | refused, nothing recorded | BD C05/C06, D F4, EM-P R4, EM-I R4, EM-K R3, SADR0001 |
| INV-10 | An answer is for the same recipe and the same step (Preparation step; Catastrophe if a step was named) or the same ingredient (Ingredients) | other | H · Provide Help · B1-S03, B1-S04 | "This help is for a different step / ingredient." | BD C11/C12, G C25–C27, EM-P R5, EM-I R5, EM-K R4 |
| INV-11 | A cancelled or closed request cannot be answered | other | H · Provide Help · B1-S03, B1-S04 | refused, nothing recorded | EM-P R6 — **cross-aggregate (HR status): checked by the application service (advisory, §5)** |
| INV-12 | An Ingredients answer contains 1 to 3 substitutes | **cardinality** | H · Provide Help · B1-S03, B1-S04 (Ingredients variant) | rejected, not recorded as help | GL G10 (v2), EM-I R6 |
| INV-13 | Only the requester may accept a help, only while it is PROVIDED; ACCEPTED is terminal | other | H · Accept help · B1-S05 | "Only the cook who asked can accept." / "already decided" | BD C07, EM-P R9 |
| INV-14 | Accepting a help closes its request | other | HR · Close help request (policy on Help accepted) · B1-S08 | — | D E2, G C22, EM-P R9 |
| INV-15 | Only the requester may reject a help, only while it is PROVIDED; REJECTED is terminal (cannot be accepted later) | other | H · Reject help · B1-S09 | "already decided" | EM-P R10 |
| INV-16 | A help answers exactly one help request | **cardinality** | H · Provide Help · B1-S03, B1-S04 | — (structural) | GL G01 |

Scenarios (EM-P unless noted): Grandma Avatar answers Sunday scones step 3 → help recorded · answer
about another step → refused · request addressed to Grandma Avatar only, a community cook answers →
refused · answer to a cancelled or closed request → refused · wheat flour → rye flour + wheat pastry
flour (2) → recorded; 0 or 4 substitutes → rejected (EM-I) · accept → request closed · requested 10:00,
no answer → open 14:59, cancelled 15:00 · answered 12:00, no decision → cancelled 15:00 · rejected
13:00 → cancelled 18:00 · Ingredients: 10:00 → cancelled 10:00 next day (derived, EM-I) · Catastrophe:
10:00 → open 10:09, cancelled 10:10 (derived, EM-K) · Menu Chef request 21 Oct 10:00 → open 23 Oct
09:59, cancelled 23 Oct 10:00; 22 Oct 15:30 → open 23 Oct 10:00 (EM-M) · cancel open → cancelled;
cancel cancelled → rejected (EM-M).

## Group B2 — Preparation step (B2-S01)

| ID | Rule | Kind | Agg · command · slice | Rejection | Sources |
|---|---|---|---|---|---|
| INV-17 | A request names one recipe and exactly one step of it | cardinality | HR · Request help preparation step · B2-S01 | "Name the step." | BD C14, GL G06/G07, EM-P R1 |
| INV-18 | The step exists in the recipe | other | HR · Request help preparation step · B2-S01 | rejected | EM-P R2 — **cross-context: Recipe Catalog lookup (§5)** |
| INV-19 | The cook's question is required (all four request commands) | other | HR · Request help <topic> · B2-S01, B3-S01, B4-S01, B1-S01 | rejected | EM-P R8, EM-I R8, EM-K R2 |
| INV-20 | A non-Menu request is addressed to the community and/or Grandma Avatar — at least one, never a Chef | cardinality | HR · Request help <topic> · B2-S01, B3-S01, B4-S01 | "Chefs are only allowed for Meal plan" / rejected | BD C05, GL G02/G03 (contradiction resolved by EM-P R3), EM-P R3 |

Scenarios (EM-P): Sunday scones, step 3 "Let dough rest in a warm place", "What means 'warm'?",
community + Grandma → created · no step → rejected · step 9 of 8 → rejected · no question → rejected
· Chef A → refused · no addressee → rejected · several open requests for the same step → allowed.

## Group B3 — Ingredients (B3-S01)

| ID | Rule | Kind | Agg · command · slice | Rejection | Sources |
|---|---|---|---|---|---|
| INV-21 | A request names one recipe and exactly one ingredient of it | cardinality | HR · Request help ingredients · B3-S01 | rejected | BD C13/C11, EM-I R1 (vs GL G08 "ingredients 0..*": resolved) |
| INV-22 | The ingredient exists in the recipe | other | HR · Request help ingredients · B3-S01 | rejected | EM-I R2 — **cross-context: Recipe Catalog (§5)** |

Scenarios (EM-I): substitute for "oil" in Sunday scones, community + Grandma → created · ingredient not
in Sunday scones → rejected. INV-19, INV-20 apply.

## Group B4 — Catastrophe (B4-S01)

| ID | Rule | Kind | Agg · command · slice | Rejection | Sources |
|---|---|---|---|---|---|
| INV-23 | A request names one recipe; a step is optional, and a named step must exist in the recipe | other | HR · Request help catastrophe · B4-S01 | rejected | EM-K R1 (board C14 overruled: step optional), G HR model |
| INV-24 | A request carries 0 to 10 pictures, held as Media URLs | **cardinality** | HR · Request help catastrophe · B4-S01 | 11 pictures → rejected | GL G05 (v2), EM-K R2 |

Scenarios (EM-K): burned Sunday scones, no step, question + pictures → created · question only →
created · 10 pictures → accepted · 11 → rejected · Grandma Avatar: "cut out the burned parts, butter the
scones, serve with orange marmalade" → recorded. INV-19, INV-20 apply.

## §5 Cross-aggregate and cross-context rules (policies — steps 8–9, never inside one aggregate)

| Rule | Policy / enforcement | Staleness window | Compensation |
|---|---|---|---|
| INV-02 premium | checked on a premium fact held locally at request time (token claim or replicated tier — step 5) | until the fact is refreshed | open: a cook who stops being premium with an open Chef request |
| INV-03 Chef availability | needs Chef city and calendar — owner unknown (Cook Profile? a Chef context?) — **open** | — | — |
| INV-11 request still open | application service reads HR before Provide Help (advisory) | seconds | open: help arriving for a request cancelled in the same second |
| INV-14 close on accept | whenever *Help accepted* then *Close help request* (B1-S08) | seconds | — |
| INV-08 restart deadline | whenever *Help rejected* then *Restart answer deadline* (B1-S10) | seconds | a timeout firing inside that window cancels a request the cook just rejected an answer for — open |
| INV-18, INV-22, INV-23 recipe data | synchronous lookup in Recipe Catalog at request time (new border) | recipe edits after the request | none — the request keeps the step/ingredient as asked |
| INV-24 pictures | URLs from Media (Conformist towards Media) | — | — |

## Rules without an example (derived, not agreed)

INV-16 (structural) · INV-13 refusal paths (non-requester accepts; accept twice) · INV-15 refusal paths ·
INV-09 Chef answering a Menu request that selected another Chef · INV-01–INV-04 have examples (EM-M).

## Open questions (red, attached to rules)

| # | Question | Rule | Blocks |
|---|---|---|---|
| Q3-1 | May a cook answer their own request? Boards: never; Help model: yes, except Chef requests. | (dropped until answered; was B-13) | step 6 Help, increment 1 |
| Q3-2 | Menu timeout: F6 says 48 h for Menu; US-CA-01 says 48 h for a Chef request and slices out "community 2 days", "Grandma Avatar 2 hours". Per topic or per provider? | INV-07 | increment 1 |
| Q3-3 | Do reject and deadline restart (INV-08, INV-15) apply to Menu requests too? | INV-08, INV-15 | increment 1 |
| Q3-4 | Who owns a Chef's city and calendar? | INV-03 | increment 1 |
| Q3-5 | Where does the premium fact come from, and what happens to an open Chef request after a downgrade? | INV-02 | step 5, increment 1 |
| Q3-6 | Can a help be accepted after its request was closed by accepting another help (several helps per request)? | INV-13, INV-14 | increment 1 |
| Q3-7 | Help arriving for a request cancelled in the same second — keep or discard? | INV-11 | step 8 |
| Q3-8 | Can Preparation step, Ingredients or Menu requests carry pictures too? | INV-24 | increment 1–3 |

Contradictions resolved in this step: picture limit (0..10, EM-K) · ingredients per request (exactly
one, EM-I) · Catastrophe step (optional, EM-K) · request addressees (community and/or Grandma,
EM-P R3) · cook-cancel threshold (none, EM-M R5).
