---
name: architecture-question-finder
description: Mine a domain knowledge graph (or raw DDD artifacts — EventStorming board, Event Model, context map, Bounded Context Canvases, Core Domain Chart, Business Model Canvas, invariants sheet) for the specific, evidenced questions that must be answered before the system's architecture can be designed — context boundaries and ownership, sync vs async integration, consistency windows, build vs buy, deployment seams. Produces a prioritized, cited Architecture Question Agenda that drops straight into a small-ADR log. Use whenever someone has run domain-knowledge-graph, or has the underlying artifacts, and asks what needs deciding before architecting or building the system, wants an "architecture backlog" or "decision agenda", or asks "what do we need to figure out before we design this" or "what's blocking us from picking services and integration patterns". Not for generating the architecture itself or a generic checklist unconnected to the artifacts.
---

# Architecture Question Finder

Turns a knowledge graph of modelling artifacts into the short list of
questions that actually determine the system's shape — not a generic
"things to think about" checklist, but the specific doubts *this* graph
leaves open, each with a receipt.

**The governing principle: the blast-radius test.** A question belongs on
this agenda only if answering it one way versus another would change the
architecture diagram — split or merge a context, flip a call from
synchronous to eventual, decide whether something is bought or built, or
move a border. If a question is answerable without moving a single box on
the diagram, it is a product or UX question, not an architecture one — note
it if it is genuinely unresolved (§6), but do not let it crowd the agenda.

This skill is the natural next step after `domain-knowledge-graph`: that
skill merges brainstorms, canvases, boards and models into one graph and
already surfaces straddlers, orphans and contradictions as findings. This
skill re-reads the same graph through an architecture lens, adds a handful
of signal categories the general-purpose graph checks don't cover (message
kinds, build-vs-buy markings, consistency windows, unconfirmed numbers), and
turns the result into an agenda a room can actually work through — one
question at a time, cited, with the options already on the table where the
artifacts show them.
 
---

## 1. Mode

**Mode: graph.** A `graph.ttl` produced by `domain-knowledge-graph` (or
compatible with its vocabulary — see `assets/dkg.ttl`) is available. This is
the reliable path: provenance, deduplication and cross-artifact merging are
already done, so a signal found here has already survived one round of
"is this real or did I just misread two boards the same way." Go to §2.

**Mode: artifacts.** No graph exists yet, only the raw artifacts (a board
photo, an Event Model, a context map, canvases, an invariants sheet, a core
domain chart). If two or more substantive artifacts are on the table,
propose running `domain-knowledge-graph` first — the signal categories
below are far more reliable once merging has happened, because half of them
*are* merge findings (straddlers, contradictions, recurring contexts). If
the user wants to proceed without it, or only one artifact exists, apply
the same signal categories by direct inspection (§3 tells you, per signal,
what to look for on the page instead of in the graph) and say plainly in
the output that this agenda has not been through the merge/dedup step, so
the same doubt may appear more than once under different names.

Either way, **never invent a question the artifacts don't evidence.** A
generic architecture checklist ("what's your uptime target?", "which cloud
provider?") is a different, non-evidence-based exercise — if the user wants
that too, do it, but label it separately (§6) so it is never confused with
what the modelling actually raised.
 
---

## 2. Running the miner (Mode: graph)

```
pip install rdflib --break-system-packages   # once, if not already present
python3 scripts/mine_signals.py graph.ttl --json /tmp/signals.json
```

The script does the mechanical half only: it walks the graph and returns
structured findings with evidence attached, one list per signal category
(see §3 for what each category means and why it matters). It does **not**
phrase questions, invent options, judge which findings are architecturally
consequential, or rank anything — that is language and judgment work, and
it happens in §4 and §5, by you, reading the JSON.

Run it once with no `--only` flag to get everything; `--only
straddlers,contradicts` narrows it while iterating. If `dkg.ttl` isn't
found beside the graph or in the working directory, the script falls back
to the copy bundled in `assets/` — it should always find one, but a warning
means subclass closure (e.g. recognising a `dkg:DomainEvent` as a
`dkg:Occurrence`) may be incomplete, and some detectors will under-report.

Read the artifact register first (`views/index.md` if `domain-knowledge-graph`
already produced one, or just `SELECT ?a ?label WHERE { ?a a dkg:Artifact ;
rdfs:label ?label }`). A graph built from one board and nothing else will
produce an agenda that is really just that board's open questions restated
— useful, but say so, the same way a one-source finding is flagged
throughout this toolkit.
 
---

## 3. The signal categories — what each one means and why it's architectural

Every category below names the concern it feeds, what the script (or, in
Mode: artifacts, a direct read of the page) is looking for, and the
question shape it seeds. Confidence follows the same three words as the
rest of this toolkit: `dkg:OnArtifact` / `dkg:Implied` / `dkg:Inferred`.

### Boundaries & ownership — who holds the data, and does the split even make sense

| Signal | What it finds | Why it's architectural |
|---|---|---|
| **Straddlers** (`straddlers`) | A Concept/Aggregate/Actor with `dkg:inContext` into two or more bounded contexts | A straddler is a shared-data decision waiting to happen: own it in one context and reference it from the other, split it into two concepts, or accept a shared kernel. Each option is a different diagram. |
| **Orphaned business objects** (`orphan_business_objects`) | Something `dkg:produces`, never afterwards `dkg:mentions`/`dkg:reads`/referenced by anything | The missing-consumer test: if nothing reads it, either the read model is undrawn (cheap to fix), a whole context is missing between producer and the real consumer, or the border is in the wrong place. All three are architecture calls. |
| **Contexts with no owned concept** (`contexts_without_owned_concept`) | A bounded context with events/commands `dkg:inContext` it but no Concept, Aggregate or BusinessObject anywhere in that same context | This is "the stretch with no aggregate" finding from the invariant and pivotal-event skills, generalised: it names the context that needs an aggregate built, and until it exists nobody can say what a service backing this context would even store. |
| **Possible duplicate contexts** (`possible_duplicate_contexts`) | Two `dkg:BoundedContext` nodes whose member sets (things `dkg:inContext` them) overlap heavily (Jaccard ≥ 0.5 by default) | Per `merging.md`'s own rule — contexts merge on responsibility, not name — a high-overlap pair drawn as two bubbles is very often one capability drawn twice. Deciding this settles whether it's one service or two before either is built. |
| **Single-source, high fan-in concepts** (`single_source_high_fanin`) | A Concept/Aggregate/BoundedContext referenced 3+ times across the graph but sourced to exactly one artifact | Whatever gets built around this concept rests on one unreviewed picture. Worth a second look before committing an interface to it. |

### Integration pattern & consistency — sync or async, and how stale is tolerable

| Signal | What it finds | Why it's architectural |
|---|---|---|
| **Unresolved message kind** (`unresolved_message_kind`) | A canvas `dkg:Message` with `dkg:messageKind "?"` | Command, query and event imply three different call shapes. A `?` means the canvas author couldn't tell — which means nobody has decided whether the caller blocks for an answer. |
| **Contradictions** (`contradicts`) | Any `dkg:contradicts` pair, whatever it's about | Always the highest-severity item on the agenda: two artifacts assert incompatible things, and an architecture built on the wrong one is expensive to unwind. Never resolve silently — report both sides verbatim, per `merging.md`. |
| **Borders with no stated timing** (`borders_without_timing`) | A `dkg:Message`'s `dkg:carries` text with no second/minute/hour/day/eventual/real-time language anywhere | A border's staleness tolerance decides whether it's a synchronous call, a cache, or a queue. If nobody wrote it down, ask before defaulting to "real-time because that's what REST does." |
| **Unwrapped external systems** (`unwrapped_external_systems`) | A `dkg:ExternalSystem` with no comment or Decision anywhere mentioning an anticorruption layer or wrapper | Every external system this graph holds either gets an ACL or doesn't; if nobody decided, the default (no ACL) is itself an architecture choice being made by omission. |

### Build vs. buy — where the investment goes

| Signal | What it finds | Why it's architectural |
|---|---|---|
| **Evolution/classification without a decision** (`evolution_without_decision`) | A Capability/Component with a Wardley `dkg:evolution` stage, a `dkg:movesTo`, or a core/supporting/generic mark from a Core Domain Chart, and no `dkg:Decision` mentioning it | Genesis/custom-built leans build; product/commodity leans buy; core leans "invest here", generic leans "buy the cheapest thing that works." A marking with no matching decision is strategy that never reached an architecture call. |
| **Classification conflicts** (`classification_conflicts`) | The same capability marked differently (e.g. core in one chart, supporting in a later one) without a formal `dkg:contradicts` edge joining the two | Per `ingest-capability-and-core-domain.md`, a capability's classification changing between two charts is exactly the kind of thing only a shared graph makes visible — and if the architecture was built on the older classification, that's worth knowing now rather than later. |

### Numbers nobody supplied — the ones a service can't be sized without

| Signal | What it finds | Why it's architectural |
|---|---|---|
| **Unconfirmed cardinalities** (`unconfirmed_cardinalities`) | A glossary `dkg:Relationship` with `dkg:cardinalityGiven false` | A derived "1..\*" is a guess wearing a business rule's clothes. Whether a real number is 3 or 3,000 can decide whether something is a field on a row or a collection needing its own storage. |
| **Placeholder numbers** (`placeholder_numbers`) | A Rule or Question whose label/comment still carries an unfilled `<n>`-style placeholder | Same reasoning, for invariants rather than cardinalities — a borrowing limit, a timeout, a retry count. Cheap to ask, expensive to hardcode a guess. |

### Unclosed doubt — pulled directly, not derived

| Signal | What it finds | Why it's architectural (sometimes) |
|---|---|---|
| **Open questions** (`open_questions`) | Every `dkg:Question`/`dkg:Hotspot` with no `dkg:answers` (directly or via a `dkg:proposedSameAs` chain to an answered one) | This is the raw agenda every other artifact in the graph already raised. Most of it is real and most of it is *not* architectural — reading each one and applying the blast-radius test is the one step in this whole skill that can't be mechanized. Keep what moves the diagram; set the rest aside per §6, don't drop it. |
 
---

## 4. From signal to question

Each JSON record has an `id`, a mechanical `detail`, and `evidence`
(artifact + locator). Turning it into an agenda row means:

1. **Apply the blast-radius test.** Would a different answer change which
   contexts exist, how two contexts talk (sync/async, push/pull), what data
   crosses a border, or what gets bought instead of built? If not, this
   signal is real but not architectural — move it to §6, don't discard the
   finding.
2. **Write the question the way the room would ask it out loud**, not the
   way the script logged it. `STRAD-3: Help request inContext 2 contexts`
   becomes *"Does Cooking Assistance or Cooking Preparation own the Help
   request lifecycle, and what does the other side keep — a reference, a
   snapshot, or nothing?"*
3. **Look for options already on the table.** If a source document already
   argues alternatives (a "Contested calls" section, a canvas's borrowed
   vs. owned split, two classification assertions), use those verbatim as
   the options rather than inventing new ones. If nothing is on record,
   say so — `"not yet explored"` is an honest option, not a gap in the
   work.
4. **Keep the evidence.** Every row cites at least one artifact + locator.
   A question with no evidence trail is indistinguishable from a generic
   checklist item, and the whole value of this agenda is that it isn't one.
5. **Do not re-ask what a Decision already answers.** `find_open_questions`
   already excludes `dkg:answers`-closed questions, but check the other
   categories too: a straddler the team already resolved in an ADR row
   should move to §5's "already settled" list, not the agenda.
---

## 5. Output — the Architecture Question Agenda

Write the agenda as a single document with this shape (tables throughout;
this is a decision agenda, not prose):

```markdown
# Architecture Question Agenda — <project>
 
**Mode: graph | Mode: artifacts.** <what went in, from which artifacts, dated>
 
## 1. What this agenda is built from
<artifact register: name, type, date, node/finding count per artifact>
 
## 2. Already settled — do not re-open
<questions/straddlers/etc. already closed by a dkg:Decision, one line each,
 so nobody spends a session re-litigating them>
 
## 3. The agenda
 
### 3a. Context boundaries & ownership
| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|----|----------|---------------------------|-----------------------|----------|------------|
| AQ-001 | ... | ... | ... | Board · event 10 | OnArtifact |
 
### 3b. Integration pattern & consistency
(same columns)
 
### 3c. Build vs. buy
(same columns)
 
### 3d. Numbers nobody supplied
(same columns)
 
## 4. Severity ranking
<the 3-5 questions to put in front of the room first, ranked by how many
 other open questions or components sit downstream of the answer — the
 same "count how many events touch it" test used elsewhere in this
 toolkit: a straddler at the center of the graph outranks a peripheral one>
 
## 5. Not on this agenda
<real findings that failed the blast-radius test (product/UX/copy
 questions), so they're visible but don't compete for the room's time here>
 
## 6. Feeding the answers back
<see §7 below>
```

Number questions `AQ-001`, `AQ-002`, … sequentially across the whole
document, not per-section — it's one agenda, and the numbering should let
someone say "let's do AQ-004 next" without ambiguity.
 
---

## 6. What this agenda deliberately leaves out

State this in the output, briefly, every time: this is not a generic
architecture checklist (SLAs, hosting, observability stack, on-call
rotation) — those are real decisions but they don't come from the
modelling artifacts, so putting them here would dilute the one thing this
document can do that a checklist can't: prove, with a citation, that the
team's own diagrams and boards already raised this exact doubt. If the team
also wants the generic checklist, offer to produce it as a clearly
separate document.
 
---

## 7. Feeding the answers back

Once the room decides an item, the natural home for the answer is a
small-ADR log row — `Date · ID · Title · Status · Author · Decision ·
Question · Options` — and the agenda is built so that its **Question** and
**Options on the table** columns can be copied straight into the log's
`Question` and `Options` columns with no rewriting. Re-ingesting the grown
log with `domain-knowledge-graph` writes the `dkg:answers` edge that closes
the loop: the next time this skill runs, that question is gone from §3 and
appears in §2 instead. This is the same mechanism `ingest-small-adr.md`
already documents ("questions still open in the graph that no Decision
touches — the agenda for the next log entry") — this skill just applies it
through an architecture-specific filter and does the prioritizing.
 
---

## 8. Worked example

`references/worked-example.md` runs the whole flow — signals in, agenda
out — against the Community Cooking board material (the same board used
throughout `domain-knowledge-graph` and the pivotal-event/invariant
skills), so you can see what a real agenda entry looks like end to end,
including a contradiction, a straddler, an unwrapped external system, and
a build-vs-buy question from a Core Domain Chart classification change.
Read it once before running this on a new graph for the first time.
