# Micro-UI contract

How the micro-UIs of the Bounded Contexts plug into the AppShell (ADR0005, AP Micro-UIs).
The AppShell team owns this document; a change here is a change of a published contract.

## Rules

1. **A micro-UI belongs to its Bounded Context.** It lives in the context's module under
   `src/main/resources/static/ui/<context>/` and is served at `/ui/<context>/...`.
   Entry point: `/ui/<context>/index.js`, an ES module that registers all its elements.
2. **It calls only its own context's API**: `this.api('<context>', '/path', { method, query, body })`.
   It never sees a token; the AppShell signs the cook in and adds authentication and the `version` header.
3. **It builds on the kit**: `import { LarderElement, define, html, fmt, errorMessage, formValues } from '/app-shell/kit.js'`.
   Shadow DOM, the kit classes (`card`, `btn`, `btn-primary`, `stack`, `row`, `grid`, `badge`, `error`, `notice`, `empty`,
   `loading`, ...) and the design tokens (`var(--larder-*)` from `/app-shell/larder.css`) give all micro-UIs one look.
   Render with `html```, which escapes every value. No other libraries, no build step.
4. **Cross-context work goes through events or documented elements.** An element emits `larder:*` events
   (`this.emit(name, detail)` - they bubble out of the shadow DOM); the AppShell orchestrates. An element may
   embed another context's element by its documented tag and attributes; it never calls another context's API.
5. **Elements are named `larder-<context>-<view>`** and read their input from attributes (observe changes with
   `observedAttributes` where noted). They render a loading state, an empty state and errors (`errorMessage`).
6. **Navigation** inside the app: `this.navigate('#/recipes/{id}')`. Links from contracts (e.g. a notification's
   `link`) can be passed as they are; the shell maps them to routes.

## Elements and events

`→` = event the element emits (detail in braces), `⇐` = method the shell or another element calls.

### Recipe Catalog (`recipe-catalog`)
| Element | Attributes | Behaviour |
|---|---|---|
| `larder-recipe-catalog-search` | – | Search by meal, diet, ingredients; result cards; "New recipe" → navigate `#/recipes/new`; card → `#/recipes/{id}` |
| `larder-recipe-catalog-recipe` | `recipe` | Full recipe (ingredients, steps). → `larder:start-cooking {recipeId}`. Owner: edit (`#/recipes/{id}/edit`), delete (→ `#/recipes`) |
| `larder-recipe-catalog-editor` | `recipe`? | Create or edit a recipe incl. ingredients and steps; after save → navigate `#/recipes/{id}` |
| `larder-recipe-catalog-step` | `recipe`, `step` (observed) | One how-to step for cooking mode: sequence number, description, illustration. Empty until both are set |
| `larder-recipe-catalog-card` | `recipe`, `compact`? | Small card of one recipe (name, meal, diet, time); click → navigate `#/recipes/{id}`. Embeddable by other contexts |
| `larder-recipe-catalog-picker` | – | Search and choose a recipe. → `larder:recipe-picked {recipeId, name}` |

### Meal Planning (`meal-planning`)
| Element | Attributes | Behaviour |
|---|---|---|
| `larder-meal-planning-plans` | – | The cook's meal plans, search by diet/occasion; "New plan" creates an empty plan → `#/meal-plans/{id}` |
| `larder-meal-planning-plan` | `plan` | Edit occasion, servings, meal, how to serve, courses. Course recipes are shown with `larder-recipe-catalog-card`. Adding a course → `larder:pick-recipe {request}`; ⇐ `recipePicked({recipeId, name, request})` |

### Meal Preparation (`meal-preparation`)
| Element | Attributes | Behaviour |
|---|---|---|
| `larder-meal-preparation-start` | `recipe` | Confirms and starts a preparation. → `larder:preparation-started {preparationId, recipeId}` |
| `larder-meal-preparation-cooking` | `preparation` | Cooking mode: current step, previous/next (big buttons). → `larder:step-changed {preparationId, recipeId, howToStepId, sequenceNumber}` on load and after every move. Buttons "This step is unclear" / "Catastrophe!" → `larder:help-needed {situation: 'STEP_UNCLEAR'\|'CATASTROPHE', preparationId, recipeId, howToStepId}` |

### Cooking Assistance (`cooking-assistance`)
| Element | Attributes | Behaviour |
|---|---|---|
| `larder-cooking-assistance-request-form` | `type`, `recipe`?, `how-to-step`? | Ask for help: title, description, preferred providers (only valid combinations, e.g. Chef only for menu proposals). → `larder:help-requested {helpRequestId}` |
| `larder-cooking-assistance-help-status` | `help-request` | Waits for help (polls), shows the request status and every help with its answer. Once the first help arrives → `larder:help-provided {helpRequestId, helpId, helpProviderType, helpProvider?}`. Per help a "Say thanks" button → `larder:thank-for-help {helpRequestId, helpId, helpProviderType, helpProvider?}` |
| `larder-cooking-assistance-request` | `help-request` | Detail page of a help request: status, helps (like help-status), withdraw while open (requester only) |
| `larder-cooking-assistance-help` | `help` | One help with its answer and a link to its request |
| `larder-cooking-assistance-board` | – | Open help requests of the community; answer one as community (at least step explanation and catastrophe), link to details |

### Media (`media`)
| Element | Attributes | Behaviour |
|---|---|---|
| `larder-media-capture` | `purpose`?, `required`? (observed) | Take or choose a photo (camera on phones), preview, "Use this picture" → `larder:picture-taken {contentType, size}` or "Skip" → `larder:picture-skipped` (no "Skip" when `required`). Does not upload by itself. ⇐ `upload(links) → Promise<{mediaId, imageLink}>` (`links`: contract `Link[]`, may be empty) |
| `larder-media-image` | `link` or `media` | Shows an image of Media (`link` = contract image link). Loads it through the API (images need a token) |

### Sharing (`sharing`)
| Element | Attributes | Behaviour |
|---|---|---|
| `larder-sharing-thanks-form` | `help`, `helper-type`, `helper-cook`?, `picture`? | Thanks for a help. Recipients follow the helper type (GRANDMA_AVATAR → GrandmaAvatar, CHEF → Chef (optional name), COMMUNITY → Cook mentioning `helper-cook`). Without `picture` it embeds `larder-media-capture` and uploads before sending. → `larder:thanks-given {thanksId}`. Missing photo consent → `larder:consent-required {purpose: 'photos-in-public-thanks'}`; ⇐ `retry()`. A mentioned cook without consent: explain, offer to thank without naming them |
| `larder-sharing-feed` | – | Thanks of the community, filters "given by me" / "to me", pictures via `larder-media-image`, delete own |

### Notification (`notification`)
| Element | Attributes | Behaviour |
|---|---|---|
| `larder-notification-bell` | – | Badge with the number of NEW notifications (polls), dropdown with the latest; click marks READ and → navigate to the notification's `link`; "All notifications" → `#/notifications` |
| `larder-notification-list` | – | All notifications, mark read, delete |

### Cook Profile (`cook-profile`)
| Element | Attributes | Behaviour |
|---|---|---|
| `larder-cook-profile-gate` | – | Checks whether the signed-in cook is registered; if not, shows the registration form. Then → `larder:cook-ready {cookId}` |
| `larder-cook-profile-profile` | – | Show and change the own profile; deregister (with confirmation) → `larder:logout` |

### Consent Management (`consent-management`)
| Element | Attributes | Behaviour |
|---|---|---|
| `larder-consent-management-consents` | – | All consent texts with the cook's state; give and revoke |
| `larder-consent-management-ask` | `purpose` | Explains one consent and asks for it. Purposes: `photos-in-public-thanks`, `mention-as-helper` (the mapping purpose → consent text id lives here). → `larder:consent-given {purpose}` or `larder:consent-declined {purpose}` |

Internal elements (not for the shell or other contexts): `larder-cooking-assistance-answer-form` (used by the
board and the request page).

## Known gaps

- `larder-cooking-assistance-request-form` has no `ingredients` attribute, so an ingredient-substitute request cannot
  be raised from the UI; the form explains this. Needs an attribute and a source of ingredient ids (Recipe Catalog).
- Answers show substitutes as "Ingredient n": ingredient names would come from Recipe Catalog by id, no element exists.
- Meal Preparation does not tell which step is the last (contract gap); cooking mode learns it from a refused "Next".

## Events the AppShell orchestrates

| Event | From | AppShell does |
|---|---|---|
| `larder:cook-ready` | Cook Profile | opens the app |
| `larder:start-cooking` | Recipe Catalog | dialog with `larder-meal-preparation-start` |
| `larder:preparation-started` | Meal Preparation | cooking mode `#/cook/{id}` |
| `larder:step-changed` | Meal Preparation | sets `recipe`/`step` of `larder-recipe-catalog-step` next to it |
| `larder:help-needed` | Meal Preparation | starts the rescue flow: picture → request → waiting → thanks |
| `larder:picture-taken` / `-skipped` | Media | rescue flow: next step; keeps the capture element to upload later |
| `larder:help-requested` | Cooking Assistance | uploads the picture linked to the help request, then waits for help |
| `larder:help-provided`, `larder:thank-for-help` | Cooking Assistance | rescue flow: thanks step (the thanks form asks for a photo of the rescued meal); elsewhere: thanks dialog |
| `larder:thanks-given` | Sharing | rescue done / feed |
| `larder:consent-required` | any | dialog with `larder-consent-management-ask`; on consent → `retry()` of the asking element |
| `larder:pick-recipe` | Meal Planning | dialog with `larder-recipe-catalog-picker`; → `recipePicked(...)` of the asking element |
| `larder:navigate` | any | route; contract links are mapped (`/cooking-assistance/helps/{id}` → `#/helps/{id}`, ...) |
| `larder:logout` | Cook Profile | signs out |
