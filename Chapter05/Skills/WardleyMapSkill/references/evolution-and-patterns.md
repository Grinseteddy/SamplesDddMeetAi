# Evolution, Climate, Doctrine — Positioning Components and Critiquing the Whole

This is the lens the user most wants checked: the **x-axis of evolution** and
what it implies. Getting a component's stage right is the Wardley equivalent of a
correct marking — it decides whether you *build, buy, or rent*, which *method*
you use, and where you *invest*. The chapter also gathers the whole-map lenses:
climate, doctrine, inertia, method-matching, and gameplay. Grounded in Simon
Wardley, *Wardley Maps: Topographical Intelligence in Business*. A menu, not a
script.

---

## 1. The four stages of evolution

Everything on the map evolves left-to-right under **supply and demand
competition**, measured by **ubiquity** (how widespread) and **certainty** (how
well understood / feature-complete). The default labels (for *activities*) are:

| Stage | One-line character | Typical decision |
|---|---|---|
| **Genesis** | The novel, rare, uncertain, constantly changing, newly discovered. | Explore; gamble; build. |
| **Custom-Built** | Uncommon; individually made and tailored; still learning. | Build / craft; expect rework. |
| **Product (+rental)** | Increasingly common; repeatable, more defined, better understood; bought off-the-shelf or rented. | Buy the product; differentiate around it. |
| **Commodity (+utility)** | Ubiquitous, standardised, fixed, undifferentiated, fit for a known purpose; consumed as a utility. | Rent the utility; never build bespoke. |

The other component *types* evolve through the same four positions with different
names — use the right vocabulary, but the positions line up:

- **Practices:** Novel → Emerging → Good → Best.
- **Data:** Unmodelled → Divergent → Convergent → Modelled.
- **Knowledge:** Concept → Hypothesis → Theory → Accepted.

**Characteristics, not gut feel.** Wardley provides a *cheat sheet* of properties
that shift across the stages — market (undefined → forming → growing → mature),
knowledge (uncertain → … → well understood), failure (tolerated/expected →
… → operational, not-tolerated), focus of value (exploration → … → reducing cost
of operation), and so on. Position a component by asking *which column its
properties sit in*, not by how long the company has had it.

---

## 2. Evolution is NOT time, maturity, or adoption — the error that ruins maps

The most damaging x-axis mistake is treating it as a timeline or an adoption
curve. Wardley is emphatic: **"mapping is about probability, not time."**

- **Not time.** A component isn't "more evolved" because you've had it for five
  years. It's more evolved because the *act itself* has become more ubiquitous
  and certain across the market.
- **Not maturity of *your* implementation.** Your bespoke system can be polished
  and old and still be **Custom-Built**, because the *activity* hasn't
  commoditised across the industry.
- **Not the diffusion / adoption S-curve.** Diffusion measures how many adopters
  a *single instance* has over time. Evolution is a different axis (ubiquity ×
  certainty across *all* instances of the act). A component can diffuse widely
  while still being early-stage, and vice versa.

**Tell-tales it's gone wrong:** the x-axis is labelled *maturity*, *time*,
*phase*, or *adoption*; components are ordered by *when the team built them*; or
"we've invested a lot in this" is used as the reason something is far right.

---

## 3. The common mis-positionings — what to challenge

- **Over-novelising (the vanity-build trap).** A component drawn as **Genesis**
  or **Custom-Built** that is really a **Product** or **Commodity** you could buy
  or rent today. Often done to justify building a pet system, to flatter a team,
  or because the team only knows their own bespoke version. *Cost:* you build
  (and maintain) what you should have rented. Test: *"Could three competitors
  each buy or rent this as a service tomorrow? If yes, it's not Genesis."*
- **Under-novelising (the outsource-the-secret-sauce trap).** A genuine
  differentiator dismissed as **Commodity** and so starved, standardised, or
  handed to a vendor. The mirror image, and easy to miss because nobody's
  defending it. *Cost:* you give away the thing that could win you the market.
  Cross-check against the value proposition / NSM (see `upstream-cross-checks.md`).
- **Position copied from a reference model or competitor.** A stage assigned
  because "that's where everyone puts it," not derived from *this* landscape.
  Unfalsifiable until tested against the characteristics.
- **Everything clustered in the middle.** A map where every component is "Product"
  is usually a map nobody pushed on — the easy default. Push for the extremes:
  what's genuinely novel here, and what's genuinely a utility?
- **Inconsistent stage labels for the type.** A *practice* marked "Commodity"
  instead of "Best practice," etc. Minor, but it signals the type wasn't
  considered.

---

## 4. Climate — the rules of the game the map must respect

Climatic patterns are forces you *cannot stop but can anticipate* (~30 in the
book, spanning competition, components, finance, inertia, prediction, speed). A
map that ignores them is a snapshot mistaken for a strategy. The high-value ones
to test a map against:

- **Everything evolves** (under supply/demand competition). A component parked in
  Custom-Built or Product as a *permanent* advantage is a contradiction — the
  market will push it right. Ask: *"What's your plan for when this commoditises?"*
- **Characteristics change as things evolve.** The Genesis end is uncertain,
  changing, and differential; the Commodity end is defined, operational, and a
  cost of doing business. Managing both the same way is an error (see *method*
  below).
- **No choice on evolution (the Red Queen).** You don't get to opt out; you adapt
  or fall behind. "We'll keep doing it our way" is not available as a strategy.
- **Past success breeds inertia.** The more profitable the current position
  (especially Product), the more the organisation resists the move to utility —
  see inertia below. Blockbuster, Kodak.
- **Co-evolution.** New practices co-evolve with evolving activities (e.g. new
  operational practices emerging alongside utility computing). A map that evolves
  an activity but assumes the *practices* around it stay fixed has missed this.
- **Efficiency enables innovation / higher-order systems create new worth.** When
  a lower component commoditises, it becomes a platform for new Genesis above it.
  A map that commoditises something but sees no new opportunity *on top of it* may
  be leaving the real prize on the table.

---

## 5. Doctrine — universal good practice the map should embody

Doctrine is the set of principles that apply *regardless of context* (~40 in the
book, grouped into phases, Phase I being "stop self-harm"). The ones most often
violated *by the map itself*:

- **Focus on user needs / know your users.** Back to the anchor — if the map
  doesn't start from a real user need, doctrine is broken at the root.
- **Remove bias and duplication.** Is the same component built in several places?
  Is something built in-house that the map itself shows as a commodity utility?
  Is positioning bent to justify a decision already taken? (Be transparent;
  challenge assumptions.)
- **Use appropriate methods (no one-size-fits-all).** Method should match
  evolution stage: **Agile/experimentation for Genesis** (high uncertainty),
  **Lean for Products** (refine, reduce waste as they mature), **Six Sigma /
  utility operations for Commodity** (standardise, drive out variance). A map
  whose implied plan applies *one* method across the whole chain — "we're agile
  about everything," or "outsource the lot" — is the classic doctrine failure.
- **The outsourcing trap.** Wardley's most-cited mistake: outsourcing *entire
  systems spanning the whole evolution axis* to a single vendor on one contract,
  because you don't understand the landscape. You should treat the commodity
  parts as utilities and keep the genesis/custom parts close — the map is exactly
  what lets you tell them apart. Outsourcing across the map with one method is the
  red flag.

---

## 6. Inertia — what will resist the move

Wardley catalogues ~16 forms of inertia (resistance to change). When a map
proposes movement, ask *what will fight it*: prior financial investment / sunk
cost, an installed base and existing customers ("we want a faster horse"), loss
of an existing and profitable business model, existing practices and skills,
fear, political capital. A map that proposes evolving a component but names *no*
inertia is almost certainly underestimating the difficulty — surface the missing
resistance, because that's usually where the strategy actually fails.

---

## 7. Gameplay — does the map enable a coherent play?

Gameplay is the ~100 context-specific moves you make *once you can see the board*
(open approaches, ecosystem plays like **ILC — Innovate/Leverage/Commoditise**,
land-grab, tower-and-moat, exploiting constraints, defensive plays). You don't
need to name a play, but it's a useful whole-map test:

- **Does this map suggest *any* deliberate play, or just "keep doing everything
  we do"?** A map with no implied move has stopped at situational awareness and
  skipped the point of having it.
- **Is the proposed play consistent with the climate?** A defensive play on a
  component the market is commoditising anyway is fighting gravity; an attack that
  ignores inertia underestimates the cost.
- **Is value captured at the right layer?** If you commoditise a component, are
  you positioned to benefit from the new Genesis it enables on top, or did you
  hand that ground to someone else?

---

## 8. The falsifiable challenges to keep in your pocket

- For any **Genesis/Custom** position on something that smells common: *"Could
  competitors buy or rent this as a service tomorrow? If yes, it's a product or
  commodity — why are we building it?"*
- For any **Commodity** position near the value proposition: *"Is this really
  undifferentiated for us, or have we just never tried to win here — and are we
  about to outsource the one thing that could win the market?"*
- For any **static** map: *"Everything evolves. Where is each of these heading in
  three years, and what resists that move?"*
- For any **one-method** plan: *"We're treating Genesis and Commodity the same
  way. Which method fits which end of this map?"*