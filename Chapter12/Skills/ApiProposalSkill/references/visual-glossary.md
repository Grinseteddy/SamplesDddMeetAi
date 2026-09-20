# Using a Visual Glossary, when one exists

Read this whenever a Visual Glossary is available for the map's contexts.
Always ask once whether one exists, even if nobody mentioned it — it is
cheap to ask and every sketch below gets more trustworthy with it. If the
glossary is still a picture, run it through `visual-glossary-interpreter`
first and work from its Glossary Brief rather than re-reading the image by
eye.

A glossary **wins on naming and shape; the map and the stories still win on
which operations and channels exist and what triggers them.** Concretely:

- **Naming.** Use the glossary's exact term spelling — case and all — for
  operation nouns (Step 4), channel/topic names (Step 5), and field names
  in every rough input/output/payload, even where the map's crossing label
  or the story's own wording differs. When they disagree, use the glossary
  term and record the discrepancy rather than silently harmonizing it —
  it belongs in the report's Terminology section.
- **Singular vs array shape.** A glossary relationship's cardinality
  decides the rough field shape directly: a `1` or `0..1` target is a
  singular field (`0..1` noted as optional); a `1..*`, `0..*`, or bounded
  `1..N` target is an array field, with the stated bound carried over as a
  note (e.g. "up to 10") — never invent a bound the glossary didn't give.
  Without a glossary, guess the shape from the story's own phrasing
  (plural work object, "each", "a list of…") and mark it
  `(unconfirmed shape)`.
- **Identity.** The glossary's identity term for an entity (`ISBN`,
  `ReservationId`) becomes that operation's id-equivalent field — in the
  rough input for a lookup, the rough output for a creation.
- **Entity vs value object.** An **entity** (its own identity, referenced
  independently) is worth its own operation or its own place in a payload;
  a **value object** (a leaf with no identity of its own, meaningful only
  as part of its parent) stays an inline field on the parent's payload and
  never becomes its own operation or channel, however often the map's
  crossing label mentions it.
- **No glossary supplied.** Proceed exactly as the unenhanced workflow
  does — names and shapes come from the crossing and the story, and every
  shape guess is marked `(unconfirmed shape)` rather than presented as
  settled. Offer `visual-glossary-interpreter` (from a picture) or note
  that field-level detail will be firmer once one exists.

If the map, the stories, and the glossary each use a different word for the
same concept, that three-way mismatch is worth a dedicated pass —
`domain-story-glossary-consistency` — rather than resolving it silently
inside this skill's report.