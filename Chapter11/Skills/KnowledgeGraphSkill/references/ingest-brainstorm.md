# Ingest — brainstorm / ideation result

Usually the first artifact, and usually a photo: stickies on a wall or a
whiteboard, sometimes clustered, sometimes dot-voted, often barely legible.

The brainstorm is the graph's **floor**. Everything ingested later gets measured
against it: which ideas survived, which were dropped, which appeared for the
first time in a canvas nobody brainstormed. That comparison is the reason to
ingest a brainstorm at all, and it only works if the ingest is faithful — a
tidied brainstorm cannot report what was lost.

## Transcribe

One line per sticky, in the room's own words, grouped as the room grouped them.
Keep:

- **the exact wording**, including fragments, jokes and contradictions;
- **the clusters the room drew** — not clusters you would draw;
- **dot votes**, if any, as written;
- **stickies you cannot read**, marked `[illegible]`, counted, and reported.

Do not deduplicate. Two people writing the same idea is data about the room.

## Map

| On the wall | Class | Notes |
|---|---|---|
| A sticky | `dkg:Idea` | `dkg:confidence dkg:OnArtifact` |
| A drawn cluster / group | `dkg:Cluster` | `dkg:inCluster` from each idea |
| Dot votes | `dkg:votes` | integer, only if counted on the picture |
| A sticky naming a person or role | also `dkg:Actor` | dual-typed; the same node |
| A sticky naming a domain noun | also `dkg:Concept` | dual-typed |
| A sticky phrased as an outcome with a measure | `dkg:Goal` | rare, and only with a measure |

**Dual typing is deliberate.** A sticky reading "grandma who actually cooks"
is both an Idea and an Actor; typing it as both is what lets a later Impact Map
merge on the actor without losing the sticky it came from.

**Do not promote.** A sticky reading "be the best cooking app" is an Idea, not a
Goal, because it has no measure. A sticky reading "help people finish dinner" is
an Idea, not an Impact, because nobody has said whose behaviour changes. The
promotion happens when a later artifact does it explicitly, and then the graph
records *who* promoted it and *when*. That record is worth more than a tidy
early graph.

## Merge hazards

- Ideas are phrased loosely and collide with everything. Auto-merge an Idea with
  a later node **never**; propose. The whole traceability lens depends on those
  proposals being real, and a sloppy merge here silently invents a heritage for
  a feature nobody brainstormed.
- Two stickies with the same words stay two nodes, joined by
  `dkg:proposedSameAs`. The duplicate is evidence of agreement in the room.

## Snippet

```turtle
:Art_Brainstorm_2026_01 a dkg:Brainstorm ;
    rdfs:label "Kickoff ideation, whiteboard photo" ;
    rdfs:comment "5 clusters, 41 stickies, 3 illegible, dot-voted." ;
    dkg:ingestedAt "2026-01-14"^^xsd:date .

:Cluster_Rescue a dkg:Cluster ; skos:prefLabel "when it goes wrong" ;
    dkg:source :Art_Brainstorm_2026_01 ; dkg:confidence dkg:OnArtifact .

:Idea_AskAStranger a dkg:Idea, dkg:Deliverable ;
    skos:prefLabel "ask a stranger mid-cook" ;
    dkg:inCluster :Cluster_Rescue ; dkg:votes 6 ;
    dkg:source :Art_Brainstorm_2026_01 ; dkg:locator "cluster 3, sticky 4" ;
    dkg:confidence dkg:OnArtifact .
```

## Report additions

Beyond the standard report: **cluster sizes, the vote distribution, and the
illegible count**. A brainstorm with one cluster holding half the stickies, or
with votes concentrated on two ideas, is telling you something about the session
that no later artifact will repeat.