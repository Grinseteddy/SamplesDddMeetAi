---
name: event-storming-invariant-finder
description: >-
  Extract business rules — invariants — from an EventStorming board: the hidden
  preconditions a command must satisfy before its event ("a copy must be
  available before a book can be borrowed"), the status models of aggregates
  read off the state stickies, and the cardinality, ordering, authorization and
  lifecycle rules the board implies but never writes down. Every rule is scoped
  to ONE bounded context and ONE aggregate, written as a Given/When/Then
  scenario with the rejection it causes; rules spanning two contexts are demoted
  to policies with a staleness window and a compensating action. Use whenever
  someone has an EventStorming board, sticky photo, Miro export or
  command/event/aggregate list and wants the rules, invariants, constraints,
  preconditions, guards, state machines or status models — "what are the rules
  behind this board", "derive the aggregate state machines", "what must be true
  before this command succeeds". Trigger even when nobody says "invariant".
  Grounded in Brandolini, Evans and Vernon.
author: Annegret Junker
---

# EventStorming Invariant Finder

An **invariant** is a business rule that must be true **before and after every
single command**, with no window in which it is allowed to be false. It is not a
preference, not a validation, not a workflow step. It is the sentence that makes
the business say *no*.

Invariants are the least visible thing on an EventStorming board, because the
board records what **happened** and rules are about what is **not allowed to
happen**. The board is a wall of successes. This skill reads the wall for the
failures it implies.

Three commitments make the output trustworthy:

- **A rule with no rejection is not a rule.** For every invariant you must be
  able to name what the business refuses, in the business's own words. If the
  answer is "nothing, it just wouldn't make sense", you have written a
  description, not an invariant. Delete it.
- **An invariant belongs to one consistency boundary in one bounded context.**
  If enforcing it needs data owned by another context, it is not an invariant
  there — it is a policy with a lag and a compensation. §6 exists for those, and
  demoting a rule into it is a *finding*, not a failure.
- **Do not invent rules.** A gap in the board is evidence about how well the
  domain is understood. An invented invariant hardens a guess into a database
  constraint, and those are the most expensive fictions in software.

## Where invariants hide on a board

Nine hiding places. Full detection heuristics per sticky colour and per
category: `references/invariant-catalogue.md`.

1. **The gap between a blue command and its orange event.** Every command can
   fail. *Why?* is the invariant. This is the richest seam on any board — one
   command usually yields two or three rules.
2. **The state stickies.** Small annotations attached to events (*To be read*,
   *Reading*, *Already read*) are the aggregate's **status model**. The set of
   legal transitions is itself an invariant, and the strongest one you will find,
   because the team wrote it down without noticing.
3. **The read model a command consults.** A green sticky next to a command is
   the data someone looks at to decide *may I?*. What they check is the guard.
4. **Policies** — *whenever X, then Y*. A policy is the shape a rule takes when
   it **cannot** be an invariant. Read it as a confession about consistency.
5. **Hotspots.** Red stickies are arguments, and people only argue about rules.
6. **Repeated events and loops.** *Can this happen twice?* is a cardinality
   invariant hiding as a drawing convention.
7. **The actor sticky.** A different actor on the same command means an
   authorization rule; the same command with two actors on the board means the
   rule is contested.
8. **Terminal events.** The last event in a lifecycle implies an absorbing
   state, and absorbing states forbid every command that came before them.
9. **The commands that are missing.** No *Return book* on a lending board means
   either an unmodelled state or an unowned rule. Absence is evidence.

## Five tests every candidate rule must pass

A candidate that fails any test is not an invariant of this aggregate in this
context. It is not discarded — it is routed (§ below the tests).

1. **The always test.** Is there any moment, however brief, in which the rule
   may be false and later repaired? If yes it is **eventually consistent** →
   route to §6 as a policy. *"Every borrowed book appears on the reading list"*
   fails this the moment the board says the list is updated automatically.
2. **The single-owner test.** Is there exactly one aggregate that holds all the
   data needed to decide? Two aggregates in one rule means: merge them, move the
   rule, or accept a lag. Never a distributed transaction.
3. **The context test.** Is every noun in the rule owned by *this* bounded
   context, in *this* context's meaning of the word? A rule that reaches across
   a border is either a local rule about a locally-held copy, or a policy. Note
   that the same word can appear in two contexts with different rules attached —
   that is normal and must be stated, not resolved.
4. **The rejection test.** Name the refusal: what the business says, who hears
   it, and what they do instead. *"Rejected: this copy is on loan until 4 March;
   would you like to reserve it?"* A rule you cannot refuse is decoration.
5. **The decision-data test.** Is the data checked *inside* the transaction, or
   is it read from a projection that may be stale? A guard on a read model is
   **advisory**: it improves the odds, it does not hold the line. Say so and name
   the real enforcement point, or accept the rule as advisory and pair it with a
   compensation.

**Routing what fails.** Test 1 or 2 fails → §6, restated as a policy. Test 3
fails → §6, as a cross-context rule with an owning context named. Test 4 fails →
drop it, with a line saying why. Test 5 fails → keep it, marked *advisory*, with
the compensating action attached.

## Scope: one context, one aggregate

State the bounded context before every rule and never let a rule float free.
Concretely:

- Rules are **numbered per context**: `INV-LEND-03`, `INV-READ-01`. The prefix
  is not decoration — it is the claim that this context can enforce it alone.
- If the board has **no context bubbles drawn**, say so and either use the
  team's own groupings, or run `pivotal-event-boundary-finder` /
  `event-storming-context-finder` first and mark the resulting cut as
  provisional. Rules derived against a provisional cut carry that caveat.
- **The same business concern legitimately produces different rules in different
  contexts.** *Availability* in a Catalog context is "is this title in print";
  in a Lending context it is "is a copy not currently on loan". Write both,
  under their own contexts, and do not reconcile them.
- A rule that keeps refusing to sit in any one context is telling you the
  boundary is wrong. Record that in §7 — it is one of the most valuable outputs
  this method produces.

## Workflow

### Step 1 — Read the board into a grid

Normalise whatever arrived into one table: event, the command that caused it,
the actor or system issuing it, the aggregate it changes, the read models
consulted, the state annotation it leaves behind, and any hotspot on it. Mark
loops, branches and anything whose position you are guessing.

Two reading rules that decide the whole result:

- **State stickies are not events and not read models.** A small annotation
  carrying a status word (*To be read*, *Reading*, *Already read*, *Automatic
  updated*) is the *post-state* of one of the things the event touched. Decide
  **which** thing it belongs to before going further; attaching a state to the
  wrong aggregate produces a confident, wrong state machine.
- **Separate provenance from meaning.** Marks that record *who added a sticky*
  (AI-proposed, imported, added later, a different hand) share a visual channel
  with marks about domain meaning. Confirm any unusual marker before building a
  rule on it.

For a busy board, show the grid and ask for confirmation before deriving. A
misread state sticky is cheap to fix here and expensive in §3.

### Step 2 — Establish the consistency boundaries

One table before any rule is written:

| Context | Aggregate | Root identity | Commands it accepts | States it owns | Data it holds |
|---|---|---|---|---|---|

An aggregate with no command is a read model. A command with no aggregate is a
gap — record it, do not guess an owner. This table is what makes test 2
answerable, so do not skip it because the answer "seems obvious".

### Step 3 — Build the status models

For each aggregate that carries state stickies:

1. **States** — collect the annotations, add the implied initial state (the one
   the creating event puts it in) and any terminal states.
2. **Transitions** — one row per legal move: from-state, command, event,
   to-state, guard.
3. **Diagram** — a Mermaid `stateDiagram-v2`, labelled `Command / Event`:

   ```mermaid
   stateDiagram-v2
       [*] --> ToBeRead: Update reading list / Reading list updated
       ToBeRead --> Reading: Select book / Book selected for reading
       Reading --> AlreadyRead: Finish book / Book finished
       AlreadyRead --> [*]
   ```

4. **Harvest the invariants the machine asserts.** A state machine is a compact
   way of writing several rules at once; write them out anyway, because the
   refusals are what the team will argue about:
    - **legal-transition rule** — the only moves permitted are the ones drawn
    - **guard rules** — one per command, naming the states it is accepted in
    - **terminal-state rule** — what an absorbing state forbids, and what the
      business does instead (a *new* entry? a re-open command? nothing?)
    - **no-backward rule** — and the compensating command if going back is
      allowed after all
    - **missing-state finding** — a status word that is a *processing note*
      rather than a business state (*Automatic updated*, *Synced*, *Pending*) is
      a gap: the real states were never elicited. Say so.

Name states in the domain's words, exactly as written on the board. Do not
tidy *To be read* into *PLANNED*.

### Step 4 — Derive the rules, category by category

Work the seven categories in `references/invariant-catalogue.md` per aggregate:
state, precondition, cardinality & uniqueness, intra-aggregate consistency,
ordering, authorization, lifecycle. Working the categories in order is what
stops the output from being three obvious rules and nothing else.

For each candidate: run the five tests, then write it up as

```
INV-LEND-02 · Borrowing limit
A Member MUST NOT hold more than <n> concurrent loans.
  Kind        cardinality
  Aggregate   Lending list (root: Member)
  Triggered   Borrow book
  Rejection   "You have reached your borrowing limit; return a book first."
  Evidence    board: Lending list read model consulted by Borrow book
  Confidence  inferred — the limit <n> is not on the board
```

**Mark confidence, always.** `on the board` (a state sticky, a read model, a
hotspot), `implied` (the board makes no sense otherwise), `inferred` (domain
knowledge, needs confirming). An inferred rule presented as read is the failure
mode of this whole method.

### Step 5 — Write the scenarios

Every invariant gets **at least one violation scenario**, because the refusal is
the rule. Add the happy path only where it clarifies the guard.

```gherkin
Scenario: A copy on loan cannot be borrowed again
  Given the Catalog Entry "Domain-Driven Design" has 1 copy
    And that copy is on loan to Member "M-17"
   When Member "M-42" issues Borrow book for "Domain-Driven Design"
   Then the command is rejected with "no copy available"
    And the Lending list is unchanged
```

Conventions: one aggregate per `Given` line; the `When` is a **command**, never
a UI action; the `Then` is a rejection plus the state that did **not** change.
Use only terms from this context's vocabulary. Where a rule is advisory (test 5),
say so in the scenario name — *"…is rejected on a best-effort basis"* — and add
the compensating scenario.

### Step 6 — Demote what could not stay

Non-negotiable section. For each rule that failed test 1, 2 or 3, write it as a
policy rather than dropping it:

- **the rule as the business states it** — verbatim, because they will look for it
- **why it cannot be an invariant** — which test failed, in one line
- **the policy form** — *whenever `<event>` in `<context A>`, then `<command>` in
  `<context B>`*
- **the local data it acts on** — the replicated copy or read model, and who owns
  the original
- **the staleness window** — in business terms ("until the nightly run", "seconds")
- **the compensating action** — what the business does when the rule was violated
  during the window. If nobody can name one, that is the finding: the business
  has never had to, which usually means the rule matters less than claimed.

### Step 7 — Say what the board cannot tell you

- **Missing states** — aggregates with commands but no state stickies; processing
  notes standing in for business states.
- **Missing commands** — a state that can be entered and never left; a lifecycle
  with no ending; the *Return book* problem.
- **Unowned rules** — rules with no aggregate able to enforce them. Each one is
  either a missing aggregate or a wrong boundary; say which you suspect.
- **Boundary stress** — any rule that would only work if two contexts merged.
  Name the merge it argues for and the cost, and leave the decision open.
- **Numbers nobody supplied** — every limit, threshold, window and count you had
  to write as `<n>`. Collect them into one list; it is the shortest possible
  agenda for the next workshop.
- **Hotspots verbatim** — every red sticky, copied through, mapped to the rule it
  is arguing about.
- **The questions that would settle it** — concrete, answerable by one expert in
  one sentence. *"If a member finishes a book they never borrowed, is that an
  error or just unusual?"* decides an invariant; your guess at the answer does
  not.

## Output: the Invariant Sheet

```
# Invariants — <board or process name>

## 1. Board as read
Grid of event · command · actor · aggregate · read models · state · hotspot.
Assumptions and unreadable stickies flagged.

## 2. Consistency boundaries
Context / aggregate / identity / commands / states / data.

## 3. Status models
Per aggregate: state table, Mermaid stateDiagram-v2, and the transition,
guard, terminal-state and no-backward invariants it asserts.

## 4. Invariants by bounded context
Per context, per aggregate, by category. Each: id · statement · kind ·
aggregate · triggering command · rejection · evidence · confidence.

## 5. Scenarios
Given/When/Then per invariant — violation always, happy path where useful.
May be folded into §4 for short boards.

## 6. Not invariants
Cross-context rules as policies with staleness and compensation; advisory
guards; dropped candidates with the test that killed them, one line each.

## 7. Gaps, hotspots and open questions
Missing states, missing commands, unowned rules, boundary stress, the list of
unsupplied numbers, hotspots verbatim, questions for the domain expert.
```

Adapt depth to the request. "Just give me the state machines" gets §3 and the
guard rules — but never drop §6, because a rule sheet that silently omits the
demotions reads as a set of enforceable constraints when it is not.

## Traps worth checking before you publish

- **The description in rule's clothing.** *"A reading list entry has a state"* is
  not an invariant. If it cannot be violated, it is a data model.
- **The workflow step.** *"The member must search before borrowing"* describes
  the happy path. Ask whether the business would actually refuse a member who
  arrived with the shelf mark already in hand.
- **The technical validation.** Formats, lengths, required fields, unique
  database keys. Business-meaningless constraints belong in the schema, not here.
- **The cross-context invariant.** The most common and most expensive error:
  writing *"a book must be borrowed before it can be read"* as an invariant when
  Lending and Reading are separate contexts. It is a policy. §6.
- **The stale guard.** Checking a read model and calling it enforcement. Two
  members pass the same availability check in the same second.
- **The state machine with no rejections.** Every arrow drawn, no arrow forbidden.
  The value is in the moves you did *not* draw, so state them.
- **The invented threshold.** `<n>` is an honest answer. `3` is a fabrication.
- **The tidied state name.** Renaming *To be read* to *PENDING* deletes the
  ubiquitous language the board was run to discover.
- **One rule per command.** Commands with exactly one guard each usually means
  the seam in hiding place 1 was worked shallowly. Go back and ask "what else
  could make this fail?" twice more.
- **Reconciling two contexts' versions of a word.** Resist. Two rules, two
  contexts, both correct.

## Working with the neighbouring skills

- No bounded contexts drawn on the board → `pivotal-event-boundary-finder` or
  `event-storming-context-finder` first; rules are only as trustworthy as the
  cut they are scoped to.
- A buildable brief — modules, model, flows, screens — rather than rules →
  `event-storming-interpreter`. The two are complementary: that skill reads the
  board's successes, this one its refusals.
- Only a process description, no board yet → `event-storming-seeder`, or
  `domain-story-event-seeder` when domain stories exist.
- A **Visual Glossary** exists → reuse its exact terms and cardinalities; a
  cardinality asserted there is an invariant candidate already elicited, and a
  contradiction between glossary and board is a §7 finding.
- Rules destined for an API contract → `openapi-spec-author` /
  `asyncapi-spec-author`; the rejections in §4 become the error responses.
- Checking an existing schema against the rules → `schema-glossary-consistency`.
- The board itself looks doubtful → `event-storming-*` critique first; deriving
  rules from a board nobody trusts multiplies the error.

## Reference files

- `references/invariant-catalogue.md` — the seven categories in full, each with
  its diagnostic question, board evidence, the elicitation prompts that surface
  it, and worked micro-examples; the sticky-colour reading table; the five tests
  in full with their misfires; Gherkin conventions; and the cross-context
  demotion recipe.
- `references/worked-example.md` — a three-context library board (Catalog
  management, Lending, Reading) worked end to end: two status models with
  diagrams, fourteen invariants with scenarios, four demoted cross-context
  rules, and the gaps the board could not answer. Read this first when unsure
  how deep to go.

## References

A. Brandolini, *Introducing EventStorming: An Act of Deliberate Collective
Learning.* Leanpub, 2021.

E. Evans, *Domain-Driven Design: Tackling Complexity in the Heart of Software.*
Boston, MA, USA: Addison-Wesley, 2003.

V. Vernon, *Implementing Domain-Driven Design.* Boston, MA, USA:
Addison-Wesley, 2013. — especially "Effective Aggregate Design": model true
invariants in consistency boundaries; use eventual consistency outside them.