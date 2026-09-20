# Using an architecture knowledge graph, when one exists

Read this whenever a `.ttl` architecture knowledge graph is available —
built by `domain-knowledge-graph` and/or grown by
`adr-and-principles-ingester`. Always ask once whether one exists for the
map's contexts, even if nobody mentioned it. Where the glossary
(`SKILL.md`'s glossary section) is the authority on *naming*, the graph is
the authority on **what's already been decided**: a room shouldn't
re-litigate a sync/async call or a technology pick this skill would
otherwise present as fresh, if an accepted ADR already made that call.

## Loading the graph

`rdflib` if it's large enough that eyeballing the file is impractical
(`pip install rdflib --break-system-packages`, then a plain `SELECT` — no
purpose-built miner is needed for this skill's narrower ask). Pull two
things:

- **Principles** — `dkg:Principle` nodes, their `dkg:statement`,
  `dkg:scope`, and `dkg:testable` flag:
  ```sparql
  SELECT ?p ?statement ?scope ?testable WHERE {
    ?p a dkg:Principle ; dkg:statement ?statement .
    OPTIONAL { ?p dkg:scope ?scope }
    OPTIONAL { ?p dkg:testable ?testable }
  }
  ```
- **Decisions** — `dkg:Decision` nodes with `dkg:status` `Accepted` /
  `Adopted`, their `dkg:decidedOption` and any `dkg:honors`/`dkg:overrides`
  edges to a Principle:
  ```sparql
  SELECT ?d ?decision ?status ?option WHERE {
    ?d a dkg:Decision ; dkg:status ?status ; dkg:decidedOption ?option .
    FILTER (?status IN ("Accepted", "Adopted"))
  }
  ```

Both lists are usually short enough to read in full rather than write a
narrower query — do that first, then use SPARQL only to re-find something
once you know what you're looking for.

## Matching a Decision or Principle to a border

Match **by what it says, not by structural links the vocabulary doesn't
have** — the base graph has no `dkg:Decision → dkg:Message` edge, so
matching is a judgment call the same way glossary-term matching is: does
the decision's or principle's text name this crossing's two contexts, or
the technology family, closely enough that a reader would agree it
applies? State the match, don't just assert the conclusion. A Decision
that merely mentions one of the two context names in passing is not the
same as a Decision *about* that border — say which kind of match it is
rather than presenting both with equal confidence.

Before citing either, check it's actually current: a `dkg:testable false`
Principle settles nothing (it's excluded from the compliance read
entirely), and a `dkg:status` of `Superseded` or `Deprecated` on a
Decision means it's history, not current guidance.

## Three places this changes the workflow

- **Step 2, new top rung.** An Accepted Decision (or a `dkg:testable true`
  Principle with no conflicting Decision) that names this border's
  integration pattern outranks even the map's own stated mechanism — a
  ratified decision beats an unconfirmed diagram note. Mark the verdict
  `graph` confidence and cite the Decision/Principle IRI or ID. If a
  Principle settles it only as a general default (no border-specific
  Decision), that's weaker than a named Decision but still stronger than
  the generic cmd/qry/evt type default — slot it in just above the type
  default rung and mark it `principle-default`, not `default`.
- **Step 3, ask only what's still open.** Before asking which technology to
  sketch toward, check whether an Accepted Decision or an unambiguous
  Principle already commits to one. If so, state the technology and its
  source instead of asking; if the graph only shows a *preference* (a
  Principle that isn't a hard mandate), still ask, but mention the
  preference as a hint. If two Accepted Decisions answer the same
  technology question differently, that's a real contradiction in the
  team's own record — surface both, verbatim, and ask the user to resolve
  it rather than silently picking either side.
- **Steps 4–5, a compliance check, not a redesign.** After sketching, check
  every operation/channel against the testable Principles that apply to
  it. A sketch that would `overrides` a testable Principle doesn't get
  quietly built around the principle instead — flag it in the row
  (`(principle conflict: P<id>)`) and report it in full in the report's
  Architecture context section. Whether to honor the principle or record a
  deliberate exception is the room's call, not this skill's.

## No graph supplied

Proceed exactly as the unenhanced workflow does — every sync/async verdict
from Step 2's map/staleness/story/type-default rungs only, and Step 3
always asks. Offer `adr-and-principles-ingester` if ADRs or a principles
document exist but haven't been graphed yet, or `domain-knowledge-graph`
if the team wants full traceability rather than just this skill's
narrower reads.