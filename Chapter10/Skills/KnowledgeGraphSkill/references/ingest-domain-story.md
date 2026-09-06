# Ingest — Domain Story

Numbered actor → activity → work-object sentences (Hofer & Schwentner), arriving
as a picture, an egon.io export, or plain text. Often several stories at once;
ingest **one story per `dkg:Artifact`**, never several into one, because
comparing stories is a large part of what the graph is for.

## Transcribe

Numbered sentences, verbatim, in the diagram's own spelling. Follow the numbers
at the arrow origins; unnumbered continuation arrows (`and takes`, `for`,
`with`) belong to the same sentence.

**Split compound sentences** into `2a` / `2b` when the clauses do different
things — `burns Meal` and `takes Pictures` are one sentence on the wall and two
facts in the graph. Say in the report that you split them; on a redraw they are
often genuinely two sentences, and that is a finding for the modellers.

Record **lanes or groups** if drawn. If none are drawn, the story has no
contexts and the graph must not invent any.

## Map

| In the story | Class / property |
|---|---|
| Actor pictogram | `dkg:Actor` |
| Activity (the verb on the arrow) | `dkg:Activity` |
| Work object | `dkg:WorkObject` (subclass of `dkg:Concept`) |
| One numbered sentence | `dkg:Sentence`, with `dkg:sequence` |
| Sentence n → n+1 | `dkg:precedes` |
| Lane / group, if drawn | `dkg:Lane` (subclass of `dkg:BoundedContext`), via `dkg:inContext` |

A `dkg:Sentence` node holds the whole fact together:

```turtle
:Sen_StoryA_03 a dkg:Sentence ; dkg:sequence 3 ;
    skos:prefLabel "Cook asks Grandma Avatar for Help with Catastrophe Pictures" ;
    dkg:performedBy :Act_Cook ;
    dkg:mentions :Act_GrandmaAvatar, :Con_Help, :Con_CatastrophePictures ;
    dkg:precedes :Sen_StoryA_04 ;
    dkg:source :Art_StoryA ; dkg:locator "sentence 3" ;
    dkg:confidence dkg:OnArtifact .
```

Keeping sentences as nodes — rather than dissolving them into edges — is what
lets the flow lens replay a story and the traceability lens show which story a
term first appeared in.

## Merge hazards

- **The same word in two senses across a story.** The classic: *Pictures* as
  diagnostic evidence and *Pictures* as a trophy shot; *Help* as a request and
  *Help* as a response; *Meal* as a pan and *Meal* as a described situation.
  When the story itself qualifies one use (`Catastrophe Pictures`), that is the
  story telling you they are two concepts — split them and say so.
- **Verbs that are UI or CRUD** (`clicks`, `updates`, `creates`) are still
  ingested verbatim. They are evidence about the story's quality, and
  `domain-story-critic` is where that gets judged.
- **Actors across stories.** `Community` in one story and `Community Cook` on a
  board: propose. Four stories agreeing on a spelling against one board is a
  real argument about the ubiquitous language, and the graph should let someone
  count it rather than settle it.
- **Work objects against glossary terms.** Auto-merge on exact match; otherwise
  propose. If a glossary is already in the graph, this ingest is where drift
  becomes visible, and every near-miss belongs in the ledger.

## Several stories at once

Ingest in the order given, reporting after each. Then add one cross-story
paragraph: which terms every story shares, which appear in only one, and where
two stories spell the same thing differently. That paragraph is the input to
`domain-story-glossary-consistency` and is often the reason someone wanted the
graph.

## Report additions

- **Sentences split**, with both halves.
- **Terms this story introduces that no other artifact has** — new vocabulary
  arriving late is worth a look.
- **Actors who do nothing** and **work objects nobody creates** — read but never
  written, which usually means a missing context.