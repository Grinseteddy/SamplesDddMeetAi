# Chart Essentials — Is This Even a Core Domain Chart, and Does It Hold?

Use this before any value judgement about core/supporting/generic. A chart built
on the wrong kind of dots can't support a decision no matter where they land.
Grounded in Millett & Tune (*Patterns, Principles, and Practices of DDD*), Nick
Tune's *Core Domain Patterns*, and the DDD Crew *Core Domain Charts*. Pick the
checks that bite hardest for the specific chart — this is a menu, not a script to
read aloud.

---

## 1. The two axes (and how to read a placement)

- **Horizontal — Business Differentiation.** How much *competitive advantage* this
  domain gives **this** business. Left = commodity (everyone has it); right =
  unique, hard for rivals to match. Owned mainly by product/business stakeholders.
- **Vertical — Model Complexity.** How hard the domain is to *model, build, and
  maintain* as software. Bottom = simple forms-over-data / CRUD; top = genuinely
  hard. Owned mainly by engineers.

Both axes are **for today**, not some imagined future, and both are *subjective
bets* — strategy is a bet on the future, so no coordinate is "correct." The point
of the technique is the **cross-discipline conversation** the two axes force, not
precision. A placement is "defensible" when business and engineering can both tell
a consistent story about why the dot sits where it does.

> **Check the orientation you were actually handed.** Most charts put
> differentiation on X and complexity on Y, but some drawings flip them, and the
> *architecture-migration* variant relabels the Y-axis entirely (see §4).
> Establish which axis is which before you critique a single placement —
> mis-reading the axes will make you "find" problems that aren't there.

---

## 2. What belongs on the chart — the valid unit

A Core Domain Chart plots **(sub)domains or bounded contexts** — coherent areas of
the problem/solution space that you could sensibly own, invest in, build, or buy
as a unit.

**Strong looks like:** "Pricing," "Fraud Detection," "Driver Dispatch," "Identity
& Access," "Catalogue Search," "Regulatory Reporting." Each is an area of capability
you could put a team around and reason about as core, supporting, or generic.

**The diagnostic:** *Is this a coherent area of the domain you could make a
build/buy/invest decision about as a whole?* If yes, it's plottable. If it's a
slice of UI, a single ticket, a department, or a wish, it isn't.

---

## 3. The five impostors — what does *not* belong on the chart

The most common defect is non-domains wearing the dot. Name the impostor, then
state the (sub)domain it implies (or flag that none is clear).

- **A feature / use case.** "The checkout button," "CSV export," "the new
  onboarding screen." Tell: it's a slice of functionality, not an area you'd staff
  and fund as a unit. The domain behind it might be "Checkout" or "Onboarding."
- **A team / org unit.** "The Platform team," "Payments squad." Tell: it names
  *who*, and it would be renamed in a reorg. A domain is *owned by* a team but is
  not the team. (Team Topologies overlays add teams *as an annotation* on top of
  the domains — that's fine; a team *as the plotted unit* is not.)
- **A technology / system.** "Kafka," "the data lake," "Salesforce," "the mobile
  app." Tell: it's a product or asset you could buy. A domain may *run on* a system
  but isn't the system — "Customer Management" can live on three different CRMs.
- **A project / initiative.** "The 2026 replatforming," "Project Atlas." Tell: it
  has a start and end date. Domains persist; projects *change* them.
- **A goal / KPI / outcome.** "Increase retention," "be the cheapest." Tell: it's
  something you want to *achieve*, not an area you can build. Plotting an outcome
  makes the differentiation axis circular.

A chart that is mostly impostors is the headline finding: it isn't yet a Core
Domain Chart, and any core/supporting/generic reading on top of it is decoration.

**Also watch:** *inconsistent units* — a coarse subdomain ("Logistics") plotted as
a peer of one fine-grained bounded context ("Address Validation"). Mixing
granularities makes the chart look complete while hiding whole areas and distorting
the relative ordering that is the chart's whole point.

---

## 4. The chart variants — know which one you're reading

The DDD Crew lists several flavours; don't expect all information on one diagram,
and critique the chart for *what it's trying to be*:

- **(Sub)Domain / Bounded-Context Portfolio.** The simplest: every domain plotted
  for a relative sense of ordering. Most critiques target this.
- **Context Map + Team Topologies.** The portfolio plus dependencies between
  bounded contexts and the Team Topologies interaction mode in play. Here a *team*
  annotation is legitimate; still check the underlying units are domains.
- **Architecture Migration.** A deliberate relabel of the Y-axis to plan the
  *order* in which you migrate from current to target architecture. If you see this
  variant, the vertical axis is **not** complexity — read the legend and don't
  critique it as if it were.

---

## 5. Assessing the placements — the question bank

Differentiation and complexity are hard and subjective; these clues (from the DDD
Crew) help you test whether a dot's position is *defensible*. Use them as probes,
not a checklist to recite.

**Differentiation (the horizontal axis):**
- How hard would it be for a **new entrant** to match or exceed this capability?
- How hard would it be for an **existing competitor** to match or exceed it?
- How much advantage is currently derived from it (revenue, brand, engagement)?
- How much advantage *could potentially* be derived from it?
- What damage would the brand take from major or recurring failures here?

**Complexity (the vertical axis) — and which kind:**
- How hard is it to design a conceptual model and build/maintain it? (*essential*
  domain complexity — the kind worth embracing if it differentiates)
- Is the current solution more complex than it needs to be for what it does?
  (*accidental* technical complexity — waste)
- Are there complex processes, calculations, or decisions happening *outside* the
  software, in people's heads and spreadsheets? (*operational* complexity — often a
  hidden opportunity to differentiate by automating it)
- What scale must it operate at? (scale adds complexity even when the rules are
  simple)
- Does it need scarce specialist talent that's expensive to acquire?
- Which **Cynefin** domain does it fall in (clear / complicated / complex)?

Distinguishing *essential* from *accidental* complexity is the most useful move on
the vertical axis: essential complexity in a differentiating domain is money well
spent; accidental complexity anywhere is the first thing to challenge.

---

## 6. The two fastest structural tests

When you have little time, these two catch the most damage:

1. **Run the unit test on every dot** (§2–§3). Features, teams, systems, projects,
   and KPIs masquerading as domains are impostors — surface these before debating
   placement, because you can't place a non-domain.
2. **Count the top-right (core) dots.** If a third or more of the chart sits in
   core territory, the differentiation axis has collapsed and the investment lens
   the chart exists to provide is dead on arrival — go straight to
   `classification-and-patterns.md` and push for a small, genuinely distinctive
   core.

