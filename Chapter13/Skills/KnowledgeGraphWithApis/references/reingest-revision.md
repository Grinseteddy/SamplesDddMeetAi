# Re-ingest — a revised artifact

The team redrew something already in the register: the glossary after an ADR
renamed a term, the board after a second session, the ADR log with three new
rows. This is the third mode. It is not *grow*, because the new picture is not
a new voice — it is the same voice, later — and matching it against the graph
with the cross-artifact rules would mint a parallel vocabulary. It is not a
rewrite, because what v1 said is evidence: a cardinality that changed between
March and September is a business decision somebody made, and the graph should
be able to show both.

**The commitments still hold.** Nothing is deleted. A node the revision dropped
stays, with an edge saying it was dropped. A node the revision changed keeps its
old statement and gains a new one, joined by `dkg:supersedes`.

## 0. Recognising a revision

Do not rely on the file name or the label ("Enhanced 09" says nothing to the
graph). Three signals, in order of weight — and only artifacts of the **same
subclass** count; a canvas and a glossary sharing terms is corroboration, not
lineage:

1. **Same artifact subclass already in the register** — a second glossary, a
   second board, a second canvas *for the same context*.
2. **Label overlap, measured both ways.** After transcribing, run
   `check_graph.py graph.ttl --overlap all labels.txt`. It reports two
   numbers per artifact — *share* (how much of the new is already held) and
   *cover* (how much of the held the new contains) — and reads them as:

   | share | cover | verdict |
   |---|---|---|
   | ≥ ⅔ | ≥ ⅔ | **revision** — same picture, redrawn |
   | ≥ ⅔ | < ⅔ | **partition** — a piece of it (§0b), or heavy borrowing |
   | < ⅓ | ≥ ⅔ | **superset** — it plus much more; revision with additions, or a merge |
   | either ≥ ⅓ | | **ask** |
   | both < ⅓ | | new artifact |

   One number alone misleads: a per-context glossary scores 100% *share*
   against the domain-wide one and is not a revision of it.
   An **API specification** carries its own key: an OpenAPI `x-api-id` is
   stable across versions by house convention, so the same id on a `dkg:Api`
   already in the graph is a revision whatever the overlap says
   (`api_inventory.py --graph` prints `REVISION` when it sees one). AsyncAPI
   has no id — run `--overlap` on the labels `api_inventory.py --labels`
   writes (operations, messages, channels, schemas). A revised spec's diff is
   operations added / removed / re-pathed, messages added / removed, schemas
   renamed; an operation whose `dkg:path` or `dkg:httpMethod` changed is a
   `dkg:supersedes` pair, since the old contract was published.
3. **A Decision in the graph that asked for it.** If an ADR row says "each
   Visual Glossary needs a legend" or "Menu is used for Menu and Meal plan" and
   the arriving glossary has a legend and says Menu, the revision is the loop
   closing — name that Decision in the artifact's `rdfs:comment`.

Two artifacts of the same type can legitimately both be new: two domain
stories, two canvases for two contexts. The overlap test tells them apart from
a redraw; the subclass alone does not. When the register holds several
artifacts of the type, run `--overlap all` first — it scores every artifact and
names the best match, so you are never comparing against the wrong glossary.

## 0b. Partitions — one artifact becomes several

A domain-wide glossary split into one glossary per bounded context is the
common case, and it is **neither a revision nor a set of new artifacts.** Each
piece overlaps the parent at a third to a half; run per piece, the five-bin
diff would call most of the parent *removed* five times over.

Treat the pieces as **one revision ingested in parts**:

- every piece gets `dkg:revisionOf` the parent, and `rdfs:comment "partition:
  <context>"`;
- match each piece's labels against the parent with the lineage-loose rule;
  unchanged / renamed / changed / added are per piece;
- **removed is computed once, against the union of all pieces**, after the
  last piece is in. Until then, report "removal pending — n pieces of m
  ingested" and emit no `dkg:absentFrom`. A term absent from the Cooking
  Assistance glossary is not removed; it lives in the Meal Planning one;
- a term appearing in **two pieces** is the partition's own answer to
  ownership — see the per-context rule in `ingest-visual-glossary.md`. It is
  never a removal and never a duplicate.

If the pieces do not arrive together, ask how many there will be and record
the answer on the parent's `rdfs:comment`; the removal step waits for it.
The reverse — several per-context glossaries merged into one — is a plain
revision with several parents: repeat `dkg:revisionOf` once per parent.

## 1. Register the revision

A new `dkg:Artifact` of the same subclass, with `dkg:revisionOf` pointing at
the earlier one. Chain revisions (`v3 revisionOf v2`, `v2 revisionOf v1`); the
lineage is walkable.

```turtle
:Art_Glossary_2026_09_15 a dkg:VisualGlossary ;
    rdfs:label "Visual Glossary Enhanced 09" ;
    dkg:revisionOf :Art_Glossary_2026_09_10 ;
    rdfs:comment "Redrawn after SADR0003 (Menu) and SADR0004 (Help split)." ;
    dkg:ingestedAt "2026-09-15"^^xsd:date .
```

If the artifact type has a companion skill that produced the revision from a
patch list (`visual-glossary-updater`, `bounded-context-canvas-author`), say so
in the comment — it tells the next reader the revision was derived, not drawn
in a room.

## 2. Transcribe, then diff before you emit

Transcribe the revision exactly as for a first ingest. Then, **before minting
anything**, lay it against the nodes whose `dkg:source` includes the earlier
artifact, and sort every item into one of four bins:

| Bin | Test | What to emit |
|---|---|---|
| **Unchanged** | same label, same class, same properties | add the revision as a `dkg:source` on the existing node. Nothing else. |
| **Renamed** | the earlier label is gone and a new one sits in its place — same relationships, same neighbours | the *existing* node: new `skos:prefLabel`, old label to `skos:altLabel`, revision added as source. IRI unchanged (`ontology.md` §2). If an ADR in the graph made this rename, `dkg:mentions` from that Decision should already point here; add it if not. |
| **Changed** | same term, but a cardinality, verb, class marking, context colour or position differs | keep the old `Relationship` / `Assertion`; mint a new one sourced to the revision; `new dkg:supersedes old`. Both stay. |
| **Added** | a term or arrow the earlier artifact did not have | mint as for a first ingest. Then run the *cross*-artifact merge rules against the rest of the graph — a term new to the glossary may be old news to a board. |
| **Removed** | a node sourced to the earlier artifact with no counterpart in the revision | the node stays; add `dkg:absentFrom :Art_<revision>`. If the revision dropped it because an ADR said so, `dkg:mentions` from that Decision is the explanation; find it. |

**Matching within a lineage is looser than across artifacts.** Case,
punctuation and singular/plural are the same team's hand; treat `Help request`
/ `Help Request` as one node here where `merging.md` would say propose. The
looser rule applies *only* to nodes whose source is the earlier revision —
never to nodes from other artifacts.

Ambiguity — a term that could be a rename of A or of B — goes in the ledger as
a proposal, exactly as in grow mode. A wrong rename is worse than a pending one.

## 3. Changed cardinalities are the point

```turtle
:Rel_HelpContainsMenuProposal_v2 a dkg:Relationship ;
    dkg:from :Con_HelpResponse ; dkg:verb "contains" ; dkg:to :Con_MenuProposal ;
    dkg:cardinality "0..3" ; dkg:cardinalityGiven true ;
    dkg:supersedes :Rel_HelpContainsMenuProposal ;
    rdfs:comment "was 1..3 in Enhanced 08" ;
    dkg:source :Art_Glossary_2026_09_15 ; dkg:confidence dkg:OnArtifact .
```

`dkg:supersedes`, not `dkg:contradicts`: one artifact changing its mind is a
different fact from two artifacts disagreeing, and `views.py` lists the first
under *changed* and the second under *contradictions*. If the graph holds a
`dkg:contradicts` between the earlier relationship and some third artifact,
check whether the revision resolves it — if the new cardinality now agrees with
the third artifact, say so in the report; do not remove the contradicts edge,
it is history too.

## 4. Special cases by artifact type

- **ADR log** — match rows on ID. A row whose Status changed: new
  `dkg:status` value, old one kept in `rdfs:comment` with the earlier date. A
  row missing from the new file: `dkg:absentFrom`, never removed. New rows: as
  a first ingest (`ingest-small-adr.md`).
- **Bounded Context Canvas** — a redrawn canvas usually changes messages
  (`dkg:Message` rows added or dropped) and field markers (`(proposed)` →
  `(given)`). A marker moving up is a confidence change on the *existing* node
  — record it as a dated `dkg:Assertion` rather than editing `dkg:confidence`,
  so the graph shows when a hypothesis became a fact.
- **EventStorming board after a second session** — new events go through
  `ingest-event-storming.md`; but a hotspot the revision *removed* is the most
  interesting removal there is. `dkg:absentFrom` plus a sentence in the report:
  was it resolved, or erased?
- **Brainstorm** — brainstorms are not revised; a second session is a second
  brainstorm. Grow, not revise.

## 5. Report — a diff, not an inventory

In this order, replacing Step 7's item 2:

1. Corroboration, as always.
2. **Unchanged / renamed / changed / added / removed**, with counts, then each
   list. Removed first among the lists — it is the one people did not expect.
3. **Which Decisions explain which changes.** A rename with a Decision behind it
   is the loop closing; a rename with none is the team deciding something they
   did not write down.
4. **Contradictions the revision resolved, or created.**
5. The merge ledger — for the *added* items only; the lineage matches are in
   the diff lists.
6. Open questions, including every ambiguous rename.
