# Coherence, Assumptions & Traceability

Once each level holds the right *kind* of thing (see
`four-levels-checklist.md`), the harder failures live in how the levels connect.
A map can have a fine goal, real actors, true behaviour-impacts, and sensible
deliverables and *still* be broken because the chain between them doesn't hold,
or because the team is spread across every branch instead of taking the shortest
path. This is usually where you add the most value.

---

## 1. The sentence test

Every complete branch should read, inside-out, as one sentence:

> **In order to *[goal]*, *[actor]* will *[impact]*, and we will support that by
> *[deliverable]*.**

Read each branch back as that sentence aloud. Where it doesn't parse, the broken
word points at the broken level:

- "In order to *build the app*…" → the goal is a solution (fix the centre).
- "…the customer will *a new dashboard*" → the impact is a deliverable (it
  belongs one level out; the real behaviour is missing).
- "…the customer will *be more loyal*" → the impact is an outcome, not an
  observable behaviour.
- A deliverable that doesn't finish the sentence for *any* impact is an orphan.

---

## 2. Every link is an assumption — name the shakiest

An impact map is a tree of bets. There are two kinds of link, and each is a
hypothesis you can state and test:

- **Deliverable → Impact:** "We believe building *D* will cause *[actor]* to
  *[impact]*." (A delivery assumption.)
- **Impact → Goal:** "We believe that if *[actor]* does *[impact]*, it will move
  *[goal]*." (A value assumption — usually the riskier of the two.)

For the map as a whole, ask:

- **Which single link, if false, collapses the most of the map?** Lead with that
  one. Often it's an impact→goal link the team treated as obvious ("if users
  share more, revenue goes up") that has never been checked.
- **How would we know we're wrong, cheaply?** Push for the smallest deliverable
  or measurement that would validate or kill the assumption *before* full build.
- **What's the base rate?** Has a similar impact moved a similar goal before, or
  is this hope?

The discipline that makes this useful: state the assumption *as the team's*, not
as your verdict — "the branch assumes X; is that tested?" — and invite them to
show you the evidence.

---

## 3. The shortest-path / prioritization lens

Adzic's central use of the map is not to build all of it — it's to find the
**shortest path to the goal**: the one actor-and-impact that moves the metric
most, for the least delivery, validated first. So:

- **Is anything prioritized at all,** or is every branch staffed at once? A map
  worked end-to-end across all branches is a feature factory with a nicer
  diagram — the exact failure mapping is meant to prevent.
- **Is the chosen first branch actually the highest-leverage one,** or just the
  most obvious / most fun to build? Challenge the selection, not just the
  contents.
- **Is the plan iterative?** The intended loop is: pick one impact, deliver the
  *minimum* to cause it, **measure against the goal**, then revisit the map. A
  map presented as a fixed 12-month plan has misunderstood the tool.
- **What gets cut?** If the team can't name which branches they're *not* doing
  yet, they haven't really prioritized.

---

## 4. Traceability — down to verifiable specs, up to the goal

Heath (2020) treats the impact map as the front of a requirements funnel:
deliverables become epics, features, and ultimately *executable specifications*
(testable "given / when / then" behaviour). That gives two traceability checks:

- **Downward — is each deliverable specifiable and verifiable?** Could it be
  written as a concrete, testable outcome, or is it so vague that "done" is a
  matter of opinion? A deliverable that resists any acceptance criterion is a
  risk: you won't be able to tell if you built the right thing.
- **Upward — does every deliverable trace to an impact and a goal?** Walk the
  tree from the leaves: any feature that doesn't connect upward is an orphan and
  a candidate for the cut list. Any goal with no measurement has no way to
  confirm the features beneath it ever worked.

Strong traceability means you can start at the metric and walk down to a testable
spec, and start at any feature and walk up to the outcome it serves. Breaks in
either direction are findings.

---

## 5. Measurement closes the loop

Without a measured goal (metric + target + deadline) none of the above can be
settled — you can't tell which impact moved the needle, which assumption was
right, or when to stop building a branch. If the goal isn't measurable, that's
frequently *the* finding, because it disables the map's entire feedback loop.
Push for: what number, at what value today, to what target, by when.