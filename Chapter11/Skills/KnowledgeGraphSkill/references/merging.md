# Merging — identity resolution across artifacts

Merging is the whole difficulty. Everything else here is transcription.

Two artifacts using one word is a **hypothesis about the domain**, and it is
wrong often enough to matter: `Pictures` on an EventStorming board means
diagnostic evidence in one place and a trophy shot in another; `Cooking
Assistance` and `Cooking Help` are two bubbles for one context. A graph that
merges on string equality will assert both errors with a straight face.

## The three verdicts

### Auto-merge

All three conditions, no exceptions:

1. **exact label match** after case-folding and trimming only — no stemming, no
   plural collapsing, no punctuation stripping;
2. **same spine class** (both `dkg:Actor`, both `dkg:Concept`, …) — subclasses
   may differ;
3. **no contradicting property.** A different `dkg:evolution`, a different
   `dkg:inContext`, a different `dkg:cardinality` on the same relationship all
   block the merge.

Auto-merges still go in the ledger. A merge nobody can see is a merge nobody can
challenge.

### Propose — `dkg:proposedSameAs`

Everything that looks right but is not certain. Both nodes stay; the edge
records the guess. Typical cases:

| Pattern | Example | Why not auto |
|---|---|---|
| Spelling or spacing variant | `Mise en place` / `Mise-en-place` | punctuation is not stripped before comparison, and the two may be different scopes |
| Singular/plural | `Recipe` / `Recipes` | on boards these are often term vs read model |
| Containment | `Pictures` / `Catastrophe Pictures` | the qualifier usually means a second concept |
| Cross-class | BMC `Key Activity: match cooks` / board `Command: Provide help` | strategy noun vs implementation verb |
| Same referent, different vocabulary | story `Community` / board `Community Cook` | a rename, or a narrowing |
| Idea → deliverable | brainstorm sticky / impact-map WHAT | the trace people most want, and most often a stretch |

Write the proposal with a reason:

```turtle
:Con_Recipes dkg:proposedSameAs :Con_Recipe ;
    rdfs:comment "plural read model vs singular term; may be catalogue vs entry" .
```

### Never merge — `dkg:contradicts`

When the two nodes carry incompatible statements. Keep both, reify both edges,
join them. Then put it in the report *verbatim, with both sources*. This is the
graph's highest-value output and the thing a pile of files can never give you.

## The two tests that catch the expensive mistakes

**One word, two concepts.** Ask, for every label appearing in more than one
artifact: *do the two uses have the same lifetime, the same audience, and the
same questions asked of them?* If any answer differs, they are two concepts
wearing one word. Split them, suffix the IRIs, and say so — the split is
usually a boundary.

**Two words, one concept.** Ask, when a new artifact introduces a noun the graph
does not have: *is this a new thing, or the old thing after a boundary?* If a
noun goes in one side and a different noun comes out and the first is never used
again, that is `dkg:renamedTo`, not a new node — and it is evidence of a context
border.

## The merge ledger

One table in the report, per ingest. Never skip it, never summarise it.

| Label seen | Existing node | Decision | Evidence | If wrong |
|---|---|---|---|---|
| Community | `:Act_CommunityCook` | proposed | four stories say *Community*; board says *Community Cook* | one responder role becomes two, or two collapse into one |
| Cook | `:Act_Cook` | auto-merged | exact, both `dkg:Actor` | — |
| Pictures | `:Con_Pictures` | **not merged** | story writes *Catastrophe Pictures* for evidence, plain *Pictures* for the share | evidence and trophy shots share a lifecycle and an audience |

The last column is what makes the ledger readable by someone who was not there.
It is also the honest admission that these are decisions, not observations.

## Merging bounded contexts

Contexts merge on *responsibility*, never on name. Two bubbles with different
names and the same commands, objects and actors are one context drawn twice —
say so, propose the merge, and keep both names as `skos:altLabel` until the team
picks one. Two bubbles with the same name in different artifacts may still be
two contexts if their contents differ; check contents before merging on a name.

## What never merges

- A `dkg:Question` with anything. Questions accumulate; two people asking the
  same thing twice is a signal about the room.
- Nodes from different confidence levels do not *inherit* the higher one. A
  merge of an `OnArtifact` node and an `Inferred` node keeps
  `dkg:OnArtifact` for the merged node **only for the properties the artifact
  actually showed**; the inferred properties stay inferred.
- Anything the user has already declined to merge. Record the refusal as a
  comment on the node so the next ingest does not re-propose it.