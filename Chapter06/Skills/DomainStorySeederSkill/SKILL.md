---
name: domain-story-seeder
description: >-
  Prepare a Domain Storytelling session by generating sample ("seed" or
  strawman) domain stories as text — numbered sentences in the actor → activity
  → work object grammar. Optionally derives them from a Wardley Map and/or a
  Capability Map: when the prompt doesn't already name a focus, the skill lists
  the map's components or capabilities and asks the user to pick up to three to
  center the stories on (the map itself is optional; a one-line domain
  description also works). Use whenever someone wants to seed, draft, generate, author, or
  prepare sample domain stories, kick off / warm up / prep a Domain
  Storytelling or event-storming-style workshop, turn a Wardley Map or
  Capability Map into a worked example scenario, or produce a numbered
  actor-action-work-object story to put in front of a room — even if they don't
  say "Domain Storytelling." This is the complement of interpreting a domain
  story: here you PRODUCE the story rather than read one.
---

# Domain Story Seeder

Domain Storytelling (Stefan Hofer & Henning Schwentner) captures a business
process as a **concrete story** in a tiny pictographic language: **actors** do
**activities** (verbs) on **work objects** (nouns, also called *work items*),
the sentences are **numbered** to fix the order, and **groups** cluster related
parts. This skill goes the *generative* direction: it writes such stories as
text to **seed a workshop** before anyone draws on the board.

## Why seed stories at all

A Domain Storytelling session works best when the room has something concrete to
react to. A blank board is intimidating; a slightly-wrong **strawman story** is
easy — people correct it, and the corrections are exactly where the real domain
knowledge surfaces. So the job here is to produce plausible, concrete, honestly
labelled starter stories from whatever strategic artifacts already exist.

Hold onto two things:

1. **These stories are disposable scaffolding, not ground truth.** Make them
   concrete and plausible, label every inference, and frame the uncertain spots
   as "is this really how it goes?" prompts for the room. Being a little wrong on
   purpose is fine — it provokes the correction that teaches you the domain.
2. **A domain story is one concrete scenario in pure domain language** — a single
   happy path, present tense, **no conditionals**. The grammar is strict; honor
   it or the output won't read as a domain story.

## The grammar (this IS the output format)

Each numbered sentence:

```
<n> <Actor> <activity-verb> <work object> [linking-word <work object> ...]
```

- **Actor** — a person/role *or* a software system that **acts**. Always the
  grammatical subject. One actor per sentence. (Dispatcher, Returning customer,
  Billing system.)
- **Activity** — a single **concrete present-tense verb** (registers, books,
  approves, picks, sends, confirms). Avoid vague verbs that hide the real action
  (manages, handles, processes, does).
- **Work object** — the noun the activity acts on (Order, Invoice, Booking,
  Route, Cup of tea).
- **Linking words** chain in more work objects and context: *to, for, with,
  from, via, at, in, into, about*. → "Dispatcher assigns Order **to** Driver."
- **Numbered** to fix sequence. **No conditionals, no loops, no branches.** One
  happy path. Alternatives and error cases become their **own** numbered story.
- **Pure domain language** — the words the business actually uses, not technical
  jargon (unless the actor itself is a system).

**Granularity** — pick one level and stay consistent:
*coarse* (a handful of big steps; good for scoping a whole session) vs.
*fine* (every interaction; good for one bounded context). For session prep,
coarse-to-medium is usually right. State which you chose.

## Workflow

### Step 0 — Establish the inputs (all optional)

Inputs may be a **Wardley Map**, a **Capability Map**, **both**, or **neither**.

- If an artifact is provided (image or text), **read it first and echo what you
  found**, so a misreading doesn't poison the story:
    - *Wardley Map* → the **anchor** (user need) at the top, each **component** in
      the value chain, and roughly where each sits on the evolution axis
      (Genesis / Custom-built / Product / Commodity).
    - *Capability Map* → the **capabilities** (stable "ability to do X" nouns),
      their **levels/hierarchy**, and any **core / supporting / generic** marks.
- If **nothing** is provided, don't block — ask for a one-line description of the
  domain or process, or proceed from whatever domain context the user has given.
  A domain story can be seeded from a single sentence.

**A light sanity check, not a critique.** Seeding does **not** require a correct
map — a rough or even shaky map still seeds perfectly usable strawman stories,
and the workshop corrects the rest. So do not pause to evaluate the map's
quality. Only flag the *blocking* defects that would leave you nothing to seed
*from*, and when you see one, name it and offer the dedicated critic skill as an
optional off-ramp rather than attempting the critique yourself:
- Wardley Map with **no anchor / user need**, or one that's really a flow
  diagram or org chart (position carries no meaning) → point to
  `wardley-map-critic`.
- Capability Map whose entries are plainly **processes, projects, or systems**
  rather than "ability to do X" capabilities → point to `capability-map-critic`.
  Phrase it as a choice ("this map looks shaky — want to pressure-test it with the
  &lt;critic&gt; first, or shall I seed from it as-is?"), then proceed however the
  user wants. Default to seeding; the off-ramp is the user's to take.

### Step 1 — Pin down the focus (ask if the prompt didn't specify it)

A domain story is scenario-sized; a whole map is far too big for one story, so a
focus is required before you write. There are two cases:

**(a) The prompt already names the focus** (e.g. "seed a story for *Ask Grandma*
and *Ask community*", or "focus on the *Claims assessment* capability"). Use it
directly — don't ask again. Honor it even if it names more than three; the cap
in case (b) is only for *your* prompt to the user, not a limit on what they can
hand you.

**(b) The prompt does not name a focus.** Then you **must ask** — do not silently
default to a guess. Present the candidates and have the user pick **at most
three**:

- From a **Wardley Map**: offer up to ~three of the most story-worthy
  **components** (and/or the **anchor user need**) as the options; the user may
  select **up to three components**.
- From a **Capability Map**: offer the **capabilities** — lead with the **core /
  differentiating** ones — as the options; the user may select **up to three
  capabilities**.

Use the interactive option picker if available (a multi-select capped at three)
so it's a couple of taps. Keep the option list itself short — surface the
strongest few candidates rather than dumping every box on the map; you can note
that more exist. Only after the user picks (or, if they explicitly say "you
choose," after you pick the single most story-worthy candidate and say which and
why) do you proceed. With **no map at all**, focus on the process the user named;
if even that is missing, ask for a one-line description first (Step 0).

Seeding more than one focus is fine — treat each chosen capability/component as
its own spine and seed a story (plus variants) per focus, up to the three-pick
cap.

### Step 2 — Mine the focus for story elements

Translate the chosen component(s) into Domain Storytelling elements.

**Wardley Map → story elements**

| Wardley element | Becomes in the story |
|---|---|
| Anchor (user need) | the **primary actor** and the **goal** the story drives toward |
| Component high in the chain (user-visible) | an **activity** the actor performs, on a **work object** |
| Component lower in the chain (supporting) | a **work object**, or a **supporting actor/system** the activity uses |
| Genesis / Custom-built component | usually a **human activity** done by hand — name the role doing it |
| Product / Commodity component | usually an **external system actor** providing a service ("Payment service confirms Payment") |
| A link in the value chain | a **linking word** ("via", "with", "from") joining two work objects |

**Capability Map → story elements**

| Capability element | Becomes in the story |
|---|---|
| A capability ("ability to do X") | one or more **activities** (the verb) on the **work object** that X implies |
| The actor who exercises it | the **subject** of those sentences (a role or a system) |
| Core / differentiating capability | the **spine** of the seed story — the scenario worth rehearsing |
| Supporting / generic capability | **off-stage** steps or **system actors** that serve the spine |
| Capability levels / hierarchy | candidate **groups / bounded contexts** if you seed more than one story |

**Then trace the focus component's downstream links — this is where the best
variants hide.** Don't stop at the focus component; follow the lines that run
*down* from it to the components it depends on, because those dependencies often
reveal that one visible activity can be **fulfilled more than one way**. The
sharpest case: a **Custom human-activity sitting on top of an AI/automation
component** (e.g. "Ask Grandma" depending on a "Specific AI" → "Gen AI"
component) almost always hides a **human-vs-system fulfillment fork** — the same
need answered by a person *or* by a system actor (an avatar/assistant). When the
dependency makes a component **domain-visible** (the user talks *to* the
avatar/assistant, not just through plumbing), promote it to a **system actor** in
the story; keep the deeper implementation components (Gen AI, Cloud) off the
page. Seed each branch of the fork as its **own** story in Step 4 — and the
*escalation* between them (avatar can't help → hand off to a human) is usually
the most valuable variant of all, because it's the real design decision the room
should make.

### Step 3 — Write the seed story

Compose the numbered sentences for **one concrete instance**. Make it real:
prefer a concrete protagonist ("a returning customer", "a first-time rider")
and concrete objects. Keep strictly to one happy path. Six to twelve sentences
is plenty for a coarse story. Give the story a **title** naming the scenario and
its trigger ("Returning customer places a repeat order").

### Step 4 — Add variants (optional, each its own story)

Because the grammar has no conditionals, seed the alternative paths and edge
cases the session should explore as **separate short numbered stories** ("…but
the item is out of stock", "…first-time customer with no account"). Offer one or
two; don't bury the main story. **Any fulfillment fork you found in Step 2**
(human path vs. system/avatar path, and the **escalation** from one to the other)
belongs here — each branch is its own story, and the escalation story is usually
the one worth leading the discussion with.

### Step 5 — Hand-off notes for the facilitator

This is what turns a generated artifact into useful session fuel. Briefly list:

- **Assumptions to confirm** — every actor, verb, or ordering you *guessed*.
  Be honest; these are the questions that will draw out real knowledge.
- **Candidate groups / bounded contexts** — suggested by capability levels or
  value-chain clusters, if you seeded more than one story.
- **Questions for the room** — the deliberately uncertain spots. Name them.

## Output template

Use this structure (drop sections that don't apply):

```
# Seed domain stories — <domain / focus>

## Source & focus
Which inputs were used; which component(s)/capability(ies) the story centers on; the granularity chosen.

## Story 1 — <scenario title>
1. <Actor> <verb> <work object> <linking-word> <work object> …
2. …

## Story 2 — <variant title>   (optional)
1. …

## For the session
- Assumptions to confirm: …
- Candidate groups / bounded contexts: …
- Questions for the room: …
```

## Quality checks before you hand it over

- No conditionals, no "if/when/unless", no loops — split branches into separate
  stories instead.
- Active voice, present tense, the **actor as subject** of every sentence.
- Concrete, specific verbs — replace "manages/handles/processes" with the real
  action.
- Domain language, not implementation. The Wardley/Capability inputs tell you
  *what* and *who*, never *how it's built* — don't invent an architecture.
- Every guessed element shows up in "Assumptions to confirm." When in doubt
  about the real business term, pick a plausible one and flag it.

## References

- `references/worked-examples.md` — four fully worked seeds (from a Wardley
  Map, from a Capability Map, from a one-line description with no artifact at
  all, and one that traces a downstream dependency to a human-vs-AI fulfillment
  fork). Read it to pattern-match the mining tables onto real stories, especially
  for how evolution stage picks human-vs-system actors, how a core capability
  becomes the spine, and how following a component's downstream links surfaces
  the best variant stories.