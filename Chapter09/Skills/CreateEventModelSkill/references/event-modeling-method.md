# Event Modeling — method reference

Read this when a Step in SKILL.md needs more depth than its summary gives —
which pattern a slice really is, how to write a Given/When/Then that earns its
keep, or how to run the completeness check without hand-waving it.

## Contents
- 1. Why one timeline, not many boards
- 2. The four patterns, in depth
- 3. Given/When/Then and Given/Shows
- 4. The information-completeness check
- 5. Common pitfalls

---

## 1. Why one timeline, not many boards

An EventStorming board can legitimately show each bounded context as its own
lane with its own local sequence, because the point of that exercise is
discovery — surfacing what each part of the business does before anyone
commits to how the parts fit together in time. Event Modeling is downstream of
that decision. It exists to answer a sharper question: **at the moment this
command runs, what did the system actually know, and where did that knowledge
come from?**

That question only has an answer if events sit on one shared, absolute
timeline. If context A logs an event and context B reacts to it, the model
must be able to say which happened first, in the same coordinate system —
otherwise "did B have the information it needed" is unanswerable. So a
finished Event Model is drawn as:

```
                 [wireframe]      [wireframe]                [wireframe]
                      │                │                          │
Meal Preparation  ────●────────────────┼──────────────●───────────┼──────▶
                       Command/Event    │               Command/Event
Community Help    ─────────────────────●───────────────────────────●─────▶
                                    Command/Event               Command/Event
Meal Sharing      ───────────────────────────────────────────────────●───▶
                                                                  Command/Event
                  ──────────────────────────────────────────────────────▶ time
```

Read left to right, the same way regardless of which lane you're looking at.
A context that does nothing for a stretch simply has a blank lane there — that
blank is informative (it tells you the context wasn't touched), not a gap in
the drawing.

## 2. The four patterns, in depth

Almost every slice on a finished model is one of these. Naming the pattern is
not decoration — it decides what the slice needs (a wireframe? a policy name?
a border crossing?) and what it doesn't.

### State Change

```
[wireframe] ──▶ Command (blue) ──▶ Event(s) (orange)
```

A person does something through a UI, a command is issued carrying the data
they supplied, and the system either accepts it (producing one or more events)
or refuses it. This is the pattern behind almost every "the user does X"
sentence in a domain story.

*Example.* `Cook reports the pan is scorched` → wireframe: a "Something's
wrong" button with a free-text field and a photo attachment → Command
`Report catastrophe` → Event `Catastrophe happened`.

**What makes it real, not decorative:** the command must be refusable. If
nothing the business would call "wrong" could ever cause this command to fail,
double-check it isn't really a State View mislabeled as an action.

### State View

```
Event(s) (orange) ──▶ Read Model (green) ──▶ [wireframe]
```

One or more events are projected into a read model — a purpose-built view of
the world, not the events themselves — and a wireframe displays it. State
Views are what make later State Changes *possible*: a person can't act
correctly on information they can't see.

*Example.* Events `Help requested` and `Help provided` project into a read
model showing the response text and who answered → wireframe: the cook's
"Help arrived" screen.

**What makes it real:** every field in the wireframe must appear in the read
model, and every field in the read model must trace to an event (this is a
local instance of the completeness check in §4). A read model with a field
nothing ever wrote is exactly as suspicious as a command with a field nothing
ever needs.

### Automation

```
Event / elapsed state ──▶ (policy) ──▶ Command ──▶ Event(s)
```

No human triggers this slice. A prior event, or a read model plus the passage
of time, causes a policy (a named piece of automatic behavior) to issue a
command on its own. There is no wireframe; there is a policy name and, often,
a duration.

*Example.* "If a Help request has been open for `<n>` minutes, the Grandma
Avatar answers it" → trigger: `Help requested` + elapsed time → policy
`Escalate unanswered request` → Command `Provide help (avatar)` → Event
`Help provided`.

**What makes it real:** name the policy and its trigger condition explicitly,
including the numbers if the domain has them (or flag the number as unknown in
§6/§8 of the output rather than inventing one). "The system does something
eventually" is not a specified automation; "after `<n>` minutes of no
response" is.

### Translation

```
Event in Context A ──▶ (border) ──▶ Command in Context B
```

An event produced by one bounded context crosses into another and, in the
process, changes vocabulary — the fact that mattered upstream becomes the
input to an action downstream, phrased in the downstream context's own terms.
This is the pattern behind a customer/supplier relationship on a context map,
made concrete enough to build.

*Example.* `Catastrophe happened` (Meal Preparation's language: a step,
a description, a timestamp) crosses into Community Help as the Command
`Request help` (Community Help's language: a situation snapshot, a photo, an
urgency flag). The border is where "what went wrong in my kitchen" becomes
"a stranger can now answer a question."

**What makes it real:** say explicitly what crosses and what stays behind —
the same discipline a context map's border contract already applies. If
everything crosses unchanged, it probably isn't a translation, it's the same
model shared across a boundary that shouldn't exist (a shared-kernel smell
worth naming rather than modeling as if it were healthy).

### Telling Automation and Translation apart

They look similar (both are "no human, and a Command results") and the
difference matters for who owns the policy:

- **Automation** stays inside one context: that context's own event triggers
  that context's own command. The policy belongs where the event happened.
- **Translation** crosses a border: an event in context A produces a command
  in context B. The policy — whatever decides *when* and *how* the crossing
  happens — usually belongs to the consuming context, since it is the one
  making the language choice.

A slice that both crosses a border **and** waits on elapsed time (the Grandma
Avatar example above) is a Translation slice with an Automation trigger — say
both, rather than forcing a single label.

## 3. Given/When/Then and Given/Shows

### State Change and Automation — full GWT

Tag each with a slice id so it's referenceable from the completeness check and
from any invariants sheet built alongside it:

```
@SLICE-04 [State Change] Meal Preparation
Scenario: A cook reports a catastrophe mid-recipe
  Given Meal preparation started (recipe: "Tomato Risotto", started_at: 18:02)
   When Report catastrophe (step: 4, description: "rice has scorched")
   Then Catastrophe happened (step: 4, description: "rice has scorched",
        reported_at: 18:11)
```

Three habits keep this honest:

- **Given is a replay, not a summary.** List the actual prior events (with
  enough of their fields to matter), not a paraphrase of "the meal is being
  cooked". If a guard depends on a field, that field must be visible in the
  Given.
- **When carries concrete, plausible data**, ideally the same instance the
  domain story used, not `{...}` or a type name. A GWT with placeholder data
  can't be run as a test and can't be checked for completeness either.
- **Then is exactly what the event carries**, not what you wish it carried.
  If the business needs a field the Then doesn't have, that is a finding for
  §5 of the output, not a reason to quietly add it here.

Write a **rejection** scenario too wherever the command is refusable — it's
usually the more informative of the pair:

```
@SLICE-04b [State Change] Meal Preparation
Scenario: A catastrophe cannot be reported after the meal is done
  Given Meal preparation started (…)
    And Meal prepared (…)
   When Report catastrophe (step: 6, description: "too late")
   Then the command is rejected with "this meal is already finished"
```

### State View — the lighter form

No command, so no rejection path — just the projection:

```
@SLICE-06 [State View] Community Help
Given  Help requested (situation: "rice scorched at step 4", photo: ref-991)
       Help provided (request: ref-1, responder: "Grandma Avatar",
                       text: "add a splash of stock and lower the heat")
Shows  the response text, the responder's name, and a "machine-generated"
       label when the responder is the avatar
```

### Translation — GWT that spans two contexts

Write the Given/When/Then from the **downstream** context's point of view
(it's the one issuing the command), but state explicitly what crossed the
border to make it possible:

```
@SLICE-05 [Translation] Meal Preparation → Community Help
Crosses  situation snapshot (recipe, step, description), the photo taken at
         the catastrophe, an urgency flag
Scenario: A catastrophe becomes a help request
  Given Catastrophe happened (step: 4, description: "rice has scorched")
        Pictures taken (of: catastrophe, ref: ref-991)
   When Request help (situation: "rice scorched at step 4", photo: ref-991,
        urgency: high)
   Then Help requested (situation: "rice scorched at step 4", photo: ref-991,
        urgency: high, opened_at: 18:11)
```

## 4. The information-completeness check

This is the method's single sharpest tool, and it is mechanical enough to run
as a literal pass over the model rather than a vibe check.

**The rule.** For every field that appears in a `When` (a Command) or a
`Shows` (a Read Model), find the event — anywhere earlier on the shared
timeline, in any swimlane — that actually produced that value. If you can't,
one of three things is true:

1. **A missing upstream event.** The story implies the fact exists but no
   event on the spine carries it. Name the event that's missing.
2. **A missing Translation slice.** The fact exists in another context but
   nothing on the model shows it crossing the border. Name the border and
   what should cross it.
3. **An assumption.** The field is something you inferred rather than
   something the story or the found events actually established. Flag it as
   an assumption in the output rather than silently keeping it in the spec.

**Worked check, from the running example.** The Command `Provide help
(avatar)` in the Automation slice needs to know *what the cook is stuck on* to
generate useful advice. Trace it back: `Help requested` (Community Help,
supplied by the Translation slice) carries `situation` and `photo` — both
present, both trace cleanly. But if the avatar's advice needed to know the
recipe's *full ingredient list* and only a text description crossed the
border, that field does **not** trace — write it up as a missing-translation
finding, and say precisely what should have crossed instead of quietly having
the avatar "just know" the recipe.

**Run it per slice, not once at the end.** Checking as each slice is written
catches a missing Translation before three more slices are built on top of the
gap it leaves.

## 5. Common pitfalls

- **A Command that can never fail is a state view wearing a blue sticker.**
  If nothing the business would call wrong stops it, ask whether it's really
  reporting a fact that already happened elsewhere (an event) rather than
  requesting a change.
- **An event named in the present or future tense.** `Meal preparing` and
  `Meal will be prepared` are not facts. If the name doesn't read naturally
  after "it is a fact that...", rename it.
- **A read model with a field nothing ever wrote**, or a command with a field
  nothing downstream ever uses. Both are completeness-check findings, not
  fine details to skip.
- **Wireframes built before the fields are settled.** A pixel-perfect mockup
  for a command whose fields keep changing is wasted work; keep Step 6 to a
  field list until the Given/When/Then is stable.
- **Timeline slicing that ignores recurrence.** A context that shows up twice
  on the spine (help requested once during planning, once during cooking, say)
  is one swimlane touched twice, not two lanes. Forcing one event per lane
  produces a phase diagram, not an Event Model — the same trap
  `domain-story-context-finder` warns about when cutting contexts in the first
  place.
- **Automation with no named trigger condition.** "Eventually, the system
  helps" is not a policy. Either name the condition (an elapsed duration, a
  count, a state) or record the missing number as an open question — don't
  let vagueness hide inside the word "automation".
- **Skipping the rejection scenario.** A State Change specified only by its
  happy path hasn't been specified — the refusal is usually where the
  business rule actually lives.