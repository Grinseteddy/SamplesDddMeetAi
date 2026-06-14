# Classification & the Core Domain Patterns

The chart sorts each domain into **Core**, **Supporting**, or **Generic** by where
it lands on differentiation × complexity — and Nick Tune's **Core Domain Patterns**
give named positions and *movements* that turn a placement into a diagnosis. This
is your richest vocabulary as a critic: instead of "I'm not sure about this dot,"
you can say "this is a *Hidden Core*, and here's why that's a trap."

The classification is always relative to a *specific* business and market. Your job
is less to assert the "right" position than to test whether each placement is
*defensible* and whether the classification *as a whole* still carries information.

---

## The three subdomain types (and what each implies)

### Core — high differentiation (right side)
The "secret sauce": a domain that creates competitive advantage, where the expected
ROI of investment is greatest. **Decision:** *build in-house, invest, put the best
people here, keep widening the gap.* For a domain to truly be core there must be
**both** high differentiation **and** at least reasonable complexity — high
differentiation with trivial complexity isn't defensible (see *Short-term* and
*Hidden Core* below).

### Supporting — low differentiation, low complexity (bottom-left)
Specific to this business and *necessary* to deliver, but not a source of
advantage; parity is enough. **Decision:** *do it efficiently; spend the minimum
that keeps it at par.* There's a cliff beyond which extra investment yields nothing.

### Generic — low differentiation, off-the-shelf (left, often high complexity)
Not unique to your business — identity, payments, email, notifications, search.
Everyone needs it; nobody wins on it. **Decision:** *buy SaaS or use open source;
never build bespoke.* Generic domains can be genuinely *complex* (payments,
identity) — which is exactly why building them is such an expensive mistake.

> A clean way to hold the three: **core = where you must be *better*; supporting =
> where you must be *good enough*; generic = where you must be *cheap* (and ideally
> someone else's problem).**

---

## The Core Domain Patterns — your diagnostic catalogue

Each pattern is a position or a movement on the chart. Name the pattern, say why it
matters, and pair it with the question that tests it.

### Decisive Core — *top-right: high differentiation, high complexity*
Maximum advantage *and* hard to build. Whoever gets this right can dominate the
market, and the complexity makes the lead defensible — but it demands a big,
focused investment. **Critic's job:** make sure a domain you both agree is decisive
is *actually being funded like it's decisive*. A decisive core starved of people is
the most expensive contradiction on the chart.

### Short-term Core — *bottom-right: high differentiation, low complexity*
Differentiating today, but so simple that competitors will copy it quickly. Not a
defensible advantage. **Question:** "If a rival could rebuild this in a quarter,
what's the plan for when they do — deepen it (push up/right) or harvest it before
it decays?"

### Hidden Core — *low complexity, but differentiating* (an anti-pattern to watch)
A simple CRUD-looking system that nonetheless drives differentiation. Two
possibilities, both worth surfacing: (a) competitors will easily reach parity, or
(b) the real complexity is still being handled **manually** by employees with
spreadsheets, and the software is just replacing paper. The hidden opportunity:
*"Can we let computers do the hard work people are doing here?"* — automating the
operational complexity can convert a Hidden Core into a Decisive one.

### Big Bet / Disruptive Core — *high complexity, differentiation unknown*
The potential advantage is real but unproven until the market responds. Because the
upside could be enormous (industry disruption), the organization injects serious
resources and makes it a primary focus. **Critic's job:** is this an *acknowledged*
bet with a learning plan, or an unexamined gamble dressed as a sure thing?

### Black Swan — *an apparent commodity that becomes core*
Sometimes the unexpected happens and a domain everyone treated as generic turns out
to be a market-maker (Tune's example: Slack, born as an internal chat tool). You
can't plan a black swan, but you *can* stay alert to a generic domain where you're
quietly doing something nobody else is. **Use sparingly** — "but what if it's a
black swan?" is not a licence to build every commodity in-house.

### Table-Stakes Former Core — *was core, drifting left*
The natural lifecycle: an innovation that once differentiated becomes something
everyone expects (contactless payment was once a reason to choose a shop; now it's
assumed). Still needed, no longer winning. **Question:** "What's replacing this as
the source of advantage now that it's table stakes — and is *that* on the chart?"

### Commoditised Core — *was core, has slid into generic*
More drastic than table stakes: what was a differentiator becomes a buyable
commodity (advanced search, once bespoke, commoditised by Elasticsearch). Continuing
to build it in-house now burns money for nothing. **Question:** "Is there a SaaS /
open-source option that just erased this advantage? If so, why are we still building
it?"

### Suspect Supporting — *top-left: high complexity, low differentiation*
A domain drowning in complexity yet providing little advantage. Why is so much
investment going into something that doesn't differentiate? One legitimate answer is
*accidental* complexity — e.g. a mid-flight migration from an old system. But then
there must be a **clear plan and timeline** to reduce it and re-allocate the effort
to higher-differentiation domains. Without that plan, this is usually the single
biggest waste on the chart. (Closely related: **Generic built in-house** — a complex
*generic* domain being built instead of bought.)

---

## The evolution-arrow lens

A static chart is a snapshot mistaken for a strategy. Domains move:

- **Leftward** (differentiation decays) as the market catches up — the default
  drift. *Table-Stakes Former Core* and *Commoditised Core* are leftward journeys.
- **Rightward** (more differentiating) by deliberate innovation — usually paired
  with an *upward* move, because deepening advantage tends to add complexity.
  *Big Bet* is a deliberate rightward push; *Black Swan* an accidental one.
- **Downward** (less complex) as teams simplify and cut total cost of ownership.
- **Upward** (more complex) — sometimes deliberate (to differentiate), often
  *accidental* and unwanted (the slide toward *Suspect Supporting*).

For each interesting domain, ask where it's heading and over what horizon. The two
classic alarms the arrows expose: **(1)** the core is decaying with *nothing lined
up to replace it* (under-investing in future advantage); **(2)** lots of people on
low-differentiation domains that could be outsourced, *starving* the core. Re-assess
on a cadence (every 6–12 months is a common starting point).

---

## The common mis-placements — what to challenge

- **Core inflation (the big one).** A third or half the chart top-right. If
  everything is core, nothing is — the chart no longer guides investment, which was
  its only job. Push: "If you could protect only three domains from a budget cut,
  which three? Those are core; be sceptical of the rest."
- **Necessity- or complexity-as-core.** "We can't operate without it" or "it's
  really hard" → placed core. Mission-critical ≠ differentiating; hard ≠
  advantageous. A complex domain that doesn't win customers is Suspect Supporting or
  Generic-built-in-house, not core.
- **Hidden / Short-term Core mistaken for Decisive.** High differentiation with low
  complexity treated as a durable advantage. Ask what makes it *defensible*.
- **Generic built in-house.** A complex commodity (payments, identity, search)
  placed and resourced as if building it added value. Cross-check against buy/SaaS
  options before accepting any in-house build of a generic domain.
- **Strategy- and industry-dependence ignored.** The same domain is core for one
  firm and generic for another (low-latency execution is core for a high-frequency
  trader, irrelevant for a buy-and-hold fund). A placement copied from a reference
  model or a competitor, rather than derived from *this* business, is unfalsifiable.
- **Static snapshot.** No arrows, no sense of movement — the chart can't tell you
  what's decaying or what to bet on next.

---

## The falsifiable challenges to keep in your pocket

- For any **core** placement: *"What can you do in this domain that a competitor
  buying the same SaaS and hiring the same people couldn't replicate within a year?
  If the honest answer is 'nothing yet,' it's parity — supporting at best — not
  core."*
- For any **bottom-right** dot: *"It's differentiating but simple — so what stops a
  rival copying it next quarter?"*
- For any **top-left** dot: *"This is expensive and doesn't differentiate. Is the
  complexity essential, or is this accidental complexity we should be paying down?"*
- For any complex **generic** dot being built: *"Is there an off-the-shelf option
  that does this? If so, what does building it ourselves buy us?"*