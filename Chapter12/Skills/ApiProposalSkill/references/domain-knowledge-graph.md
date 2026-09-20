# Working from a domain knowledge graph

Read this whenever a `.ttl` from `domain-knowledge-graph` is an input — alone
(graph lane) or beside images (mixed lane). It replaces the Context Map, the
Domain Stories and the Visual Glossary as *sources*; it does not change what
Steps 1–8 produce.

## Contents

1. The one thing to know: the graph has no context map in it
2. Run the extractor
3. Which section feeds which step
4. The border rungs, and what each one is allowed to claim
5. Graph confidence is not sync/async confidence
6. Terminology and shape from the graph
7. Journeys from the graph
8. Mixed lane — a graph plus images
9. What a graph cannot supply
10. When the extractor's output looks wrong
11. Worked example

---

## 1. The graph has no context map in it

The `dkg:` vocabulary has classes for a story, a glossary, a board, an Event
Model, a canvas, an ADR log — and none for a context map. There is no
`dkg:ContextMap` and no upstream/downstream edge. The map is **implicit**: it is
whatever those other artifacts say crosses from one `dkg:BoundedContext` to
another. So in the graph lane Step 1 is not "copy the map's borders into a
ledger" but "derive the borders, and say what each one rests on".

That is a strength, not a workaround. A drawn map is one artifact's opinion; a
border the graph derives can show that a canvas, a board *and* a story all
point at it — or that only one sentence in one story does.

If the team did draw a context map and it was never ingested, that is the mixed
lane (§8): use the picture too.

## 2. Run the extractor

```bash
pip install rdflib --break-system-packages   # once
python3 scripts/graph_inputs.py <graph.ttl> --json /tmp/graph_inputs.json
python3 scripts/graph_inputs.py <graph.ttl> --context "Rack management"   # narrow to one context's borders
```

It prints seven Markdown sections and never writes to the graph. Run it rather
than reading the Turtle by eye: border derivation means joining every edge's
two ends to their contexts, and a graph of a few hundred nodes is past the
point where that is reliable by hand. Show the user §0–§2 of its output before
going further — a wrong border is cheapest to catch here.

## 3. Which section feeds which step

| Extractor section | Stands in for | Feeds |
|---|---|---|
| §0 What the graph holds / cannot supply | the "did you bring stories? a glossary?" asks | Inputs; report §0 and §9 — **carry every gap line over** |
| §1 Bounded contexts | the map's nodes | Step 1; landscape diagram nodes (off-board ends get `offboard`) |
| §2 Border candidates | the map's edges and their labels | Step 1 ledger, one row per candidate |
| §3 Journeys | the Domain Stories | Step 2 story rung; Step 6 |
| §4 Terminology and shape | the Visual Glossary | Step 1 `Glossary term` column; Steps 4–5 names and shapes; report §7 |
| §5 Decisions and principles | the architecture graph | Step 2 top rung; Step 3; `(principle conflict)` flags — rules in `architecture-graph.md` apply unchanged, it is the same file |
| §6 Open questions | — | report §9; a question that *is* the sync/async call for a border goes beside that border in §2 |
| §7 Rules per context | an invariants sheet | failure cases worth naming beside a Step 6 journey |

## 4. The border rungs, and what each one is allowed to claim

| Rung | What the graph holds | Direction | Type | Put in the ledger as |
|---|---|---|---|---|
| **B1** | a canvas `dkg:Message` with `dkg:from` / `dkg:to` | drawn | `cmd`/`qry`/`evt` verbatim, or `?` | a border. "on both canvases" is the strongest a graph border gets; "only on X's canvas — Y's canvas does not claim it" is a finding for §9 |
| **B2** | an Event Model translation slice whose members sit in two swimlanes | read from member order — **confirm** | `?` | a border, direction flagged |
| **B3** | `reactsTo` / `triggers` / `reads` whose two ends are in different contexts | from the edge | `evt` / `cmd` / `qry` | a border, `(implied by the board)` |
| **B4** | sentence *n* in one lane, *n+1* in another, and nothing on B1–B3 for that pair | story order | `?` | a border, `(story handoff only)` |
| **B5** | a glossary arrow from a context's own term to a term another context owns | holder → owner | `?` | a border, `(glossary reference only)` |

Rules that keep this honest:

- **One ledger row per candidate, rung kept.** Add a `Rung` column to the Step 1
  ledger in the graph lane. It is the graph-lane equivalent of "is this on the
  map or did I infer it".
- **A `?` stays `?` in Step 1.** Type it in Step 2 the normal way — from the
  story sentence at the crossing, then the type default — and say that you did.
  A B5 row is usually a `qry` (the holder needs to look the term up) and a B4
  row is whatever the sentence's verb makes it, but those are *your* readings,
  so they carry `inferred`, not `stated`.
- **The extractor folds duplicates only when it is sure** (same ends, same
  label, or same concept and same type). Two rows that look like the same
  crossing in different words — a B3 *Bicycle returned* and a B1 *Bicycle
  checked in* — are yours to judge: merge them in the ledger and name both
  evidences, or keep both and ask.
- **Straddlers are not border evidence.** A node with two `dkg:inContext` is
  skipped for B2–B3 on purpose; it is a modelling question, and §4 of the
  extractor output marks it.
- **Pending merges change the map.** A row noted `NOT A BORDER if 'A' = 'B' is
  confirmed` connects two contexts the graph holds a `dkg:proposedSameAs`
  between — typically a story lane and a board bubble with different names. Ask
  once ("is *Docking* the same context as *Rack management*?"). If yes, drop
  that row and re-home the merged context's other borders; if unanswered, keep
  the row out of Steps 4–5 and list it in §9. Never sketch an API between two
  names for one context.
- **Dropped nodes.** `DROPPED in a later revision` means a redraw of the source
  no longer shows it (`dkg:absentFrom`). Leave it out of the sketch and say so.

## 5. Graph confidence is not sync/async confidence

Two different scales; do not collapse them.

- `dkg:confidence` on the evidence node says how sure the graph is that **the
  border exists**: `on-artifact` (someone drew it), `implied`, `inferred`.
- Step 2's `graph` / `principle-default` / `stated` / `inferred` / `default`
  says how sure this skill is about **sync vs async**.

Carry the first into the ledger as its own column (`Border confidence`). A
border that is `inferred` — it came from an analysis, or from a canvas that
needed a receiver — gets `(inferred border)` on every operation or channel
sketched from it, and a line in §9. It can still have a `stated` sync/async
verdict; both facts are true at once.

How Step 2's rungs read in the graph lane:

1. **Decision / principle** — extractor §5. Only rows marked `Current? yes`;
   only principles marked testable. `Names contexts` is a text match to get you
   to the right rows, not proof the decision is *about* that border — read the
   decision and state the match (`architecture-graph.md`, "Matching").
2. **The map already says** — rarely available. `dkg:messageKind` is a message
   *type*, not a mechanism: `evt` on a canvas does not mean a broker. This rung
   fires only when a Message's `carries`/comment, or the canvas artifact's own
   assumption lines, state a mechanism in words ("every dashed arrow is an
   event on the bus", "via ACL, REST"). Quote it.
3. **Staleness** — not in the vocabulary. Silent unless a comment states one.
4. **The story** — extractor §3: find the sentence where `Crosses` is filled,
   then read the *next* row. Same actor using the result → sync. A different
   actor, a time word ("the next morning"), or a new trigger → async.
5. **Type default** — as always, and as always marked `(default)`.

Expect more `default` verdicts from a graph without canvases than from a
hand-drawn map with mechanisms on its arrows. Say so; do not compensate by
reading more into rung 2 than is there.

## 6. Terminology and shape from the graph

Everything in `visual-glossary.md` applies; the graph adds distinctions a
picture cannot carry.

- **`skos:prefLabel` is the term.** `Also spelled` (altLabels) are the other
  artifacts' wordings — *Bike*, *Booking*. Use the prefLabel in every name and
  list each altLabel that a story or message actually used in report §7. The
  extractor already resolved story wording to the concept, so you do not have
  to guess that *Booking* means *Reservation*; the graph says so.
- **Kind decides what gets an operation.** `entity` / `aggregate root` → may
  have its own operation. `value object` → inline field only. `term
  (unmarked)` → the glossary never said; treat as inline and flag
  `(unmarked term)`. A concept listed as `… — not a glossary term` (a board's
  business object, a story's work object) has **no** shape authority: name from
  it, mark the shape `(unconfirmed shape)`.
- **Cardinality `DERIVED`** (`dkg:cardinalityGiven false`) was worked out by
  whoever ingested the glossary, not drawn by the team. Use it for
  singular-vs-array, but append `(derived cardinality)` and never carry its
  bound over as a note. A `contested` relationship has two incompatible
  cardinalities in the graph — show both, pick neither.
- **`Renamed across a border to`** (`dkg:renamedTo`) is the most useful line in
  the section for API work: *Rider* in one context is *Customer* in the next.
  A crossing between those two contexts carries the **provider's** term in its
  operation and the consumer translates — sketch the field once, under the
  provider's name, and add `(consumer calls this <other term>)`. That is an
  anticorruption layer in one parenthesis, and it belongs in report §7.
- **Owner context** tells you the provider of a `qry`: the context that owns
  the term answers questions about it. A B5 row pointing the other way is worth
  a second look.
- **Straddlers and pending term merges** go to §9 as-is.

## 7. Journeys from the graph

Extractor §3 gives one block per `dkg:DomainStory`, already marked *candidate
Arazzo file* or not. Still apply Step 6's judgment:

- A crossing into a pending-merge lane is not a crossing until the merge is
  answered (§4). Recount after the answer; a journey can drop to zero.
- `(no lane)` sentences: the story was ingested without lanes, so the graph
  cannot place them. `(via actor's lane)` placements are weaker than the
  sentence's own — say which you relied on.
- A story whose sentences were split on ingest (`2a`/`2b`) keeps both halves
  as steps.
- Step sketch lines cite the ledger row: *"#3 → #4: Bicycle distribution →
  Rack management — ledger row 3, sync step"*.

## 8. Mixed lane — a graph plus images

Someone hands over the graph *and* a photo: a context map that was never
ingested, a new story, a redrawn glossary.

1. Run the extractor. Compare each image against §0's artifact list by label
   and date. **Already in the graph** → the image is a convenience; work from
   the graph and use the picture only to check a locator. **Not in the graph**
   → read it exactly as the artifact lane would.
2. Build the ledger from the graph first, then add the image's borders. Add a
   `Source` column: `graph`, `image`, or `both`.
3. **Where they disagree** — direction, type, a border one has and the other
   lacks, a spelling, a cardinality — **put both in the row and flag it.**
   Neither wins by default: the graph is what the team ingested and agreed; the
   image may be newer, or may be one person's sketch. Ask which is current when
   the disagreement changes a verdict; otherwise report it in §9.
4. A drawn map's **mechanism and staleness labels** are exactly what the graph
   cannot hold (§9), so an un-ingested context map is the most valuable image
   to have beside a graph. Ask for one if the team has it.
5. Close by offering to ingest the new images with `domain-knowledge-graph`, so
   the next run is graph-only. Do not write to the `.ttl` from this skill.

## 9. What a graph cannot supply

The extractor prints these under "What it cannot supply". Every line goes into
report §9, reworded if you like, never dropped.

- **Staleness windows and integration mechanisms** — no property for either.
- **Relationship patterns** (Open Host, Conformist, ACL…) — at most a `via ACL`
  comment.
- **A border nobody wrote down** — a graph of three glossaries and no board has
  B5 rows only. That is a vocabulary map, not a traffic map; say so, and ask
  for a context map or stories (mixed lane) before sketching operations.
- **Fewer than two contexts, or no border on any rung** — stop. Offer
  `event-storming-context-mapper` / `domain-story-context-finder` and an
  ingest, or fall back to the artifact lane.

## 10. When the extractor's output looks wrong

The script reads the vocabulary as `domain-knowledge-graph` documents it. A
graph emitted with different habits can defeat it: lanes attached to actors
rather than sentences, translation crossings reified as `dkg:Assertion` with no
slice, messages without `dkg:from`. Signs: contexts listed but zero borders
while a canvas is in §0; a story with every sentence `(no lane)`.

Then look directly — the JSON dump, or a short query:

```sparql
PREFIX dkg: <https://w3id.org/dkg/ns#>
SELECT ?a ?p ?b ?ca ?cb WHERE {
  ?a ?p ?b . ?a dkg:inContext ?ca . ?b dkg:inContext ?cb .
  FILTER (?ca != ?cb && ?p NOT IN (dkg:inContext, dkg:source, dkg:precedes))
}
```

Every row is an edge with its ends in two contexts — raw border evidence. Add
what you find to the ledger with rung `B3*` and say the extractor missed it.

## 11. Worked example

`assets/example-graph.ttl` (bicycle sharing) exercises every rung:

```bash
python3 scripts/graph_inputs.py assets/example-graph.ttl
```

What a good run does with it:

- **Row "Reserve a bicycle", B1, on both canvases** → `sync`, confidence
  `graph`: SADR0004 is Adopted, names both contexts, and decides
  request/reply. It `OVERRIDES` P1 — an *acknowledged* override, so report it
  under Architecture context, not as a `(principle conflict)`.
- **"Bicycle unlocked", B1, one-sided** → `async`, `principle-default` (P1,
  testable, no conflicting decision); §9 notes Bicycle distribution's canvas
  never claims it.
- **"Bicycle returned", B3** → `async`; story sentence 5's "the next morning"
  corroborates. Payload field is `Bicycle`, never `Bike`.
- **"Charge fare" to the off-board Payment provider** — type `?`, border
  `inferred`: sketched, flagged `(inferred border)`, and first in the open
  questions. `Fare` is a value object: an inline field, no `/fares` resource.
- **Rack management ↔ Docking, B4** — a pending merge. Asked about, not
  sketched. If confirmed, the journey has two crossings, not three.
- **Rider → Customer** — the Billing-facing payload says `Customer`, with
  `(Bicycle distribution calls this Rider)`.
- **SADR0002 "gRPC everywhere" is Superseded** — history. Step 3 still asks
  which synchronous technology to sketch toward.