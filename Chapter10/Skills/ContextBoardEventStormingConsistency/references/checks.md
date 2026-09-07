# Checks — detection, guards, severity

Contents:

1. The node-matching ladder
2. The eleven codes, in the order they are run
3. The pattern-evidence table
4. The divergence allowlist
5. The severity rubric
6. Phrasing that survives contact with the team

---

## 1. The node-matching ladder

Before any edge can be checked, each map node must be matched to board
evidence. Work down the ladder and stop at the first rung that fits; record the
rung, because it decides whether a difference is a finding or a synonym.

| Rung | Example | Treat as |
|---|---|---|
| **Exact** | map `Meal Planning` = bubble `Meal Planning` | matched |
| **Case / spacing variant** | `Cook Profile` / `cook profile` | matched, note once |
| **Synonym the team uses** | map `Community Help` = bubble `Cooking Help` | matched, raise `TERM` (minor) |
| **Collapse** | one map node covering two bubbles | matched, check `COLL` |
| **Split** | two map nodes covering one recurring bubble | matched, raise `COLL` |
| **Traceable to another artifact** | map `Notification` = an Event Model swimlane | **not matched to this board** — `NODE` |
| **Unmatched** | node with no bubble, no events, no objects | `NODE` |

The fifth and sixth rungs are where most of the value is. A node that matches a
*different artifact* better than it matches the board is not an error — it is a
provenance finding, and it changes what the whole report is measuring.

---

## 2. The eleven codes

Run in this order. Direction first, because everything downstream depends on
it; language and silences last, because they need the ledger complete.

### `DIR` — direction contradicted

**Detect.** For every map edge, find the object that justifies it in the term
ledger. The context that **produces** it is upstream. Compare with the
arrowhead as drawn (or the `U`/`D` labels, which win over arrowhead style when
both are present and disagree — say so).

**Guards.**
- An edge may be justified by more than one object, and they may point opposite
  ways. That is not a direction error; it is **mutual dependency**, and it is
  either a genuine Partnership or a boundary defect. Route it to `PAT`.
- Conformist and Anticorruption edges are often drawn from the *downstream*
  side by convention — the arrow shows who is protecting themselves. Check the
  label before calling the arrow reversed.
- A read model replicated into the consumer still leaves the writer upstream.
  Replication is a mechanism, not a direction.

**Severity.** Blocking by default. A reversed arrow is the one defect on a map
that reliably makes someone build the integration backwards.

**Typical resolution.** Map moves. If the team insists the arrow is right, the
question is which object they think crosses — and the answer is usually an
object the board never drew.

---

### `EDGE` — an edge with no crossing, or a crossing with no edge

**Detect.** Two sweeps.

*Forward:* for each map edge, name the concrete thing that moves — an object
written in one and read in the other, an actor, a policy whose event and
command sit in different contexts, a shared external system. An edge with none
of these is unsupported.

*Reverse:* for each ledger row where the writer and a reader are in different
contexts, find the map edge. If there is none, the map is missing an edge.

**Guards.**
- Unsupported is not false. Rank the three explanations explicitly: drawn
  nowhere but real, decided after the session, or not a crossing at all.
- Timeline adjacency is not a crossing. Two contexts that follow each other
  with nothing passing between them is a *gap* (`GAP`), and one of the most
  informative findings a board produces.
- An edge justified only by "both read the same off-board thing" is not an edge
  between them. Both get an edge to the off-board upstream instead.

**Severity.** Unsupported edge: significant. Undrawn edge carrying money,
consent, or safety-relevant data: blocking.

**Typical resolution.** Ask, then back to the wall. The reverse sweep almost
always resolves *back to the wall* — the crossing is real and the board never
drew its consumer.

---

### `COLL` — recurrence mishandled

**Detect.** Count the appearances of each context on the board timeline, then
count its nodes on the map. `n` appearances → 1 node is correct. `n`
appearances → `n` nodes is the classic failure. Two distinct bubbles → 1 node
is the rarer inverse.

Evidence that two clusters are **one** context: same commands, same objects
written and read, overlapping actors, same external systems, same shape. Any
three of those and the burden shifts to whoever wants two.

**Guards.**
- Two clusters can be genuinely two contexts if a **non-functional** demand
  differs by an order of magnitude — response time being the usual one. That is
  an argument about a number, not about the model, and the number is usually
  not on the board. Name it as the one condition that would make the split
  real, and ask for it.
- Do not collapse on name alone. Two bubbles both called *Sharing* that write
  different objects and serve different actors are two contexts with one bad
  name — that is `TERM`, not `COLL`.

**Severity.** Significant, rising to blocking when the split context owns an
aggregate whose lifecycle then crosses the invented border — that is a standing
invitation to implement one thing twice.

**Typical resolution.** Map moves. Merge, and record the appearances as
evidence of centrality rather than as nodes.

---

### `NODE` — node with no evidence, or bubble with no node

**Detect.** The ladder in §1, both directions.

**Guards.**
- A node marked *off-board* or *external* by the map is not unsupported — it is
  the map doing its job. Check `EXT` instead.
- A missing node may be a deliberate scope exclusion. If the map states one,
  honour it and list the bubble under allowed divergence.
- A node traceable to another artifact gets its own phrasing: not "no
  evidence", but "evidence from a different artifact — say which one governs".

**Severity.** Unmatched node: significant. Missing bubble: significant. Node
from another artifact: significant, and it is usually the report's headline.

---

### `OWN` — ownership the board contradicts

**Detect.** From the ledger. Three shapes:

1. The map assigns a term to a context that **never writes it** on the board.
2. Two contexts write the same term (**contested ownership**) and the map
   silently gave it to one.
3. The map assigns a term **no context writes** — an off-board concept given a
   local owner.

**Guards.**
- Writing a *copy* is not owning. A context that snapshots a value at a border
  and stores it is a legitimate replica; ownership stays with the writer of
  record. Ask whether the second write creates or copies before calling it
  contested.
- Roles differ from ownership. Two contexts writing *Help response* for two
  different responders is one aggregate with two writers, not two owners.

**Severity.** Contested ownership: blocking — every edge drawn around it is
wrong until resolved. Silent resolution of a contest: blocking, because the map
looks settled and is not.

**Typical resolution.** Ask. This is the one finding class where the board
genuinely cannot decide, and the map may be recording a decision that was made.

---

### `PAT` — pattern the evidence does not support

**Detect.** Per edge, hold the pattern against the evidence table in §3. The
four recurring offenders:

- **Partnership** without mutual crossings on the board. Almost always the
  answer when direction was never settled.
- **Shared Kernel** chosen to avoid deciding ownership — the highest ongoing
  cost of any pattern, since both sides must coordinate every change.
- **Anticorruption Layer** with no model difference named. An ACL that
  translates nothing is a proxy.
- **Conformist** where the downstream demonstrably has its own model of the
  term. Conformist means adopting the upstream's model wholesale.

**Guards.**
- Patterns are decisions, and the board is not where decisions live. If the map
  has patterns and the board has no evidence either way, that is allowlisted —
  not a finding.
- A map with no patterns anywhere is one `PAT` finding about the map, not one
  per edge.

**Severity.** Minor by default. Significant for Partnership and Shared Kernel,
because both carry real coordination cost and are usually chosen by default.

**Typical resolution.** Map moves — or ask, since the team may know an
organisational fact the board cannot show.

---

### `CONTRACT` — what crosses vs what exists

**Detect.** For each border contract, check every named payload against the
board: is it produced by an event upstream? Then check what the contract says
stays behind against what downstream events actually read.

Three shapes:

1. A payload **nothing produces** — often the most quietly damaging finding,
   because the contract reads as settled.
2. A payload the contract **omits** that a downstream event demonstrably reads.
3. Something the contract says stays upstream that the board shows crossing.

**Guards.**
- Contracts are written at business resolution. "The situation" covering three
  board objects is fine; do not demand field-level correspondence.
- A payload built at the border rather than lifted from an existing object is
  legitimate — say it is constructed, not that it is missing.

**Severity.** Significant, rising to blocking when the missing payload is a
constraint with a physical consequence (allergies, consent, money, safety).
That specific case is worth checking for deliberately: the context that acts on
outside advice is often the one with no access to who is affected.

---

### `TERM` — renamed, normalised, two-model

**Detect.** Compare every map name and payload word against the board's exact
spelling. Then look for one word carrying two models across a border with the
map silent about it.

**Guards.**
- A rename toward a Visual Glossary's agreed term is an improvement, not a
  defect. Note it as an alignment.
- The same word with the *same* model on both sides means there is no border
  there — that is `EDGE`, not `TERM`.

**Severity.** Minor for names. Significant for one word, two models, because
that is exactly the collision a border exists to record and it will be
implemented twice.

---

### `EXT` — external systems and off-board upstreams

**Detect.** Every external-system sticky on the board should appear on the map
as an external node with a stated choice: conform, wrap, or walk away. Every
noun read by two or more contexts and written by none should appear as a dashed
off-board upstream.

Then the inverse: a map node named after a **system, vendor, database or
team**, drawn as an ordinary bounded context.

**Guards.**
- An external system that appears *inside* a context's cluster on the board,
  alongside that context's own actors, doing the same work as they do, is a
  **participant inside that context**, not a context of its own. Promoting it
  to a node produces two services sharing one aggregate. This is a common and
  expensive error; check it explicitly whenever a system appears in more than
  one cluster.
- A vendor node is fine on a *deployment* map. Check which map this is first.

**Severity.** Missing off-board upstream read by many: significant. External
system promoted to a context: significant, blocking if it splits an aggregate.

---

### `STALE` — mechanism and staleness

**Detect.** Compare each edge's mechanism and staleness window against the
board's own clock: gaps between events, policies with implied waits, phases the
business already tolerates a delay across.

**Guards.**
- Numbers are almost never on a board. A window nobody supplied is `<n>` and
  belongs in the open questions, not the findings.
- Only report a contradiction when the board *shows* the clock: an event
  sequence that takes days, or an urgency the domain's own vocabulary names.

**Severity.** Minor, except where the edge is labelled synchronous and the
board shows a wait of hours or days (or the reverse — an asynchronous edge on a
crossing the domain treats as urgent). Then it is significant, because the
mechanism will be built wrong.

---

### `GAP` — a board silence the map presents as settled

**Detect.** Four specific silences, each checkable:

1. **Orphan object in a contract.** An object written by an upstream event and
   read by no event anywhere, named as a border payload.
2. **A context with no aggregate.** A stretch of events writing no business
   object at all, given a confident border and contract. Whatever the map says
   crosses that border has nothing to carry it.
3. **A missing policy.** An edge labelled with an event mechanism where the
   board never drew the policy that fires it — the trouble and the response are
   both on the wall with nothing between them.
4. **Hotspots.** Red stickies mapped to the border each argues about. **No
   hotspots on a board with contested ownership** means disagreement was never
   captured, not that there was none — say so.

**Guards.** Not every silence is the map's fault. Phrase these as *the board
cannot support this yet*, and route them to **back to the wall**.

**Severity.** Significant. An orphan object in a contract is the sharpest
version and often the single most useful finding in the report.

---

## 3. The pattern-evidence table

What a board can and cannot say about each pattern. For the full catalogue, read
`event-storming-context-mapper/references/pattern-catalog.md`; this table is
only what is *checkable* here.

| Pattern | Board evidence that supports it | Evidence that rules it out |
|---|---|---|
| Open Host Service | one writer, many readers, no negotiation drawn | a single consumer; per-consumer variation |
| Customer/Supplier | one writer, one or few readers, downstream demonstrably shapes the payload | no crossing at all |
| Conformist | downstream reads upstream's object and writes no model of its own | downstream writes its own version of the term |
| Anticorruption Layer | an external system whose model differs from the local term | no model difference nameable |
| Shared Kernel | the same object written by both sides | one writer only |
| Partnership | crossings in both directions, both sides writing | crossings one way only |
| Published Language | an off-board standard or several external consumers | a single internal consumer |
| Separate Ways | no crossing anywhere | any object crossing |
| Big Ball of Mud | contested ownership across several terms | — |

**Nothing on a board settles a pattern by itself.** Patterns encode
organisational facts. Use this table to reject unsupported claims, not to award
patterns the map didn't make.

---

## 4. The divergence allowlist

Do not report these. Publish the list instead.

- **Team ownership, org structure, who can negotiate.** Never on a board.
- **Relationship patterns where the board shows nothing either way.** Decisions,
  not observations.
- **Integration mechanisms and staleness windows nobody supplied.** `<n>`, open
  question.
- **Deployment, technology, repository and service boundaries.** Different map.
- **Nodes the map explicitly marks off-board, external or out of scope.** The
  map doing its job.
- **Contexts the map states as deliberately excluded from scope**, where the
  board covers them. Honour stated exclusions; test them once, then let them
  pass.
- **Contract payloads at business resolution.** "The situation", "the plan" —
  a contract is not a schema.
- **Decisions the person has already explained in conversation.** Record them as
  resolved, don't re-litigate.
- **Aesthetic and layout choices**, unless the layout *is* the finding (a map
  that reproduces the timeline).
- **Anything the board marked uncertain or unreadable.** Unchecked, not absent.

Two sentences per class is enough in the report.

---

## 5. Severity rubric

| | Test |
|---|---|
| **Blocking** | Someone following the map would build the wrong thing, or the two artifacts assert different facts about the domain. Reversed arrows, contested ownership, an aggregate split across an invented border. |
| **Significant** | The divergence defeats the purpose of having a shared map: recurrence flattened, an unsupported hub, a contract built on nothing, a node from another artifact. Nobody builds wrong tomorrow; everybody argues in three weeks. |
| **Minor** | Coverage and cosmetics: unlabelled arrows, normalised spelling, a missing staleness number. Fix while passing. |

Every finding also carries a direction: **map moves**, **back to the wall**, or
**ask**. Report the counts of each in §1 — a report where most findings are
*back to the wall* is telling the team something different from one where most
are *map moves*, and that difference is the headline.

---

## 6. Phrasing that survives contact with the team

The failure mode of this skill is a report that reads as an accusation. Three
habits prevent it:

- **Name the evidence, then the inference.** "Events 7/8 and 15/16 carry the
  same commands, the same two objects and overlapping responders — so the two
  help nodes look like one context drawn twice" beats "the map wrongly splits
  the help context".
- **Say what would settle it, in one sentence someone can answer.** Every
  finding that depends on a fact the board lacks needs a question, not a
  verdict.
- **Separate "the board never drew this" from "the board contradicts this".**
  These are different sentences with different consequences, and mixing them is
  how a whole report gets dismissed.