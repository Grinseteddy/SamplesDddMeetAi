# The Four Levels — Critique Question Bank

For each level of the impact map: the core diagnostic question (Adzic, 2012),
what a *strong* version looks like, and the common holes to probe. Pick the
questions that bite hardest for the specific map — this is a menu, not a script
to read aloud. The order matters: a weak **goal** undermines everything below
it, so start there.

---

## 1. WHY — the Goal (centre of the map)

**Diagnostic:** Why are we doing this? What is the measurable business outcome we
want — *not* the thing we plan to build?

**Strong looks like:** an outcome in the world, stated so precisely that you
could tell, on a given date, whether it happened — e.g. "reduce checkout
abandonment from 70% to 50% by Q3," not "improve checkout."

**Common holes:**
- **A solution wearing the goal's hat.** "Build the loyalty app" / "migrate to
  the cloud" is a deliverable, not a goal. Ask "why?" repeatedly until you hit
  the actual outcome it's meant to produce; critique the gap between the two.
- **Unmeasurable.** No metric, no target number, no deadline — so no one can say
  whether an impact worked or when to stop. An unmeasured goal turns the whole
  map into opinion.
- **A vanity / proxy metric.** Measurable but disconnected from real value
  ("increase signups" when the business needs *paying, retained* users). Easy to
  move, doesn't move the business.
- **More than one goal smuggled into the centre.** Two outcomes bundled as one
  means the actors and impacts below serve different masters; the map should
  arguably be split.
- **A goal no one can act on inside the time frame** (so broad it's a mission
  statement, e.g. "delight our customers"), giving no real constraint on what to
  build.

---

## 2. WHO — the Actors

**Diagnostic:** Whose behaviour must change to reach the goal? Who can produce
the desired effect — and who can *obstruct* it?

**Strong looks like:** specific, named roles or personas — primary actors who can
help, supporting actors who enable, and *negative* actors who could block — each
concrete enough to picture a real person.

**Common holes:**
- **"Users" as a single undifferentiated actor.** Hides the fact that no one in
  particular has been thought about. Different segments need different impacts.
- **Only enablers, no obstructers.** The map lists everyone who can help and
  forgets who can *stop* the goal — the regulator, the ops team that must
  support the feature, the partner who controls a channel, the internal team
  whose process the change disrupts. Missing negative actors is where plans get
  blindsided.
- **An actor who's really a system or a department**, too coarse to have a
  changeable "behaviour" (e.g. "Marketing" rather than the specific person whose
  action you need).
- **The actor with the most leverage isn't on the map at all** — often a
  back-office or gatekeeper role no one in the room represented.
- **Actors invented to justify a pre-chosen deliverable** rather than discovered
  from the goal.

---

## 3. HOW — the Impacts

**Diagnostic:** How should each actor's **behaviour** change? What do we want
them to do differently — do more, do faster, start doing, or stop doing — so
that the goal moves? Which actor behaviours could *obstruct* the goal?

**Strong looks like:** a change in what an actor *does*, observable from the
outside, that plausibly moves the goal — "customers reorder without phoning
support," "agents resolve a ticket in one touch."

**Common holes:**
- **A deliverable in the impact slot.** The single most common impact-mapping
  error: "a mobile app," "a new dashboard," "an API" are things we build, not
  behaviour changes. Test every impact with "is this something the *actor does
  differently*, or something *we make*?" If it's the latter, it belongs one level
  out, and the real impact is missing.
- **A restatement of the goal, not a behaviour.** "Customers are more loyal"
  is an outcome, not an observable action — what do loyal customers *do*?
- **Only positive impacts.** Adzic stresses mapping behaviours that *obstruct*
  the goal too, so they can be designed against. A map with no "impacts to avoid"
  has usually not thought about failure.
- **An impact no plausible deliverable could cause**, or one that wouldn't
  actually move the goal even if achieved — a dead branch.
- **Impacts so vague they can't be tested** ("engage more"), giving no way to
  know whether a delivered feature worked.

---

## 4. WHAT — the Deliverables

**Diagnostic:** What is the *smallest* thing we could build or do to support a
desired impact? What's our first bet — knowing it's a bet?

**Strong looks like:** a short, prunable list of options per impact, each framed
as "the cheapest experiment that could cause this behaviour," explicitly held as
an assumption to be validated.

**Common holes:**
- **Treated as scope, not options.** The deliverables read as a committed,
  must-build backlog rather than a set of bets to test and prune. The whole point
  of putting them at the leaves is that they're the *first thing to cut* — a map
  that can't drop a deliverable has lost the plot.
- **No alternatives.** A single deliverable per impact means the team locked onto
  one solution without asking "what else could cause this behaviour, cheaper?"
- **Gold-plating / over-specified.** Rich feature detail far beyond what's needed
  to test whether the impact even occurs (Heath, 2020). Over-building before
  validating the impact assumption.
- **Orphan deliverables.** A feature that doesn't trace up to any impact (and so
  to no goal) — the feature factory leaking back in. Flag it for the cut list.
- **Deliverables that can't be expressed as anything verifiable** — too vague to
  become a testable story or acceptance criterion (Heath, 2020), so "done" will
  be a matter of opinion.

---

## The two fastest structural tests

When you have little time, these two catch the most damage:

1. **Read the goal: is it an outcome or a thing to build?** A solution-as-goal
   miscentres the entire map.
2. **Read each impact: actor-behaviour, or a feature?** Features-as-impacts mean
   the team skipped straight to building and never asked what change they were
   buying.