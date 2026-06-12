---
name: domain-story-interpreter
description: Interpret domain stories (Domain Storytelling diagrams in the Hofer & Schwentner pictographic language) and turn them into a structured prototype brief — domain model, entity state machines, actors/roles, bounded contexts, user flows, and screens. Use this skill whenever the user shares or describes a domain story, a pictographic process diagram (numbered verb-labeled arrows running between actor pictograms and work-object pictograms, often grouped into lanes or boxes), an egon.io / Domain Storyteller export, or asks to "interpret", "read", "analyze", or "build a prototype / app / data model / screens from" such a diagram — even if they never say the words "domain storytelling". Trigger it any time the input is a numbered actor→activity→work-object story that needs to be translated into something buildable.
---

# Domain Story Interpreter

Domain Storytelling (Stefan Hofer & Henning Schwentner) captures a business process as a **concrete story** drawn in a tiny pictographic language: **actors** do **activities** (verbs) on **work objects** (nouns), the sentences are numbered to give order, and **groups** cluster related parts. This skill reads such a story and produces a **prototype brief**: the artifacts a builder needs to stand up a working prototype.

The job is interpretation, not redrawing. Read carefully, transcribe faithfully, derive the buildable structure, and surface what the story leaves unsaid.

## Core mental model

A domain story is **one concrete scenario** — a single happy path, not a flowchart. It deliberately has **no conditionals**. That has two consequences you must respect:

1. Branches and alternatives show up as **separate numbered sentences** (e.g. "7 finishes → Task done" and "8 rejects → Task rejected") or as separate stories — never as if/else inside one arrow. When you build a prototype you *do* need the branches, so reconstruct them explicitly and flag them as inferred.
2. The story shows what *does* happen in this example, not everything that *could*. Anything a prototype needs but the story omits (validation, errors, empty states, auth details) is an open question, not an invention to slip in silently.

## Workflow

Follow these steps in order. Do not jump to screens before you have transcribed the story.

### Step 1 — Transcribe the story (and confirm)

If the input is an image, **read it into a numbered sentence list first**. Misreading the diagram poisons everything downstream, so transcribe before interpreting.

Each numbered arrow starts a sentence. The sequence number sits at the **origin** of the arrow (next to the actor). Read each sentence as:

```
<Actor> —(N verb)→ <Work object> —(preposition/linking verb)→ <Work object> → ...
```

Unnumbered arrows after the first (`to`, `via`, `at`, `with`, `from`, `in`, `near`, `and`) continue the same sentence and link in more work objects. Write each as a plain line:

```
1. Mechanic brings Bicycle to Rack
2. Commuter registers App
3. Commuter pays Monthly fee via App
```

Record the **groups** too — the labeled lanes/boxes around parts of the story (see Step 4) — and any annotations.

For a non-trivial or hard-to-read diagram, show the user your transcription and ask them to confirm before you continue. A wrong reading is cheap to fix here and expensive to fix later.

### Step 2 — Catalogue actors and roles

List every actor. Classify each as a **person** (→ user role/persona) or a **software system** (→ external integration). Watch for **role transitions**: an activity like "Anonymous *becomes* Cook" means one user gains a new role/permission level, not two unrelated people. Note when the same actor name appears in two positions of one sentence (e.g. a Craftsman *assigns a task to* a Craftsman) — that's two role instances (manager vs. assignee), which matters for permissions.

### Step 3 — Extract the domain model and state machines

Go through every work object and classify it:

- **Entity (data)** — something the system stores and manipulates (Task, Recipe, Project, Invoice, Monthly fee, Code). These become data models. Infer plausible attributes from how the story uses them.
- **UI surface / channel** — a device or app the work happens *through* (App, phone). Not an entity; it tells you *where* an activity occurs.
- **Physical / external object** — a real-world thing the system only references (Bicycle, Rack, Public transport station, Home, Meal). Usually represented as a record/ID, sometimes with location data.

Then find **state machines**. Cluster work objects that share a base noun but carry different adjectives:

```
New task → Task → Assigned Task → Task in progress → Task done
                                                    ↘ Task rejected
```

That cluster is **one entity (Task)** moving through **states**. Each adjective is a state; each activity that produces the next form is a **transition**. Output: the state list, the transition table (from → to, triggered by which verb + actor), and which states are terminal. State machines are the backbone of the prototype — they define statuses, the buttons that change them, and the screens per status.

### Step 4 — Map groups to modules

Groups carry meaning. In practice they are usually one of:

- **Subdomain / bounded context** — a labeled lane or box (e.g. "User management", "Accounting", "Bicycle booking", "Task Management", "Billing"). Each becomes a **module / feature area** of the prototype. Group the use cases and entities under their subdomain.
- **Optional** — a box literally labeled "optional" marks steps that may be skipped. Those activities become **optional features / skippable steps** in the flow, not required path.
- **Variant / assumption** — annotations that note other cases or assumptions. Carry these into open questions.

If there are no groups, treat the whole story as one module and say so.

### Step 5 — Derive use cases and the user journey

Each numbered activity by a person is a **use case** = a user action the prototype must support. Order them by sequence number into the **user journey** (the navigation flow). For each use case capture: actor/role, the action (verb), the entity acted on, the channel ("via App" → in-app), required context (prepositions: "with Code", "at Rack", "from Project"), and the resulting state change if any. Mark optional steps and inferred branches.

### Step 6 — Propose screens and navigation

Turn the in-app use cases into screens. One screen (or screen-action) per human activity that happens on a UI channel; group screens by subdomain into app sections; gate them by role. For each screen note its purpose, key UI elements (forms, lists, buttons that fire the transitions from Step 3), and what it navigates to next. Physical/external steps (a mechanic racking a bike, a cook eating a meal) are *not* screens — note them as off-app context.

### Step 7 — Surface open questions

Because the story is one concrete path with no conditionals, list what a real prototype still needs the user to decide: alternative/error paths not shown, validation rules, auth and account model, multiplicity ("can a project have many tasks?"), data ownership and permissions, and any annotation-flagged variants. Keep this honest and specific.

## Output: the Prototype Brief

Produce these sections, in this order. Keep prose tight; use tables where they add clarity.

```
# Prototype Brief — <story name>

## 1. Story transcription
Numbered sentences, grouped by their subdomain/group label.

## 2. Actors & roles
Each actor: person vs. system, the role(s) it plays, any role transitions.

## 3. Modules (bounded contexts)
Each group/lane → a module, with the use cases and entities it owns.

## 4. Domain model
Entities with inferred attributes. UI surfaces and external objects listed separately.

## 5. State machines
For each entity with a lifecycle: states, transition table (from → to, trigger, actor), terminal states. A small text/Mermaid state diagram is welcome.

## 6. Use cases & user journey
Numbered list following sequence order: role · action · entity · channel · required context · state change. Mark [optional] and [inferred branch].

## 7. Screens & navigation
Screen list grouped by module and gated by role: purpose, key elements, next step.

## 8. Open questions & assumptions
What the story does not pin down that the prototype must.
```

Adapt depth to the request: if the user only wants the data model, lead with sections 4–5; if they want to start building UI, emphasize 6–7. But always ground the answer in an explicit transcription (section 1) so the interpretation is checkable.

## When the user wants to go further

If the user then asks to actually build the prototype (a clickable UI, a React app, a doc), use the brief as the spec and the relevant file/frontend skills to produce it. The brief's modules become app sections, its state machines become status logic and buttons, and its user journey becomes the navigation. Don't silently add features the story and brief don't justify — confirm additions first.

## References

- `references/pictographic-language.md` — the building-block legend and the grammar of domain-story sentences. Read it if a diagram element is ambiguous or the user asks about notation.
- `references/worked-examples.md` — three diagrams fully worked into prototype briefs (a bicycle-sharing service, a recipe app, a craftsman job tracker). Read it to pattern-match on real interpretations, especially for lanes-as-subdomains, the "optional" group, role transitions, and entity state machines.