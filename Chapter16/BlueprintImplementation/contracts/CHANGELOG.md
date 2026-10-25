# Cook Assistance contracts — changelog

## 2.0.0 — 2026-10-24 (approved in step 4 of the implementation blueprint)

Both specs move to a new major version; 1.0.0 stays as the released record in `*/v1/`, deprecated,
sunset date **2027-04-30**. The service implements 2.0.0 only.

### OpenAPI "Cook Assistance – Help API" (`openapi/cook-assistance.openapi.yaml`)
- **Breaking:** `type`/`AnswerType` → `topic`/`HelpTopic` (`MENU`, `PREPARATION_STEP`, `INGREDIENTS`, `CATASTROPHE`); answer discriminator `answerType` → `topic`; `Substitutes` → `IngredientSubstitutes`.
- **Breaking:** `HelpRequestStatus` `OPEN/ANSWERED/CLOSED` → `REQUESTED/CLOSED/CANCELLED`; `Help.isAccepted` → `Help.status` (`PROVIDED/ACCEPTED/REJECTED`).
- **Breaking:** `POST /help-requests/{id}/closure` (`closeHelpRequest`) → `POST /help-requests/{id}/cancellation` (`cancelHelpRequest`).
- **Breaking:** accepting a help closes its request (no ANSWERED); one ingredient per request (`ingredientId` replaces `ingredientIds`); step required for Preparation step, optional for Catastrophe; pictures 0..10 and Catastrophe only; substitutes 1..3; `preferredProviders` → `addressees`; `helpProviderType` taken from the caller, Grandma Avatar answers by message only; rejection codes renamed after the invariants.
- **Added:** `POST /helps/{id}/rejection` (`rejectHelp`), `POST /help-requests/{id}/chef-review` (`requestChefReview`), `chefId`, `plannedMealDate`, `answerDeadline`, query parameter `addressee`, `topic`.
- Title "Cooking Assistance – Help API" → "Cook Assistance – Help API".
- Open for step 5: the error format (`Error` or RFC 9457 Problem Details).

### AsyncAPI "Cook Assistance – Help Events" (`asyncapi/cook-assistance.asyncapi.yaml`)
- **Breaking:** Kafka → RabbitMQ (AMQP 0.9.1 over TLS, ADR0003); topic exchange `cook-assistance` with routing keys `help.requested`, `help.request.cancelled`, `help.request.closed`, `help.request.chef-review-requested`, `help.provided`, `help.accepted`, `help.rejected`.
- **Breaking:** `HelpRequestAnswered` removed (no such state); payloads follow the OpenAPI changes.
- **Added:** `HelpRequestCancelled`, `ChefReviewRequested`, `HelpRejected`; receive operation for the Grandma Avatar AI's `HelpProvided` (queue `cook-assistance.grandma-avatar-help-provided` bound to exchange `grandma-avatar`, routing key `grandma-avatar.help.provided`).
- Deliberate lint deviation: channel addresses are AMQP routing keys with dots, not kebab-case paths (8 warnings `house-channel-address-kebab-case`).
- Note for the Grandma Avatar AI owners: its Chapter 13 spec binds to exchange `cooking-assistance`, routing key `cooking-assistance.help.requested`; 2.0.0 publishes on `cook-assistance` / `help.requested`.

### 2.0.0 revision r1 — 2026-10-24 (step 5, ADR-CA-006; before release)
- OpenAPI: `Error` schema replaced by RFC 9457 `Problem` (`type`, `title`, `status`, `detail`, `instance`, extensions `code`, `rule`); every error response served as `application/problem+json`; `503 ServiceUnavailable` with `Retry-After` added to every operation; edge behavior (404/405/415/400 as Problems) stated in `info.description`.
- OpenAPI (step 5, ADR-CA-013; additive): optional `Age` response header on help request and help responses, present only when served from the cache during a database outage.

### 2.0.0 revision r2 — 2026-10-25 (step 9 slice check, back to step 4; before release)
- OpenAPI: `requestHelp` adds `409` (`ConcurrencyConflict`, a generated identity already taken, ADR-CA-005) and the `422` code `TopicNotYetSupported` (topics are served one increment at a time); `415 UnsupportedMediaTypeResponse` on the three operations with a request body (`requestHelp`, `requestChefReview`, `provideHelp`) — `405` stays an edge behavior in `info.description`, since it answers methods no operation declares; `ChefOnlyForMenu` example `rule` INV-01 → INV-20 as raised by the domain (INV-01 states the same restriction for the Chef side); response component `ServiceNotAvailableResponse` → `InternalErrorResponse` (it is the 500, not the 503).
- AsyncAPI: the consumed Grandma Avatar answer becomes the named `GrandmaAvatarAnswer` (`oneOf`, discriminator `answerType`) with `GrandmaAvatarPreparationStepExplanation` fully specified (`images` default `[]`) and the three other kinds named, carrying `answerType` only until their increments; the receive operation names the `TopicNotYetSupported` refusal.
- Lint: OpenAPI clean; AsyncAPI unchanged (8 deliberate `house-channel-address-kebab-case` warnings).

### 2.0.0 revision r3 — 2026-10-25 (step 9 test-list preparation, back to step 4; before release)
- OpenAPI: the `requestHelp` description now matches its schema — body violations, including the conditional rules (recipe for every topic but Menu, one step for Preparation step, one ingredient for Ingredients, at least one addressee, pictures Catastrophe-only and ≤ 10), answer `400 InvalidRequest`; `422` lists only rules the schema cannot express (`QuestionRequired` for a question of blanks, `StepNotInRecipe`, `IngredientNotInRecipe`, the Chef rules, `TopicNotYetSupported`).
- OpenAPI (ADR-CA-010): `413 PayloadTooLargeResponse` (body > 1 MB) on the three operations with a body; `429 TooManyRequestsResponse` with `Retry-After` (60 requests/min per cook, anonymous per address) on all ten operations; both limits named in `info.description`. Lint clean.

### 2.0.0 revision r4 — 2026-10-25 (step 9, item "version header missing", back to step 4; before release)
- OpenAPI: `VersionParameter` keeps `required: true` and `enum: ["2.0.0"]` but drops `default: "2.0.0"`. A default cannot apply to a parameter the client must send, and express-openapi-validator 5.6.2 fills request defaults (`useDefaults: true`, not configurable) before checking `required` — with the default, a missing header was silently accepted. Deliberate deviation from the house-style convention "required version header with a matching default". Lint clean.

