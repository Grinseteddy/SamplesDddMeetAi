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
| cooking-assistance | Complete examples `BurningCatastropheHelpRequest`, `HelpRequestList` (one per help type), `StayCalmHelp` under `components/examples`, used by the help request and help responses; example on `PreferredProviders` | The examples built from single property examples broke the contract's own invariants (e.g. a `MENU_PROPOSAL` with `howToStep` and `ingredients`). Found with the Prism mock |
| recipe-catalog | Example `"02:00"` on `preparationTime` | Without it the mock produced a value violating the `HH:MM` pattern. Found with the Prism mock |

## Known lint warnings

Redocly reports two `no-invalid-schema-examples` warnings for `StayCalmHelp` (`answer` "must NOT have unevaluated
properties"). They are false positives of Redocly's strict additional-property check through the `oneOf` of `Answer`;
Prism validates the same example against the schema without errors (`scripts/smoke-mocks.sh`, `GET /helps/{helpId}`).

## Context map corrections (for the book figure)

- Cooking Assistance is upstream (OHS) of Meal Planning and Meal Preparation (Q1).
- Sharing reads the help response from Cooking Assistance, not from Meal Preparation (Q2).
- Sharing is downstream (customer) of Media and Consent Management (Q3).

## Decided in phase 2

- Consent texts are seeded by Consent Management's migration `V3__consent_texts.sql`:
  `5f8d8a1c-515d-4eae-a6b1-0a0313edfc31` "I allow to use my photos in public thanks and recipes." (contract example) and
  `3c6e2b7a-9d41-4f0b-8e5a-2b7c9d1e4f60` "I allow other cooks to mention me as helper in their thanks." - the one Sharing checks.
- Cook registration (`User`) carries no consent, so Cook Profile does not call Consent Management although the
  context map shows "POST Cook, Consent". Either the registration contract gets consents, or the edge is drawn wrongly.

Implementation decisions where the contracts are silent (details in the module code):

| Context | Decision |
|---|---|
| all | Duplicates and rule violations answer 400 - the contracts have no 409 |
| Cook Profile | The registered cookId is the token's `cookId`; email unique (case-insensitive); a cook may set their own `status`, incl. `premium` |
| Recipe Catalog | `recipe:admin` grants no write rights (every write operation lists only `recipe:write` + owner); diet filter is exact (VEGETARIAN does not find VEGAN); ingredient filter matches whole names, all must match |
| Recipe Catalog | PATCH cannot clear `subtitle`, `mainImage`, `illustration` or `furtherImages` - the generated models cannot tell "absent" from "null" |
| Media | Image ≤ 5 MiB, JPEG/PNG/GIF/WebP, ≤ 20 links; the business object id is the UUID in the last path segment of `Link.url`; only the uploader deletes; bytes first then metadata, orphaned objects possible but never dangling metadata |
| Media | `format: uri` + `maxLength` generates `@Size` on `java.net.URI`, which fails at runtime; the Media module maps `string+uri` to `String` |

## Decided in phase 3

- **New edge Recipe Catalog → Meal Preparation.** Starting a preparation "sets the current step to the first how-to
  step of the recipe" - Meal Preparation must read the recipe's steps. The context map lacks this edge; Meal
  Preparation now has a Recipe Catalog client and keeps a snapshot of the steps per preparation.
- **No server-to-server call Meal Planning / Meal Preparation → Cooking Assistance.** Neither contract has a help
  operation; help requests are raised by the micro-UIs directly at Cooking Assistance (ADR0005, AppShell orchestrates
  the rescue flow). Cooking Assistance stays upstream (Q1), the edge on the map is a UI integration. The unused
  generated clients were removed.
- **Meal Planning derives a diet** it does not store in its contract: each course keeps a snapshot of its recipe's
  diet; a plan suits diet D when every course is at least as restrictive as D (VEGAN > VEGETARIAN > NORMAL).
  Meal plans are read and searched only by their owner.
- **Meal Preparation keeps a snapshot** of the recipe's steps; editing the recipe does not disturb a cook at the stove.
- **Upstream calls go over HTTP with the caller's token** (`UpstreamClients`, `BearerTokenRelay`), also inside the
  monolith, so a context can be cut out later without touching its callers.

## Decided in phase 4

- **Notification ignores HelpRequested (contract conflict).** The AsyncAPI wants a notification "for each preferred
  provider group", but receivers are Cook ids and Notification knows no group members. The event is consumed and
  acknowledged, no notification is created. HelpProvided notifies the requester.
- **Grandma answers only when she is asked** (`GRANDMA_AVATAR` among the preferred providers) and only what she can
  answer validly from ids: catastrophes and step explanations. Ingredient substitutes and menu proposals stay open
  for community and chefs - a guessed amount or invented recipe could spoil the dish. The AI sits behind the port
  `HelpAdvisor`; today a deterministic recipe box, an LLM adapter plugs in via `larder.grandma-avatar-ai.advisor`.
- **Cooking Assistance enforces the stricter limit where REST and AsyncAPI differ**, so every accepted request is
  publishable (description 2000, substitute name 100, ingredients/howToStep required for their type).
- **Help journey latency ~0.5 s**, dominated by two outbox polls (200 ms each).

## Decided in phase 5

- **Thanks are checked against three upstreams, in this order:** the help exists (Cooking Assistance) and the caller
  is its requester; the recipients are who actually helped (COMMUNITY ↔ Cook, CHEF ↔ Chef, GRANDMA_AVATAR ↔
  GrandmaAvatar); the picture exists (Media); the giver consented to photos in public thanks; every mentioned cook
  consented to being mentioned (Consent Management). One thanks per help. No transaction is open during the calls.
- **Every thanks needs the giver's photo consent**, because `pictures` is mandatory. Consent text ids are properties
  (`larder.sharing.consents.*`); Sharing asks by purpose, only its adapter knows the ids.
- **`sharing:admin` grants reading only**, like `recipe:admin`.
- **Sharing reads only what it needs from a Help** (tolerant reader): the generated Cooking Assistance client cannot
  deserialize the `oneOf` answer, so the adapter skips it. A consumer that needs the answer requires a generator fix.
- **Outbox relays send `mandatory`** (as the AsyncAPI bindings demand); unroutable messages are logged and counted.

## Decided in phase 6

- **Micro-UIs as plain Web Components** in each context's module, on a shared kit from the AppShell; no framework,
  no frontend build. Integration contract: `app-shell/MICRO-UI.md`.
- **The AppShell orchestrates** the flows across contexts (start cooking, rescue flow, picking a recipe for a meal
  plan, asking for a missing consent and retrying); ambient parts (notification bell) work by choreography.
  Cooking mode is a UI composition: Meal Preparation drives, Recipe Catalog shows the step text.
- **The browser signs in with OIDC Authorization Code + PKCE** (public client, tokens in memory only); micro-UIs never
  see a token. Static UI files are public, every API call carries the cook's token.
- **The rescue picture belongs to the help request** (Media link `helpRequest`); the thanks ask for a new picture of
  the rescued meal.

## Open

- **The ubiquitous language is spelled differently:** `Meal` is `dinner` in Meal Planning but `DINNER` in Recipe
  Catalog and Cooking Assistance; Meal Planning's `diet` parameter is a free string while Recipe Catalog has a `Diet`
  enum. One spelling for the published language would spare every consumer a translation.
- Relaying the cook's token means starting a preparation or setting meal plan courses also needs `recipe:read`; the
  contracts do not say so.
- Meal Preparation has no operation to list, finish or abandon a preparation - preparations are never closed.
- **REST and AsyncAPI of Cooking Assistance disagree:** length limits (5000 vs 2000, 200 vs 100), the answer shape
  (flat in REST, nested per answer kind in AsyncAPI), required ingredients/howToStep per type.
- **No proof of being a chef:** any cook can answer as `CHEF`; a scope or claim is missing.
- **Missing events:** no "declined" event when Grandma does not answer, no event when a request is reopened
  (its last help deleted) or withdrawn.
- **Help requests have no pictures** although the context map shows "POST ... Pictures, Help request"; therefore
  Cooking Assistance does not call Media yet.
- **Consent revocation has no effect on existing thanks:** a cook who revokes the mention or photo consent stays
  named / shown. Needs a decision, possibly a ConsentRevoked event.
- **Media has no cheap existence check** (`GET /images/{id}` returns the whole image) and does not reveal the
  uploader, so Sharing cannot require the picture to be the giver's own.
- A Help carries no chef name, so a Chef recipient's `chefName` cannot be verified; `cooks[]` allows several cooks
  although one help has one provider.
- Notification's `receivers` would expose other cooks' ids once a notification has several receivers.
- **UI gaps:** ingredient-substitute requests cannot be raised from the UI (no ingredient selection), substitutes
  show "Ingredient n" (no ingredient names without Recipe Catalog), cooking mode learns the last step only from a
  refused "Next" (see `app-shell/MICRO-UI.md`).
- Cook Profile's contract example has `name: Joe`, `givenName: Doe` - given name and family name look swapped.
- Meal Preparation: the contract's `HowToStep` has only `howToStepId` and `sequenceNumber` - no description. The
  cook at the stove gets the step text only from Recipe Catalog.

- Recipe Catalog: should `recipe:admin` really have no rights beyond reading? Should "suitable for this diet" include stricter diets (VEGAN ⊂ VEGETARIAN)?
- Cook Profile: who may set `premium` - the cook, or only a payment/admin process?
- Media: limits (size, formats, links) and the link → business object rule belong in the contract.

