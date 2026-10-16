# Quality Requirements: SOLID for API Design

CRUD gets an API working; it does not keep it working for years. APIs that must
be maintained and evolved need further qualities — above all, the ability to
extend the API without breaking its clients (xiao2021crud, zalando_guidelines,
junker2025crafting). This file carries the SOLID principles, originally
formulated for object-oriented design (martin2000principles), over to API design
(kolb2026solid, merson2020ideals), maps each to the ISO/IEC 25010 quality
characteristics it serves, and turns each into concrete rules and review checks
for the spec this skill writes.

Read this file **before writing** (step 4) and again for the **quality review**
(step 5). When the task is to **extend or change an existing spec**, §3 (Liskov)
and §5 (Open/closed) are mandatory.

## Contents
1. Which qualities matter — a business decision
2. Quality map (principle → ISO/IEC 25010)
3. Liskov substitution — backward compatibility over time
4. Interface segregation — segregate by capability, not by consumer
5. Open/closed — additive evolution, versioned breaking changes
6. Dependency inversion — links and the spec as the shared abstraction
7. Single responsibility — one Bounded Context per API
8. Quality review checklist (step 5)
9. Reporting (step 7)
10. Sources

---

## 1. Which qualities matter — a business decision

Which qualities matter, and to what degree, is a **business decision**, not the
spec author's. ISO/IEC 25010:2023 supplies the shared vocabulary: nine product
quality characteristics with 40 sub-characteristics, from functional suitability
to safety (iso25010_2023). Junker and Lazzaretti discuss each from an API
perspective (junker2025crafting).

This skill covers only the qualities **the shape of an endpoint decides**.
Performance efficiency, reliability and security depend more on implementation
and operation; note them as open items if the user raises them, but do not
claim the spec delivers them.

In step 1, if the conversation, an architecture knowledge graph, ADRs or
architecture principles name prioritized qualities, use them. Otherwise apply
all five principles below at default strength and say so in the summary — do
not interrogate the user about ISO characteristics.

## 2. Quality map

| Principle | ISO/IEC 25010:2023 characteristic (sub-characteristics) |
|---|---|
| Liskov substitution | Compatibility (co-existence, interoperability); Flexibility (replaceability) |
| Interface segregation | Functional suitability (functional appropriateness); Interaction capability (user error protection) |
| Open/closed | Maintainability (modifiability); Compatibility (co-existence) |
| Dependency inversion | Flexibility (adaptability); Maintainability (testability) |
| Single responsibility | Maintainability (modularity, reusability); Interaction capability (appropriateness recognizability, self-descriptiveness) |

These principles are architecture principles. When the user keeps an
architecture or domain knowledge graph, they can be recorded there too
(see the `adr-and-principles-ingester` / `domain-knowledge-graph` skills).

---

## 3. Liskov substitution — backward compatibility over time

**Principle.** Backward compatibility is the Liskov substitution principle
applied over time: a new version of an API must be usable wherever the previous
version was used (liskov1994behavioral, seemann2021backwards). Consumers that
already use an API must not fail when a new version is deployed
(zalando_guidelines). To stay compatible you may **weaken preconditions or
strengthen postconditions** (seemann2021backwards).

**Rules — a new revision of an existing spec must not:**
- add a **required** request field, parameter or header to an existing operation;
- **reject inputs it used to accept** — tighten `minLength`/`maxLength`,
  `pattern`, `maxItems`, `format`, remove an `enum` value from a request, make a
  nullable request field non-nullable;
  (all of these strengthen preconditions)
- **remove a response field**, make a `required` response field optional,
  change a field's **type** or **meaning**, or add a value to a response `enum`
  that clients were told was closed (all of these weaken postconditions)
  (zalando_guidelines);
- rename or move a path, or change an `operationId` that clients generate code from.

**Allowed:** new optional request fields, relaxed request constraints, new
response fields, new operations. Adding response fields is safe **only if
clients are tolerant readers** that ignore what they do not know
(fowler2011tolerant) — so say so in `info.description` (see §5).

**Checks.**
- *Syntactic:* when an earlier version of the spec exists, diff it with
  **oasdiff** (oasdiff); see `references/validation.md`. (Buf does the same for
  Protocol Buffers — buf_breaking.)
- *Semantic:* tooling only detects syntactic changes. A semantic change that
  leaves the structure untouched — a field that used to carry the customer
  number and now carries the customer ID — passes every check
  (zalando_guidelines, junker2025crafting). So **compare every changed property
  `description` against the previous version by hand**; if the meaning changed,
  it is a breaking change even if oasdiff is silent. This is one more reason the
  house style makes descriptions mandatory.

## 4. Interface segregation — segregate by capability, not by consumer

**Principle.** No client should be forced to depend on methods it does not use
(martin1996isp). Taken literally, this yields one API per consumer, as in
Backends for Frontends (newman2015bff, merson2020ideals). For externalized
business capabilities that makes little sense: they are meant to serve many
consumers. Evans recommends opening **one shared protocol** to all who need to
integrate and handling a single consumer's idiosyncratic needs with a **separate
translator**, so that the shared protocol stays simple and coherent
(evans2015reference). Segregate along **business capabilities**, ideally one API
per Bounded Context (evans2003ddd, vernon2013implementing). Such APIs stay lean
and avoid "world-dominating" APIs (junker2025crafting).

**Rules.**
- Do **not** add consumer-specific operations, query flags or fields
  (`/catalog-entries-for-mobile-app`, `?forPartnerX=true`, `partnerXDisplayName`)
  to the shared API. Note the need and propose a separate translator / BFF
  outside this spec.
- Name operations and fields in the Bounded Context's Ubiquitous Language, not
  in a consumer's screen or process vocabulary.
- Serve *functional appropriateness*: each operation does one business thing;
  avoid "god endpoints" that switch behaviour by a `type`/`action` field
  (stocker2024refactoring). Prefer a dedicated operation (Extract Operation).
- Serve *user error protection*: constrain inputs (`enum`, `pattern`,
  `minLength`, `format`, `required`) so invalid requests fail with `400` at the
  contract rather than deep in the implementation — the existing house style
  already demands generous constraints; treat them as a quality requirement,
  not decoration.

## 5. Open/closed — additive evolution, versioned breaking changes

**Principle.** Open for extension, closed for modification (meyer1988oosc).
GitHub's published versioning policy for its REST API is a good template
(github_api_versions).

**Rules — within a version, changes are additive only:** new endpoints, new
optional request fields, new response fields. Existing fields keep their meaning
and type; no required request fields are added to existing operations
(github_api_versions, zalando_guidelines, junker2025crafting_versioning).

**Before introducing a new version**, check whether the change actually reflects
a **new business requirement**; if so, a new endpoint (or event) is the better
answer than a new version of an old one (junker2025crafting_versioning,
zalando_guidelines). Ask the user when unclear.

**When a breaking change is unavoidable**, ship it as a **new major version that
runs alongside the old one**, so clients can migrate without a big bang, with a
migration guide and a deprecation timeline announced at runtime through the
`Deprecation` and `Sunset` headers (junker2025crafting_versioning,
github_api_versions, rfc9745, rfc8594). In this house style that means:

1. New file (or new `servers` base path) with `info.version` bumped to the next
   **MAJOR**; `x-api-id` stays the same; the `VersionParameter` `enum`/`default`
   is locked to the new version.
2. In the **old** spec, mark each affected operation `deprecated: true`, state in
   its `description` the successor operation/version, the sunset date and a link
   to the migration guide, and add the deprecation headers to its success
   responses via shared components:

```yaml
components:
  headers:
    DeprecationHeader:
      description: |
        Date from which this operation is deprecated (RFC 9745), as a
        structured-field date, e.g. "@1798761600".
      schema:
        type: string
    SunsetHeader:
      description: |
        HTTP-date after which this operation may stop responding (RFC 8594).
      schema:
        type: string
    DeprecationLinkHeader:
      description: |
        Link to the migration guide (rel="deprecation") and the successor
        version (rel="successor-version").
      schema:
        type: string
```

   and in the deprecated operation's success response:

```yaml
      headers:
        Deprecation:
          $ref: '#/components/headers/DeprecationHeader'
        Sunset:
          $ref: '#/components/headers/SunsetHeader'
        Link:
          $ref: '#/components/headers/DeprecationLinkHeader'
```

3. Both versions stay in production for the announced period (Two in
   Production, Limited Lifetime Guarantee — zimmermann2023patterns); the
   version header (Version Identifier) lets clients and gateways route.

**The client side has to do its part.** Additive evolution only works if
clients are **tolerant readers**: they ignore unknown fields and do not treat
enumerations as closed (fowler2011tolerant, daigneau2011sdp,
junker2025crafting_versioning). Zimmermann et al. name Tolerant Reader as the
client-side complement to the provider-side evolution patterns
(zimmermann2023patterns). Make this explicit in the contract:

- Add one sentence to `info.description`, e.g. *"Clients must ignore unknown
  response fields and handle unknown enumeration values; the API evolves
  additively within a major version."*
- Do **not** set `additionalProperties: false` on response schemas — it
  declares the model closed and turns every additive change into a validation
  failure for strict clients (the ruleset warns).
- For a **response** enumeration that is expected to grow (statuses, categories),
  say so in its `description` ("New values may be added; treat unknown values
  as …") and, if the user follows the Zalando guidelines, mark it with
  `x-extensible-enum` instead of `enum`. Request enumerations may stay closed —
  they are input validation (§4).

## 6. Dependency inversion — links and the spec as the shared abstraction

**Principle.** For interfaces, dependency inversion means clients rely on
**links they discover** rather than on URLs they construct
(martin1996dip, fielding2008hypertext). Servers can then restructure routing
without breaking clients; only the entry points must remain stable
(richardson2008maturity, fowler2010rmm). Hypermedia decouples clients from URLs
but **not from the domain model** behind them (zalando_guidelines) — what a URL
represents can still change (§3). The shared abstraction is therefore the **API
description itself**, which states not only where a resource is but what it
means. Interface definitions belong to the specification rather than to the
code (boeckeler2025sdd); in this approach the specification is also part of the
domain knowledge graph.

**Rules.**
- Where a response points to a related resource or a sub-resource, return a
  **link** (`format: uri` property ending in `Link`, as `abstractLink` /
  `tagsLink` in the example), not just a bare id the client must turn into a
  URL. Ids may be returned *in addition* for identity.
- Keep the documented entry points (top-level collections) stable; nested URLs
  reached via links may be restructured.
- Where one operation's output feeds another, add an OpenAPI `links` object
  on the response (e.g. `createCatalogEntry` 201 → `getCatalogEntryById` with
  `catalogEntryId: $response.body#/catalogEntryId`) so generators and contract
  tools can follow it.
- Treat descriptions and examples as part of the abstraction: every schema and
  important property carries `examples`, so consumers can state their
  expectations as concrete examples.

**Testability.** Whether providers and consumers still agree on the spec is
checked with contract tests (robinson2006cdc, pact_docs, specmatic) — Specmatic
can run them straight from this YAML. Schema-based tests catch structural
changes; a change in **meaning** is caught only when consumers state their
expectations as concrete examples. In the summary, recommend contract tests
and point to the examples in the spec as their seed.

## 7. Single responsibility — one Bounded Context per API

**Principle.** Single responsibility is about people: a module should have only
one reason to change, and that reason is a single, narrowly defined business
function (martin2014srp, martin2017clean). For an API: it exposes **one business
capability — or, better, one Bounded Context with its Ubiquitous Language**
(merson2020ideals, evans2003ddd). APIs that span several capabilities mix models
whose terms mean different things in different parts of the organization; they
are hard to understand, and the meaning of their parts drifts as each group
pulls them its own way (evans2015reference, stocker2024refactoring). A single
API for an entire application is already a smell (zalando_guidelines,
junker2025crafting). Splitting should **stop where a business process would be
torn apart**: one API still covers a complete process within its Bounded
Context (zalando_guidelines).

**Rules.**
- One spec = one Bounded Context (this is why step 2 offers one `.yaml` per
  context). If a single-service description clearly spans several capabilities,
  say so and propose a split before writing.
- Smell signals while writing: one term needs two different schemas
  (`Book` for catalog vs. `Book` for lending); tags that read like separate
  departments; scopes with different owning teams; `info.description` needs
  "and" to join unrelated purposes. Report them.
- Do not split a complete process across specs: all steps of a process owned by
  this context stay in this spec, even if that makes it larger.
- Serve *appropriateness recognizability* and *self-descriptiveness*:
  `info.description` states the one capability and its consumers; every tag,
  operation and property is described in the context's Ubiquitous Language.

---

## 8. Quality review checklist (step 5)

Walk the finished spec through this list. Fix what you can; report the rest.

| # | Principle | Check |
|---|---|---|
| L1 | Liskov | Previous version exists? → oasdiff breaking diff is clean (or the change ships as a new major, §5). |
| L2 | Liskov | Every changed property `description` keeps its meaning. |
| I1 | Interface seg. | No consumer-specific operations, flags or fields. |
| I2 | Interface seg. | No god endpoints switching behaviour on a `type`/`action` field. |
| I3 | Interface seg. | Request inputs constrained (`required`, `enum`, `pattern`, lengths, `format`). |
| O1 | Open/closed | `info.description` states the tolerant-reader expectation. |
| O2 | Open/closed | No `additionalProperties: false` on response schemas. |
| O3 | Open/closed | Growable response enums documented as open. |
| O4 | Open/closed | Deprecated operations carry `deprecated: true`, successor, sunset date, and `Deprecation`/`Sunset`/`Link` headers. |
| D1 | Dependency inv. | Related/sub-resources reachable via `...Link` (`format: uri`) properties. |
| D2 | Dependency inv. | Chained operations have OpenAPI `links`. |
| D3 | Dependency inv. | Schemas carry `examples` usable for contract tests. |
| S1 | Single resp. | Spec covers exactly one Bounded Context / capability. |
| S2 | Single resp. | No complete process split across specs. |
| S3 | Single resp. | `info.description`, tags, operations and properties are in the Ubiquitous Language. |

O2 and O4 (`deprecated` without a description) are also linted by the ruleset;
L1 is run with oasdiff; the rest is judgment.

## 9. Reporting (step 7)

Add a short **Quality** section to the summary: one line per principle with its
ISO/IEC 25010 characteristics, "met" / "open" and — for open items — what is
missing or what the user must decide (e.g. "S1 open: spec mixes catalog and
lending; proposed split"). Name qualities out of scope (performance efficiency,
reliability, security) only if the user asked for them.

---

## 10. Sources

BibTeX keys from the author's `references.bib`. Keys marked † were cited in the
source chapter but are **missing** from the supplied `.bib` and must be added.

- Compatibility & Liskov: liskov1994behavioral, seemann2021backwards †
  (the `.bib` only has seemann2021code, the book, whose ch. 11 compares
  version ordering with LSP subtyping — either cite that key or add the
  source of the pre/postcondition quote), zalando_guidelines, fowler2011tolerant, oasdiff, buf_breaking
- Interface segregation: martin1996isp, newman2015bff, merson2020ideals,
  evans2015reference, evans2003ddd, vernon2013implementing, junker2025crafting,
  stocker2024refactoring
- Open/closed: meyer1988oosc, github_api_versions, junker2025crafting_versioning
  (chapter title and pages still TODO), rfc9745 †, rfc8594 †,
  daigneau2011sdp †, zimmermann2023patterns †
- Dependency inversion: martin1996dip, fielding2008hypertext,
  richardson2008maturity, fowler2010rmm, boeckeler2025sdd, robinson2006cdc,
  pact_docs, specmatic
- Single responsibility: martin2014srp, martin2017clean
- Quality model: iso25010_2023, junker2025crafting
- Framing: xiao2021crud, rfc9110, rfc5789, fielding2009post,
  martin2000principles, kolb2026solid †
