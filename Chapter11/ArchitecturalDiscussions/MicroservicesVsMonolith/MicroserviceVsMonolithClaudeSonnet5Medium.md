# Prompt 2026-09-13

Use the knowledge graph and discuss if a monolith, a microservice or a modular monolith should be used for the project.

[Domain Knowledge Graph](../../assets/larderEnahnced4.ttl)

# Answer

Looking at the graph, this is a strong case for starting with a **modular monolith**, not a microservices architecture — with one candidate exception. Here's the reasoning, tied to specific evidence in your knowledge graph.

## Why not microservices (yet)

**The bounded contexts are still being discovered and contested, not settled.** The graph itself documents this in real time:
- `Q_HelpOneContextOrTwo` — the two EventStorming board photos *disagree* about whether Cooking Assistance and Cooking Help are one context or two.
- SADR0004 only *just* split "Help" into Help Request / Help Response (2026-09-11) — a domain concept that had been treated as one thing across five domain stories and the glossary until very recently.
- `Q_WhoOwnsIngredient` — Meal Planning and Recipe Catalog canvases *both* claim to own the "Ingredient" term.
- `As_GrandmaAvatarOwnContext_BCC` directly contradicts `As_AvatarInsideAssistance_placeholder` — is Grandma Avatar its own context or a responder inside Cooking Assistance? Unresolved.
- Multiple canvases (Cook Profile, Recipe Catalog) have open questions like "who reads this?" with **no answer anywhere on the map**, and canvases repeatedly note fields "owned by nobody on the map."

Microservices bake bounded-context boundaries into physical, networked, independently-versioned seams. Doing that while the boundaries themselves are still shifting is expensive to undo — every renamed concept or re-drawn boundary becomes a distributed migration instead of a code refactor. A modular monolith lets you keep the module boundaries (mirroring your bounded contexts) while the *cost* of getting one wrong is a package rename, not a service decomposition.

**The scale target doesn't demand it.** The Goal node is explicit: `min. 2,500 paying members in two years`. That's a modest B2C scale that a well-built monolith handles comfortably — nothing in the Wardley map or impact map points to a load profile (e.g. massive fan-out, wildly divergent scaling needs per feature) that would force independent scaling of services.

**Team size.** All ADRs are attributed to a single author ("Junker"), and nothing in the graph suggests multiple autonomous teams. Microservices earn their keep primarily by letting independent teams deploy independently — that benefit doesn't exist yet here. Conway's Law cuts the other way: one team, one deployable, one release train is simpler to operate.

## Where a modular monolith fits your own artifacts well

You already have the module map for free — the nine Bounded Context Canvases translate almost directly into modules:

| Context | Suggested module role |
|---|---|
| Cook Profile | Core module (identity/registration) |
| Meal Planning | Core module |
| Meal Preparation | Core module |
| Recipe Catalog | Core module (read-heavy, "generic" per its own canvas) |
| Media | Core module |
| Sharing | Core module |
| Notification | Core module (or a thin cross-cutting service) |
| Consent Management | Cross-cutting module enforced centrally |
| Grandma Avatar AI | **Candidate for early extraction** (see below) |

Consent Management is a good argument *for* the monolith approach specifically: it's referenced from Cook Profile, Sharing, and Media (`Msg_*_Consent` edges, `Con_Consent` mentions), and one of its own open questions is literally "why does Media not consult this context?" A single in-process module that everything can call synchronously makes it much easier to actually enforce consent everywhere, versus coordinating a compliance rule across independently-deployed services with eventual consistency.

## The one seam worth cutting early: Grandma Avatar AI

This is the one place your graph gives a real technical (not just organizational) reason to separate a deployable from day one:
- It's already modeled with its own Bounded Context Canvas and an **anticorruption layer** (`Ctx_GrandmaAvatarAI`, domain roles "Interchange"/"Gateway") — the team has already drawn a translation boundary around it.
- The Wardley map places its underlying capability (`Cap_SpecificAI` → `Cap_GenAI`) as Custom-Built resting on a Commodity Gen-AI layer — a different technology profile (LLM calls, possibly different language/runtime, different scaling curve, possibly higher latency/cost per call) than the rest of the CRUD-ish business logic.
- The Capability Map marks Grandma/Grandpa as **core** (differentiating), so it's worth isolating for iteration speed — but the Impact Map gives it the *lowest* priority (3) of any deliverable (`As_GrandmaCore_CM` vs `As_GrandmaPriority3_IM` — a contradiction your own graph flags). That mismatch is worth resolving with stakeholders before over-investing in it architecturally, but it doesn't change the technical case for keeping it behind an ACL.

So: build it as a separate service behind the ACL that's already been designed, called synchronously or via events from the monolith — a "modular monolith plus one satellite service," rather than either a pure monolith or full microservices.

## Bottom line

Start with a **modular monolith**, one module per bounded context, single deployable, single database (schemas can mirror module boundaries so a future split isn't a data-migration nightmare), with **Grandma Avatar AI carved out as an independent service** from the start because it already has a designed ACL and a genuinely different technology/scaling profile. Revisit true microservice decomposition only once (a) the contested boundaries above are actually resolved and stable in production, and (b) you have evidence of team-scaling or load-scaling pressure that the monolith can't absorb — neither of which your graph shows yet.