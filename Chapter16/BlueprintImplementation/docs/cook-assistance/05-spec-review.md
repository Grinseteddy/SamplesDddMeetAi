# Cook Assistance — spec review and reconciliation (step 4c)

2026-10-24. Provided specs (copied, originals untouched):
- `contracts/openapi/cook-assistance.openapi.yaml` ← `Chapter13/DomainModelHelpHelpRequest/ClaudeOpus55Medium/cooking-assistance-help.openapi.yaml` (OpenAPI 3.1.0, "Cooking Assistance – Help API" 1.0.0)
- `contracts/asyncapi/cook-assistance.asyncapi.yaml` ← `…/cooking-assistance-help.asyncapi.yaml` (AsyncAPI 3.1.0, "Cooking Assistance – Help Events" 1.0.0)

## 1. Validation and lint

| Spec | Schema validation | Spectral (house style) |
|---|---|---|
| OpenAPI | valid (Spectral `spectral:oas`) | 0 errors, 0 warnings |
| AsyncAPI | valid (`@asyncapi/cli` 6.2.1 `validate`) | 0 errors, 1 warning: `asyncapi-info-license` (info has no license) |

## 2. Reconciliation table

| Slice | Board element | Expected contract | Found | Status |
|---|---|---|---|---|
| B1-S01 | Request help meal plan | `POST /help-requests` type Menu, selected Chef | `requestHelp` with `type: MENU_PROPOSAL`, `preferredProviders` — **no selected Chef** | name drift + missing field |
| B1-S02 | Request Chef review | operation | — | **missing** |
| B1-S03 | Provide Help (Cook, Chef) | `POST /help-requests/{id}/helps` | `provideHelp` | ok (rules drift, §3) |
| B1-S04 | Provide Help (Grandma Avatar message) | AsyncAPI **receive** *Help provided* from Grandma Avatar AI | — (spec only sends) | **missing** |
| B1-S05 | Accept help | `POST /helps/{id}/acceptance` | `acceptHelp` — moves request to ANSWERED | ok + behavior drift |
| B1-S06 | Cancel help request (Cook) | operation | `closeHelpRequest` (`POST …/closure`, by requester) | **name drift** (close ≠ cancel) |
| B1-S07 | Cancel help request (time) | none (internal); state CANCELLED visible | status enum has no CANCELLED | **enum drift** |
| B1-S08 | Close help request (policy) | none (internal); state CLOSED visible | CLOSED exists, reached by `/closure` | behavior drift |
| B1-S09 | Reject help | operation | — | **missing** |
| B1-S10 | Restart answer deadline (policy) | none (internal) | — | ok |
| B2-S01 | Request help preparation step | `POST /help-requests` type Preparation step | `type: PREPARATION_STEP_EXPLANATION` | name drift |
| B3-S01 | Request help ingredients | `POST /help-requests` type Ingredients, one ingredient | `type: INGREDIENT_SUBSTITUTE`, `ingredientIds` 0..50 | name drift + cardinality drift |
| B4-S01 | Request help catastrophe | `POST /help-requests` type Catastrophe, 0..10 pictures | `type: STEPS_TO_MITIGATE_CATASTROPHE`, pictures 0..20 | name drift + cardinality drift |
| B1-S01…B4-S01 | published *Help requested* | AsyncAPI channel + message → Grandma Avatar AI, Notification | `helpRequested` on `cooking-assistance/help-requests` (Kafka) | ok, protocol drift |
| B1-S03/S04 | published *Help provided* | channel + message → Notification | `helpProvided` on `cooking-assistance/helps` | ok, protocol drift |
| — | *Help request answered* | — (no such state any more) | `helpRequestAnswered` | **no slice claims it** |
| B1-S06/S07 | *Help request cancelled* | publish decision | — | open (proposal 18) |
| B1-S08 | *Help request closed* | publish decision | `helpRequestClosed` | ok if published |
| B1-S05 | *Help accepted* | publish decision | `helpAccepted` | ok if published |
| B1-S09 | *Help rejected* | publish decision | — | open (proposal 18) |
| B1-S02 | *Chef review requested* | publish decision | — | open (proposal 18) |
| read models | Help request, Help | GET operations | `listHelpRequests`, `getHelpRequestById`, `listHelpsForHelpRequest`, `getHelpById` | ok (query side, step 11) |

## 3. Proposed improvements

"Breaking" = breaking for a consumer of 1.0.0. Neither spec has been implemented by this run; see
the question on how to version them.

### OpenAPI
| # | Finding | Change | Breaking |
|---|---|---|---|
| 1 | Discriminator drift (F5): `AnswerType` INGREDIENT_SUBSTITUTE / PREPARATION_STEP_EXPLANATION / STEPS_TO_MITIGATE_CATASTROPHE / MENU_PROPOSAL | request `type` → `HelpTopic` enum `MENU`, `PREPARATION_STEP`, `INGREDIENTS`, `CATASTROPHE`; answer discriminator follows | yes |
| 2 | Status drift: OPEN / ANSWERED / CLOSED | `HelpRequestStatus` → `REQUESTED`, `CLOSED`, `CANCELLED`; `Help.status` → `PROVIDED`, `ACCEPTED`, `REJECTED` (replaces `isAccepted`) | yes |
| 3 | B1-S06: `POST …/closure` is the cook's cancel | rename to `POST /help-requests/{id}/cancellation` (`cancelHelpRequest`), 200 → CANCELLED, 409 when not REQUESTED (INV-05, INV-06) | yes |
| 4 | B1-S09 missing | add `POST /helps/{helpId}/rejection` (`rejectHelp`), requester only, 409 when not PROVIDED (INV-15) | no (additive) |
| 5 | B1-S02 missing | add `POST /help-requests/{id}/chef-review` (`requestChefReview`) with `chefId` (INV-04) | no |
| 6 | Accept moves request to ANSWERED; "several helps may be accepted" | accept closes the request (INV-14); description per Q3-6 (open) | yes (semantics) |
| 7 | INV-03: Chef selected by name | add `chefId` to the create body (required when `MENU` and addressed to a Chef) | no |
| 8 | INV-20: addressees | `preferredProviders` → `addressees` (1..2 of `COMMUNITY`, `GRANDMA_AVATAR`; `CHEF` only for `MENU`) — or keep the name and only document the rules | yes if renamed |
| 9 | INV-21: one ingredient | `ingredientIds` (0..50) → `ingredientId` (required for `INGREDIENTS`) | yes |
| 10 | INV-17 / INV-23: one step | `howToStepId` required for `PREPARATION_STEP`, optional for `CATASTROPHE` | yes (tighter input) |
| 11 | INV-24: pictures | `pictures` maxItems 20 → 10; only for `CATASTROPHE` until Q3-8 is answered | yes |
| 12 | INV-12: substitutes | `Substitutes.substitutes` maxItems 50 → 3 | yes |
| 13 | INV-09: Grandma Avatar answers by message only | `provideHelp` refuses `helpProviderType: GRANDMA_AVATAR` (403); provider type taken from the caller's role, not the body | yes |
| 14 | Rejections named after the rules | 422/409 `code` values renamed to the invariant names (`StepNotInRecipe`, `NotAnAddressee`, `ChefOnlyForMenu`, `PremiumRequiredForChef`, `ChefNotAvailable`, `QuestionRequired`, …) and each traced to its `INV-nn` | yes (codes) |
| 15 | Error format | `Error` (code/message/details) → RFC 9457 Problem Details — **decided in step 5**, applied then | yes |
| 16 | Title says "Cooking Assistance" (G4) | rename to "Cook Assistance – Help API"; server path stays | no |

### AsyncAPI
| # | Finding | Change | Breaking |
|---|---|---|---|
| 17 | **Protocol Kafka contradicts ADR0003 (RabbitMQ)** | servers → AMQP 0-9-1 (RabbitMQ); channels → exchange `cook-assistance` + routing keys `help.requested`, `help.provided`, … (as the Grandma Avatar AsyncAPI in the graph consumes) | yes |
| 18 | Published events vs slices | remove `helpRequestAnswered`; decide which leave the context: *Help request cancelled*, *Help rejected*, *Chef review requested* (and keep *Help request closed*, *Help accepted*?) — question below | yes (removal) |
| 19 | **B1-S04 missing (walking skeleton!)** | add a **receive** operation for *Help provided* from Grandma Avatar AI (its own message schema, translated in our ACL) | no |
| 20 | Payloads follow proposals 1, 2, 9, 11, 12 | mirror the OpenAPI changes; `HelpProvided` keeps the request status out (no ANSWERED) | yes |
| 21 | License warning | add `info.license` | no |

## 4. Decisions (2026-10-24)

- Versioning: **new major 2.0.0** for both specs; 1.0.0 kept as the released record in `contracts/*/v1/`, every operation deprecated, sunset **2027-04-30**; the service implements 2.0.0 only.
- Published events (proposal 18): Help requested, Help provided, **Help request cancelled, Help request closed, Help accepted, Help rejected, Chef review requested**.
- Proposals 1–14 and 16–21 **accepted** and applied; 15 (Problem Details) waits for step 5.
- Amendments made while applying: proposal 7 also adds `plannedMealDate` (INV-03 needs the meal date); `answerDeadline` added to the help request (INV-07 made visible); receive channel bound to the Grandma Avatar AI's own exchange (`grandma-avatar`, routing key `grandma-avatar.help.provided`, as its Chapter 13 spec publishes) so it never collides with our own `help.provided`.

## 5. Re-lint after applying

| Spec | Result |
|---|---|
| OpenAPI 2.0.0 | Spectral house style: 0 errors, 0 warnings |
| OpenAPI 1.0.0 (deprecated record) | Spectral house style: 0 errors, 0 warnings |
| AsyncAPI 2.0.0 | `@asyncapi/cli` 6.2.1: valid; Spectral: 0 errors, 8 warnings (`house-channel-address-kebab-case` — AMQP routing keys, deliberate) |

Changelog: `contracts/CHANGELOG.md`.
