# Context map and classification

Once the lanes exist, two follow-up questions decide whether they are useful:
**how do they relate**, and **which of them matter most**.

---

## Part 1 — The context map

A cut without seams is half an answer. For every pair of contexts that touch in
the story, record three things:

1. **What flows** — the concrete thing, in domain words: a registered commuter, a
   booking, an access code, a completed ride, a fee.
2. **Which direction** — who produces it, who consumes it.
3. **Which pattern** — the shape of the relationship.

Read the flows straight off the story: a sentence whose work object was created
in another lane is a crossing. In the bicycle story, *unlocks Bicycle with Code*
sits in rack management but consumes a *Code* produced by booking — that is the
crossing, and it is also the reason those two lanes are distinct.

### Relationship patterns

**Upstream/downstream (customer/supplier).** The downstream context depends on
the upstream one, and the upstream team has reason to care about its needs.
The most common relationship. Use when there is real negotiating room.

**Conformist.** Downstream accepts the upstream model as-is, with no
translation. Appropriate when the upstream is a shared standard, or when
translation would cost more than it saves — and dangerous when the upstream
model is a poor fit, because its concepts leak in.

**Anticorruption layer (ACL).** Downstream translates the upstream model into
its own terms at the boundary. Use whenever the upstream model would distort a
context you care about — nearly always the right answer next to a legacy system
or an external vendor. It costs a translation layer and buys model integrity.

**Shared kernel.** Two contexts share a small, explicitly agreed model subset.
Powerful and expensive: every change needs both teams. Use only for genuinely
stable, small, jointly-owned concepts, and never as a way to avoid deciding
where a boundary goes.

**Published language / open host service.** One context publishes a documented
interface many consumers use. Right when a context has several downstreams —
otherwise premature.

**Separate ways.** No integration at all; the contexts solve their needs
independently, even at the cost of duplication. Legitimate when the coupling
would cost more than the duplication.

**Partnership.** Two contexts succeed or fail together and coordinate closely.
Honest when true, but flag it: it means the boundary does not buy independence,
which is sometimes a sign the two should be one context.

### Choosing between them

- Is the upstream something you control? → customer/supplier or partnership.
- Is it external, legacy, or badly modelled for your purposes? → ACL.
- Is it a standard everyone conforms to? → conformist or published language.
- Are two contexts sharing a concept neither owns? → shared kernel, or
  reconsider the boundary.
- Do they barely touch? → separate ways, and say so plainly.

### Presenting it

A short table is usually enough:

| From | To | What flows | Pattern | Note |
|---|---|---|---|---|

A picture helps for four or more contexts — a simple Mermaid graph with arrows
labelled by what flows. Keep it small; a context map that needs a legend is
being over-specified this early.

---

## Part 2 — Core, supporting, generic

Only do this when the question calls for it — build-vs-buy, where to put the
strongest team, where to invest. Skip it when the person asked only where the
boundaries are.

**Core** — what differentiates this business from its competitors. Build it,
staff it well, keep the model clean. There are usually one or two, occasionally
three. If everything looks core, nothing is.

**Supporting** — necessary, specific to this business, but not a differentiator.
Build simply, or buy and adapt. Most contexts land here.

**Generic** — a solved problem any business has (identity, payments,
notifications, accounting). Buy or use a commodity service; building it burns
capacity for no advantage.

### Deciding

Ask, per context:

- Would a competitor doing this noticeably better take customers from us?
  (Yes → core.)
- Could we buy this off the shelf and stay competitive? (Yes → generic.)
- Is it specific to our business but invisible to customers as an advantage?
  (→ supporting.)
- Would the business notice if it were merely adequate? (No → not core.)

Two cautions. **Complexity is not coreness** — a fiendishly complex context can
still be generic (tax calculation), and the pairing of *high differentiation*
with *high complexity* is what makes something worth building yourself. And a
domain story alone is thin evidence for this: it shows what happens, not what
wins customers. State the classification as a hypothesis and ask the person to
confirm it against their strategy.

### Handing off

Once classified, the natural next steps are:

- `core-domain-chart-author` — renders the contexts on differentiation ×
  complexity, with arrows for recommended moves.
- `core-domain-chart-critic` — challenges the placements.
- `capability-map-critic` — if the person also has a capability map, reconcile:
  capabilities and subdomains should be recognisably the same business, and
  where they disagree, one of the two artifacts is wrong.