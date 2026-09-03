---
name: screen-flow-from-event-model
description: >-
  Turn an Event Model (Dymitruk: a swimlaned timeline cut into State Change /
  State View / Automation / Translation slices, often arriving as several
  board photos that need stitching) into a SIMPLE screen flow as a Mermaid
  flowchart — screens as nodes, the command or policy that moves the user as
  labelled edges, plus a screen table, a transition table and a reachability
  check. Use whenever someone has an Event Model, an event-modeling board, or
  event-model-author output and wants the screens, the navigation, the UI or
  user flow, a sitemap, a click path, or "which screens do we need and how do
  you get from one to the next" — including "make a screen flow from these
  images", "turn this event model into a mermaid diagram", "draw the
  navigation for this board", or an uploaded board photo with "what are the
  screens?". Trigger even when nobody says Event Model, screen flow or
  Mermaid. Draws only what the board draws — missing screens are reported as
  open questions, never invented.
author: Annegret Junker
---

# Screen Flow from an Event Model

An Event Model already contains a screen flow — it just doesn't draw one. The
wireframes along its top row *are* the screens; the commands under them are
the buttons; the events and policies are the reasons the user ends up
somewhere else. This skill extracts that and nothing else: **one Mermaid
flowchart a designer or developer can read in ten seconds**, backed by a
screen table, a transition table, and a check that every screen can actually
be reached and left.

The job is extraction, not design. Every screen traces to a wireframe, a read
model, or an explicit assumption you flagged. **A screen the board doesn't
draw doesn't go on the diagram** — it goes in §5 as a question. Adding the
login page, the home dashboard and the settings screen the model forgot is
the single most common way this output stops being trustworthy: the moment
one screen is invented, the reader can no longer tell which of the others
were.

## What "simple" means here, and why it's the constraint

The parent skill (`event-model-author`) already renders the full grid, with
wireframes, commands, events, read models and policies all drawn. Repeating
that is not this skill's job. Simple means:

- **One node kind carries the meaning: the screen.** Commands, events and
  read models appear as *edge labels* or not at all.
- **No Given/When/Then, no field lists, no invariants.** Those live in the
  Event Model and in `event-model-invariant-finder`.
- **Automation and Translation slices produce no screen.** A policy is an
  arrow that moves the user, not a page they look at.
- **A ceiling of about 15 screens per diagram.** Past that, split — one
  diagram per swimlane plus a small overview — rather than shipping a hairball.

If the resulting picture can't be understood without reading the tables under
it, it has stopped doing its job. Cut it down.

## Inputs

**Required: the Event Model.** One or more images (photos, screenshots, Miro
exports), or an already-transcribed model — the output of `event-model-author`
counts and skips Step 1 almost entirely. If the user asks for a screen flow
with no model in context:

> Upload the **Event Model** — board photos are fine, and several are normal
> since the timeline is usually wider than one screenshot. Tell me the
> left-to-right order if the filenames don't.

**Optional, worth one ask if convenient:** a **Visual Glossary** or the team's
own vocabulary list, which fixes screen names in the language the team
actually speaks; and any **Domain Story**, which tells you which actor starts
where. Neither is required — ask once, accept a no, and proceed noting what
the labels are based on.

**If the board isn't really an Event Model** — a raw EventStorming wall, an
undifferentiated sticky pile, no wireframe row at all — say so and offer
`event-model-author` first. A screen flow derived from a board with no
wireframes is a guess about UI dressed as an extraction.

## Step 1 — Read and stitch the board

Reading errors compound: a misread wireframe becomes a screen that doesn't
exist, and every arrow around it inherits the mistake. Full procedure and
worked examples in `references/reading-the-board.md`; the short form:

1. **Put the images in timeline order** and say which order you used. Ask
   rather than guess.
2. **Confirm the swimlanes recur identically** across images — same names,
   same top-to-bottom order. Resolve an illegible lane label from an image
   where it's readable, never from the stickies sitting in that row.
3. **Classify every seam**: *overlap* (dedupe the repeated column),
   *abutment* (nothing lost), or *gap* (a stretch this capture never shows —
   which is a hole in the input, not an empty stretch of the domain).
4. **A line running off a frame edge is unresolved**, never continuing. Ask.
5. **The same wireframe near a seam in two images is one screen** until
   matched content *and* lane position prove otherwise.

Then transcribe, before interpreting anything: the wireframe row, the command
under each wireframe, the events, the read models feeding wireframes, and the
policies. For a non-trivial board show this strip and get it confirmed —
cheap now, expensive later.

## Step 2 — Decide what counts as a screen

This is the judgement the whole output rests on. An Event Model draws a
wireframe **every time** a screen participates in a slice, so the wireframe
count is almost always higher than the screen count.

| On the board | On the flow |
|---|---|
| The same UI drawn at several points on the timeline | **One screen**, several edges |
| A State Change wireframe and the State View right after it, showing the same page | **One screen** — the form and its result list are one thing the user is looking at |
| A State View whose read model feeds a genuinely different page | A separate screen |
| A workflow drawn twice because two triggers reach it (e.g. an approval step reached both from a new submission and from an escalation) | **One screen set, two inbound edges.** Recurrence is not duplication — this is the mistake that doubles a flow |
| An Automation or Translation slice | **No screen.** It becomes an edge label, or nothing |
| A read model with no wireframe attached | **No screen** — but flag it in §5: either a view nobody built, or data used internally |
| A lane whose content is entirely automation (notification, integration, an AI responder) | **No screens at all.** Say so explicitly; a thin lane is a finding, not an omission to paper over |

**Name screens in the board's own vocabulary** (or the glossary's, if one was
given and it disagrees — the glossary wins on spelling, the board wins on what
exists). Prefer the wireframe's own caption; failing that, name it for what
the user does there, never for the technical component.

**Mark the actor** on each screen if the model names one — a flow whose
screens belong to different roles is a different flow, and readers assume one
user unless told.

## Step 3 — Decide what counts as an edge

An edge means *the user ends up somewhere else*. The trigger comes from the
board:

| Cause on the board | Edge | Label |
|---|---|---|
| The user issues a command (State Change) | solid | the command, worded as the button reads: `Submit claim` |
| An event routes the user onward (a State View follows a State Change) | solid | the command that produced it — don't draw command and event as two hops |
| A policy moves the user or changes what they see (Automation) | dashed | `auto: <condition>` |
| Translation between contexts | usually **none** — invisible to the user. Draw it only if the user visibly lands somewhere else, and label it `auto:` |
| A command that can be refused | optional dashed self-loop, `refused: <reason>` | off by default — offer it, and add it only when refusals are the point |
| Back / cancel / return | draw only if the board draws it | inventing back-buttons is how a flow becomes fiction |

**Entry points:** one small start node per actor who enters the flow, pointing
at their first screen. **Terminals:** screens the board never leaves are drawn
as terminals — and are worth a sentence in §5, because a real terminal is rare
and an accidental one is a modelling gap.

## Step 4 — Group by swimlane, if grouping helps

With three or more lanes carrying screens, wrap each lane's screens in a
subgraph named for the bounded context; it shows at a glance where the flow
crosses a boundary, which is usually where the handoffs and the waiting are.
With one or two lanes, stay flat — a subgraph around everything is noise.

## Step 5 — Render the Mermaid

Declare these `classDef`s verbatim, so this skill's output matches the colour
language the project's other event-modeling and EventStorming skills already
use (screens carry the wireframe grey; automation carries the policy lilac):

```
classDef screen fill:#f4f4f4,stroke:#999,color:#333
classDef auto fill:#BD93F9,stroke:#7C4DBD,color:#000
classDef ext fill:#F8C1D8,stroke:#C2185B,color:#000
classDef start fill:#fff,stroke:#333,color:#333
```

| Thing | Shape | Syntax | Class |
|---|---|---|---|
| Screen | stadium | `S1(["Draft claim"])` | `screen` |
| Entry point (one per actor) | circle | `A0(("Claimant"))` | `start` |
| Waiting / status screen the board actually draws for an automation | hexagon | `P1{{"Waiting for approval"}}` | `auto` |
| Something outside the app (an email, an external system's UI) | parallelogram | `X1[/"Email to the approver"/]` | `ext` |

```
flowchart LR
  S1(["Draft claim"]) -- "Submit claim" --> S2(["Claim status"])
  S2 -. "auto: no decision within 5 days" .-> S3(["Escalation"])
```

Syntax that reliably breaks a render, so check each before shipping:

- **No nested quotes** in a label — write `Get code button`, not `Get "code" button`.
- **Quote any label containing parentheses, commas or `>`**, and prefer
  rewording to escaping.
- **Unique node ids**, declared once with their class; refer to them by id
  afterwards rather than redeclaring the label.
- **Every dashed edge gets a verb-ish label** (`auto:`, `refused:`) — an
  unlabelled dashed arrow tells the reader nothing.
- Direction `LR` for a mostly-linear process, `TD` when the flow branches wide
  early. Pick one and say why in a clause.

Add a one-line legend under the diagram in prose (screens grey, dashed =
happens without the user acting), rather than a legend subgraph — on a
15-node picture the subgraph costs more than it explains.

## Step 6 — Reachability check

The cheapest value in the whole output, and the reason to draw the picture
rather than write the list:

- **Every screen has an inbound edge** unless it's an entry point. One that
  doesn't is either a missing trigger on the board or a screen nobody can get
  to.
- **Every screen has an outbound edge** unless it's a declared terminal.
- **Every actor's entry point reaches every screen that actor owns**, walking
  the arrows.
- **Every trouble branch has an exit.** Boards routinely draw the happy path
  out of a failure and no other — a screen you can only leave by succeeding is
  a finding.

Report what fails as questions, don't patch it by adding arrows.

## Output

```
# Screen Flow — <process name>

## 0. Inputs
Images used and their order, seam classifications, what was declined
(glossary, domain story), and which lanes carry no screens.

## 1. Screens
# · Screen · Swimlane · Actor · Source (image + wireframe/slice) · What happens here

## 2. Transitions
From · Trigger (command / policy) · To · Kind (user / auto / refusal)

## 3. The flow
The Mermaid flowchart, plus one line of legend prose.

## 4. Reachability check
Unreachable screens, dead ends, branches with no exit — or a line saying it
passes.

## 5. Gaps & open questions
Wireframes that might be one screen or two; read models with no view;
screens the flow seems to need that the board never draws; automations whose
trigger is unstated. Each with the question that would settle it.
```

Adapt to the ask. "Just give me a mermaid diagram of the screens" gets §3 with
a short §1 under it and an offer of the rest — keep §1 even then, because it's
what makes the picture checkable. A handover-quality spec gets everything.

When a section's depth is unclear, let §4 decide it: include whatever the
reachability check needs in order to be checkable by someone holding the board
photos, and leave the rest out.

## Working with neighbouring skills

- **No Event Model yet, only a domain story and contexts** → `event-model-author`
  builds one; its §3 slices feed Step 2 here directly.
- **The board is a raw EventStorming wall** → `event-storming-interpreter`
  first; a wall with no wireframes can't produce an honest screen flow.
- **The user wants the clickable thing, not the map** → `prototype-from-event-model-and-domain-story`.
- **The user wants the full grid, not just the screens** → `event-model-author`
  Step 9 and its `references/mermaid-grid.md`.
- **"What would refuse this command?"** → `event-model-invariant-finder`
  produces the guards; they become the `refused:` self-loops here if the user
  wants them drawn.
- **Screen names disagree with the team's terms** → `visual-glossary-interpreter`
  or `domain-story-glossary-consistency` settles the vocabulary before you
  label anything.

## Reference files

- `references/reading-the-board.md` — stitching multiple images, reading each
  of the four slice patterns off a photo, telling a wireframe from a read
  model at low resolution, and the screen-identity decision table with worked
  ambiguous cases.

## References

A. Dymitruk, *Event Modeling*, eventmodeling.org — the four slice patterns
(State Change, State View, Automation, Translation) and the convention that
every user-facing slice carries a wireframe.

S. Hofer and H. Schwentner, *Domain Storytelling.* Boston, MA, USA:
Addison-Wesley, 2022.