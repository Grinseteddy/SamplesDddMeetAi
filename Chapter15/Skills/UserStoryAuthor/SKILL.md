---
name: user-story-author
description: Write user stories for ONE bounded context from a Domain Knowledge Graph (dkg Turtle) — actor, goal and benefit traced to graph nodes, acceptance criteria from the graph's Given/When/Then rules, open questions from its hotspots. Asks for the graph and/or the bounded context if either is missing.
---

# User Story Author (from a Domain Knowledge Graph)

Turn the part of a **Domain Knowledge Graph** that belongs to **one bounded
context** into a backlog of **user stories** — each one traceable, node by node,
to the artifacts the team actually drew.

The graph is the one built by `domain-knowledge-graph`: a `graph.ttl` using the
`dkg:` vocabulary (`https://w3id.org/dkg/ns#`), fed by impact maps, domain
stories, glossaries, EventStorming boards, Event Models, canvases and ADR logs.

Use this skill whenever someone wants user stories, a backlog, story cards,
epics, a story map or "tickets" for a bounded context, a swimlane, a module or a
service, and a knowledge graph is (or should be) the input — even if nobody says
"knowledge graph" or "bounded context".

The output is always three things:

1. **The stories** — Connextra form, acceptance criteria, provenance per line.
2. **A coverage table** — every command, slice and read model in the context,
   and which story covers it (or why none does).
3. **Open questions** — what the graph could not answer, for the room to settle.

Three commitments, inherited from the graph:

- **Never invent the benefit.** The "so that" comes from an Impact, Goal, Value
  Proposition or Capability *in the graph*. If none is reachable, the story says
  `so that ⟨open — no impact or goal in the graph⟩` and the gap is reported. A
  plausible-sounding benefit nobody agreed on is the most common way a backlog
  lies.
- **Speak the graph's language.** Actors, work objects, commands and events use
  the node's `skos:prefLabel`, verbatim. Never swap in a synonym, and never
  "tidy" a term — if the label is awkward, that is a finding.
- **Never settle an open question.** Hotspots, contradictions and pending
  merges become questions on the story, not decisions baked into its criteria.

---

## Step 1 — Get both inputs. Ask for whatever is missing.

The skill needs exactly two inputs:

| Input | Accepted forms |
|---|---|
| **Domain Knowledge Graph** | a `.ttl` file (attached, in the project, in the working directory, or on a connected folder); a pasted Turtle block. A `views/index.md` from `domain-knowledge-graph` is a weak fallback — say so, and ask for the `.ttl`. |
| **Bounded context** | a name — matched against the graph's `dkg:BoundedContext`, `dkg:Lane` and `dkg:Swimlane` nodes by `skos:prefLabel` or `skos:altLabel`, case- and punctuation-insensitive. |

Look before asking: check attachments, the project, and the working directory
for a `.ttl` that declares the `dkg:` prefix, and the prompt for a context name.
Then:

- **Both present** → confirm in one line ("Writing stories for *Cook
  Assistance* from `graph.ttl`.") and go on.
- **Graph present, context missing** → run `--list-contexts` (Step 2) and ask
  which context, with `AskUserQuestion`: the contexts as options, the one with
  most members first. More than four contexts → the four largest as options;
  the rest are reachable through "Other" — list them in the question text.
- **Context present, graph missing** → ask for the graph: "Please attach the
  knowledge graph (`graph.ttl`) — I'll write the *<context>* stories from it."
  If they have no graph yet, offer `domain-knowledge-graph` to build one from
  their artifacts; do not write stories from memory of the domain.
- **Both missing** → ask for both in one message: the graph first (it is what
  makes the context list possible), the context name optional ("or I'll list
  the contexts the graph holds").
- **Context named but not in the graph** → the script exits `3` with the
  closest names; ask "Did you mean *X*?" with those as options. Exit `2` means
  the name matches several contexts — ask which one. **Never pick the closest
  match yourself**: a wrong context produces a confident, wrong backlog.

Unattended (no one to answer): if either input is missing, stop and state what
is missing and what was found. Do not guess a context.

---

## Step 2 — Cut the context out of the graph

```bash
pip install rdflib --break-system-packages -q   # once
python3 scripts/context_slice.py graph.ttl --list-contexts
python3 scripts/context_slice.py graph.ttl --context "<name>" --out slice.json
```

`slice.json` holds, for the chosen context:

| Key | What it is |
|---|---|
| `actors`, `commands`, `events`, `rules`, `terms` | nodes with `dkg:inContext` this context, each with labels, sources, locators, confidence and every in/out edge |
| `attached` | one hop out: slices, Given/When/Then rules, story sentences, messages, deliverables hanging off members |
| `why` | Impacts, Goals, Deliverables, Value Propositions, Capabilities reachable upward (≤ 3 hops via `realises`, `supports`, `towardsGoal`, `impactedActor`, `proposedSameAs`, `mentions`) |
| `questions`, `decisions` | hotspots and questions raised in the context, and ADR decisions that `dkg:answers` them |
| `contradictions`, `straddlers`, `pendingMerges` | disagreements, nodes in two contexts, unconfirmed `proposedSameAs` |

If `scripts/context_slice.py` is not available, use the SPARQL fallback at the
end of this file — same content, less convenient.

Report the slice in two lines before writing stories: member counts by kind,
and how many members rest on a single artifact. A context built from one board
yields stories that are one board's opinion — say so.

---

## Step 3 — Find the story candidates

Work through the slice in this order of strength. One candidate per row; the
same behaviour found twice (a slice *and* its command) is one story with two
sources, not two stories.

| Graph evidence | Story shape | Strength |
|---|---|---|
| `dkg:Slice`, `sliceKind "state change"` | *As ⟨actor of its command⟩, I want to ⟨command⟩, so that …* — GWT rules on the slice become the criteria | strongest — already a unit of work |
| `dkg:Slice`, `sliceKind "state view"` | *As ⟨actor⟩, I want to see ⟨read model⟩ …* | strong |
| `dkg:Command` with `dkg:performedBy` an Actor and `dkg:triggers` an event | *As ⟨actor⟩, I want to ⟨command⟩ …*; the event is the observable outcome | strong |
| Domain-story `dkg:Sentence` (actor → activity → work object) with no matching command | *As ⟨actor⟩, I want to ⟨activity⟩ the ⟨work object⟩ …* — flag "no command on any board yet" | medium |
| `dkg:ReadModel` read by a command or shown in a slice, with no state-view slice | a "see / find" story | medium |
| `dkg:Policy` / `sliceKind "automation"` | **not** a story of its own — becomes a criterion on the story whose event the policy reacts to, or an enabler (below) | — |
| `dkg:Message` inbound (`cmd`/`qry`) from another context | *integration story*: *As ⟨collaborating context⟩, I need to ⟨message⟩ …*; tag `integration` | medium |
| `sliceKind "translation"` | integration story, tag `integration` | medium |
| `dkg:Command` with no actor | write it, actor `⟨open⟩`, and ask who issues it | weak |

Two shapes to refuse:

- **"As a system, I want …"** Automation belongs on the story of the human or
  context whose event triggers it. If there is genuinely no one, write it as an
  **enabler** (*"Enabler: when ⟨event⟩, ⟨policy⟩"*), not as a user story.
- **CRUD stories** ("manage help requests"). If the graph only offers a CRUD
  verb, keep it verbatim and put the question in Open questions — the critic
  skills will want to know.

Actors from **other** contexts appear only as collaborators or as integration
story requesters. A **straddler** (an actor or term in two contexts) gets a
note on every story it appears in: its meaning here may not be its meaning
there.

---

## Step 4 — Write each story

**Benefit ("so that").** Walk `why` for the story's nodes, nearest hop first:

1. an **Impact** whose `dkg:impactedActor` is the story's actor → state the
   behaviour change, and add the Goal it `towardsGoal` as the outcome;
2. a **Deliverable** the command is `proposedSameAs` / `realises` → use its
   Impact; mark *(via unconfirmed merge)* if the link is a `proposedSameAs`;
3. a **Value Proposition** or a **Capability** that `supports` a Goal;
4. nothing → `so that ⟨open⟩`, listed in Open questions.

**Acceptance criteria.** In this order, keeping each criterion's provenance:

1. `dkg:Rule` nodes that `mentions` the story's command or slice — copied as
   Given/When/Then, *verbatim*; these are `OnArtifact`.
2. The triggered event as the main success criterion: *"Then ⟨event⟩ is
   recorded"*, in the event's own words.
3. `dkg:Policy` reacting to that event: *"And ⟨policy outcome⟩"*.
4. Glossary `dkg:Relationship` cardinalities on the work object
   (`cardinalityGiven true` only): *"A ⟨term⟩ has at most ⟨n⟩ ⟨term⟩"*.
   Derived cardinalities (`false`) go to Open questions, never to criteria.
5. Decisions with status Adopted/Accepted that answer a question on the story
   — as a criterion, with the ADR id. A *Proposed* decision is a question,
   not a criterion.

Never write criteria from domain intuition. If the graph gives none, the story
has `Acceptance criteria: ⟨none in the graph⟩` and a question.

**Confidence.** Each story carries the *weakest* confidence of the nodes it
stands on (`OnArtifact` > `Implied` > `Inferred`), and the number of
artifacts behind it.

**Template:**

```markdown
### US-<CTX>-<nn> · <command or slice label>

**As** <actor prefLabel>, **I want to** <command / activity, verbatim>
**so that** <impact / goal / ⟨open⟩>.

**Acceptance criteria**
- Given … When … Then …            — <rule label> · <artifact>, <locator>
- Then "<event label>" is recorded  — event · <artifact>, <locator>

**Traces to** Cmd_… · Ev_… · Slice_… · Imp_… → Goal_…
**Confidence** OnArtifact · 2 artifacts   **Tags** state change
**Depends on** US-<CTX>-<nn> (event order)
**Open questions** <hotspot / contradiction / pending merge, verbatim>
```

Keep stories small: one command, one primary event. A slice that hides two
commands is two stories — say why it was split.

---

## Step 5 — Order them, check them, report

**Order.** Sequence stories by the `dkg:sequence` / `dkg:precedes` order of
their events. Where the graph gives no order, say so — do not invent a flow.
If the context has more than ~8 stories, group them under the actors' goals as
a light story map (backbone = activities in event order, stories beneath).

**INVEST check, briefly, per story** — flag, don't fix:
dependent on another context's event (*Independent*), no criteria
(*Testable*), no benefit (*Valuable*), more than one command (*Small*).

**Deliver**, in this order:

1. One line: context, graph, how many stories, how many rest on a single
   artifact.
2. The stories, in order.
3. **Coverage table** — `node · kind · story · note`, one row per command,
   slice and read model in the context. Uncovered nodes stay in the table with
   the reason (no actor, enabler, out of scope). An uncovered node is a finding.
4. **Open questions** — every `⟨open⟩`, every hotspot touching the context,
   every contradiction and pending merge, every Proposed decision, verbatim with
   sources. Never answer them.
5. Optionally, if asked: a CSV for backlog import
   (`id,title,as,i_want,so_that,acceptance_criteria,tags,confidence,traces`).

Write the stories in the language the user writes in; graph labels stay in the
graph's own spelling and language.

Output format follows the host: a document (or the host's document artifact
type) by default, a Markdown file if asked, CSV only if asked.

---

## Failure modes to resist

- **The friendly benefit.** "…so that I save time." If the graph has no
  impact, the story has no benefit — report it.
- **Gap filling.** A command with no actor, an event with no command — write
  what exists, mark the hole. Holes are the input to the next workshop.
- **Synonym drift.** The story says *request*, the graph says *Help Request*.
  Use the graph.
- **Context bleed.** Pulling in commands from a neighbouring context because
  the flow "obviously continues". Stop at the boundary; the crossing becomes an
  integration story or a dependency note.
- **Turning hotspots into criteria.** A red sticky is a question until an ADR
  row answers it.
- **Feeding the stories back into the graph silently.** This skill reads the
  graph; it does not write to it. If the team wants the backlog in the graph,
  that is a `domain-knowledge-graph` ingest of a derived analysis
  (`dkg:Inferred` at best).

---

## SPARQL fallback (when the script is unavailable)

Load with `rdflib` and run each query with `?ctx` bound to the context IRI.

```sparql
PREFIX dkg:  <https://w3id.org/dkg/ns#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>

# contexts
SELECT ?ctx ?label (COUNT(?m) AS ?members) WHERE {
  VALUES ?t { dkg:BoundedContext dkg:Lane dkg:Swimlane }
  ?ctx a ?t ; skos:prefLabel ?label .
  OPTIONAL { ?m dkg:inContext ?ctx }
} GROUP BY ?ctx ?label ORDER BY DESC(?members)

# commands with actor, event, work object
SELECT ?cmd ?cmdL ?actorL ?evL ?objL WHERE {
  ?cmd dkg:inContext ?ctx ; a ?t ; skos:prefLabel ?cmdL .
  VALUES ?t { dkg:Command dkg:Activity }
  OPTIONAL { ?cmd dkg:performedBy/skos:prefLabel ?actorL }
  OPTIONAL { ?cmd dkg:triggers/skos:prefLabel ?evL }
  OPTIONAL { ?cmd dkg:actsOn/skos:prefLabel ?objL }
}

# slices and Given/When/Then rules touching the context
SELECT ?x ?xL ?kind WHERE {
  ?m dkg:inContext ?ctx . ?x dkg:mentions ?m ; skos:prefLabel ?xL .
  OPTIONAL { ?x dkg:sliceKind ?kind }
}

# the "why": impacts and goals for an actor or command
SELECT ?impL ?goalL ?metric WHERE {
  { ?imp dkg:impactedActor ?actor } UNION
  { ?cmd dkg:proposedSameAs|dkg:realises ?d . ?d dkg:realises ?imp }
  ?imp skos:prefLabel ?impL .
  OPTIONAL { ?imp dkg:towardsGoal ?g . ?g skos:prefLabel ?goalL . OPTIONAL { ?g dkg:metric ?metric } }
}

# questions and their decisions
SELECT DISTINCT ?qL ?dL ?status ?opt WHERE {
  { ?q dkg:inContext ?ctx } UNION { ?m dkg:inContext ?ctx ; dkg:raises ?q }
  VALUES ?qt { dkg:Question dkg:Hotspot } ?q a ?qt ; skos:prefLabel ?qL .
  OPTIONAL { ?d dkg:answers ?q ; skos:prefLabel ?dL ; dkg:status ?status ; dkg:decidedOption ?opt }
}
```
