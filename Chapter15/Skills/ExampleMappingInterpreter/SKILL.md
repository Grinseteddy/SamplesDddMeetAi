---
name: "example-mapping-interpreter"
description: "Interpret an Example Mapping session (Matt Wynne's BDD technique: yellow story, blue rules, green examples, red questions) from a photo or description into a checked brief with acceptance criteria and Gherkin."
author: Annegret Junker
---

# Example Mapping Interpreter

Example Mapping (Matt Wynne, Cucumber, 2015) is a short, time-boxed conversation — typically 25 minutes for one user story — between business, development and testing (the "three amigos"). It uses four colours of card:

| Card | Colour | Role |
|---|---|---|
| Story | **Yellow** | The user story under discussion, placed at the top. One per map. |
| Rule | **Blue** | An acceptance criterion / business rule the story must satisfy. Laid out in a row beneath the story. |
| Example | **Green** | A concrete example illustrating one rule, placed in the column beneath that rule. Often titled in the "The one where…" style ("The one where the customer has no card on file"). |
| Question | **Red** | An open question nobody in the room could answer, or an assumption to verify. Usually set to one side. |

This skill reads such a map — a photo of a table or wall, a Miro/Mural screenshot, or a typed list — and produces an **Example Mapping Brief**: a faithful transcription, a set of well-formed acceptance criteria with Gherkin scenarios, a readiness verdict, and the gaps the team must close.

The job is interpretation, not invention. **Do not add rules, examples or answers that are not on the map.** Where the map is thin, say so and offer *suggested* additions clearly marked as yours, never mixed into the transcription.

## Core mental model

The map is a tree read top-down:

```
STORY (yellow)
 ├── RULE (blue) ── EXAMPLE (green), EXAMPLE (green), …
 ├── RULE (blue) ── EXAMPLE (green), …
 └── …
QUESTIONS (red) — attached to the story, a rule, or an example
```

Three things follow:

1. **Rules are general, examples are concrete.** A rule states a constraint ("Members can borrow at most 5 books"). An example names a specific situation and its outcome ("The one where Ana already has 5 books and tries to borrow a 6th → refused"). A green card that is general is really a rule; a blue card that is a single case is really an example. Flag misplacements.
2. **Every example should illustrate exactly one rule, and every rule should have at least one example.** A rule with no examples is not yet understood; an example under no rule hints at a hidden rule nobody named.
3. **The shape of the map is itself the result.** Wynne's heuristics: many red cards → the story is not ready, there is too much uncertainty; many blue cards → the story is probably too big and should be split (often along rules); a rule with many examples → the rule may be too complex and hide several rules; a tidy map with few reds → ready to build.

Colours drift between teams and tools (sticky-note pads vary, digital boards use their own palette). Resolve a card's role by its position and wording as much as its colour, and state any assumption.

## Workflow

Follow in order. Do not write Gherkin before the transcription is settled — a misread rule poisons every scenario beneath it.

### Step 1 — Reconcile the legend
Identify which colour/position plays which role *on this map*. Note deviations from the canonical palette. If a colour is ambiguous and no legend resolves it, state the assumption you proceed with.

### Step 2 — Transcribe the map (and confirm if hard to read)
Transcribe verbatim:
- the story (and its format: "As a… I want… so that…" or a plain title),
- each rule, numbered R1, R2, …, left to right,
- each example under its rule, numbered R1.E1, R1.E2, …,
- each question, numbered Q1, Q2, …, with what it is attached to (story, rule, example, or unattached).

Mark any card you cannot read with confidence as `[illegible: best guess …]`. For a busy or blurry photo, show the transcription and ask the user to confirm before continuing.

### Step 3 — Check the structure
Work through the map and record findings, each tied to card IDs:
- **Misplaced cards**: rule-shaped examples, example-shaped rules, questions written as rules ("Should we allow…?" on a blue card).
- **Rules without examples** and **examples without a rule** (name the hidden rule you suspect, marked as inferred).
- **Duplicate or overlapping rules**, and **contradictions** between rules or between an example and its rule.
- **Vague examples**: missing a concrete starting situation or a concrete outcome ("works correctly"). An example must say what the world looks like, what happens, and what the observable result is.
- **Missing edge cases** worth asking about: boundaries of numeric rules (exactly at the limit, one over), empty/none cases, the unhappy path, concurrency or timing. List these as *questions to raise*, not as new examples.
- **Story fit**: rules that serve a different story or user than the yellow card names.

### Step 4 — Readiness verdict
Give one of three verdicts with the reasons, using the shape of the map:
- **Ready** — few or no blocking questions; each rule has examples; examples are concrete.
- **Ready after answering questions** — the structure is sound but listed reds block specific rules; name which rules are blocked by which questions.
- **Not ready / split the story** — too many open questions, or so many rules that the story should be sliced. When recommending a split, propose concrete slices built from groups of rules (e.g. "Story A: R1–R3, Story B: R4–R6"), keeping each slice independently valuable.

Count cards (rules, examples, questions) and report the counts — they make the verdict checkable.

### Step 5 — Acceptance criteria and Gherkin
For each rule, write it as a crisp acceptance criterion, then turn each of its examples into a Gherkin scenario:

```gherkin
Feature: <story title>

  Rule: <R1 text>

    Example: <R1.E1 title>
      Given <the concrete starting situation>
      When <the single action or event>
      Then <the observable outcome>
```

Guidelines:
- Use the `Rule:` keyword (Gherkin 6+) so the map's structure survives into the feature file.
- Keep the domain language of the cards; do not translate it into UI clicks ("When the member borrows a book", not "When I click Borrow").
- One `When` per scenario. Keep `Given` to what the example actually depends on.
- Where several examples differ only in data, offer a `Scenario Outline` with an `Examples:` table.
- If an example's outcome is unknown because a red card is open, still write the scenario but mark the `Then` as `# BLOCKED by Q3` rather than guessing.
- Rules without examples get a placeholder comment, not invented scenarios.

### Step 6 — Questions and next steps
List every red card verbatim with an owner role suggestion (product owner, domain expert, developer, tester) and which rules it blocks. Then add your own questions from Step 3, clearly separated and marked as raised by the interpreter.

## Output: the Example Mapping Brief

```
# Example Mapping Brief — <story title>

## 1. Legend (as read on this map)
Colour/position → role, deviations flagged.

## 2. Transcription
Story; rules R1…Rn with examples Rx.Ey beneath each; questions Q1…Qn with attachment. Verbatim.

## 3. Structure findings
Misplaced cards, gaps, overlaps, contradictions, vague examples, suggested edge cases — each tied to card IDs.

## 4. Readiness verdict
Counts (rules / examples / questions), verdict, reasons; proposed story split if relevant.

## 5. Acceptance criteria & Gherkin
One acceptance criterion per rule, then a .feature block with Rule/Example structure. Blocked outcomes marked.

## 6. Open questions
Red cards verbatim with owner and blocked rules; then interpreter-raised questions, separated.
```

Adapt depth to the request: if the user only wants Gherkin, lead with §5 but keep §2 so the reading is verifiable. If they want a readiness check before sprint planning, lead with §4. If they ask for a `.feature` file, write §5's Gherkin to a file as well.

## Several maps at once

When given multiple maps (one per story), produce one brief per story, then a short cross-map section: rules that recur across stories (candidate shared domain rules), terms used inconsistently between maps, and questions that block more than one story.

## Connecting to other modelling artifacts

If the user also has a visual glossary, domain story, event model or EventStorming board, check that the map's nouns and verbs match the glossary's terms and flag drift. Rules often correspond to invariants an aggregate enforces, and examples to Given/When/Then specs on event-model slices — point out those correspondences when the other artifacts are present, without inventing them when they are not.