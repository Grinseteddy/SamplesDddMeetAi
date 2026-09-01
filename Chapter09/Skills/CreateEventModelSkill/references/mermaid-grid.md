# Rendering the grid as Mermaid

Read this when you reach Step 9 / output §4. A table gives the redraw spec;
this diagram gives the same information as a picture — and a picture makes a
model's self-consistency errors visible in a way a table doesn't (a read
model nothing ever writes, a "Translation" edge that never actually crosses a
subgraph). Build both, from the same slice data, and let any mismatch between
them be your own check that the model holds together.

This is now the **default** rendering for output §4, not something to add
only if asked. Following the exact conventions below means every Event Model
this skill produces looks the same, and matches the color legend this
project's EventStorming skills already use — blue commands, orange events,
green read models, lilac policies/external systems — so a person moving
between an EventStorming board and an Event Model isn't learning a second
color language.

## Node shapes and colors

Declare these five `classDef`s at the top of every diagram, verbatim:

```
classDef wireframe fill:#f4f4f4,stroke:#999,color:#333
classDef command fill:#4A90D9,stroke:#2C5F8A,color:#fff
classDef event fill:#F5A623,stroke:#B9770E,color:#000
classDef readmodel fill:#7ED321,stroke:#4F8F0F,color:#000
classDef policy fill:#BD93F9,stroke:#7C4DBD,color:#000
```

| Thing | Shape | Syntax | Class |
|---|---|---|---|
| Wireframe | stadium | `W1(["Screen name"])` | `wireframe` |
| Command | rectangle | `C1["Command name"]` | `command` |
| Event | rounded rectangle | `E1("Event name")` | `event` |
| Read Model | cylinder | `R1[("Model name")]` | `readmodel` |
| Policy (Automation) | hexagon | `P1{{"policy: condition"}}` | `policy` |

Attach the class at declaration with `:::classname`, e.g.
`E1("Bicycle placed in rack"):::event`. Open the diagram with a small
`LEGEND` subgraph carrying one sample node of each class — cheap, and it
means nobody has to memorize the color key:

```
subgraph LEGEND[Legend]
  direction LR
  LG_W(["wireframe"]):::wireframe
  LG_C["command"]:::command
  LG_E("event"):::event
  LG_R[("read model")]:::readmodel
  LG_P{{"policy"}}:::policy
end
```

**Avoid nested quotes inside a label** (`["Get "code" button"]` breaks
parsing) — write `Get code button` instead of quoting the inner word.

## Layout: one subgraph per swimlane

```
subgraph BD[Bicycle distribution]
  direction LR
  ...nodes for this context, in timeline order...
end
```

- `direction LR` inside every subgraph, so each lane reads left to right like
  the timeline itself.
- **Chain nodes in actual causal order within a lane**: wireframe → command →
  event → (next) wireframe → command → event. This mirrors how Dymitruk's own
  diagrams are drawn, and it's also the cheapest sanity check available — if
  you can't chain a lane's nodes into a single left-to-right sequence, the
  slices inside it are probably out of order.
- **A gap in a lane is honest, not a defect.** If a context does nothing for a
  stretch while other lanes are busy (e.g. a whole Automation slice happens
  entirely inside another context), leave its lane empty there rather than
  inventing a filler node to keep the picture looking busy.
- Declare subgraphs in the same order they first appear on the swimlane strip
  (output §2) — Mermaid doesn't guarantee vertical ordering, but declaring
  them consistently keeps repeated redraws of the same model stable enough to
  diff.

## Edge conventions — the arrows carry meaning, so keep them consistent

| Edge | Syntax | Means |
|---|---|---|
| Sequence | `A --> B` | within-lane: this produced that, next |
| Read-model write | `A -. adds .-> R` / `A -. removes .-> R` | an event writing into a projection — always say which direction |
| Guard | `A -. guard .-> C` | an event named in a command's Given, feeding its precondition |
| Translation | `A == Translation ==> B` | a Command in one context triggered by an Event in another — **this edge should always cross a subgraph boundary.** If it doesn't, it isn't a Translation; relabel it as a plain sequence |
| Cross-context automation trigger | `A -. crosses to <Context> .-> P` | the hybrid case in `event-modeling-method.md` §2 — an Automation whose trigger event belongs to a different context than the policy it feeds |

Every dotted edge gets a verb label. An unlabeled dotted arrow doesn't say
whether it's a write or a read, and the diagram loses exactly the information
a table would have kept in its column headers.

## Worked fragment (copy this shape)

```
flowchart LR
  classDef wireframe fill:#f4f4f4,stroke:#999,color:#333
  classDef command fill:#4A90D9,stroke:#2C5F8A,color:#fff
  classDef event fill:#F5A623,stroke:#B9770E,color:#000
  classDef readmodel fill:#7ED321,stroke:#4F8F0F,color:#000
  classDef policy fill:#BD93F9,stroke:#7C4DBD,color:#000

  subgraph LEGEND[Legend]
    direction LR
    LG_W(["wireframe"]):::wireframe
    LG_C["command"]:::command
    LG_E("event"):::event
    LG_R[("read model")]:::readmodel
    LG_P{{"policy"}}:::policy
  end

  subgraph CTX[Context name]
    direction LR
    W1(["Screen"]):::wireframe --> C1["Do the thing"]:::command --> E1("Thing done"):::event
  end
```

## When to build it

By default, once Steps 1–5 have produced a settled swimlane strip and a full
set of specified slices. Ship the diagram with every Event Model this skill
produces — it's the payoff of doing the earlier steps correctly, not a
separate deliverable to build only when asked. Build the table first (it's
the checkable spec), then the diagram from the same data, then look for any
place the two disagree — that disagreement is a bug in one of them.

## Pitfalls specific to the diagram

- **Too many crossing edges.** If a diagram is dense with Translation arrows
  jumping between the same two subgraphs, that's a signal the boundary
  between them might be drawn in the wrong place — a chatty diagram is
  evidence about the contexts, not just about the drawing.
- **Forcing a diagram past ~15 slices.** Past that size a single flowchart
  gets unreadable. Split it: one diagram per phase of the process, or offer a
  full diagram plus a "zoomed" one for the context under discussion.
- **Decorative filler nodes.** A blank stretch in a lane is information (see
  above). Don't paper over it to make the picture look symmetric.
- **Reusing the same node id for two different facts.** Each event, command,
  read model and wireframe needs its own id across the whole diagram, even
  across subgraphs — id collisions silently merge two different things into
  one node.