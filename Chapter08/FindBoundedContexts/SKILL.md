---
name: pivotal-event-boundary-finder
description: >-
  Propose pivotal events as the borders between bounded contexts — screening a
  domain-event timeline for the events that change the state of the whole
  process (phase change, irreversibility, handover of people, language shift,
  commitment, change of clock), vetoing each candidate with the
  narrow-interface test, then drawing the divider lines, naming the segments
  between them as candidate contexts, and specifying what crosses each border.
  Use whenever someone has an event timeline — an EventStorming board photo, a
  Miro export, a past-tense event list, or a process description — and wants the
  cut points: "which of these are pivotal events", "where do we draw the divider
  lines", "mark the boundaries on our event flow", "review the contexts the team
  drew", or "where does this process break into contexts or services". Tests
  scope-exclusion claims and finds contexts missing from the board. Trigger even
  when nobody says "pivotal event" or "bounded context". Grounded in Brandolini
  and Evans.
---

# Pivotal Event Boundary Finder

A **pivotal event** is a domain event after which the business is doing
something else. Not a big event, not a loud one — one that changes the state of
the *whole process*, so that the people, the vocabulary, the pace and the rules
on the far side are different from the near side. On an EventStorming board it
is drawn as a vertical divider running down the wall through the timeline.

Those dividers are the cheapest boundary evidence a domain gives you: the
business has already decided that this is where one thing ends and another
begins. This skill finds them, argues for each from the events actually on the
wall, and turns the accepted ones into **candidate bounded-context borders**
with a named interface at each crossing.

Two commitments make the output trustworthy:

- **Do not invent events.** A thin stretch of timeline is evidence about how
  well the domain is understood, not a gap to fill. A divider drawn through
  invented events hardens a fiction into an architecture.
- **Say what this method cannot see.** A timeline cut finds borders that run
  *across* the flow. Contexts that run *underneath* it — identity, pricing,
  notification, catalogue — have no pivotal event and will be missed by
  construction. Naming them as missing is part of the deliverable (§6).

## What makes an event pivotal

Six tests fire, one test vetoes. Full diagnostics, evidence rules and
anti-signals: `references/pivotality-tests.md`.

1. **Phase** — does the state of the *whole process* change? You should be able
   to say "before this we are doing X; after this we are doing Y" without
   mentioning any single record. A change in one aggregate's status is not a
   phase change.
2. **Irreversibility** — can you undo it, or does going back require a
   *compensating business action* with its own name (cancellation, refund,
   recall, withdrawal, return)? Compensation instead of undo is a strong signal.
3. **Handover** — do the people change? A different department, a different
   system of record, a different expert you would have to invite to the
   workshop. The board's own actor stickies are the evidence.
4. **Language** — does a noun get re-modelled on the far side? *Cart* becomes
   *Order*, *Candidate* becomes *Employee*, *Order* becomes *Shipment*. A new
   identifier being minted is the visible form of this and is worth hunting for
   explicitly.
5. **Commitment** — is a promise created that someone can be held to? Money
   moves, an obligation starts, a clock someone can sue over begins running.
6. **Clock** — does the tempo change? A queue, a batch, a wait for a human, a
   nightly run. Where a wait is already normal, eventual consistency is already
   accepted, so a boundary here costs the business nothing.

**The veto — the narrow-interface test.** After this event, how much of the
upstream detail does the downstream work actually need? If a short payload
suffices, the border is real. If downstream keeps reaching back for upstream
detail — scorecards, line-item history, the full case file — the event is a
milestone inside one context, however dramatic it looks. This test overrides the
other six, because it is the one that predicts what the split will cost.

A defensible divider: **phase fires, at least two of tests 2–6 fire, and the
narrow-interface test does not veto.**

## Mode: propose or review

Check what arrived and say which mode you are in.

- **No dividers marked** → *propose* them. Work the whole method.
- **Dividers already drawn** (vertical lines, phase labels, swimlane breaks) →
  *review* them. Same analysis, but the output argues with the wall: which
  dividers the evidence supports, which is a milestone in costume, and which
  unmarked event has a better claim than one that was marked. Keep their names;
  argue for a rename only where the current one actively misleads.
- **Context bubbles drawn but no dividers** → the bubbles are the claim; score
  the timeline anyway and report where your dividers agree with their outlines
  and where they cut through one. **Read repeated bubbles with the same name as
  one context appearing several times, not as several contexts** — a team that
  drew *Help* four times has already found a cross-cutting context, and counting
  the repetitions as segments would bury their own finding.
- **Several boards, or branches running in parallel** → screen each branch's own
  line, then check whether the same pivotal event appears in more than one. An
  event that is pivotal in two independent flows is the strongest border you
  will find.

### Reviewing a scope exclusion

A board often arrives with a claim attached: *that stretch isn't in the system —
it's manual / offline / happens in the kitchen / another team owns it.* Treat
the claim as testable, not as given, because a stretch of events left outside
every boundary looks identical whether it is genuinely out of scope or merely
undiscovered.

The claim usually confuses **the activity** with **the software that accompanies
it**. Physical or manual work being outside the system does not put the context
outside it: if the app shows the steps, tracks where someone is, or lets them
ask for help about *this* step, there is a session-shaped context in scope even
though nobody automates the frying.

Test the exclusion with the diagnostics in Step 7 — a consumer reaching into the
excluded stretch, an aggregate minted at its border with nothing to read it, and
a total absence of aggregates within it. If those fire, say so and propose the
missing context; if none fire, endorse the exclusion explicitly, since a
confirmed scope edge is a useful result.

## Workflow

### Step 1 — Establish the event line

Normalise whatever arrived into one ordered list of domain events, past tense,
in business order. Record alongside each: the actor or system, the aggregate or
record it touches, and any hotspot attached to it. Mark explicitly:

- **loops** — events that recur (*Interview held*, *Item added to cart*)
- **branches** — alternative outcomes (*Application rejected*)
- **parallel strands** — events that do not depend on the previous one
- **events whose position in the sequence is a guess**

Loops and branches matter more here than in any other boundary method: **a
divider must be crossed exactly once.** An event inside a loop cannot be one.

**Separate provenance markers from domain meaning.** Boards carry marks about
*who put a sticky there* — AI-proposed, imported from another board, added after
the session, written in a different hand or colour — and these share a visual
channel with marks about *what the event means*. An icon you read as "raised
automatically by the system" may mean "suggested by an assistant", which says
nothing about the domain. Confirm any unusual marker before building an argument
on it, and be suspicious when the same marker appears on events in otherwise
unrelated parts of the board: provenance scatters, domain semantics cluster.
Never let an unconfirmed marker carry a boundary — find a second, structural
reason or drop the point.

For a busy or hard-to-read board, show the event line and ask for confirmation
before scoring. Misreadings are cheap to fix here and expensive later.

### Step 2 — Screen out the ineligible

Before scoring, drop candidates that cannot be dividers, and say why in one
line each — the screening is itself a finding:

- technical or UI events (*Record saved*, *Email sent*, *Message published*)
- events inside a loop or that recur later in the flow
- the first and last events on the board — those are the edges of scope, not
  internal borders (unless the board deliberately starts mid-process)
- events that are consequences of another event with no work between them —
  score the one the business names, not its echo

### Step 3 — Score the survivors

One row per surviving candidate. This table is the argument:

| # | Event | Phase | Irrev. | Handover | Language | Commit | Clock | Narrow interface | Verdict |
|---|-------|-------|--------|----------|----------|--------|-------|------------------|---------|

Fill cells with the *evidence*, not a tick — "recruiters → HR ops + IT", "Cart
ceases to exist; Order minted", "compensation = withdrawal letter". A tick you
cannot expand into evidence is a guess.

### Step 4 — Choose the dividers

Rank by strength and apply the ratio: expect roughly **one divider per 5–12
events**; a 30–60 event Big Picture board usually yields **3–6**. If more than
about one event in four is coming out pivotal, you are marking milestones —
re-run the narrow-interface test on the weakest half.

Where two adjacent candidates both score well (*Offer accepted* and *Contract
signed*), only one is the border. Pick the one the business treats as the point
of no return, and record the other in §7 as the contested call.

State the divider convention plainly, because it decides who owns the event:
**the pivotal event is produced by the upstream context and consumed by the
downstream one; the line is drawn immediately after it.**

### Step 5 — Name the segments

Each stretch between dividers is a candidate bounded context. Name it for the
**business capability**, in the domain's own words. Two checks:

- Name it without *Pre-*, *Post-*, *Before*, *After*, or any word from the
  divider event itself. If you cannot, you have named a phase, not a context.
- If the honest name is *Miscellaneous* or *Processing*, the cut is wrong.

Per segment write: a one-line responsibility, the events it holds, the
aggregates it **owns**, the actors and systems in it, and the terms it owns.

### Step 6 — Specify each border

A divider is only useful once you know what crosses it. For each one record:

- **the contract** — the pivotal event's payload: exactly what downstream needs
- **what deliberately does not cross** — the detail that stays upstream. This
  list is the evidence that the narrow-interface test passed, so write it out.
- **direction and relationship** — customer/supplier, published language,
  conformist, anticorruption layer, separate ways
- **consistency** — how long downstream may lag, in business terms

### Step 7 — Say what the timeline cannot see

Non-negotiable section. A pivotal-event cut finds sequential borders and is
blind to everything else:

- **Missing consumers — run this test on every divider.** An aggregate minted at
  a border must have a named consumer on the far side. If the artefact the
  pivotal event produces is read by nobody — a settled plan, an approved
  application, a signed contract that nothing downstream touches — then either
  the divider is wrong, or **a context is missing from the board**. This is the
  one detector here that finds a context nobody drew, so state the result
  explicitly even when it passes.
- **Stretches with no aggregate.** A run of events where no aggregate is ever
  touched is under-explored, not empty. Absent and undiscovered look identical;
  say which you think it is and what would tell them apart. Corroborate before
  concluding: a context elsewhere reading a read model produced in that stretch
  is good evidence that something in there is real.
- **Cross-cutting contexts** — identity, pricing, notification, catalogue,
  compliance. They serve every segment and have no pivotal event. List the ones
  visible in the board's actors, systems and read models. A context the team has
  already drawn several times over is one of these; name it once and say so.
- **Recurring contexts** — a context that appears on both sides of a divider
  (rejected candidates flowing back into sourcing). The divider is still a
  phase border, but the context is a supplier to both sides, not two contexts.
- **Straddling aggregates** — any aggregate whose lifecycle crosses a divider.
  The most expensive mistake available here. Either move the events, or split
  the model in two with the pivotal event between them, and say which.

### Step 8 — Show what was contested

- **Near misses** — every strong candidate you rejected, with the test that
  killed it. In a 20-event board expect two or three.
- **Coarser cut** — which divider to drop first, and what is lost.
- **Finer cut** — where a segment would split if it grows, and what it costs.
- **What would settle it** — the concrete question for a domain expert. "If a
  signed contract is voided in week one, does recruiting reopen the requisition
  or does HR handle it?" decides a border; the wording matters more than your
  guess at the answer.
- **Hotspots** — copy every red sticky through verbatim. They cluster on
  dividers, because that is where the disagreements live.

## Output: the Pivotal Event Cut

```
# Pivotal Event Cut — <board or process name>

## 1. Event line as read
Ordered events with actor · aggregate · loop/branch marks · assumptions flagged.

## 2. Screening
Candidates removed before scoring, one line of reason each.

## 3. Scoring
The six tests plus the narrow-interface veto, per surviving candidate, with
evidence in the cells.

## 4. The dividers        (or: Review of the dividers drawn)
Per accepted pivotal event: the evidence, and what changes on the far side.

## 5. Segments as candidate contexts
Per segment: name · responsibility · events · aggregates owned · actors · terms.
Plus the divider strip — the redraw spec:
| # | Event | Segment | Divider after? |

## 6. Border contracts
Per divider: what crosses · what stays · direction · relationship · consistency.

## 7. What the timeline cannot see
Missing consumers per divider, stretches with no aggregate, cross-cutting
contexts, recurring contexts, straddling aggregates. Plus, when the board came
with a scope-exclusion claim: whether the exclusion survives the tests.

## 8. Contested calls & alternatives
Near misses, coarser cut, finer cut, the questions that would settle them,
hotspots verbatim.
```

Adapt depth to the request. "Just mark the pivotal events" gets §3, §4 and a
short §8 — but never drop §7, because a cut that silently omits the cross-cutting
contexts reads as complete when it is not.

## Traps worth checking before you publish

- **The loud event** — everyone argues about it, nothing downstream changes.
  Argument marks a hotspot, not a border.
- **The reporting milestone** — it gets celebrated, escalated or dashboarded,
  but the same people carry on with the same vocabulary.
- **The gate** — an approval or check that blocks progress inside one phase
  (*Background check cleared*). Gates are rules; dividers are transitions.
- **The looped event** — cannot be a divider, however pivotal it feels.
- **The orphaned artefact** — a divider that mints something nobody reads. Fix
  the divider or find the missing context; do not publish the cut as it stands.
- **Reading provenance as semantics** — an icon, colour or hand that records who
  added the sticky, mistaken for what the event means.
- **A divider per phase label** — the team's existing swimlane titles are prior
  art, not evidence. Score them like everything else.
- **Chopping every noteworthy event** — twelve events, seven dividers. That is a
  timeline with extra lines.
- **Dividers where the board is thin** — no actors, no aggregates. Say the board
  is under-explored there rather than drawing a confident line through it.
- **The aggregate straddle** — see §7. Check it last, always.

## Working with the neighbouring skills

- Boundaries from the *full* board grammar (commands, aggregates, read models,
  policies) rather than the timeline alone → `event-storming-context-finder`.
  That skill is the thorough cut; this one is the fast, timeline-first cut, and
  the two should be reconciled when both are available. Agreement between them
  is strong evidence; disagreement is worth a paragraph.
- A buildable brief rather than boundaries → `event-storming-interpreter`.
- Only a rough process description, no events yet → `event-storming-seeder`
  first, then come back.
- The team also has domain stories → `domain-story-context-finder`; treat
  agreement across notations as the strongest evidence available.
- A **Visual Glossary** exists → its bounded-context colouring is prior art.
  Reuse its exact terms rather than competing with them.
- Segments to be marked core/supporting/generic →
  `core-domain-chart-author` renders them, `core-domain-chart-critic` challenges
  the placements.

## Reference files

- `references/pivotality-tests.md` — the six tests and the veto in full: the
  diagnostic question, what counts as evidence, strength alone, anti-signals and
  worked micro-examples for each; the scoring rubric; granularity guidance by
  board size; the divider-ownership convention; and the relationship patterns
  available at a border.
- `references/worked-example.md` — a 22-event recruiting board worked end to
  end in propose mode: screening, scoring, two accepted dividers, three rejected
  near-misses, border contracts, and the cross-cutting contexts the timeline
  missed. Read this first when unsure how deep to go.

## References

A. Brandolini, *Introducing EventStorming: An Act of Deliberate Collective
Learning.* Leanpub, 2021.

E. Evans, *Domain-Driven Design: Tackling Complexity in the Heart of Software.*
Boston, MA, USA: Addison-Wesley, 2003.

V. Vernon, *Implementing Domain-Driven Design.* Boston, MA, USA:
Addison-Wesley, 2013.