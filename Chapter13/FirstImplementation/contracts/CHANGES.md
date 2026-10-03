# Contract changes against Chapter12 (ClaudeFable51)

The originals in `Chapter12/OpenAPIs/ClaudeFable51` and `Chapter12/AsyncAPIs/ClaudeFable51` stay untouched.
This folder holds the reconciled contracts the code is generated from.

## AsyncAPI

| # | Problem in Chapter12 | Resolution |
|---|---|---|
| 1 | Exchange `cooking-assistance` vs `cook-assistance` | `cooking-assistance`, owned by Cooking Assistance |
| 2 | Routing keys did not match (`cooking-assistance.help-request.created` vs `help.requested`) | `cooking-assistance.help.requested`, `cooking-assistance.help.provided` |
| 3 | `HelpRequestCreated` vs `HelpRequested` | `HelpRequested` everywhere (sticky name on the context map) |
| 4 | Cooking Assistance consumed and published `HelpProvided` on the same exchange (would consume its own events) | Grandma Avatar publishes on its own exchange `grandma-avatar`, key `grandma-avatar.help.provided`; Cooking Assistance consumes from queue `cooking-assistance.grandma-avatar-help-provided` and republishes |
| 5 | Payload schemas copied into three files and drifted (invariants, `answerTitle` 120 vs 200, `SubstituteIngredient.name` 120 vs 100) | Cooking Assistance is the published language; Notifications and Grandma Avatar `$ref` its schemas. Grandma keeps only its outbound `HelpProvidedPayload` |
| 6 | Queue bindings only in prose | `x-bindings` (exchange + routing key) on every consumer queue; code declares exactly these |

## OpenAPI

| File | Change | Why |
|---|---|---|
| sharing | `helpId` (required, immutable) on `Thanks` and `ThanksCreate` | Sharing reads the Help from Cooking Assistance (`GET /helps/{helpId}`) to know who helped (Q2) |
| all | `title` on inline response schemas (e.g. `HelpRequestLink`) | Typed generated code instead of `InlineObject` |
| cooking-assistance | `Answer` declares `answerType: HelpType`; the four answer kinds use `$ref: HelpType` + `const` | Typed discriminator; states the invariant "answer type equals request type" in the contract |

## Context map corrections (for the book figure)

- Cooking Assistance is upstream (OHS) of Meal Planning and Meal Preparation (Q1).
- Sharing reads the help response from Cooking Assistance, not from Meal Preparation (Q2).
- Sharing is downstream (customer) of Media and Consent Management (Q3).

## Open

- The map has no edge Recipe Catalog → Meal Preparation, although a preparation is started for a recipe.
- Sharing needs the id of the consent text "may be mentioned as helper".
