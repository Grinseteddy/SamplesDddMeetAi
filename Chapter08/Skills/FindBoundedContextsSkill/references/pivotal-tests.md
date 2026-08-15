# Pivotality tests in full

Contents:

1. The six tests, one by one
2. The narrow-interface veto
3. Scoring rubric
4. Granularity by board size
5. The divider convention: who owns the pivotal event
6. Relationship patterns available at a border
7. Anti-signal catalogue

---

## 1. The six tests, one by one

Each test below gives the diagnostic question to ask the room, what counts as
evidence *on the board* (as opposed to in your head), how much the test is worth
on its own, and what it looks like when it misfires.

### Test 1 — Phase

**Ask:** "Before this event we are doing ______; after it we are doing ______."
Can you complete both blanks without naming a single record or status?

**Evidence:** the purpose of the events on either side, in the domain's own
words. *Finding out who might fit the job* → *deciding which of them we want* →
*turning the chosen one into a colleague*.

**Alone:** necessary but not sufficient. Every method finds phase changes; the
question is whether this one is also a *language* change. Phase alone often
produces the classic failure of this technique — timeline joints presented as
boundaries.

**Misfires when:** the phrase you complete the blanks with is just the status of
one aggregate ("before, the order is pending; after, it is confirmed"). That is
a state transition inside a lifecycle, which every event has.

### Test 2 — Irreversibility

**Ask:** if we had to go back, would we *undo* it, or would we run a differently
named business process?

**Evidence:** the existence of a compensating action with its own vocabulary —
cancellation, refund, recall, withdrawal, return, void, rescind — and usually
its own paperwork, approver, or cost.

**Alone:** strong. Irreversibility is the business telling you it treats the two
sides as different worlds. It is also why eventual consistency across this line
is safe: the business has already built the compensation path.

**Misfires when:** the "compensation" is a soft delete or an edit screen. If a
clerk can fix it without telling anyone, nothing crossed.

### Test 3 — Handover

**Ask:** who is holding the process before, and who after? Would you invite the
same expert to a workshop about both sides?

**Evidence:** the actor stickies change; an external system appears or
disappears; a different system of record starts being written to; the work moves
to a different team, shift, site or company.

**Alone:** moderate. Organisations reorganise, and Conway's law cuts both ways —
today's handover may be an accident of last year's restructure. Weigh it, do not
obey it. But a handover *to a different company* is close to decisive: someone
else already owns that capability.

**Misfires when:** the same person changes hats. One recruiter who screens and
then interviews is not a handover; a role change is a hint, and roles never name
contexts.

### Test 4 — Language

**Ask:** does a noun mean something different, or get modelled differently, on
the far side? Is a new identifier minted here?

**Evidence:** the vocabulary on the stickies. *Cart* stops appearing and *Order*
starts. *Candidate* becomes *New hire* becomes *Employee*. A number gets issued:
order number, policy number, tracking number, employee ID. Attributes that
mattered upstream stop being mentioned downstream.

**Alone:** decisive, in the sense Evans intended. When the language changes, the
boundary is real even if nothing else fired. The trouble is that language change
is easy to assert and hard to verify — hunt for the *identifier* and the
*dropped attributes*, because those are checkable.

**Misfires when:** the rename is cosmetic. Same attributes, same lifecycle, same
questions asked of it, different label on the sticky because a different team
wrote it.

### Test 5 — Commitment

**Ask:** after this, who can be held to what?

**Evidence:** money moves or is promised; a signature; an SLA clock starts; a
regulatory obligation attaches; an external party is now owed something. Look
for the events immediately downstream — obligations generate deadline-shaped
events (*Payment due*, *Probation ended*, *Delivery window opened*).

**Alone:** strong, and it usually drags irreversibility along with it, which is
why the two so often fire together. Commitment is also the best predictor that
the two sides will have *different failure modes* — upstream loses opportunity,
downstream loses money or trust.

**Misfires when:** the commitment is internal and soft — a plan, an intention, a
forecast. Intentions are revised; obligations are compensated.

### Test 6 — Clock

**Ask:** does the tempo change here? Is there a natural wait?

**Evidence:** a queue, a batch, a nightly job, a wait for a human, a wait for
the post, a wait for an external party. On a board this shows up as a stretch
where nothing happens for hours or days, or a pink external system that answers
later.

**Alone:** weak on its own, but it is the test that makes a boundary *cheap*.
Where the business already tolerates a wait, eventual consistency across the
border costs nothing to introduce, so a border with a modest case elsewhere
becomes attractive.

**Misfires when:** the wait is technical (a retry, a poll, a nightly ETL that
exists because of an old integration). That is an artefact of the current
implementation, not of the domain.

---

## 2. The narrow-interface veto

**Ask:** list what downstream genuinely needs from upstream, in fields. Then ask
whether downstream will ever need anything else — and if so, how often.

**Evidence:** trace the read models and rules downstream of the candidate. Every
green sticky downstream that was produced upstream is a strand of the interface.
Two or three strands with stable content is a narrow interface. A downstream
that consults the entire upstream case file is not.

**Why it vetoes:** the other six tests measure whether the *business* treats the
two sides as different. This one measures what the *split will cost you*. A
border with a wide interface produces two modules that have to be deployed
together, a chatty integration, and a shared model that neither side owns. The
business drama at that point is real; the boundary is not.

**Worked contrast:**

- *Offer accepted* — downstream (onboarding) needs name, contact, role, level,
  compensation, start date, manager, legal entity. Eight fields, stable. It
  never needs the interview scorecards. **Narrow. Passes.**
- *Hiring decision made* — downstream (offer preparation) needs the level and
  compensation reasoning, which comes from the scorecards; if the candidate
  negotiates, it goes back to the debrief notes; if the candidate declines, the
  runner-up's whole dossier is needed. **Wide. Vetoed** — this is a milestone
  inside selection, not a border.

---

## 3. Scoring rubric

| Verdict | Condition |
|---------|-----------|
| **Divider** | Phase fires + ≥2 of tests 2–6 + no veto |
| **Contested** | Phase + 1 other, no veto — or a strong candidate adjacent to a stronger one |
| **Milestone** | Any number of tests fire but the narrow-interface test vetoes |
| **Not a candidate** | Removed at screening (technical, looped, board edge, echo) |

Language (test 4) may substitute for the phase test when the identifier change
is unambiguous — a new number minted and the old noun disappearing from the
board is a phase change whether or not anyone can articulate it.

Record the verdict *and* the deciding test. "Milestone — vetoed by interface
width" tells a team something; "not pivotal" does not.

---

## 4. Granularity by board size

| Events on the line | Expect | Alarm above |
|--------------------|--------|-------------|
| 8–15 | 1–2 dividers | 3 |
| 16–30 | 2–4 | 6 |
| 30–60 | 3–6 | 9 |
| 60+ | 4–8, and consider splitting the board first | 12 |

Roughly one divider per 5–12 events. Segments smaller than about four events are
usually phases; segments larger than about fifteen are usually hiding an
internal border that this method cannot see because it has no pivotal event —
say so and hand off to `event-storming-context-finder`.

If a board yields *zero* dividers, that is a legitimate answer, and a useful
one: it means the flow is one context, or the board covers only part of one.
Say which you think it is.

---

## 5. The divider convention: who owns the pivotal event

The pivotal event is **produced by the upstream context** and **consumed by the
downstream context**. The line is drawn immediately after it. Consequences worth
stating out loud in the output, because teams get this wrong and then argue
about it in code review:

- The upstream context owns the event's schema and publishes it.
- The event's name uses the *upstream* language. *Offer accepted* is recruiting
  vocabulary; onboarding may well call the same fact *New hire confirmed*
  internally, and that translation is exactly what an anticorruption layer or a
  published language is for.
- The event is the *only* thing guaranteed to cross. Anything else downstream
  wants is a new, negotiated interface — and a request for one is a signal the
  border may be in the wrong place.

---

## 6. Relationship patterns available at a border

Pick one per divider and justify it in a clause.

- **Customer/supplier** — downstream's needs are negotiated into upstream's
  plan. The default at a pivotal event, and healthy.
- **Published language** — the event is a stable, documented contract that
  several downstreams consume. Right when a divider fans out to more than one
  segment.
- **Open host service** — upstream offers a general interface to many.
- **Conformist** — downstream accepts upstream's model wholesale, no
  negotiation. Common when upstream is a package or another company.
- **Anticorruption layer** — downstream translates at the border to protect its
  own model. Right where the language test fired hardest.
- **Separate ways** — the two sides genuinely do not need each other. Rare at a
  pivotal event, since the event itself is a dependency.
- **Shared kernel / partnership** — a shared model or a joint plan. Treat with
  suspicion at a pivotal event: if the two sides need a shared kernel across the
  divider, the interface probably was not narrow.

---

## 7. Anti-signal catalogue

| Looks pivotal because… | Actually is | Fix |
|------------------------|-------------|-----|
| Everyone argues about it | A hotspot | Record the disagreement, do not draw a line on it |
| It is reported to management | A reporting milestone | Ask what *work* changes; usually nothing |
| It blocks progress (approval, check) | A gate / rule inside one phase | Model as a policy, keep the segment whole |
| It is technically significant (saved, published, synced) | An implementation event | Drop at screening |
| It happens many times | A looped event | Cannot be a divider; a divider is crossed once |
| It is the first/last event | The scope edge | Boundaries of the board, not within it |
| The team's swimlane says so | Prior art | Score it like any other candidate |
| It is complex and important | Depth inside a context | Complexity concentrates *inside* cores, not at borders |
| An icon says it is system-raised | Possibly a provenance mark (AI-proposed, imported, added later) | Confirm the marker's meaning; find a structural second reason or drop the point |
| It mints an artefact | Nothing downstream reads that artefact | Wrong divider, or a context missing from the board — say which |