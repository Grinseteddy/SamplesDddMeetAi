---
name: event-storming-interpreter
description: Interpret an EventStorming board (Alberto Brandolini's notation) from a photo or screenshot and turn it into a buildable brief: bounded contexts as modules, a domain model with aggregate state machines, commands/policies/flows, actors, external systems, and open hotspots. Use this whenever the user shares or describes an EventStorming result -- a board of colored stickies where orange = domain events (past tense), blue = commands, large yellow = aggregates, small yellow = actors, green = read models, lilac/pink = policies or external systems, red = hotspots -- usually on a left-to-right timeline clustered into bounded contexts. Also trigger when they ask to interpret, read, analyze, or build an app / data model / services from such a board, even if they never say 'EventStorming' or name the flavour, or for any orange-events-on-a-timeline sticky board, a Miro/Mural/whiteboard DDD session photo, or a request to extract domain events, aggregates, or bounded contexts.
---

# Event Storming Interpreter

EventStorming (Alberto Brandolini) explores a business domain by sticking colored notes on a long wall. The spine is a line of **domain events** — facts in the past tense — arranged left-to-right in time. Around them grow the **commands** that cause events, the **actors** who issue commands, the **aggregates** that enforce the rules, the **read models** people consult to decide, the **policies** that react automatically, the **external systems** at the edges, and the **hotspots** that mark trouble. Related notes cluster into **bounded contexts**.

This skill reads such a board and produces a **buildable brief**: the artifacts a builder needs to stand up a working prototype — the same destination as the `domain-story-interpreter`, reached from a different notation.

The job is interpretation, not redrawing. Read carefully, transcribe the timeline faithfully, derive the buildable structure the board already encodes, and surface what it leaves unsaid. **Do not invent stickies that aren't there** — an overview board is often *only* events and hotspots, and a brief built on fabricated commands and aggregates is worse than an honest, sparse one.

## Core mental model

A finished slice of an EventStorming board reads as one **grammatical sentence**, always in the same order:

```
An ACTOR (or a POLICY) issues a COMMAND
   to an AGGREGATE,
   which produces a DOMAIN EVENT,
   informed by / updating a READ MODEL.
```

Internalize three things this implies:

1. **The event is the anchor, and it is a fact.** Every orange note should be a past-tense statement ("Book purchased", "Page read", "Notification sent"). If a note reads like an intention ("Purchase book") it is a command on the wrong color, or you've misread the color — check. Events that read like failures ("Book not found") are legitimate and important; flag them, don't silently normalize them away.

2. **Color tells you the role, but only together with icon, position, and the grammar.** Tools and teams drift from the canonical palette — the same pink may mean "external system" on one board and "automated policy" on another; the same yellow may be an aggregate or an actor, separated only by size and icon. When a note's role is ambiguous, resolve it by where it sits in the sentence above and what its text says, not by color alone. The legend is in `references/notation-legend.md`.

3. **The board may be at any flavour, and you adapt.** A *Big Picture* storm is coarse: mostly events, actors, external systems, hotspots — few or no commands/aggregates. A *Process-level* or *Software-design* storm carries the full grammar. Read what is actually present. If the board has no commands or aggregates, your brief reports contexts + timeline + hotspots and lists *candidate* aggregates explicitly marked as inferred — it does not pretend the design-level detail was on the wall.

## Workflow

Follow these steps in order. Do not jump to a domain model before you have transcribed the timeline — a misread spine poisons everything downstream.

### Step 1 — Reconcile the legend (what the colors mean *on this board*)

Before reading content, read the board's own conventions. Scan the notes and build the actual color+icon → role mapping this board uses, then line it up against the canonical palette in `references/notation-legend.md`. Note any deviation explicitly (e.g. "policies here are pink with a gear, not the canonical lilac"). If a color is genuinely ambiguous and no legend note resolves it, say so and state the assumption you'll proceed with. This mapping becomes section 1 of the brief and makes the whole interpretation checkable.

### Step 2 — Transcribe the timeline (and confirm)

Read every **orange domain event** and list them in board order: left-to-right is time, and clusters/columns usually correspond to bounded contexts. Write them as a numbered, past-tense list grouped by their cluster. Watch for **pivotal events** (an event with a vertical divider line) — they mark phase or context boundaries.

For a busy or hard-to-read board, show the user your transcribed event list and ask them to confirm before continuing. A wrong reading is cheap to fix here and expensive later.

### Step 3 — Identify bounded contexts (the future modules)

The labeled bubbles, lanes, swimlanes, or visible clusters are **bounded contexts / subdomains** (in the example board: Purchase, Catalog management, Audio summary, Notification, Catalog search, Lending, Bookshelf, Reading, Notes…). Each becomes a **module / feature area** of the prototype. Assign every event and its surrounding notes to a context. If the board has no visible grouping, infer candidate contexts from event clustering and label them as inferred, or treat the whole board as one module and say so.

### Step 4 — Parse each slice into its grammar sentence

For each domain event, walk outward to assemble the sentence from Step 0's mental model:

- the **command** (blue) that caused it,
- the **actor** (small yellow) who issued the command, **or** the **policy** (lilac/pink + gear) that fired it automatically,
- the **aggregate** (large yellow) that handled it,
- the **read model(s)** (green) feeding the decision or updated by the event.

Capture each slice as a row: `context · actor/policy · command · aggregate · event · read models`. Where a note is missing (e.g. an event with no visible command), leave the cell blank rather than inventing one — a gap is signal.

### Step 5 — Trace the flows (arrows between slices)

Arrows drawn between notes are **causation / triggering**, almost always an event in one slice setting off a command in another. That is a **policy / saga across contexts** ("whenever *Reading position marked*, *Read book*"). List every arrow as `source event → target command [policy/saga]`. These cross-context flows are the integration backbone of the system and matter enormously for the build.

### Step 6 — Build the domain model and state machines

Turn the catalogued notes into buildable structure:

- **Aggregates → entities.** Each large-yellow aggregate becomes a data entity. Infer plausible attributes from the commands it accepts and events it emits.
- **Read models → views/queries.** Each green note is a query/projection a screen or policy reads. Note which events feed it.
- **State machines.** Cluster the events that belong to one aggregate and read them as a lifecycle. The events are the transitions; the implied statuses are the states. Produce, per stateful aggregate: the state list, a transition table (`from state → to state`, triggered by which command + which event), and the terminal states. State machines are the backbone of the prototype — they define statuses, the buttons that change them, and the screens per status. A small text or Mermaid diagram is welcome.

### Step 7 — Catalogue actors and external/automated systems

List every **actor** (small yellow) → a user role/persona for the prototype, with the commands each can issue (this is your permissions model). List every **external system** (pink) and **automated policy** (gear) → an integration or background automation, with what triggers it and what it does. Watch for the same actor name appearing as issuer in one slice and subject in another — that's two role instances (e.g. manager vs. member), which matters for permissions.

### Step 8 — Surface hotspots and open questions

Collect every **red hotspot** verbatim — these are the team's own flagged problems and must survive into the brief, not be smoothed over. Then add your own honest open questions: events with no command or actor, nouns that switch roles between slices, failure events with no handling path, missing read models behind a decision, aggregates with no lifecycle, and any note you could not read with confidence. Keep this specific.

## Output: the Event Storming Brief

Produce these sections, in this order. Keep prose tight; use tables where they earn their place. Always ground the brief in the explicit legend (§1) and timeline (§2) so the interpretation is checkable.

```
# Event Storming Brief — <board name>

## 1. Legend (as read on this board)
The color/icon → role mapping actually used, with deviations from the canonical palette flagged.

## 2. Event timeline
Domain events in board order, grouped by bounded context. The backbone — past tense, numbered.

## 3. Bounded contexts (modules)
Each bubble/cluster → a module: the events, commands, aggregates, and read models it owns.

## 4. Domain model
Aggregates → entities with inferred attributes. Read models listed separately as views/queries.

## 5. Aggregate state machines
For each aggregate with a lifecycle: states, transition table (command → event → new state), terminal states. Text/Mermaid diagram welcome.

## 6. Commands, policies & flows
Per slice: context · actor/policy · command · aggregate · event · read models.
Then the cross-context flows from the arrows: source event → target command [policy/saga].

## 7. Actors & external systems
Human roles (with the commands they may issue → permissions) and external/automated systems (with their triggers).

## 8. Hotspots & open questions
Red hotspots verbatim, then the gaps the brief leaves for the user to resolve.
```

Adapt depth to the request and to the board's flavour. If the user only wants the domain model, lead with §4–5; if they want to start building UI, emphasize the contexts (§3) and the flows (§6). If the board is a coarse Big Picture storm, §4–6 will be thin and §2/§3/§8 carry the weight — that's correct, not a failure. But always include §1 and §2 so the reading is verifiable.

## When the user wants to go further

If the user then asks to actually build (a clickable UI, a React app, a service skeleton, a doc), use the brief as the spec and the relevant file/frontend skills to produce it: contexts become app sections or services, aggregate state machines become status logic and buttons, commands become the actions/endpoints, read models become the screens and queries, and the arrow-flows become the events that wire services together. Don't silently add features the board and brief don't justify — confirm additions first.

## References

- `references/notation-legend.md` — the full Brandolini palette: every sticky color, its icon, its role, and the one-sentence grammar that connects them; plus external systems, hotspots, pivotal events, opportunities, the three flavours (Big Picture / Process / Software Design), common icon conventions, and the tool-variation caveats. Read it whenever a note's color or role is ambiguous, or the user asks about notation.
- `references/worked-example.md` — a full library-management board (Purchase, Catalog, Lending, Reading, Notes, …) worked end-to-end into an Event Storming Brief. Read it to pattern-match on a real interpretation: legend reconciliation, slice parsing, cross-context arrow-flows, aggregate state machines, and role-switching nouns.