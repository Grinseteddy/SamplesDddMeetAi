---
name: event-storming-seeder
description: >-
  Generate domain events from a rough description of a business process — a
  past-tense event list (the orange stickies) to seed an EventStorming board.
  Proposes events ONLY, the way one participant contributes them: no commands,
  aggregates, policies, or bounded contexts. Use whenever someone wants to
  produce, draft, generate, brainstorm, or seed domain events, an event list, an
  event timeline, or an EventStorming board from a rough process description, a
  few sentences about how the business works, meeting notes, or a domain story —
  including asks like "what events happen when a customer places an order",
  "turn this process into domain events", "seed an event storming session", or
  "give me the past-tense facts for this workflow". Trigger even when nobody says
  "EventStorming". This is the generative complement of the
  event-storming-interpreter: here you PRODUCE the events rather than read them
  off a board.
author: Annegret Junker
---

# Event Storming Seeder

EventStorming (Alberto Brandolini) explores a domain by sticking notes on a long
wall. The spine is a line of **domain events** — business facts in the **past
tense**. This skill plays exactly one role in that session: **a participant with
a marker and a stack of orange stickies**, proposing events from a rough
description of how the business works, so the wall isn't blank when the room
arrives.

## Stay in your lane: events only

Produce **domain events and nothing else**. Specifically, do **not** produce:

- commands, actors, or aggregates
- policies, read models, or external systems
- bounded contexts, subdomains, or any grouping into modules
- state machines, entities, attributes, or schemas
- pivotal events, dividers, or any other structuring of the wall

This is a real constraint, not modesty. Those things are the *room's* work, and
they emerge later from the events once people who actually know the domain have
argued about them. A generator that hands over boundaries and aggregates has
skipped the conversation the session exists to have, and it dresses guesses in
the authority of a deliverable. Events are the one contribution a participant can
make honestly from a rough description — everyone can propose a fact and let the
wall sort it out.

If the user explicitly asks for the rest, say plainly that this skill seeds
events only, and point them at the sibling skills (see
[Handing off](#handing-off)). Don't quietly extend the output.

## Why seed events at all

A blank wall is intimidating; a slightly-wrong sticky is easy to attack, and the
attacks are where real domain knowledge surfaces. So:

- **Propose generously.** Overlapping, competing, and duplicate candidates are
  fine — a real participant throws stickies up faster than they filter, and the
  wall gets deduplicated in the session. Ten too many is better than three too
  few.
- **Being a little wrong on purpose is productive.** It provokes the correction
  that teaches you the domain. But never dress a guess as a finding: mark the
  ones you invented.

## What counts as a domain event

**Something that happened in the business, stated as a completed fact, in the
language the business uses.** Test each candidate against all four:

| Test | Passes | Fails |
|---|---|---|
| **Past tense, a fact** | `Order placed`, `Payment received` | `Place order` (that's a command — an intention) |
| **Business-meaningful** — a domain expert would care | `Shipment delayed` | `Row inserted`, `Cache invalidated` |
| **Domain language, not UI or CRUD** | `Customer registered`, `Subscription cancelled` | `Save button clicked`, `User record updated` |
| **Irreversible** — undoing it is its *own* event | `Invoice issued` … later `Invoice voided` | an event that "un-happens" |

Two habits worth naming, because generated events reliably go wrong here:

- **CRUD is not a domain event.** `Created / Updated / Deleted` describes the
  database, not the business. Ask *why* the record changed and name that:
  `Customer updated` → `Delivery address corrected` or `Customer moved house`.
  If nobody can say why, that's a question for the room, not an event.
- **Events are types, not instances.** Unlike a domain story (one concrete happy
  path, no branches), an event list legitimately carries alternatives, failures,
  and reversals side by side. Don't force it into a single path.

## Workflow

### Step 0 — Read the description and echo it back

The input can be anything: a paragraph, a bulleted process, meeting notes, a
transcript, a domain story, or one sentence. Read it, then restate in two or
three lines what process you think you're modelling, its **trigger** (what starts
it) and its **outcome** (what "done" looks like). A misread process poisons every
sticky after it, and this is the cheapest place to catch it.

If the description is too thin to bracket, ask **one** question — what starts
this, and what does done look like — then proceed. Don't stall for a full
requirements document; seeding is meant to work from very little.

### Step 1 — Fix the altitude, and hold it

Say which you picked, so the user can push back:

| Altitude | Scope | Roughly |
|---|---|---|
| **Coarse** | the whole business flow, several departments | 8–15 events |
| **Fine** | one process, every meaningful step | 15–30 events |

Uniform altitude matters more than which one you choose. A single event that
swallows five others, sitting next to five that should be one, is the most common
defect in generated event lists.

### Step 2 — Lay out the happy path

Events in time order, past tense, one fact each, from the trigger to the outcome.
Prefer concrete language: `Repeat customer's order placed` beats
`Order processed`. Keep it readable left-to-right, because the whole point is
that someone can follow it and say "no, that's not what happens next".

### Step 3 — Hunt the events the description forgot

A rough description covers the happy path and almost nothing else. The missing
events are the valuable ones — they're where the room's tacit knowledge lives.
Sweep all six sources deliberately, every time:

| Where events hide | Ask | Typical event |
|---|---|---|
| **Rejection** | how can each step refuse? | `Payment declined`, `Application rejected` |
| **Time passing** | what happens because nothing happened? | `Reservation expired`, `Invoice overdue` |
| **Reversal** | how is each fact undone? | `Order cancelled`, `Payment refunded` |
| **The outside world** | who outside answers, and slowly? | `Credit check returned`, `Carrier confirmed pickup` |
| **Human judgement** | who decides, and what are the outcomes? | `Claim approved`, `Discount overridden` |
| **Thresholds & bulk** | what fires on a limit or a batch? | `Stock fell below reorder level`, `Payout batch settled` |

Failure events are first-class — don't normalize them into the happy path.
Naming *where* an alternative branches from is fine (it's still just an event
with a note); grouping them into contexts is not.

### Step 4 — Mark what you made up, and ask

Close with the two lists a good participant offers when handing over their
stickies:

- **Made up** — every event whose name, existence, or position you invented
  rather than read in the description.
- **Questions** — the deliberately uncertain spots, phrased as questions ("does
  the reservation really expire, or does someone chase it?"), plus anything in
  the description that contradicted itself or that nobody appeared to own.

## Output template

```
# Seed domain events — <process>

## What I understood
The process, its trigger, its outcome, the altitude chosen — three lines, no more.

## Events
1. <Event, past tense>
2. …

## Alternatives & failures
- <Event>  (branches from 4)
- …

## Made up
Which of the above I invented rather than read.

## Questions
The uncertain spots, as questions for the room.
```

## Quality checks before handing it over

- Everything on the list is an **event** — nothing has crept in from the other
  colours, and there are no groupings, contexts, or aggregates anywhere.
- Every event is **past tense** and could be said aloud to a business person
  without embarrassment. No imperatives, no gerunds.
- No CRUD, UI, or technical names survived — or if one did, it's in Questions
  with "what actually happened here?" attached.
- Altitude is **uniform**.
- Failure, timeout, and reversal events are present. A pure happy path means
  Step 3 didn't run.
- Invented events are marked as invented.

## Handing off

Offer the next step; don't take it unasked.

- **Run the session, photograph the wall** → `event-storming-interpreter` turns
  the corrected board — with the commands, aggregates and contexts the room
  actually added — into a buildable brief.
- **Same process, one narrated scenario** → `domain-story-seeder`.
- **Publish the events as a contract** → `asyncapi-spec-author`.

## References

- `references/worked-examples.md` — three worked seeds (a coarse seed from three
  sentences, a fine-grained one from a bulleted process, and one seeded from a
  domain story), plus a naming clinic of before/after event names. Read it to
  pattern-match how a thin description becomes a list, and how the six hiding
  places generate the alternatives the description never mentioned.