# Capability Essentials — Is This Even a Capability, and Does the Map Hold?

Use this before any value judgement about core/supporting/generic. A coloured-in
map built on the wrong kind of boxes can't support a decision no matter how the
boxes are coloured. Grounded in The Open Group, *Business Capabilities V2*
(G211). Pick the checks that bite hardest for the specific map — this is a menu,
not a script to read aloud.

---

## 1. What a capability *is*

A **business capability** is a stable noun phrase naming *an ability the
enterprise has* — "what the business can do," held constant while the *how*, the
*who*, and the *with-what* underneath it change over time.

**Strong looks like:** "Demand Forecasting," "Claims Management," "Customer
Onboarding," "Pricing," "Regulatory Compliance Management." Each could survive a
reorganization, a system replacement, and a process redesign and still be the
same capability.

**The diagnostic:** *Could this still be called the same thing after you swap the
team, the software, and the process behind it?* If yes, it's probably a real
capability. If the name dies when you change the implementation, it isn't one.

---

## 2. The five impostors — what a capability *isn't*

The single most common defect in a capability map is non-capabilities wearing the
box. Name the impostor, then state the capability it implies (or flag that none
is clear).

- **A process / activity (verbs).** "Process a claim," "onboard the customer,"
  "run payroll" describe *how work flows*, not *what the business can do*. Tell:
  it reads as a verb or a sequence. The capability is the stable noun behind it
  ("Claims Management").
- **An org unit / team.** "The Claims Department," "Marketing," "the Data Team."
  Tell: it names *who*, and it would be renamed in a reorg. A capability is owned
  *by* an org unit but is not the org unit.
- **A project / initiative.** "Cloud Migration," "Project Atlas," "the 2026 CRM
  rollout." Tell: it has a start and end date. Capabilities persist; projects
  *change* capabilities.
- **A technology / system.** "Salesforce," "the data lake," "the mobile app."
  Tell: it's a product or asset you could buy. A capability may *depend on* a
  system but is not the system — you can deliver "Customer Management" on three
  different CRMs.
- **A goal / KPI / outcome.** "Increase retention," "be the cheapest," "delight
  customers." Tell: it's something you want to *achieve*, not something you can
  *do*. (This one matters here because the core/supporting/generic marking is
  *about* strategic contribution — keep the capability and the outcome it serves
  distinct, or the marking becomes circular.)

A map that is mostly impostors is the headline finding: it isn't yet a capability
map, and any core/supporting/generic marking on top of it is decoration.

---

## 3. Stratification vs. leveling — two different structures

G211 gives two ways to organize the same set of capabilities. Keep them straight,
and keep *both* separate from the value marking.

- **Stratification = tiers by altitude / audience.** G211's worked example tiers
  capabilities as **Strategic** (direction-setting, executive span of control),
  **Core** (the customer-facing engine of the business), and **Supporting**
  (essential but behind the scenes). This is about *where in the business* a
  capability sits and *which stakeholder cares*, not about competitive advantage.
  **Caution:** these tier names overlap with the value-marking words but mean
  something different — see `classification-core-supporting-generic.md`. When a
  map says "core," always ask which sense is meant.
- **Leveling = decomposition depth.** Each Level 1 capability decomposes into L2,
  L3… for audiences who need more detail (executives often want only L1;
  architects and planners go deeper). Leveling answers *how much detail*, not
  *how important*.

---

## 4. Map-level coherence — the good-map question bank

- **MECE-ish.** Are the capabilities *mutually exclusive* (no two boxes that are
  really the same ability under different names) and *collectively exhaustive*
  enough (no glaring gap — e.g. a business that clearly sells but has no "Sales"
  or "Channel Management" capability)? Perfect MECE is a myth; large overlaps and
  obvious holes are the findings.
- **Consistent granularity among siblings.** Do the children of one parent sit at
  the same level of abstraction? A branch where "Pricing" sits beside "Set the
  Tuesday discount" is mixing an L1 ability with an L3 task.
- **Consistent leveling.** Are you comparing like with like — no L1 capability
  drawn as a sibling of someone else's L3? Mismatched depth makes the map look
  complete while hiding whole areas.
- **Stable nouns throughout.** Run the swap test (section 1) across the whole map.
  One or two impostors are easy to fix; a map that's half impostors needs
  rebuilding before it can be marked.
- **The map is implementation-agnostic.** A good capability map describes the same
  business whether it's run by 10 people or 10,000, on-prem or cloud. If the map
  changes every time the org chart or tech stack does, it's modelling the *how*,
  not the *what*.

---

## 5. The two fastest structural tests

When you have little time, these two catch the most damage:

1. **Run the swap test on every box.** Verbs, team names, project names, and
   product names that fail it are impostors — fix these before anything else.
2. **Count the "core" boxes (once they're marked).** If a third or more of a real
   capability map is marked core, the value marking has collapsed and the
   investment lens it's supposed to enable is dead on arrival — go to
   `classification-core-supporting-generic.md`.