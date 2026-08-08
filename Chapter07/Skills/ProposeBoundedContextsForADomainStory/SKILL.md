---
name: domain-story-context-finder
description: >-
  Detect and propose subdomains / bounded contexts from one or more Domain
  Stories — cutting the numbered actor→activity→work-object sentences into named
  contexts (the labeled lanes of a grouped domain story), justifying each cut
  from linguistic and behavioural evidence in the story, mapping how the
  contexts relate, and flagging contested sentences and alternative cuts.
  Use whenever someone shares or describes a domain story — a
  picture, an egon.io export, or plain numbered sentences — and wants to know
  where the boundaries are: "find the bounded contexts", "where would you cut
  this", "group this story into subdomains", "which lanes should this story
  have", "how would we split this into modules, services, or teams", or "review
  the lanes we already drew". Trigger even when nobody says "bounded context",
  "subdomain", or "Domain Storytelling", and whether the story arrives with
  groups drawn (then validate and refine that cut) or with none (then propose
  one). Grounded in Hofer & Schwentner and Evans/Vernon.
---

# Domain Story Context Finder

A domain story is one concrete scenario: **actors** perform **activities**
(verbs) on **work objects** (nouns), numbered to fix the order. A *grouped*
domain story adds labeled lanes — and those lanes are the interesting part,
because each one is a claim about where the domain divides: a **subdomain** in
the problem space, and a candidate **bounded context** in the solution space.

This skill produces (or challenges) that claim. Given a story, cut it into named
contexts, show the evidence for each cut, and be honest about the calls that
could reasonably have gone the other way.

## The one thing to get right

**A bounded context is a language boundary, not a time slice.**

The most common failure is to cut the story into contiguous chunks of the
timeline — steps 1–3 are context A, 4–6 are context B — because the story is
drawn left-to-right and numbered. Real contexts recur. A well-cut story usually
returns to the same lane several times at different points in the sequence, and
a proposed cut where every lane is one unbroken run of consecutive numbers is
almost always a phase diagram wearing a bounded-context costume.

The boundary you are looking for is where **the same word starts meaning
something different** (Evans). A *Bicycle* being racked by a mechanic is an
asset with a condition and a location; a *Bicycle* being booked is inventory
with an availability window; a *Bicycle* being ridden home is a vehicle in
someone's possession. Same pictogram, three models, three contexts — and they
are scattered across the sequence, not stacked in it.

## Mode: propose or review

Check what arrived, and say which mode you are in.

- **No groups drawn** → *propose* a cut. Work the whole method below.
- **Groups already drawn** → *review* the existing cut. Do the same analysis,
  but frame the output as agreement and disagreement with what is there: which
  lanes the evidence supports, which sentence is in the wrong lane, which two
  lanes should merge, where a lane is doing two jobs. Do not silently replace
  their names with yours — a name the team already uses has value your
  alternative does not, so argue for a rename only when the current one misleads.
- **Several stories** → far better evidence. Cut across the *set*: an activity
  that appears in three stories with the same meaning is one context's
  responsibility; the same word used differently in two stories is a boundary
  practically drawing itself. Reconcile into one cut covering all of them.

## Workflow

### Step 1 — Transcribe the story

If the input is an image, read it into a numbered sentence list **before**
interpreting anything. A misread arrow poisons every downstream boundary claim.

Each numbered arrow begins a sentence; the number sits at the arrow's origin,
next to the actor. Unnumbered arrows (`to`, `via`, `at`, `with`, `from`, `near`)
continue the same sentence and pull in more work objects:

```
<Actor> —(N verb)→ <Work object> —(preposition)→ <Work object> → ...

1. Mechanic brings Bicycle to Rack
2. Commuter registers App
3. Commuter pays Monthly fee via App
```

Record existing group labels and annotations verbatim. For a hard-to-read or
non-trivial diagram, show the transcription and ask for confirmation before
continuing — wrong readings are cheap to fix here and expensive later.

### Step 2 — Build the signal table

Before proposing any boundary, lay the evidence out sentence by sentence. This
table is the whole analysis in miniature, and writing it stops you from
pattern-matching a familiar architecture onto an unfamiliar domain:

| # | Actor | Activity | Work objects | What it is *for* (purpose in domain terms) | Sense of the shared nouns here |
|---|-------|----------|--------------|--------------------------------------------|-------------------------------|

The last two columns do the work. "What it is for" is the business reason the
step exists — not what it does mechanically. "Sense of the shared nouns" is
where you record that *Bicycle* means an asset in row 1 and inventory in row 5.

### Step 3 — Run the cut signals

Now look for boundaries. Nine signals, strongest first — full diagnostic
questions, the evidence each needs, and the traps for each in
`references/cut-signals.md`:

1. **Language shift** — the same noun carries a different model, lifecycle, or
   set of attributes. The primary signal; a boundary with this evidence is
   almost always real.
2. **Purpose change** — the business reason for the step changes (getting bikes
   where riders are ≠ taking money ≠ granting physical access).
3. **Actor / expertise change** — a different role, department, or kind of
   domain expert. Strong, but not sufficient alone: one actor can move through
   many contexts (a Commuter does).
4. **Handoff / pivotal moment** — responsibility, custody, or state passes
   somewhere and the story could plausibly pause. Boundaries like to sit here.
5. **Work-object cohesion** — cluster sentences by the objects they touch; a
   tight cluster of co-occurring nouns is context-shaped. Watch for the object
   that appears *everywhere* — that one is shared, not owned.
6. **Ownership and lifecycle** — who creates the thing, who only reads it. The
   creator's context usually owns the model; readers get a copy or a reference.
7. **Rate of change / volatility** — steps that change on different business
   clocks (pricing rules vs. lock hardware) resist living in one model.
8. **Consistency need** — what genuinely must be atomic stays together;
   eventual consistency across a step is a boundary you can afford.
9. **Organisational seam** — different teams, systems of record, or vendors
   already own these steps (Conway). Real, but describes the *current*
   organisation, so weigh it as evidence rather than obeying it.

Two sentences in the same lane should share at least two signals. A boundary
worth drawing usually shows up in three or more.

### Step 4 — Form and name the contexts

Group the sentences by the evidence, then name each group. Names carry the
proposal, so spend real effort here — rules, good and bad patterns, and the
granularity check (how many contexts, and when a lane is too big or too small)
are in `references/naming-and-granularity.md`. The short version:

- Name for **responsibility in the domain's language**, as a noun phrase:
  *Bicycle distribution*, *Bicycle booking*, *Rack management*, *Accounting*,
  *Riding*.
- Never name a context after a single entity alone (*Bicycle*) — that produces
  a data-owning service, not a context — and never after a technical layer
  (*BicycleService*, *Database*, *Backend*).
- If the honest name is *Miscellaneous*, the cut is wrong.

For each context, write: a one-line responsibility, the sentences it holds
(non-contiguous is expected and healthy), the actors involved, the terms it
**owns**, and the terms it **borrows with a different meaning** — that last list
is the argument for its existence.

### Step 5 — Map the relationships

Contexts are only useful with the seams between them named. For each pair that
touches, record what actually flows and in which direction — a registered
Commuter, a booking, a code, a completed ride — then label the relationship
(customer/supplier, conformist, shared kernel, anticorruption layer, published
language, separate ways). Patterns and how to choose between them:
`references/context-map-and-classification.md`.

### Step 6 — Classify (only if it helps the question being asked)

If the person is heading toward build-vs-buy, team shape, or investment, mark
each subdomain **core** (differentiating), **supporting**, or **generic**
(commodity) with a one-line reason. Criteria and the handoff to
`core-domain-chart-author` / `core-domain-chart-critic`:
`references/context-map-and-classification.md`. Skip this section entirely when
the question was just "where are the boundaries".

### Step 7 — Show what was contested

This is the section that earns trust. Never present one cut as *the* answer.

- **Contested sentences** — every sentence that could sit in two contexts, with
  the argument each way and the reason you placed it where you did. In a
  ten-sentence story, expect two or three.
- **Coarser cut** — what merges if the team wants fewer contexts, and what is
  lost.
- **Finer cut** — what splits if a lane grows, and what it would cost.
- **What would settle it** — the concrete question to put to a domain expert.
  "Does the operations team think of a return as putting a bike back into
  circulation, or as closing out a rental?" decides a lane; the wording of the
  question matters more than your guess at the answer.

## Output: the Context Cut

Produce these sections in this order. Keep prose tight; use tables where they
earn their space.

```
# Context Cut — <story name>

## 1. Story transcription
Numbered sentences, exactly as read.

## 2. Signal table
Per sentence: actor · activity · work objects · purpose · noun senses.

## 3. Proposed contexts        (or: Review of the existing lanes)
Per context: name · responsibility (one line) · sentences · actors ·
terms owned · terms borrowed with a shifted meaning · which signals justify it.

## 4. Lane assignment
The story in sequence order with its lane label — the redraw spec:
| # | Sentence | Lane |

## 5. Context map
Relationships between contexts: what flows, which direction, which pattern.

## 6. Subdomain classification        (only when asked or clearly useful)
Core / supporting / generic, one line of reasoning each.

## 7. Contested calls & alternative cuts
Contested sentences (argument both ways), a coarser cut, a finer cut, and the
questions that would settle them.
```

The lane assignment table (section 4) is what makes the proposal *drawable* —
someone can take it straight to egon.io or a whiteboard and redraw the story
with lanes. Always include it, even when the answer is one context.

Adapt depth to the request. "Just tell me the contexts" gets sections 3, 4 and a
short 7. A workshop prep gets everything. But always ground the answer in the
transcription and signal table, so the cut is checkable rather than asserted.

## Traps worth naming out loud

These recur often enough to check against explicitly before you publish a cut.
Full list with the fixes: `references/cut-signals.md`.

- **Timeline slicing** — lanes that are contiguous runs of numbers. Re-examine;
  a real lane almost always recurs.
- **One lane per step** — ten sentences, eight lanes. That is a to-do list.
- **Entity-shaped contexts** — every sentence touching *Bicycle* in one lane.
  That is a database table with ambitions.
- **CRUD lanes** — *Data entry*, *Reporting*, *Validation*. Technical layers,
  not domain boundaries.
- **The app as a context** — *App*, *Mobile*, *Frontend* is a delivery channel
  the domain acts through, not a piece of the domain.
- **Copying the org chart** — sometimes right, but it needs its own evidence.
- **Silently inventing steps** — the story shows what happens in this scenario.
  If the cut needs a step that was never told, say so in the open questions
  instead of drawing it in.

## Working with the neighbouring skills

- Coming from a raw diagram and you also need the buildable structure (domain
  model, state machines, screens) → `domain-story-interpreter`.
- The story itself looks shaky (CRUD verbs, collapsed actors, hidden branches)
  → say so briefly and point at `domain-story-critic`. Cutting a broken story
  into contexts locks the breakage into an architecture.
- The team has a Visual Glossary → its bounded-context grouping is prior art;
  reconcile against it rather than competing with it, and reuse its exact terms.
- Contexts marked core/supporting/generic → `core-domain-chart-author` renders
  them, `core-domain-chart-critic` challenges the placements.
- Contexts as the starting point for an EventStorming board →
  `event-storming-seeder`.

## Reference files

- `references/cut-signals.md` — the nine signals in full: diagnostic question,
  what counts as evidence, how strong each is alone, the anti-signals, and the
  trap catalogue with fixes.
- `references/naming-and-granularity.md` — naming rules and patterns, how many
  contexts a story of a given size should yield, the subdomain-vs-bounded-context
  distinction and when it matters, and the too-big / too-small tests.
- `references/context-map-and-classification.md` — relationship patterns
  (customer/supplier, conformist, ACL, shared kernel, published language,
  separate ways) with selection guidance, plus core/supporting/generic criteria
  and the handoff to the Core Domain Chart skills.
- `references/worked-example.md` — a ten-sentence bicycle-sharing story cut end
  to end, ungrouped input to lane assignment, including the contested calls and
  the alternatives that were rejected. Read this first when unsure how deep to go.

## References

S. Hofer and H. Schwentner, *Domain Storytelling: A Collaborative, Visual, and
Agile Way to Build Domain-Driven Software.* Boston, MA, USA: Addison-Wesley,
2022. ISBN 978-0-13-745891-2.

E. Evans, *Domain-Driven Design: Tackling Complexity in the Heart of Software.*
Boston, MA, USA: Addison-Wesley, 2003.

V. Vernon, *Implementing Domain-Driven Design.* Boston, MA, USA:
Addison-Wesley, 2013.

S. Millett and N. Tune, *Patterns, Principles, and Practices of Domain-Driven
Design.* Indianapolis, IN, USA: Wrox, 2015.