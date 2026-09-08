# The twelve fields

Each field: what belongs in it, where to source it from a context map, how it
fails, and — where the canvas has one — the vocabulary to choose from.

## Contents

1. Name
2. Purpose
3. Strategic classification
4. Domain roles
5. Inbound communication
6. Outbound communication
7. Ubiquitous language
8. Business decisions
9. Assumptions
10. Verification metrics
11. Open questions
12. Provenance markers

---

## 1. Name

The context map's own spelling, unchanged. If the map carries two names for what
is one context — a recurring context drawn twice under different labels — put
**one** name on the canvas, record the other in assumptions, and say which
artifact each came from. Renaming to something better than what the team says is
how a canvas stops being theirs.

A name that is a system, a vendor, a database or a team is a finding: note in
open questions that nobody has yet stated the business capability it owns.

## 2. Purpose

One or two sentences, in the business's words, answering *why does this exist*.
Test it three ways:

- Would a domain expert say this sentence? If it contains "service", "module",
  "API" or "manages data", no.
- Does it name a **responsibility** rather than a **mechanism**? "Gets a member
  onto a bicycle and takes it back" passes; "stores booking records" does not.
- Does it distinguish this context from its neighbour? If swapping the name
  makes it still true, it is a description of the system, not of this context.

Source: the context map's per-context responsibility line, the pivotal-event
cut's segment description, or the purpose sentence a domain-story cut wrote.
Recurrence belongs here: "serves both booking and return" is a purpose fact.

## 3. Strategic classification

Three sub-fields, each with its own vocabulary. **Every one of them is
`(unknown)` unless a source states it** — a context map does not say which
domain is core, and inferring it from how central a node looks is exactly the
mistake a Core Domain Chart exists to prevent.

**Domain** — `core` · `supporting` · `generic`.
Source: a Core Domain Chart, a capability map with markings, or an explicit
statement. If a cut *hypothesized* coreness ("probably the core domain, worth
running through a chart"), record it as `(proposed)` with the word "hypothesis"
kept.

**Business model** — how the context earns its place:
`revenue generator` · `engagement creator` · `compliance enforcer` ·
`cost reducer`. More than one is allowed and common; none is honest when the
map says nothing about money or obligation.

**Evolution** — `genesis` · `custom built` · `product` · `commodity`.
Source: a Wardley Map, or the map's own "conformist to an off-board supplier"
style statements, which usually mean product or commodity. A context the team is
inventing from scratch is genesis; one they are hand-building because nothing
fits is custom built.

## 4. Domain roles

What kind of context this behaves like. One to three, each with the behaviour
that argues for it. A role that predicts nothing about the context should be
left off — every context tagged *Gateway* teaches nobody anything.

| Role | Behaves like |
|---|---|
| **Specification model** | holds the definitions others execute against — rules, plans, templates |
| **Execution model** | carries out what a specification says, in real time |
| **Audit model** | records what happened for later scrutiny; append-mostly |
| **Approver** | its whole job is to say yes or no to something someone else wants |
| **Enforcer** | applies constraints continuously, not at a decision point |
| **Gateway** | routes between the inside and something outside — people, systems, other contexts |
| **Interchange** | translates between two models, owning neither |
| **Analysis** | aggregates data to produce insight; consumes far more than it emits |
| **Draft** | holds work in progress that has not yet committed anybody |
| **Engagement creator** | exists to make people come back; its value is participation |
| **Segregated core** | the differentiating logic, deliberately separated from what surrounds it |

Read these off behaviour, not vocabulary. A context whose inbound messages are
all requests and whose outbound are all answers-from-elsewhere is a gateway,
whatever it is called.

## 5. Inbound communication

**Messages this context handles**, with the collaborator that sends them. One
row per message, per collaborator: the same message from two collaborators is
two rows, and that repetition is a fact about coupling.

| Collaborator | Message | Type | What it carries | Evidence |

- **Type** is `cmd`, `qry`, `evt` or `?` — see the SKILL's Step 3 for the rules.
- **What it carries** comes from the border contract's "crosses" line. If the
  contract also has a "stays behind" line, that is the most valuable half; keep
  it in the field table even though it does not fit the picture.
- **Evidence** points at the contract, the sticky, or the ledger row.

An inbound event is one the context **subscribes to**. It arrives from its
publisher, so it appears here even though every arrow on the map points the
other way.

Failure mode: inbound rows called *Create X* / *Update X* / *Get X*. That is a
data interface. Go back to the board for the business's verbs.

## 6. Outbound communication

**Messages this context emits**, with the collaborator that receives them.

| Message | Type | Collaborator | What it carries | Evidence |

Three kinds land here, and the third is the one people miss:

- **events it publishes** — facts, un-refusable, usually the bulk;
- **commands it sends** to another context — it is asking someone else to act,
  which is a stronger coupling than an event and worth naming as such;
- **queries it issues** — it needs someone else's data to do its job. Outbound
  even though the answer flows back. A context with many outbound queries has a
  **wide interface**, and that belongs in open questions: wide interfaces
  collapse two contexts into one under implementation pressure.

## 7. Ubiquitous language

**This field is drawn, not written.** On the canvas it is a small term graph —
the terms this context owns, the glossary's own cardinalities on the edges,
borrowed and undefined terms dashed. A glossary is a picture; flattening it to a
comma-separated list throws away the relationships that make it worth having.
The field table below the diagram repeats it in words, because the picture caps
what fits and a borrowed sense needs a sentence.

| Term | What it means *here* | Owned or borrowed | Cardinality |

**A Visual Glossary is the proper source for this field, and worth asking for
every time.** It is the artifact that holds the team's agreed vocabulary; the
map's term ledger only holds who writes what. With a glossary, this field is
`(given)` — the team's own spellings, their own relationships, their own
multiplicities. Without one it is `(derived)`, reconstructed from the ledger,
and worth saying so on the canvas.

The glossary covers the whole domain, so **partition it** rather than copying
it. The map's term ledger settles ownership: the context that *writes* a term
owns it.

**Owned** terms are the ones this context writes — its aggregates, their states,
its business objects. Keep the glossary's exact spelling, including its
inconsistencies, and carry its cardinalities across: they are the shape of the
data this context is responsible for, and several of them are business decisions
in disguise (see §8).

**Borrowed** terms are the ones it reads from elsewhere. For each, state the
owner's sense too, because that difference *is* the border:

> *Bicycle* — here, a bookable unit with a status and a location. In Rack
> management, a physical thing in a numbered slot with a lock and an unlock
> code. Borrowed; the two models should not merge.

Two results of the partition belong in open questions rather than in this field:
a term this context clearly uses that **the glossary never defines**, and a
glossary term **no context on the map owns**. The first is a patch to the
glossary; the second is a missing context or an off-board supplier.

A term list with no borrowed terms usually means nobody looked. A term list
identical to the one on the next canvas means the partition never happened.

## 8. Business decisions

The rules this context **enforces**, in business words. A good one names
something the context refuses, or a judgement it makes on the business's behalf.

Sources, in order of quality: an invariants sheet (its per-aggregate invariants
are business decisions already written, one line each); **the Visual Glossary's
cardinalities**, which state rules nobody wrote as rules — a term that *selects*
`1..*` of another is saying it cannot exist with none, and that is a refusal the
software will have to make; the board's policies; the guards implied by state
machines.

Two exclusions decide this field:

- **Cross-context rules are not business decisions here.** A rule needing
  another context's data cannot be enforced in one place — on an invariants
  sheet these are the demoted §6 entries with a staleness window. They belong in
  open questions, or as a policy on the map, and putting them here quietly
  promises enforcement nobody can deliver.
- **Behaviour is not a decision.** "Notifies responders" is what it does.
  "A booked bicycle may not be booked again" is what it decides.

Keep the rejection wording when a source has one — "you cannot answer your own
question" is more useful in a workshop than "self-answer prohibited".

## 9. Assumptions

The reading decisions behind this canvas, stated so the room can overturn them
cheaply. Typical entries: two map bubbles read as one context; a message type
guessed; a collaborator assigned where the map was ambiguous; a spelling
normalized.

This field is what makes the rest of the canvas arguable. An empty assumptions
box on a canvas derived from an incomplete map is not confidence, it is an
undisclosed guess.

## 10. Verification metrics

How anyone would know this context is doing its job. **Almost never on a context
map** — write `(unknown) — none supplied` and mean it.

If a metric is worth proposing, mark it `(proposed)` and keep it to things the
context could actually observe about itself: time from a request to its first
answer, share of requests that reach a resolution, share of decisions the
context refused. Business outcomes owned by nobody in particular ("more rides
taken") are not this context's metric.

## 11. Open questions

One sentence each, and each one **names the field it would settle**. This is the
handover to the next session, so questions that no single person could answer in
a sentence are the wrong size.

Sources: the map's own open questions, filtered to this context; the `?` message
types from the ledger; the unmatched messages from the reconciliation; anything
marked `(proposed)` above.

## 12. Provenance markers

Four, used on every field:

| Marker | Means |
|---|---|
| `(given)` | a source states this |
| `(derived)` | read off a source by an argument the canvas can show |
| `(proposed)` | mine; nobody has agreed to it |
| `(unknown)` | nothing to derive from — the box stays empty |

They are the difference between a canvas that documents a decision and a canvas
that manufactures one. Keep them on the field headings even when the whole
canvas is derived, because a reader skimming twelve files reads the markers
before the content.