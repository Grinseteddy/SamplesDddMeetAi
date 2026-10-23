---
name: event-storming-detail-interpreter
description: >-
    Interpret a detail-level (Process-Level or Design-Level) EventStorming board into a per-aggregate design brief: command handlers, invariants, policies, read models, failure paths and gaps.
author: Annegret Junker
---

# EventStorming Detail Interpreter

Big Picture EventStorming explores a whole business; **detail-level** EventStorming zooms into one process or one bounded context and makes it rigorous. Brandolini distinguishes two detail flavours:

- **Process-Level (Process Modelling)** -- adds commands, actors, policies, read models and external systems around the events, so that every event has an explanation. The "picture that explains everything": no event may appear from nowhere.
- **Design-Level (Software Design)** -- additionally introduces **aggregates** (large yellow), i.e. the units that receive commands, protect invariants and emit events. Sometimes the team uses yellow "system" stickies first and refines them into aggregates later.

This skill reads such a board and produces a **Detail Design Brief** precise enough to hand to an implementer (or to downstream skills such as aggregate-behavior-author, event-storming-invariant-finder or blueprint-service-implementation). The job is faithful interpretation plus rigorous checking. **Never invent stickies.** Where the board is silent, record a gap or a clearly labelled *inferred* candidate -- never present inference as if it were on the wall.

## The grammar you check everything against

Every slice of a detail board must form this sentence, left to right:

```
READ MODEL (green) informs an ACTOR (small yellow) / UI
  -> who issues a COMMAND (blue)
  -> to an AGGREGATE or SYSTEM (large yellow / pink)
  -> which emits one or more DOMAIN EVENTS (orange)
  -> which a POLICY (lilac: "whenever <event> then <command>") may react to
  -> and which update READ MODELS (green) for the next decision.
```

The loop *event -> policy -> command* is how processes continue without a human; the loop *event -> read model -> actor -> command* is how they continue with one. Every orange event should be reachable through one of these paths, and every blue command should land on exactly one yellow aggregate/system.

Canonical palette (boards drift -- reconcile first, see Step 1):

| Sticky | Colour | Role |
|---|---|---|
| Domain event | orange | past-tense fact |
| Command | blue | intention, imperative |
| Actor | small yellow | person/role issuing a command |
| Aggregate / system | large yellow | handles command, enforces invariants, emits events |
| External system | pink | third party outside our control |
| Policy | lilac / purple | reactive rule "whenever ... then ..." |
| Read model | green | data shown to make a decision |
| Hotspot | red / magenta (often rotated) | problem, question, conflict |
| UI sketch | white | screen/form mock |
| Constraint / rule | yellow or white note on aggregate | invariant written by the team |

## Workflow

Work in order. Do not design aggregates before the slices are transcribed -- a misread slice corrupts the model.

### Step 1 -- Reconcile legend and flavour
Read the board's own colour/icon conventions and map them to the palette above; note every deviation. Then decide the flavour from evidence: **no aggregates present -> Process-Level; aggregates present -> Design-Level**. State it. If yellow "system" stickies are present but not clearly aggregates, treat them as *aggregate candidates* and say so. If the board is actually Big Picture (few/no commands), stop and recommend event-storming-interpreter.

### Step 2 -- Transcribe the flow (and confirm when busy)
List every sticky in board order, grouped into **slices** (one command -> handler -> event(s) unit). Keep text verbatim; mark unreadable notes `[?]`. Note branches (one command, alternative events, e.g. *Payment Authorised* vs *Payment Declined*), parallel lanes, swimlanes, and pivotal/temporal events ("End of month reached", clock icon). For a large or blurry board, show the transcription and ask the user to confirm before continuing.

### Step 3 -- Parse slices into the grammar
One row per slice:
`# | trigger (actor / policy / time / external) | command | aggregate/system | event(s) | read models consulted | read models updated`
Leave cells blank where the board is blank. Record the source of each command: human (actor), automated (policy), time-triggered, or external system.

### Step 4 -- Build per-aggregate command handlers
Group slices by aggregate. For each command it accepts, write:
- **Preconditions / invariants** it must check -- taken from constraint notes, hotspots, branching failure events and command wording. Mark each `on board` or `inferred`.
- **Success event(s)** and **failure / rejection event(s)**.
- **State it needs** to decide (this defines the aggregate's minimal internal state -- only what invariants require, not everything shown in read models).

Check aggregate boundaries: an invariant that needs data from two aggregates signals either a wrong boundary or a policy/eventual-consistency step -- flag it. Commands that mention several aggregates are a smell -- flag them.

### Step 5 -- Derive lifecycles
Per aggregate, read its events as transitions and produce: states, a transition table (`from -> command -> event -> to`), the initial and terminal states, and commands that are illegal in each state. A short Mermaid `stateDiagram-v2` is welcome. Name inferred states explicitly as inferred.

### Step 6 -- Policies and process flow
Write every policy as `whenever <event> [and <condition>] then <command>` and list the read model it needs to evaluate the condition (missing read model = gap). Classify each as within-aggregate, cross-aggregate (eventual consistency), or cross-context (integration). Identify long-running processes (sagas/process managers) and their compensations; if a failure event has no policy or actor reacting to it, flag a **dead end**.

### Step 7 -- Read models and UI
For each green note: which events project into it, which actor/policy reads it, and the **fields the decision needs** (infer from the command it precedes; mark inferred). Attach white UI sketches to the command they produce.

### Step 8 -- Completeness check
Run these checks and report each finding with sticky references:
1. Event without cause (no command, policy, external system or clock before it).
2. Command without handler (no aggregate/system after it).
3. Command without trigger (no actor, policy or time).
4. Aggregate without invariant (only CRUD -- maybe not an aggregate).
5. Happy path only -- commands with no rejection event.
6. Policy without read model for its condition.
7. Decision (actor -> command) without a read model.
8. Event not past-tense / command not imperative (wrong colour or wording).
9. Same term used with two meanings, or two terms for one thing (language drift).
10. Every red hotspot -- verbatim, unresolved.

## Output: Detail Design Brief

```
# Detail Design Brief -- <process / bounded context>

## 1. Legend & flavour
Colour/icon mapping as read, deviations, flavour (Process-Level / Design-Level) and why.

## 2. Transcribed flow
Slices in board order, verbatim, [?] for unreadable.

## 3. Slice table
# | trigger | command | aggregate | event(s) | RM consulted | RM updated

## 4. Aggregates & command handlers
Per aggregate: purpose, minimal state, then per command: preconditions/invariants (on board / inferred), success events, failure events.

## 5. Lifecycles
Per aggregate: states, transition table, terminal states, Mermaid diagram.

## 6. Policies & processes
"whenever ... then ..." list with read models, classification, sagas and compensations.

## 7. Read models & UI
Per read model: source events, consumers, needed fields.

## 8. Completeness findings
Numbered findings from Step 8, each referencing stickies.

## 9. Hotspots & open questions
Red hotspots verbatim, then the questions the team must answer.
```

Adapt depth to the ask: for "just the invariants" lead with section 4; for "is our board complete?" lead with section 8. Always keep sections 1-3 so the reading is verifiable.

## Handing on
If the user wants to continue, the brief maps directly onto implementation: aggregates -> domain model classes, command handlers -> application service methods, events -> event types, policies -> event handlers/process managers, read models -> projections and queries, lifecycles -> status logic. Offer the matching downstream skill if available (aggregate-behavior-author, event-storming-invariant-finder, read-model-author, blueprint-service-implementation), but don't start building unasked, and confirm any additions the board doesn't justify.