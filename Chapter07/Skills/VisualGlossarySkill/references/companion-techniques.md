# Companion techniques — pairing the glossary with a dynamic artifact

A Visual Glossary is the static half of a modelling practice. Read this file
once you know which dynamic artifact the glossary accompanies, to pick the right
cross-checks and to phrase your open questions in the language that team is
already using.

## The general shape

Every collaborative modelling technique in this family is strong on **behaviour**
and weak on **structure**. They produce verbs — activities, events, commands,
slices — and they refer to nouns constantly without ever defining them. A board
can say `Order Shipped` two hundred times without anyone agreeing what an Order
contains, whether it can ship partially, or how many addresses it has.

That gap is the glossary's job. So the relationship is always the same:

- The **dynamic artifact** names nouns in passing and asserts *transitions*.
- The **glossary** defines those nouns and asserts *structure and multiplicity*.
- **Every noun in the dynamic artifact should be a glossary term**, and every
  glossary term should be touched by something dynamic — gaps in either
  direction are findings.
- **The glossary is authoritative on vocabulary; the dynamic artifact is
  authoritative on behaviour.** Neither overrides the other in the other's
  domain, and the domain experts override both.

What changes between techniques is *which* cross-checks pay off.

---

## Domain Storytelling (Hofer & Schwentner)

**The dynamic artifact**: one concrete scenario — numbered sentences of
`Actor → activity → work object`, no conditionals, no branches.

**What the glossary supplies**: the work objects. A domain story mentions a
`Bicycle` and a `Rack` without saying a Rack contains one-to-many Locks; the
glossary does.

**Cross-checks worth running**

- **Terminology fidelity** — the story's work objects and actor names must match
  glossary terms exactly. Story `Bike` vs glossary `Bicycle` is the single most
  valuable finding, because divergent vocabulary is what the glossary exists to
  prevent.
- **Cardinality contradiction** — a story step that violates a glossary
  multiplicity (one bicycle racked in two places when the glossary says `0..1`).
- **Coverage both ways** — glossary terms no story touches (missing story, or
  dead vocabulary); story work objects the glossary lacks (incomplete glossary,
  or an off-language coinage).

This cross-check already exists in the house as
`domain-story-critic/references/visual-glossary-cross-check.md` — feed it the
Glossary Brief rather than duplicating the work.

---

## EventStorming (Alberto Brandolini)

**The dynamic artifact**: a timeline of sticky notes — orange domain events in
past tense, blue commands, large yellow aggregates, small yellow actors, green
read models, lilac policies, pink external systems, red hotspots — clustered
into bounded contexts.

**What the glossary supplies**: the nouns those events and commands are *about*,
plus the structure that a board of past-tense verbs cannot express.

**Cross-checks worth running**

1. **Event nouns are glossary terms.** Every orange sticky is
   `<Noun> <past-tense verb>`. Extract the nouns from `Order Shipped`,
   `Payment Captured`, `Tag Applied` and check each against the glossary. A noun
   that appears in twenty events and nowhere in the glossary is a term the team
   uses daily and has never defined.

2. **Aggregates must agree — and this is the highest-value check.** The board
   names aggregates on large yellow stickies. Your Step 4 clustering derives
   aggregates independently, from composition-flavoured edges. Compare them:
    - A glossary cluster with no matching aggregate — either a missing aggregate
      or your cluster is really just attributes.
    - A board aggregate that the glossary doesn't hold together as a cluster —
      the aggregate may lack the structure to enforce its own invariants.
    - **Boundary disagreement** is the interesting case. If the board makes
      `Order` an aggregate but the glossary shows `Line Item` with its own
      identity and independent relationships, the board is claiming an invariant
      boundary the glossary does not support. Report it; don't resolve it.

3. **Cardinalities become candidate invariants.** This is the most useful thing
   the glossary gives an EventStorming board. An aggregate exists to enforce
   consistency rules, and the glossary's multiplicities *are* rules: `1..10
   Authors` means the Book aggregate must reject the eleventh; `0..25 Tags`
   means something rejects the twenty-sixth. Restate each cardinality inside the
   relevant aggregate as an invariant, and ask which command enforces it. A
   cardinality with no command that could violate it is either unenforced or not
   really a rule.

4. **Read model fields are glossary terms.** Green stickies project data for a
   view. Every field on one should be a glossary term or an explicitly derived
   value. An undefined field on a read model is undefined vocabulary reaching
   the user interface.

5. **Hotspots are often glossary questions.** Red stickies capture disagreement
   and confusion, and a large share of them are terminological — *"is a customer
   the buyer or the account holder?"*, *"two teams mean different things by
   shipment"*. Walk the hotspots against your open questions: some are already
   answered by the glossary (say so — that's a hotspot the team can retire),
   and some belong in your open questions because the glossary silently picked a
   side. **A glossary that quietly resolves a live hotspot is a finding**, not a
   success; the team hasn't actually agreed.

6. **Bounded contexts.** The board clusters into contexts explicitly. If the
   glossary also groups terms, the groupings should agree; if the glossary
   doesn't group, the board's contexts are the natural clustering to propose. A
   term appearing in two contexts with different relationships is normal DDD —
   the same real thing modelled twice on purpose — but confirm it's deliberate.

Pair with `event-storming-interpreter` when the board itself needs reading.

---

## Event Modeling (Adam Dymitruk)

**The dynamic artifact**: a timeline of vertical slices, each showing a
wireframe, a command, the event(s) it produces, and the read models that
project them. The organising discipline is **information completeness** — every
piece of data appearing on a screen or read model must be traceable back to
data carried by some earlier event.

**What the glossary supplies**: the shared field vocabulary that makes
information completeness checkable at all. You can only trace `Title` from an
event to a view if both call it `Title`.

**Cross-checks worth running**

- **Field traceability.** Walk each read model's fields back through the events
  that feed it. Glossary terms make the walk mechanical; a field that changes
  name between event and view is exactly the bug this practice is meant to
  prevent, and the glossary is what exposes it.
- **Optionality flows forward.** A glossary `0..1` means the field may be
  absent, so every downstream slice must handle absence. `Book has 0..1
  Abstract` means every view showing an abstract needs an empty state. Check
  the wireframes.
- **Mandatory fields constrain command payloads.** A glossary `1` says the
  thing cannot exist without that part, so the creating command must carry it.
  A required glossary term missing from the creation command's payload is a
  gap — either the command is incomplete or the cardinality is wrong.
- **Bounded numeric limits belong on a slice.** `0..25 Tags` has to be enforced
  somewhere. Which command validates it, and what does the UI do at 25?
- **Terms with no slice.** A glossary term that no command writes and no read
  model displays is either a missing slice or vocabulary nobody needs.

---

## When the glossary stands alone

Perfectly legitimate — teams often build the glossary first, precisely to agree
the language before modelling behaviour. Interpret it normally, and note in the
brief that its structure is **unexercised**: no behaviour has yet tested whether
these terms and multiplicities survive contact with a real scenario. The most
useful thing you can offer is to name the two or three scenarios that would
stress it hardest.

## When the glossary is being derived, not read

Sometimes the ask runs the other way — a board or a set of stories exists and
the user wants a glossary out of it. That is a different job, but the same
material: harvest nouns from the events, commands, work objects and read model
fields; cluster them; and let the *questions* this skill generates (missing
inverses, undefined terms, unstated identity) drive what the new glossary must
decide. Say clearly which terms came from the board and which you inferred.