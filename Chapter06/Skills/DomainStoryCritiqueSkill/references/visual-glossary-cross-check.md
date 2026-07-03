# Visual Glossary cross-check

*Use this file only when the person provides a Visual Glossary.* Of all the
artifacts you might be handed alongside a domain story, this one is special: it
is the **terminology authority**. The others are checked for coverage; the
glossary is checked for coverage **and** dictates the words the story must use.

## What a Visual Glossary is

A Visual Glossary is the **static companion** to domain stories (Hofer &
Schwentner, 2022). A domain story shows one *dynamic* scenario unfolding over
time; the glossary shows the *static structure* of the domain's nouns:

- the **work objects** drawn as entities (the noun stickies),
- the **relationships** between them, each with a **label** ("stores,"
  "contains," "located at," "relates to"),
- the **cardinalities** on those relationships (`0..*`, `1`, `0..1`, `1..*`),
- usually clustered into the same **bounded contexts** as the stories (the
  ellipses or boxes).

It is built *from* the work objects that appear across the domain stories, and
its whole purpose is to pin down the **agreed terms** — the ubiquitous language —
and how they relate. Story and glossary are meant to stay consistent: the
glossary defines the vocabulary, the stories speak it.

**The headline rule:** when a glossary is provided, its terms are the agreed
language. The story must use them, and so must you — when you rewrite a step or
suggest a fix, use the glossary's word ("Bicycle," "Rack," "Commuter"), never
your own paraphrase ("bike," "stand," "user").

## How to read a Visual Glossary

1. List the **work objects / entities** — the noun stickies. These are the
   agreed terms.
2. List the **actors**, if the glossary includes them (people stickies such as
   Tourist, Commuter, Hotel).
3. Record each **relationship** as `source —label→ target`, with its
   **cardinalities**.
4. Record the **bounded-context groups** (the ellipses): which terms belong to
   which subdomain.
5. Note **inconsistencies inside the glossary itself** (a term spelled two ways,
   the same noun appearing in two contexts) — these are findings too, but read
   the *intended* term charitably rather than critiquing a typo as a concept.

### Worked example — reading the bicycle-sharing glossary

**Bounded contexts → terms:**
- *User Management*: Tourist, Commuter
- *Partner Management*: Hotel
- *Bicycle Distribution*: Bicycle
- *Bicycle Management*: Bicycle
- *Rack Management*: Rack, Code, Lock
- *Accounting*: Monthly Fee
- *(ungrouped / shared)*: Fee, Station Public Transport

**Relationships (with cardinalities):**
- Tourist —pays→ Fee
- Hotel —charges→ Fee `0..*`
- Tourist —drives→ Bicycle
- Commuter —drives→ Bicycle
- Hotel —stores→ Bicycle `0..*`
- Bicycle *(Management)* —relates to→ Bicycle *(Distribution)* `1`
- Rack —stores→ Bicycle `0..1` / `0..*`
- Rack —located at→ Station Public Transport `0..1`
- Rack —contains→ Lock `1..*`
- Code —unlocks→ Lock `1`
- Code —locks→ Bicycle
- Commuter —uses→ Code
- Commuter —pays→ Monthly Fee `1..*`

**Glossary-internal notes (charitable):** "realtes to" and "Mangement" are
typos — read them as "relates to" and "Management." Two `Fee` concepts coexist
(a one-off **Fee** that a Tourist pays / a Hotel is charged, and a recurring
**Monthly Fee** a Commuter pays) — plausibly deliberate, worth one confirming
question. **Bicycle** appears in two contexts (Distribution and Management)
related `1`-to-`1`: in DDD that can be the *same* real bicycle modelled in two
bounded contexts (legitimate), or accidental duplication — ask which.

## The four checks

### 1. Terminology fidelity — the headline check
The story's work objects and actor names must match the glossary's terms
*exactly*. Findings:
- **Term mismatch** — the story says "Bike" / "Stand" / "User" where the glossary
  says "Bicycle" / "Rack" / "Commuter." The story should adopt the glossary's
  word. This is the single most valuable finding here, because divergent
  vocabulary is precisely what the glossary exists to prevent.
- **Undefined term** — a work object or actor in the story the glossary doesn't
  list. Either the glossary is incomplete (extend it) or the story coined an
  off-language term (rename it). Ask which; don't assume.
- **Unused term** — a glossary term no story ever touches. Either a story is
  missing, or it's dead vocabulary the glossary should drop.

### 2. Relationship & cardinality consistency
The glossary's relationships and cardinalities constrain what a story can
plausibly show. Findings:
- A step that **contradicts a cardinality** — the glossary says a Bicycle is
  stored at `0..1` Rack, but a story has one bicycle racked in two places at
  once; or the glossary says a Rack `contains 1..* Lock` but a story implies a
  rack with no lock.
- A **relationship the glossary defines that no story exercises** — e.g. "Rack
  located at Station Public Transport" never appears in any told scenario;
  possibly a missing story.
- A **relationship a story implies that the glossary lacks** — the story connects
  two nouns the glossary never relates; the glossary may need that edge.

### 3. Bounded-context alignment
The glossary groups terms into subdomains; the story's lanes/groups should agree.
Findings: a story lane named differently from the glossary's context for the same
terms (story "Billing" vs glossary "Accounting"); a term the story files under a
different context than the glossary does; a glossary context that no story
touches at all.

### 4. Coverage both ways
Walk every glossary term and ask "does a story use it?"; walk every story work
object and ask "is it in the glossary?" The gaps in either direction are the
findings.

## Catalogue of story-vs-glossary findings

The same shape as the other cross-checks, plus two that are unique to the
glossary (marked ★):

- **Dropped term/relationship** — in the glossary, never used by a story.
- **Unsupported addition** — in a story, absent from the glossary.
- **★ Term mismatch** — same concept, different word. The most valuable finding.
- **★ Cardinality contradiction** — a story violates a glossary multiplicity.
- **Bounded-context misalignment** — story groups and glossary contexts disagree.
- **Whole context missing** — a glossary subdomain with no story.

Keep every finding open and falsifiable, as with the other cross-checks: *"The
glossary calls this a Commuter; the story says User — is that the same person, or
two different roles you want to keep distinct?"* The glossary is authoritative on
vocabulary, but the authors are authoritative on intent — so surface the mismatch
and let them decide whether the story or the glossary moves.