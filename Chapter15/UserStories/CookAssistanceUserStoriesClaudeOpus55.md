# Cook Assistance — User Stories

Oct 18, 2026 · @Annegret Junker

## Scope

This backlog has 13 user stories, 6 integration stories and 1 enabler for the bounded context *Cook Assistance* (alt. *Cooking Assistance*). 7 of the 20 rest on a single artifact. Every line traces to `FullGraphWithHistory.ttl`; nothing comes from outside the graph.

- **Slice:** 26 members (2 commands, 3 events, 14 rules, 2 terms, 1 capability, 1 value proposition, 3 async messages), plus 10 sync endpoints and 113 attached nodes. 17 of the 26 members rest on one artifact, mostly invariants from the Help and Help Request models.
- **No Event Model in the graph.** So there are no slices and no Given/When/Then; criteria come from model invariants, glossary cardinalities and API canvas rules, each with its source.
- **Benefit caveat:** no Impact names this context's actors directly. Cook stories use the context's own value proposition, which is on both API canvases. Its link to the goal *Larder - recipe sharing platform with min. 2,500 paying members in two years* runs only through unconfirmed merges (*Cook* ≟ *Cook as author*). The impact *Provide cooking support* is also missing from impact map v3.
- **No ADR decisions** in the graph answer any of the 20 open questions below, so none became a criterion.
- **Out of scope:** *Cooking Help*, the board v1 context pending a merge with this one, and actors' work in neighbouring contexts. Crossings appear as integration stories.
- Labels are kept verbatim, including misspellings such as *porovided*.

## Story map

The board gives one order: *Help requested* (event 7) before *Help provided* (event 8). The activities below follow it; within an activity the graph gives no order.

| Actor | 1 · Ask for help | 2 · Look after my request | 3 · Answer a request | 4 · Use the answers |
| --- | --- | --- | --- | --- |
| Cook | US-CA-01 Help Meal plan · US-CA-02 Help with Ingredients · US-CA-03 Help for Meal Preparation Step · US-CA-04 Meal Preparation Catastrophe | US-CA-05 See · US-CA-06 Change · US-CA-07 Delete my help request | — | US-CA-12 See the helps · US-CA-13 Accept a help (gap) |
| Community Cook · Chef | — | — | US-CA-08 Provide help (Community Cook) · US-CA-09 Provide help (Chef) · US-CA-10 Change · US-CA-11 Delete my help | — |
| Neighbouring contexts | I-04 Meal Planning · I-05 Meal Preparation post a Help request | — | I-01 · I-02 Grandma Avatar AI answers | I-03 Notification · I-06 Sharing |

Enabler E-01 (Pictures from Media) sits under US-CA-04.

## Shared criteria — every Help Request

US-CA-01 to 04 each include these criteria; every one is drawn with a given cardinality or written on a sticky.

| Criterion | Source |
| --- | --- |
| A Help Request contains exactly 1 title, 1 description, 1 type, 1 Requester, 1 Status, 1 help Request Id | Help Request model v1 + v2 |
| A Help Request contains 1..2 preferred Provider, each Chef, Grandma Avatar or Community | Help Request model v1 + v2 |
| "Chef help is porovided exclusively" | Help Request model v1 + v2, beside Chef |
| A Help Request belongs to 1 Meal | Visual Glossary v1 + v2 |
| A Cook posts 0..\* Help Requests | Visual Glossary v1 + v2 |
| Then "Help requested" is recorded and published | EventStorming board v1 + v2, event 7; received on the Grandma Avatar and Notifications canvases |

Not criteria, because the graph disputes them: the event's name (Q2), who a request goes to (Q1), the Picture limit (Q15), and the new request's status OPEN, which only the AsyncAPI contract states (Q22).

## Cook stories

The benefit for every Cook story is the context's value proposition, "Provides help for cooks in planning and preparation of meals and dinners" (both Cooking Assistance API canvases). It is abbreviated below as **VP**. US-CA-01 to 04 are split along the four sub-terms of *Help Request* in the glossary and the four domain stories. That split stands only if the room confirms the four types are kinds of request (Q3).

### US-CA-01 · Help Meal plan

**As** Cook, **I want to** request help with my Meal plan from a Chef, **so that** I get help in planning and preparation of meals and dinners (VP).

- All shared criteria.
- type is *Menu proposal*; preferred Provider Chef is allowed "Only for type — Menu proposal" — Help Request model v1 + v2, beside Chef.
- "Chef support only for menu proposal" — Help Request model v1 + v2.

**Traces to** Request help · POST /help-requests (sync canvas, row 2) · Help Meal plan · domain story A, sentence 03 "Cook asks Chef for Help" · Help requested · capability "Cooks can create help requests and select who can answer them…" **Confidence** OnArtifact · 9 artifacts   **Tags** state change **Open** Q1, Q3, Q13; Chef ≟ Chefs as senior partner (pending merge).

### US-CA-02 · Help with Ingredients

**As** Cook, **I want to** ask the Community for Help with Ingredients, **so that** I get help in planning and preparation of meals and dinners (VP).

- All shared criteria.
- type is *Ingredient Substitute*; recipe (0..1) is "Mandatory — Preparation Step Explanation — Ingredient Substitute" — Help Request model v1 + v2.
- ingredients (0..\*) "Only for type — Ingredient Substitute" — Help Request model v1 + v2.

**Traces to** Request help · POST /help-requests · Help with Ingredients · domain story B, sentence 03 · reads Ingredients, Recipe **Confidence** OnArtifact · 9 artifacts   **Tags** state change **Open** Q1, Q3; Community Cook ≟ Community (pending).

### US-CA-03 · Help for Meal Preparation Step

**As** Cook, **I want to** ask Grandma Avatar for Help with a Meal Preparation Step, **so that** I get help in planning and preparation of meals and dinners (VP).

- All shared criteria.
- type is *Preparation Step Explanation*; recipe is mandatory (as US-CA-02).
- howTo Step (0..1) "Only for type — Preparation Step Explanation" — Help Request model v1 + v2.

**Traces to** Request help · POST /help-requests · Help for Meal Preparation Step · domain story C, sentence 03 **Confidence** OnArtifact · 9 artifacts   **Tags** state change **Depends on** I-01 (Grandma Avatar answers through Grandma Avatar AI) **Open** Q1, Q3.

### US-CA-04 · Meal Preparation Catastrophe

**As** Cook, **I want to** ask Grandma Avatar for Help with Pictures of my burnt Meal, **so that** I get help in planning and preparation of meals and dinners (VP).

- All shared criteria.
- type is *Steps to Mitigate Catastrophe*.
- ⟨Picture limit not a criterion⟩ — glossary v2 says 0..10, Help Request model v2 says 0..\* (Q15).

**Traces to** Request help · POST /help-requests · Meal Preparation Catastrophe · domain story E, sentence 03 · reads Catastrophe, Pictures **Confidence** OnArtifact · 8 artifacts   **Tags** state change **Depends on** E-01 (Pictures from Media), I-01 **Open** Q3, Q15; the recipe rule names only two types — is a recipe needed here?

### US-CA-05 · See my help requests

**As** Cook, **I want to** see my Help Requests, filtered by Status, **so that** I get help in planning and preparation of meals and dinners (VP).

- GET /help-requests filters by Requester and Status and returns help requests — sync canvas, row 1.
- GET /help-requests/{helpRequestId} returns one help Request — sync canvas, row 3.

**Confidence** Implied (the canvas names no actor) · 1 artifact   **Tags** state view **Open** Q17.

### US-CA-06 · Change my help request

**As** Cook, **I want to** change my Help Request, **so that** I get help in planning and preparation of meals and dinners (VP).

- "Only requester is allowed to" — sync canvas, PATCH /help-requests/{helpRequestId}.
- Returns a link to the changed help request.

**Confidence** Implied · 1 artifact   **Tags** state change **Open** Q17; which fields may change, in which Status? The graph is silent.

### US-CA-07 · Delete my help request

**As** Cook, **I want to** delete my Help Request, **so that** I get help in planning and preparation of meals and dinners (VP).

- "Only requester is allowed to" and "Only in status open" — sync canvas, DELETE /help-requests/{helpRequestId}.

**Confidence** Implied · 1 artifact   **Tags** state change **Open** Q17, Q21 ("delete" is the API verb; no domain verb exists).

### US-CA-12 · See the helps for my request

**As** Cook, **I want to** see the Helps given to my Help Request, **so that** I get help in planning and preparation of meals and dinners (VP).

- GET /helps filters by help Request Id, type and help provider — sync canvas, row 6.
- "If help provider is Chef only original requester can see the help" — sync canvas, GET /helps.
- GET /helps/{helpId} returns one help — sync canvas, row 8.

**Confidence** Implied · 1 artifact   **Tags** state view **Open** Q6, Q17.

### US-CA-13 · Accept a help (gap)

**As** ⟨open — no artifact says who accepts⟩, **I want to** accept a Help, **so that** ⟨open⟩.

- A Help contains 1 isAccepted — Help model.
- "At least one Help must be accepted" — Help Request model v2, beside Answered (supersedes v1's "At least one Help must answer it").

**Command** ⟨none⟩ — no board command and no endpoint; PATCH /helps is provider-only. **Confidence** Implied · 2 artifacts **Open** Q7, Q16.

## Help provider stories

The board's *Provide help* is performed by Chef, Community Cook and Grandma Avatar. Grandma Avatar is an external system in *Grandma Avatar AI*, so its answers are integration stories (I-01, I-02). The Help criteria below come from the Help model unless stated; it is the only artifact that draws them.

### US-CA-08 · Provide help as Community Cook

**As** Community Cook, **I want to** Provide help on a Help Request, **so that** ⟨open — no Impact names Community Cook or Community⟩.

- A Help is for 1 Help Request (glossary v1 + v2) and refers to 1 help request and 1 help requester.
- A Help contains 1 answer, 1 answer title, 1 help provider type, 1 isAccepted, 1 help Id, and 0..1 help provider.
- answerType "Must be the same as request type".
- "Same recipe as in request" · "Same howTo as in request" · "Same ingredientId as in request".
- "A cook can answer their own request - except for Chef requests".
- "Answered help requests can be still answered" — Help Request model v2.
- By type, glossary v2: 1..3 Ingredient Substitute · 1 Preparation Step Explanation · 1..10 Steps to Mitigate Catastrophe · 0..10 Picture.
- Then "Help provided" is recorded (board v1 + v2, event 8) and published (Cooking Assistance async canvas, Events Send).

**Traces to** Provide help (≟ story verb *provides*) · POST /helps (sync canvas, row 7) · domain story B, sentence 04 · Community provides Help 0..\* **Confidence** OnArtifact · 6 artifacts   **Tags** state change **Open** Q4, Q5, Q6, Q10; Community Cook ≟ Community (pending); the AsyncAPI marks the request ANSWERED on HelpProvided (Inferred) while v2 ties Answered to an accepted Help.

### US-CA-09 · Provide help as Chef

**As** Chef, **I want to** Provide help with a Menu proposal, **so that** Chefs as senior partner "Provide cooking support" towards the Larder goal *(via unconfirmed merge Chef ≟ Chefs as senior partner; impact absent from impact map v3)*.

- All Help criteria of US-CA-08.
- A Help contains 1..3 Menu proposal; a Chef provides 0..\* Help — glossary v2.
- "Chef support only for menu proposal" — Help Request model v1 + v2.
- "If help provider is Chef only original requester can see the help" — sync canvas, GET /helps.

**Traces to** Provide help · POST /helps · domain story A, sentence 04 · impact *Provide cooking support* → goal *Larder…* **Confidence** OnArtifact · 8 artifacts   **Tags** state change **Open** Q4, Q13; Q18 (benefit path).

### US-CA-10 · Change my help

**As** Help provider, **I want to** change my Help, **so that** ⟨open⟩.

- "Only provider is allowed to" — sync canvas, PATCH /helps/{helpId}.
- Returns "Link to changed help request", as the canvas wrote it (Q9).

**Confidence** Implied (*Help provider* is a glossary term, not a drawn actor) · 1 artifact   **Tags** state change **Open** Q9.

### US-CA-11 · Delete my help

**As** Help provider, **I want to** delete my Help, **so that** ⟨open⟩.

- "Only provider is allowed to" — sync canvas, DELETE /helps/{helpId}.

**Confidence** Implied · 1 artifact   **Tags** state change **Open** Q8 (does deleting the last Help reopen its request?), Q21.

## Integration stories

Each crossing comes from the context maps (v1–v3) and the API canvases. Wiring that two contracts draw differently is a question here, not a criterion.

### I-01 · Grandma Avatar AI receives help requests

**As** Grandma Avatar AI, **I need** each new Help Request with its event, **so that** "Grandma / Grandpa AI" can deliver "Provide cooking support" *(via unconfirmed merge Grandma Avatar ≟ Grandma / Grandpa AI)*.

- Cook Assistance sends the event with payload Help Request — async canvas ("Help request created"); context map v2 and Grandma Avatar canvas ("Help requested").

**Confidence** OnArtifact · 4 artifacts   **Tags** integration **Open** Q1 (does every request reach Grandma Avatar?), Q2, Q11.

### I-02 · Grandma Avatar AI delivers help

**As** Grandma Avatar AI, **I need to** deliver Help provided with a Help response to Cook Assistance, **so that** "Grandma / Grandpa AI" can deliver "Provide cooking support" *(same unconfirmed merge)*.

- Cook Assistance receives "Help provided" with payload help — async canvas, Events Receive; context maps v1 + v2.

**Confidence** OnArtifact · 4 artifacts   **Tags** integration **Open** Q6, Q11; the Grandma AsyncAPI sends HelpProvided without helpRequestStatus, which Notifications requires.

### I-03 · Notification hears about help

**As** Notification, **I need** Help requested and Help provided with their Help request and Help response, **so that** ⟨open⟩.

- Context map v2 and the Notifications async canvas (Events Receive) name both events.

**Confidence** OnArtifact · 3 artifacts   **Tags** integration **Note** The rule "Help requests go to whole community or Chef" belongs to Notification, not to this context. **Open** Q1, Q2.

### I-04 · Meal Planning posts a help request

**As** Meal Planning, **I need to** POST a Help request with Menu, Ingredients and Recipe (read models) and GET the Help response, **so that** ⟨open⟩.

- Context map v2 arrows, both directions.

**Confidence** Implied · 2 artifacts   **Tags** integration **Open** Q12.

### I-05 · Meal Preparation posts a help request

**As** Meal Preparation, **I need to** POST a Help request with Menu, Ingredients, Recipe and Pictures (read models) and GET the Help response, **so that** ⟨open⟩.

- Context map v2 arrows, both directions.

**Confidence** Implied · 2 artifacts   **Tags** integration **Open** Q12.

### I-06 · Sharing reads a help

**As** Sharing, **I need to** GET Help, **so that** ⟨open⟩.

- Context map v3 arrow "Sharing → Cooking Assistance: GET Help".

**Confidence** OnArtifact · 1 artifact   **Tags** integration **Open** Q12, Q14.

### E-01 · Enabler — Pictures from Media

Cook Assistance reads Pictures from Media ("GET Pictures", context map v2, Conformist towards Media). US-CA-04 needs this.

## Coverage

Every command, endpoint, event, message and read model in the context has a story, except two board readings flagged in the last rows.

| Node | Kind | Story | Note |
| --- | --- | --- | --- |
| Request help | command | US-CA-01–04 | performed by Cook |
| Provide help | command | US-CA-08, 09; I-02 | ≟ story verb *provides* (pending) |
| POST /help-requests | endpoint, row 2 | US-CA-01–04 |  |
| GET /help-requests · GET /help-requests/{id} | endpoint, rows 1, 3 | US-CA-05 |  |
| PATCH /help-requests/{id} | endpoint, row 4 | US-CA-06 |  |
| DELETE /help-requests/{id} | endpoint, row 5 | US-CA-07 |  |
| GET /helps · GET /helps/{id} | endpoint, rows 6, 8 | US-CA-12 |  |
| POST /helps | endpoint, row 7 | US-CA-08, 09 |  |
| PATCH /helps/{id} | endpoint, row 9 | US-CA-10 |  |
| DELETE /helps/{id} | endpoint, row 10 | US-CA-11 |  |
| Help requested | event, seq 7 | US-CA-01–04; I-01, I-03 |  |
| Help request created | event (async canvas) | I-01 | ≟ Help requested (pending, Q2) |
| Help provided | event, seq 8 | US-CA-08, 09; I-02, I-03 |  |
| publishes HelpRequestCreated · publishes HelpProvided · consumes HelpProvided | async message | I-01; US-CA-08; I-02 | Inferred (AsyncAPI) |
| Help request | read model | US-CA-05 | ≟ Help Request (pending) |
| Help response | read model | I-04, I-05 | read in Meal Planning / Meal Preparation |
| Help provider | read model + term | US-CA-10, 11 | used as the actor |
| Menu · Ingredients · Recipe | read model | US-CA-02; I-04, I-05 | owned by neighbouring contexts |
| Pictures · Catastrophe | read model | US-CA-04; E-01 |  |
| Event Model slices | — | none | the graph holds no Event Model |
| Request help by Community Cook and Grandma Avatar | board performer | **no story** | Q19 |
| Request help → Help provided | board trigger | **no story** | Q5 |

## Open questions

None of these has an ADR decision. Q1–Q14 are the graph's own questions, verbatim. Q15–Q22 are contradictions, pending merges and gaps that this backlog surfaced.

| # | Question | Source | Blocks |
| --- | --- | --- | --- |
| Q1 | Is a Help Request at a Community, at a Grandma Avatar, or at both? | 10 artifacts; glossary "at 1 … at 1" contradicts Help Request model "preferred Provider 1..2" | US-CA-01–03, I-01, I-03 |
| Q2 | Which name does the event on the Cooking Assistance → Grandma Avatar border carry: 'Help request created' or 'Help requested'? | async canvas vs context map v2 | shared criteria, I-01 |
| Q3 | Are Ingredient Substitute, Preparation Step Explanation, Steps to Mitigate Catastrophe and Menu proposal kinds of request, or contents of a help? | Help Request / Help models | split of US-CA-01–04 |
| Q4 | Must every Help contain substitutes, an explanation, mitigation steps and menu proposals at once? | glossary v2 | US-CA-08, 09 |
| Q5 | Is 'Help provided' triggered by 'Provide help' or 'Request help'? | EventStorming board | coverage |
| Q6 | Is the answer called 'Help' or 'Help response'? | board vs glossary | US-CA-08, 12, I-02 |
| Q7 | When does a Help Request become Closed? | Help Request model v2 | US-CA-13 |
| Q8 | Does deleting the last Help reopen its request? | API contracts | US-CA-11 |
| Q9 | Does PATCH /helps/{helpId} return a link to the changed help or to a help request? | sync canvas | US-CA-10 |
| Q10 | Whose text limits hold for help payloads? | AsyncAPI contracts | US-CA-08 |
| Q11 | Is the path 'cook-assistance' or 'cooking-assistance'? | contracts; plus two recorded wiring contradictions (exchange and queue names) | I-01, I-02 |
| Q12 | Do Meal Planning, Meal Preparation and Sharing consume HelpProvided as an event? | Cooking Assistance AsyncAPI | I-04–I-06 |
| Q13 | Where do Chef, Dinner and Meal Planning help live in the glossary? | domain story A vs glossary | US-CA-01, 09 |
| Q14 | Who can a thanks go to — and is it still for a Help? | Thanks / Sharing | I-06 |
| Q15 | Picture on a Help Request: 0..10 or 0..\*? | glossary v2 vs Help Request model v2 (recorded contradiction) | US-CA-04 |
| Q16 | Who accepts a Help, and with which command? | gap: isAccepted and "At least one Help must be accepted" exist, no command or endpoint does | US-CA-13 |
| Q17 | Is the API's Requester the Cook? | pending merges Requester ≟ help requester ≟ cook Id | US-CA-05–07, 12 |
| Q18 | What benefit does this context serve on impact map v3? | *Provide cooking support* absent from v3; ≟ *supports unexperienced members* pending; Cook ≟ Cook as author, Chef ≟ Chefs as senior partner pending | all "so that" |
| Q19 | Do Community Cook and Grandma Avatar also request help? | board draws them as performers of *Request help* | coverage |
| Q20 | Are *Cooking Help* and *Cook Assistance* one context? | pending merge; *Cooking Help* absent from board v2 | scope |
| Q21 | What is the domain verb for deleting a help request or a help? | only the API's DELETE exists | US-CA-07, 11 |
| Q22 | Do the contract-only rules hold: "A help request can be deleted only by its requester and only while OPEN", new requests start OPEN? | OpenAPI / AsyncAPI, Inferred | shared criteria, US-CA-07 |
