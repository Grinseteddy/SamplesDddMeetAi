# Map Essentials — Is This Even a Map, and Does the Value Chain Hold?

Use this before any judgement about evolution positions or strategy. A beautiful
picture built on the wrong structure can't support a decision no matter how the
components are placed. Grounded in Simon Wardley, *Wardley Maps: Topographical
Intelligence in Business*. Pick the checks that bite hardest for the specific
map — this is a menu, not a script to read aloud.

---

## 1. What makes a map a *map* (and not a diagram)

Wardley draws a hard line between a **map** and the diagrams business usually
calls maps (mind maps, systems diagrams, business-process flows, customer
journeys, concept maps). A map has a small set of essential elements:

- **Visual.** You can see the whole landscape at once, as a gameboard.
- **Context-specific.** It's about *this* business and *this* user, not a generic
  template.
- **An anchor.** A fixed reference everything is positioned against. On a
  geographic map it's magnetic North; on a Wardley Map it's the **user and their
  need.**
- **Position that has meaning** relative to the anchor — *both* axes carry
  information (value-chain position on the y-axis, evolution on the x-axis).
- **Movement.** Because position means something, you can show how pieces *move*
  (evolve) across the board.

**The single defining test — "space has meaning":** *If you move a component and
the meaning of the picture doesn't change, it isn't a map.* On a real Wardley
Map, sliding a component left or right (more/less evolved) or up or down
(more/less visible to the user) changes what the map is telling you, and someone
can challenge that move and argue about it. That arguability is the entire point.

> Wardley's recurring complaint: *most things called "maps" in business aren't
> maps — they lack an anchor and situational awareness.* Naming that, kindly, is
> often the most valuable single finding you can deliver.

---

## 2. The impostors — what gets drawn instead of a map

When the *space-has-meaning* test fails, identify which non-map it actually is,
then say what it would take to turn it into a map (usually: add the user-need
anchor, and place components on a real evolution axis).

- **A business-process / flow diagram.** Boxes connected by "then this happens"
  arrows. Tell: the arrows are *sequence or flow*, not *dependency on a need*,
  and there's no evolution axis. Re-anchor on the user need and the chain becomes
  a chain of *needs*, not steps.
- **A systems / architecture diagram.** Components and integrations, but position
  is arbitrary — you could rearrange the boxes freely. Tell: no anchor, no
  evolution; left-right and up-down mean nothing.
- **A mind map / concept map.** A central idea with radiating associations. Tell:
  links are "relates to," not "is needed by"; nothing evolves.
- **An org chart in disguise.** Components are really teams/departments, and the
  links are reporting lines. Tell: rename the company and the boxes change. A map
  is about *what the user needs*, not *who reports to whom*.
- **A 2×2 / maturity matrix.** Looks Wardley-ish but the x-axis is labelled
  *maturity*, *time*, or *adoption* and there's no value chain hanging off a user
  anchor. Tell: no chain of needs, and the x-axis isn't evolution (see
  `evolution-and-patterns.md`).

A picture that is mostly an impostor is the headline finding: it isn't yet a
Wardley Map, and any climate/doctrine/gameplay reasoning layered on top is
decoration until the structure is fixed.

---

## 3. The anchor — get this right or nothing else matters

- **There must be a user, and a need.** "Public," "customer," "the business" —
  fine, as long as it's a *who* with a *what they need*. The anchor sets the
  context for every position below it.
- **One map, one anchor (usually).** Multiple unrelated users stacked into one
  map is a sign two maps have been mashed together; the value chain loses
  meaning. Different user types can each warrant their own map.
- **The anchor is a need, not a solution.** "Wants a cup of tea" is a need;
  "wants our TeaApp v2" is a pre-baked solution masquerading as a need, and it
  quietly bends the whole map toward a decision already made (a doctrine
  violation — see `evolution-and-patterns.md`, *remove bias*).

---

## 4. Value-chain / chain-of-needs hygiene

The y-axis is a **chain of needs**: each component sits below the thing that
*needs* it, and visibility to the user decreases as you go down. Check:

- **Every link is a real dependency.** "A needs B to deliver value to the user,"
  not "A happens then B happens." Flow and dependency are different relations;
  drawing flow on the y-axis breaks the map.
- **The chain terminates at the user need** at the top and at commodity/utility
  foundations at the bottom. A chain that floats with no user at the top has lost
  its anchor.
- **No orphans, no dangling needs.** A component nothing depends on (why is it on
  the map?) and a need with nothing beneath it (how is it met?) are both gaps
  worth surfacing.
- **Visibility ordering is roughly right.** The components the user directly
  experiences sit high; the plumbing sits low. Wardley treats the y-axis as a
  *heuristic* (less strict than the x-axis), so don't nitpick exact heights —
  flag only clear inversions (a deep-infrastructure component drawn as
  user-facing, or vice versa).

---

## 5. Components and grain

- **Know the component type.** Wardley maps carry **activities** (what we do),
  **practices** (how we do it), **data**, and **knowledge** — and all of them
  evolve, each with its own stage labels (see `evolution-and-patterns.md`).
  Mixing types without noticing is fine in itself, but *mixing the stage labels*
  (calling a practice "Genesis" when it should be "Novel," etc.) causes confusion.
- **Consistent grain.** Sibling components should sit at a similar level of
  detail. A map where "Compute" sits beside "the specific Tuesday cron job" is
  mixing a coarse capability with a fine task, which distorts both the chain and
  the evolution reading.
- **Don't over-fixate.** Wardley is explicit that you should *not* get hung up on
  making every component relationship perfect — early maps are rough and that's
  fine. Reserve the critique for structural breakage (no anchor, flow-as-chain,
  evolution-as-time), not cosmetic imperfection.

---

## 6. The two fastest structural tests

When you have little time, these two catch the most damage:

1. **Run the space-has-meaning test.** Try mentally shuffling two components. If
   nothing about the map's claim changes, it isn't a map yet — fix that before
   anything else.
2. **Trace one path from a low component up to the user need.** If you can't say
   "the user needs A, which needs B, which needs C" in plain dependency language,
   the y-axis is carrying flow or hierarchy instead of a chain of needs.