---
name: context-map-board-consistency
description: >-
  Check a Context Map against the EventStorming board it came from and report
  where the map claims what the stickies do not support: arrows reversed against
  the board's producer/consumer evidence, edges with nothing crossing them, one
  context drawn twice on the board turned into two nodes, ownership the board
  contradicts, patterns with no evidence, contracts naming payloads no event
  produces, external systems and upstreams missing, and board silences presented
  as a working system. Produces a Conformance Report: a term ledger of every noun
  by who writes it, reads it and owns it, coded findings with severity and cited
  sticky evidence, and two patch lists. Use whenever someone has a board (photo,
  Miro export, sticky list) plus a context map, integration diagram or list of
  bounded contexts and asks to check, validate, diff, reconcile or review them:
  "does our context map match the board", "did we get upstream/downstream
  right". Re-checks report fixed / new / still-open. Trigger even when nobody
  says "context map".
compatibility: >-
  Pairs with event-storming-context-mapper (which draws the map this skill
  checks) and event-storming-interpreter (which reads the board); degrades
  gracefully without either. No scripts or dependencies.
author: Annegret Junker
---

# Context Map ↔ EventStorming Board Consistency

An **EventStorming board** is what a room observed: events that happened,
commands that caused them, objects written, read models consulted, systems at
the edges. A **Context Map** is what somebody concluded from it — where the
model boundaries run, who is upstream, who translates, who breaks when the
other side changes.

The map is drawn later, usually by fewer people, often in a different tool, and
sometimes from a different artifact entirely. It drifts from the board the
moment either is touched. This skill diffs them.

**The board is evidence; the map is a claim.** That asymmetry drives every
judgement here, and it has a limit that matters more than the rule itself:
**the board is not ground truth for intent.** It is one session's snapshot of a
domain, incomplete by design, and a map may legitimately encode decisions made
after everyone went home. So an edge the board cannot support is
**unsupported**, not **wrong** — and keeping those two words apart is the whole
credibility of the report.

**This is a conformance check, not a critique of either artifact.** You are not
judging whether the map is a good map or the board a good board. If the person
wants the map's *own* quality challenged, that is
`event-storming-context-mapper` in review mode, or a Devil's Advocate pass;
offer it, don't do it here.

## Core mental model

Six things shape every finding:

1. **Split authority, and it splits sharply.** The board is authoritative on
   **observation** — who produced which object, who consulted it, which actors
   appear, which systems are pink, which policies were drawn. The map is
   authoritative on **decision** — team ownership, relationship patterns,
   integration mechanisms, whether a border is worth paying for. Neither wins
   automatically. Report the move both ways and recommend one.

2. **Direction is the one thing that is nearly mechanical.** The writer is
   upstream. An object produced by an event in context A and consumed as a read
   model by an event in context B settles direction, and a map arrow pointing
   the other way is a real defect no amount of intent explains away. Check
   direction before anything else; it is the highest-yield pass in the skill.

3. **Absence on the board is not contradiction.** A board is missing most of
   its domain. Three different findings hide behind one arrow with no sticky
   under it: the crossing happens and was never drawn, the crossing was decided
   after the session, or the crossing does not exist. Say which you think it is
   and what would settle it. A checker that reads every silence as an error
   produces a hundred findings, gets read once, and is never run again.

4. **Recurrence is where maps break most reliably.** A board is arranged by
   time, so a context serving three phases gets drawn three times. Collapsing
   those appearances into one node is the map's central job, and failing to is
   the single most common defect — it turns one supplier into three phases and
   hides the context that everything bends around. The reverse error, two
   genuinely different bubbles collapsed because they share a word, is rarer
   and worse.

5. **Publish what you did not report.** A board cannot express team ownership,
   deployment, patterns, staleness windows or anything the room never discussed.
   Maintain the allowlist in `references/checks.md` and print it as its own
   section. Showing what you deliberately let pass is what makes the findings
   you did report credible.

6. **Findings, not verdicts.** Every disagreement is two groups understanding
   one domain differently. Phrase each so one person can settle it in a
   sentence.

## Preconditions

You need **a board and a map**. If the map is missing, this is
`event-storming-context-mapper`, not this skill. If the board is missing,
say so — a map with nothing to check it against can only be critiqued, not
verified.

Ask three things if they are not obvious, then proceed:

- **Was the map drawn from this board?** A map derived from a Wardley map, an
  Event Model, a team structure or last year's architecture is not drifting
  from the board — it never came from it, and half the findings change
  meaning. Ask this first; it is the cheapest question here and the one most
  likely to reframe the whole report.
- **Which is newer?** A map newer than the board usually means the board is
  stale, not that the map is wrong.
- **Is the board complete, or one session's slice?** A board with a stated
  scope exclusion makes "missing context" a different finding from a board that
  claims to cover everything.

**If the board carries no context bubbles**, the map's nodes are the only cut
in evidence. Check the map against the raw stickies anyway — ownership,
direction, crossings and recurrence are all still checkable — and mark every
node-level finding **provisional**, stating that the cut was never drawn on the
wall. Offer `pivotal-event-boundary-finder` as the way to get an independent
cut to compare against.

## Workflow

### Step 1 — Read the board into a grid

One row per event: event · command · actor or system · business objects
**produced** · read models **consumed** · the context bubble it sits in · any
policy or hotspot on it.

Two reading rules decide the entire report:

- **Produced vs consumed is the axis of the method.** Direction, ownership and
  every edge come from it. If the notation is ambiguous — one colour used for
  both, an object with no icon — say so and mark those rows `uncertain`. A
  finding built on a misread producer is confidently backwards, and it is the
  one kind of error that destroys trust in the rest.
- **Copy the board's spelling exactly**, including its inconsistencies.
  *Catastrophy* in one place and *Catastrophe* in another is evidence about the
  ubiquitous language. Normalising it silently deletes a finding.

Flag stickies in the overlap of two bubbles, and stickies outside every bubble.

If `event-storming-interpreter` is installed, its board-reading steps give you
this grid; use them rather than re-deriving the notation.

### Step 2 — Read the map into a node/edge table

Do not diff a picture. Transcribe first — a misread arrowhead invents a
disagreement that was never there.

Nodes: name, marked internal / external / off-board, exactly as drawn. Edges:
source, target, arrowhead direction as drawn, the `U`/`D` labels if present,
pattern label, whatever the map says crosses, mechanism, staleness. Missing
cells are **`(not stated)`**, never a guess — an unlabelled arrow is itself a
finding and inventing a pattern for it hides one.

Maps arrive as pictures, as Mermaid, as a markdown table, or as prose in a
document. All are fine. Prose is the most dangerous, because direction hides in
verbs: *"planning informs cooking"* is an arrow, and *"they share the plan"* is
not a direction at all — mark it `(not stated)` and check it as one.

Show the transcription and ask for confirmation whenever anything was hard to
read. What you could not read is **unchecked**, not absent.

### Step 3 — Build the term ledger

The engine of the check, and the thing the team will screenshot. One row per
noun on the board:

| Term | Written by (board) | Read by (board) | Map says owned by | Verdict |
|---|---|---|---|---|
| Cook | Cook Profile | every context | Cook Profile | consistent |
| Meal plan | Meal Planning | *nobody* | Meal Planning | **orphan — see GAP** |
| Help request | Cooking Assistance, Cooking Help | both | two nodes | **COLL + OWN** |
| Recipe | *nobody* | 7 events, 3 contexts | *no node* | **off-board upstream missing** |
| Pictures | Media | Cooking Help, Sharing | Media | crossing not on map → **EDGE** |

Read the verdict column straight off. One writer with readers elsewhere is an
edge whose direction is already settled. Two writers is contested ownership,
and if the map picked a side silently that is a finding in its own right. No
writer and several readers is an off-board upstream the map probably lacks. No
readers is an orphan, and an orphan named in a border contract is the sharpest
finding this skill produces.

### Step 4 — Reconcile the nodes

Collapse the board's bubbles by recurrence first — the same context drawn four
times is one node with four appearances — then set the two lists side by side.
Every node on the map traces to bubbles and events, or it does not. Every
bubble on the board appears in some node, or it does not.

Watch specifically for a node imported from a **different artifact**: an Event
Model swimlane, a team name, a service in the deployment diagram. It is not
wrong, but it is not on this board, and the map should say where it came from.

**An external system or process taking part in an event — a pink sticky — is
allowed to be an ordinary bounded context on the map.** It has its own model,
its own language and its own release cycle; it is simply outside the team's
control. Never report a node for existing where the board has a pink sticky,
and never report how it was drawn — marked or unmarked, dashed or solid, named
after the vendor. The only `EXT` finding is something **missing**: a pink
sticky with no node, or an off-board upstream neither artifact drew. A stated
choice of conform, wrap or walk away is worth having, but it belongs in the
open questions, not the findings.

Codes: `NODE`, `COLL`, `EXT`.

### Step 5 — Check the edges

Per edge, in this order — direction, then existence, then pattern, then
contract, then mechanism. Pattern before direction is how a map ends up all
Partnership, and checking a contract before you know which way the arrow points
wastes the pass.

Codes: `DIR`, `EDGE`, `PAT`, `CONTRACT`, `STALE`.

Then the reverse sweep, which is the one people skip: every crossing in the
term ledger that has **no edge on the map**. Undrawn edges are usually more
valuable than wrong ones, because nobody is arguing about them.

### Step 6 — Check the language and the silences

`TERM` — words the map renamed, normalised or reconciled, and one word carrying
two models across a border without the map saying so.

`GAP` — the board's own silences that the map presents as solved: an orphan
object in a contract, a context with no aggregate given a confident border, a
policy the board never drew standing behind an edge labelled "event". A map
that hides the board's gaps reads as a description of a working system, and
that is the most expensive thing a map can do.

Full detection recipes, false-positive guards and severities for all eleven
codes are in `references/checks.md`. Read it before Step 4.

| Code | Check |
|---|---|
| `DIR` | ★ Arrow direction contradicted by producer/consumer evidence |
| `EDGE` | ★ Edge with no crossing, or a crossing with no edge |
| `COLL` | ★ Recurrence mishandled — one context as several nodes, or two as one |
| `NODE` | Map node with no board evidence, or a bubble missing from the map |
| `OWN` | Ownership the board contradicts, or contested ownership silently resolved |
| `PAT` | Relationship pattern the evidence does not support |
| `CONTRACT` | What the map says crosses vs what the board actually produces |
| `TERM` | Renamed, normalised, or one word with two models |
| `EXT` | A pink sticky with no node, or an off-board upstream nobody drew |
| `STALE` | Mechanism or staleness window the board's own clock refuses |
| `GAP` | A board silence the map presents as settled |

★ = the three nobody finds by eye, and the three worth most of the report's
length.

### Step 7 — Allowlist, severity, direction

Walk the candidate findings against the allowlist in `references/checks.md` and
move everything that belongs there. Then print the allowlist.

Severity:

- **Blocking** — the two artifacts assert different facts about the domain, or
  the map would send someone to build an integration backwards. Direction
  errors and contested ownership live here.
- **Significant** — divergence that defeats the point of having a map:
  recurrence flattened, an unsupported hub, a contract naming a payload nothing
  produces.
- **Minor** — coverage and cosmetics: an unlabelled arrow, a spelling
  normalised, a staleness window nobody supplied.

Resolution direction is **map moves**, **back to the wall**, or **ask** — and
say which you would pick. "The map should collapse Cooking Assistance and
Cooking Help into one node, or the team must state the response-time difference
that makes them two" is useful; "these differ" is not.

**Back to the wall** deserves its own emphasis. A good share of findings here
are not map defects at all — they are the board being thin, and the honest
resolution is an hour with the stickies, not an edit to the diagram.

### Step 8 — Two patch lists

Close with edits concrete enough to apply without further discussion. One list
for the map (*reverse the Preparation → Planning arrow; merge the two help
nodes; add Recipe Catalogue dashed as an off-board upstream*), one for the
board (*draw the consumer of Meal plan or accept that Shopping is missing; put
a state sticky on Help request*). This is what turns a report into half an hour
of work someone can actually do.

## Output: the Conformance Report

```
# Conformance Report — <map name> × <board name>

## 0. Scope checked
Which artifacts, which revision, what could not be read (UNCHECKED), and
the three preconditions: was the map drawn from this board, which is
newer, is the board complete or a slice. State bubbles-drawn or not.

## 1. Verdict
One line: consistent / consistent with drift / diverged / not derived
from this board. Finding counts by severity.

## 2. Board as read
The grid. Assumptions, overlaps and unreadable stickies flagged.

## 3. Map as read
Nodes and edges as transcribed, with (not stated) cells left empty.

## 4. Term ledger
Every noun × written by · read by · map's owner · verdict.

## 5. Node reconciliation
Board bubbles collapsed by recurrence, set against the map's nodes.

## 6. Findings
Ordered by severity, biggest first. Each one: ID · code · evidence
(sticky, event number, map edge) · why it matters · map moves / back to
the wall · recommendation.

## 7. Undrawn edges and missing nodes
The reverse sweep — crossings the board shows and the map does not, and
the off-board upstreams neither drew.

## 8. Allowed divergence
What you deliberately did not report, and why.

## 9. Patch list
Two lists — edits to the map, and what to take back to the wall.

## 10. Already consistent
Name what matches. It tells the team what to protect, and it is the
evidence that you checked everything rather than only what looked broken.
```

Adapt depth to the ask. "Does our map match?" wants §1, §4 and §6 plus the
patches. Always keep §0, the term ledger, §7 and §8 — the ledger is what makes
the report arguable, §7 is where the value usually is, and §8 is what stops it
being dismissed as pedantry.

## Re-check mode

"Check it again" after a revision is the most common second message this skill
sees. **Do not re-run the report from scratch** — the person has read the last
one and wants the delta.

```
## Fixed          — what closed, named by finding ID
## New breakage   — what this revision introduced
## Unchanged      — one compact paragraph, not a re-listing
```

Three patterns worth watching for, because they are what re-checks are *for*:

- **The fix that moved.** Two help nodes merged, and the same recurrence error
  now appears on the media nodes. The map was spot-fixed, not re-read.
- **Half-applied fixes that score worse.** An arrow reversed on the map but its
  contract left describing the old direction now says two contradictory things,
  where before it said one wrong thing consistently.
- **Decisions made in conversation that the artifacts have not caught up with.**
  If the person argued a position two messages ago and the new map contradicts
  it, that is the headline. They are usually unaware; the map was drawn before
  the argument landed.

Keep finding IDs stable across revisions so "F3 is still open" means something.

## Traps worth checking before you publish

- **Treating the board as ground truth.** The board is one session. Findings
  phrased as "the map is wrong" where the honest phrasing is "the board never
  drew this" will be dismissed wholesale, along with the findings that were
  right.
- **The timeline in disguise.** If the map's nodes run left to right in board
  order and every arrow means "and then", the map copied the timeline instead
  of reading it. That is one finding about the whole map, not thirty about its
  edges — say it once, at the top.
- **Counting appearances as evidence of separateness.** The board draws a
  supplier once per phase it serves. Recurrence argues that a context is
  *central*, never that it is several.
- **Normalising to compare.** Fixing spelling or reconciling *User* and *Cook*
  in your own ledger to make the diff tidy deletes exactly the translation the
  border exists to record.
- **Scoring the absence of patterns.** A map with no relationship patterns is
  incomplete, not wrong, and one `PAT` finding covers it. Do not emit one per
  edge.
- **Inventing the consumer.** Giving an orphan object a reader because a system
  "would obviously need it". Mark it undrawn and ask.
- **Checking a map against the wrong artifact.** If the map's nodes match an
  Event Model's swimlanes or a team roster better than they match the board,
  stop and say so — that is the finding, and every downstream comparison is
  measuring the wrong distance.
- **One map for everything.** Strategic, team and deployment maps answer
  different questions. A "defect" may just be a team map being checked as a
  model map. Establish which one this is in §0.

## Working with the neighbouring skills

- **`event-storming-context-mapper`** — draws the map this skill checks. When
  the patch list grows past roughly a third of the edges, redrawing beats
  patching: hand over. Its `references/pattern-catalog.md` is the authority for
  `PAT` findings and for upstream/downstream determination — read it rather
  than re-deriving the nine patterns here.
- **`pivotal-event-boundary-finder`** — when the board has no bubbles, or when
  a `NODE`/`COLL` finding turns into a real argument about where the cut
  belongs. An independent cut is the cheapest way to settle it.
- **`event-storming-invariant-finder`** — its demoted cross-context rules are
  border contracts in another notation. A rule it demoted with a staleness
  window and no edge on the map is a free `EDGE` finding.
- **`event-storming-interpreter`** — for reading the board's notation.
- **`schema-glossary-consistency` / `domain-story-glossary-consistency`** — the
  same conformance shape one artifact over. If a Visual Glossary exists, its
  terms and cardinalities belong in the §4 ledger, and a glossary term owned by
  no context on the map is a §7 finding.
- **`core-domain-chart-critic`** — a hub the board bends around, which the map
  drew as a thin node, is a candidate core domain and worth taking there.

## Reference files

- `references/checks.md` — the eleven codes in full: detection recipe,
  false-positive guards, severity default and typical resolution for each; the
  node-matching ladder; the pattern-evidence table; the divergence allowlist;
  and the severity rubric. Read it before Step 4 — it carries the detail this
  file only names, including the phrasing guidance that keeps a report from
  reading as an accusation.

## References

E. Evans, *Domain-Driven Design: Tackling Complexity in the Heart of Software.*
Boston, MA, USA: Addison-Wesley, 2003 — Part IV, "Strategic Design": the
relationship patterns and the map that records them.

V. Vernon, *Implementing Domain-Driven Design.* Boston, MA, USA:
Addison-Wesley, 2013 — "Context Maps": mapping the integrations that exist
before designing the ones you want.

A. Brandolini, *Introducing EventStorming: An Act of Deliberate Collective
Learning.* Leanpub, 2021 — the board, its notation, and the boundaries a
timeline can and cannot show.

DDD Crew, *Context Mapping* — the upstream/downstream notation and pattern
labels used here.