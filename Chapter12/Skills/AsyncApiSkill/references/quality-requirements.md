# Quality Requirements: SOLID for Event-Driven APIs

CRUD-style thinking gets events flowing; it does not keep an event API working
for years. APIs that must be maintained and evolved need further qualities —
above all, the ability to extend the API without breaking its consumers
(xiao2021crud, zalando_guidelines, junker2025crafting). This file carries the
SOLID principles, originally formulated for object-oriented design
(martin2000principles), over to API design (kolb2026solid, merson2020ideals),
maps each to the ISO/IEC 25010 qualities it serves, and translates each into
concrete rules for **channels, operations and messages** in an AsyncAPI 3.1.0
document.

Read this file **before writing** (step 2) and again for the **quality review**
(step 4). When the task is to **change an existing spec**, §4 (Liskov) and §5
(Open/closed) are mandatory.

## Contents
1. Which qualities matter — a business decision
2. Quality map (principle → ISO/IEC 25010)
3. Why events differ from endpoints
4. Liskov substitution — backward compatibility over time
5. Open/closed — additive evolution, versioned breaking changes
6. Interface segregation — segregate by capability, not by consumer
7. Dependency inversion — the spec as the shared abstraction
8. Single responsibility — one Bounded Context per API
9. Quality review checklist (step 4)
10. Reporting (step 6)
11. Sources

---

## 1. Which qualities matter — a business decision

Which qualities matter, and to what degree, is a **business decision**, not the
spec author's. ISO/IEC 25010:2023 supplies the shared vocabulary: nine product
quality characteristics with 40 sub-characteristics, from functional suitability
to safety (iso25010_2023). Junker and Lazzaretti discuss each from an API
perspective (junker2025crafting).

This skill covers only the qualities **the shape of the event API decides**.
Performance efficiency, reliability and security — throughput, partitions,
replication, delivery guarantees, broker auth — depend more on implementation
and operation; capture the facts the user gives in `bindings` and `servers`,
but do not claim the spec delivers those qualities.

If the conversation, ADRs or architecture principles name prioritized
qualities, use them. Otherwise apply all five principles at default strength
and say so in the summary — do not quiz the user about ISO characteristics.

## 2. Quality map

| Principle | ISO/IEC 25010:2023 characteristic (sub-characteristics) |
|---|---|
| Liskov substitution | Compatibility (co-existence, interoperability); Flexibility (replaceability) |
| Interface segregation | Functional suitability (functional appropriateness); Interaction capability (user error protection) |
| Open/closed | Maintainability (modifiability); Compatibility (co-existence) |
| Dependency inversion | Flexibility (adaptability); Maintainability (testability) |
| Single responsibility | Maintainability (modularity, reusability); Interaction capability (appropriateness recognizability, self-descriptiveness) |

These are architecture principles too; when the user keeps an architecture or
domain knowledge graph, they can be recorded there (`adr-and-principles-ingester`,
`domain-knowledge-graph`).

## 3. Why events differ from endpoints

Three differences shape every rule below:

1. **Direction decides who the "client" is.** For a `send` operation, the
   message payload is this application's **postcondition**: consumers depend on
   it. For a `receive` operation, the payload is this application's
   **precondition**: producers must satisfy it. Apply the Liskov rules with the
   right side in mind (§4).
2. **Messages outlive versions.** A log-based broker (Kafka) retains old
   messages; consumers replay them, and new consumers start from the beginning.
   A consumer of a new version must therefore still read messages written under
   the old one — compatibility runs **both ways** across the retention window.
3. **There is no response to carry a warning.** HTTP has `Deprecation` and
   `Sunset` response headers (rfc9745, rfc8594); a broker has only the message
   itself. Deprecation has to travel in message headers and in the spec (§5).

---

## 4. Liskov substitution — backward compatibility over time

**Principle.** Backward compatibility is the Liskov substitution principle
applied over time: a new version of an API must be usable wherever the previous
version was used (liskov1994behavioral, seemann2021backwards). Consumers that
already use an API must not fail when a new version is deployed
(zalando_guidelines). To stay compatible you may **weaken preconditions or
strengthen postconditions** (seemann2021backwards).

**Rules for messages this application sends** (postconditions — consumers
depend on them). A new revision must not:
- remove a payload or header property, or make a `required` one optional;
- change a property's **type**, `format` or **meaning**;
- rename a message `name`, channel key or channel `address`, or move a message
  to a different channel;
- add a value to a payload `enum` that consumers were told was closed;
- change the **partition / routing key** or ordering semantics (consumers rely
  on per-key ordering even though no schema says so);
- stop publishing a message type that is still documented on the channel.

**Rules for messages this application receives** (preconditions — producers
depend on them). A new revision must not:
- add a **required** payload or header property;
- tighten constraints (`minLength`/`maxLength`, `pattern`, `minimum`/`maximum`,
  `format`, removing an `enum` value) — that rejects messages it used to accept.

**Allowed:** new optional properties on either side, relaxed constraints on
received messages, new messages and new channels. Adding properties to sent
messages is safe **only if consumers are tolerant readers** that ignore what
they do not know (fowler2011tolerant) — see §5.

**Checks.**
- *Syntactic:* compare the old and new spec — `asyncapi diff` for channels
  and operations, `scripts/payload_compat.py` for payloads and headers (see
  `references/validation.md`). For payloads stored in a schema registry, set the
  subject's compatibility mode so the broker rejects incompatible schemas —
  `FULL_TRANSITIVE` (or at least `BACKWARD_TRANSITIVE`) for retained topics,
  because of replay (§3). Buf does the same for Protocol Buffers payloads
  (buf_breaking); oasdiff is the OpenAPI counterpart (oasdiff).
- *Semantic:* tooling only detects syntactic changes. A semantic change that
  leaves the structure untouched — a field that used to carry the customer
  number and now carries the customer ID — passes every check
  (zalando_guidelines, junker2025crafting). So every payload property carries a
  `description` (§7), and **every changed description is compared with the
  previous version by hand**. A changed meaning is a breaking change even if
  the diff is silent.

## 5. Open/closed — additive evolution, versioned breaking changes

**Principle.** Open for extension, closed for modification (meyer1988oosc).
GitHub's published REST versioning policy is a good template
(github_api_versions); the same rules carry over to events.

**Within a version, changes are additive only:** new messages on an existing
channel, new channels, new optional payload or header properties. Existing
properties keep their meaning and type; no required properties are added to
received messages (github_api_versions, zalando_guidelines,
junker2025crafting_versioning).

**Before introducing a new version**, check whether the change reflects a **new
business requirement**; if so, a **new event** (a new message, often on the
same channel) is the better answer than a new version of an old one
(junker2025crafting_versioning, zalando_guidelines). `TaskEscalated` beats
`TaskAssigned v2` with an `escalated` flag. Ask the user when unclear.

**When a breaking change is unavoidable**, ship it as a **new major version that
runs alongside the old one**, so consumers can migrate without a big bang, with
a migration guide and an announced deprecation timeline
(junker2025crafting_versioning, github_api_versions). These are the
provider-side evolution patterns Version Identifier, Two in Production and
Limited Lifetime Guarantee (zimmermann2023patterns). In this house style:

1. **New channel, new major.** Add a channel whose `address` carries the major
   version (`task-management/tasks.v2`; keep the old one), its own messages
   (`taskCreatedV2` → `name: TaskCreated`, payload `TaskCreatedV2Payload`) and
   its own `publish…V2` operations. Bump `info.version` to the next **MAJOR**.
   The producer **dual-publishes** to both channels until the sunset date.
2. **Mark the old parts deprecated.** AsyncAPI 3.1.0 has no `deprecated` field
   on channels, operations or messages, so use the extensions `x-deprecated:
   true` and `x-sunset: <ISO date>` on the old channel, its operations and its
   messages; the `description` names the successor channel/message, the sunset
   date and a link to the migration guide (the ruleset enforces this). On
   payload properties, JSON Schema's own `deprecated: true` works.
3. **Announce at runtime in the message.** Brokers have no response headers, so
   carry the HTTP semantics of `Deprecation` (rfc9745) and `Sunset` (rfc8594)
   as optional properties of the shared `MessageHeader`, set by the producer on
   every message of a deprecated channel:

```yaml
    MessageHeader:
      # … correlationId, messageId, source as before …
      properties:
        deprecation:
          description: |
            Set on messages of a deprecated channel: the date from which the
            channel is deprecated, following the semantics of the HTTP
            Deprecation header (RFC 9745).
          type: string
          format: date-time
        sunset:
          description: |
            Set on messages of a deprecated channel: the date after which the
            producer stops publishing here, following the semantics of the
            HTTP Sunset header (RFC 8594).
          type: string
          format: date-time
        successor:
          description: Address of the channel that replaces this one.
          type: string
```

   Add these properties only when the document actually deprecates something,
   or when the user wants the header ready for future use.

**The consumer side has to do its part.** Additive evolution only works if
consumers are **tolerant readers**: they ignore unknown properties and do not
treat enumerations as closed (fowler2011tolerant, daigneau2011sdp,
junker2025crafting_versioning); Zimmermann et al. name Tolerant Reader as the
client-side complement (zimmermann2023patterns). For events this has one more
part: **a consumer ignores message types it does not know** on a channel it
reads — otherwise adding a new event to a channel breaks it. Make this explicit:

- `info.description` ends with: *"The API evolves additively within a major
  version: consumers must ignore unknown message types, unknown properties and
  unknown enumeration values."* (the ruleset warns if missing).
- No `additionalProperties: false` on payload or header schemas (the ruleset
  warns).
- The house style's single-value `status` enum per lifecycle event is fine — it
  pins the state *that event* reports. For a status-like property that is
  expected to grow (categories, reasons), say so in its `description` ("New
  values may be added; treat unknown values as …").

## 6. Interface segregation — segregate by capability, not by consumer

**Principle.** No client should be forced to depend on methods it does not use
(martin1996isp). Taken literally, this yields one API per consumer, as in
Backends for Frontends (newman2015bff, merson2020ideals). For externalized
business capabilities that makes little sense: they serve many consumers. Evans
recommends opening **one shared protocol** to all who integrate and handling a
single consumer's idiosyncratic needs with a **separate translator**
(evans2015reference). Segregate along business capabilities, ideally one API per
Bounded Context (evans2003ddd, vernon2013implementing); such APIs stay lean and
avoid "world-dominating" APIs (junker2025crafting).

**Rules.**
- Publish **one set of domain events** for all consumers. Do not add
  consumer-specific channels (`tasks-for-billing`), messages or payload fields
  (`billingCostCenter`). A consumer that needs another shape builds it on its
  side (an anticorruption layer or a derived stream outside this spec); note
  the need in the summary.
- Name channels, messages and properties in the Bounded Context's Ubiquitous
  Language — not in a consumer's process vocabulary.
- *Functional appropriateness:* one message per business fact. Avoid generic
  "god events" (`TaskChanged` with a `changeType` field, `EntityUpdated` with a
  diff) that force every consumer to parse every change
  (stocker2024refactoring); prefer the specific past-tense events from the
  lifecycle. Conversely, keep events that consumers always need together on one
  channel so they don't have to join streams.
- *User error protection:* constrain payloads (`required`, `enum`, `pattern`,
  lengths, `format`, `minimum`/`maximum`) so an invalid message is rejected at
  the contract (schema-registry validation, consumer validation) rather than
  deep in a consumer — especially for `receive` operations.

## 7. Dependency inversion — the spec as the shared abstraction

**Principle.** For interfaces, dependency inversion means clients rely on what
they **discover** rather than on what they construct themselves
(martin1996dip, fielding2008hypertext). Providers can then restructure without
breaking clients; only the entry points must remain stable
(richardson2008maturity, fowler2010rmm). Hypermedia decouples clients from
addresses but **not from the domain model** behind them (zalando_guidelines).
The shared abstraction is therefore the **API description itself**: the
AsyncAPI specification states not only where a message flows but what it means.
Interface definitions belong to the specification rather than to the code
(boeckeler2025sdd); in this approach the specification is also part of the
domain knowledge graph.

**Rules.**
- **Consumers depend on the spec, not on broker internals.** Channel addresses,
  hosts and binding details come from the spec (generate clients/config from
  it); keep them in `servers`, `channels` and `bindings`, never only in
  descriptions. Channel keys and the documented channels are the stable entry
  points; use channel `parameters` / server `variables` for parts that vary by
  environment.
- **Point, don't copy, where state lives elsewhere.** An event either carries
  the state consumers need (event-carried state transfer) or notifies and points
  to it (event notification) (fowler2017eventdriven). When it notifies, include
  a **link** — a `format: uri` property ending in `Link` (`taskLink`) to the
  resource in the owning context's REST API — in addition to the id, so
  consumers follow a link instead of building URLs.
- **Self-descriptive payloads.** Every payload and header property has a
  `description` (one domain-language sentence on what the value means — not a
  restatement of its name); reusable schemas get a schema-level `description`.
  This is what makes semantic changes visible (§4).
- **Examples.** Every message carries `examples` (headers + payload), so
  consumers can state their expectations as concrete examples.

**Testability.** Whether producers and consumers still agree on the spec is
checked with contract tests (robinson2006cdc): Pact message pacts (pact_docs)
or Specmatic, which runs contract tests straight from an AsyncAPI document
(specmatic). Schema-based tests catch structural changes; a change in
**meaning** is caught only when consumers state their expectations as concrete
examples. In the summary, recommend contract tests and point to the message
`examples` as their seed.

## 8. Single responsibility — one Bounded Context per API

**Principle.** Single responsibility is about people: a module should have only
one reason to change, and that reason is a single, narrowly defined business
function (martin2014srp, martin2017clean). For an API: it exposes **one
business capability — or, better, one Bounded Context with its Ubiquitous
Language** (merson2020ideals, evans2003ddd). APIs spanning several capabilities
mix models whose terms mean different things in different parts of the
organization; they are hard to understand and their meaning drifts as each
group pulls them its own way (evans2015reference, stocker2024refactoring). A
single API for an entire application is already a smell (zalando_guidelines,
junker2025crafting). Splitting should **stop where a business process would be
torn apart**: one API still covers a complete process within its Bounded
Context (zalando_guidelines).

**Rules.**
- One document = the events **one Bounded Context owns**. It `send`s only
  events about its own aggregates; events it consumes from other contexts
  appear as `receive` operations referencing *their* messages, not as messages
  it re-publishes under its own name.
- Channel addresses start with the owning context (`task-management/…`).
  A shared "enterprise event bus" spec covering every context is the event
  version of the single-API-for-everything smell — propose one spec per
  context.
- Smell signals: one term needs two payload shapes (a `Task` for planning, a
  `Task` for billing); channels whose events are owned by different teams;
  `info.description` needs "and" to join unrelated purposes. Report them.
- Keep a complete lifecycle together: all events of one aggregate's process
  (`Created → Assigned → Processed → Completed`) stay in one spec, even if a
  split would make each file smaller.
- *Appropriateness recognizability / self-descriptiveness:* `info.description`
  states the one capability and its consumers; every message has `summary` and
  `description` in the Ubiquitous Language.

---

## 9. Quality review checklist (step 4)

Walk the finished spec through this list. Fix what you can; report the rest.

| # | Principle | Check |
|---|---|---|
| L1 | Liskov | Previous version exists? → `asyncapi diff` and `payload_compat.py` show no breaking change (or it ships as a new major channel, §5); registry compatibility mode noted. |
| L2 | Liskov | Every changed property `description` keeps its meaning; partition/routing key unchanged. |
| L3 | Liskov | `send` messages: nothing removed or narrowed. `receive` messages: nothing newly required or tightened. |
| O1 | Open/closed | `info.description` states the tolerant-reader expectation, including unknown message types. |
| O2 | Open/closed | No `additionalProperties: false` on payload or header schemas. |
| O3 | Open/closed | New business requirement modeled as a new event, not a new version. |
| O4 | Open/closed | Deprecated channels/operations/messages carry `x-deprecated`, `x-sunset`, successor in `description`; successor channel exists; `MessageHeader` carries `deprecation`/`sunset`. |
| I1 | Interface seg. | No consumer-specific channels, messages or fields. |
| I2 | Interface seg. | No generic "changed" events with a type switch. |
| I3 | Interface seg. | Payloads constrained, especially on `receive`. |
| D1 | Dependency inv. | Addresses and broker details live in `servers`/`channels`/`bindings`. |
| D2 | Dependency inv. | Notification events carry `...Link` URIs to the owning resource. |
| D3 | Dependency inv. | Every payload/header property has a `description`; every message has `examples`. |
| S1 | Single resp. | Spec covers exactly one Bounded Context; it publishes only its own events. |
| S2 | Single resp. | No lifecycle split across specs. |
| S3 | Single resp. | `info.description`, messages and properties are in the Ubiquitous Language. |

O1, O2, O4 and D3 are linted by the ruleset; L1 is run with `asyncapi diff` and `scripts/payload_compat.py`;
the rest is judgment.

## 10. Reporting (step 6)

Add a short **Quality** section to the summary: one line per principle with its
ISO/IEC 25010 characteristics, "met" / "open", and for open items what is
missing or what the user must decide (e.g. "S1 open: spec publishes billing
events; propose moving them to the Billing context"). Mention performance,
reliability and security only if the user asked for them.

---

## 11. Sources

BibTeX keys from the author's `references.bib`. Keys marked † are cited in the
source chapter but **missing** from the supplied `.bib`.

- Framing: xiao2021crud, martin2000principles, kolb2026solid †,
  merson2020ideals, asyncapi2023spec
- Quality model: iso25010_2023, junker2025crafting
- Liskov: liskov1994behavioral, seemann2021backwards † (the `.bib` has only
  seemann2021code), zalando_guidelines, fowler2011tolerant, oasdiff,
  buf_breaking
- Open/closed: meyer1988oosc, github_api_versions,
  junker2025crafting_versioning, rfc9745 †, rfc8594 †, daigneau2011sdp †,
  zimmermann2023patterns †
- Interface segregation: martin1996isp, newman2015bff, evans2015reference,
  evans2003ddd, vernon2013implementing, stocker2024refactoring
- Dependency inversion: martin1996dip, fielding2008hypertext,
  richardson2008maturity, fowler2010rmm, fowler2017eventdriven,
  boeckeler2025sdd, robinson2006cdc, pact_docs, specmatic
- Single responsibility: martin2014srp, martin2017clean

Adaptations for events (§3, the message-header form of Deprecation/Sunset,
dual publishing, unknown message types, schema-registry compatibility modes)
are this skill's transfer of the chapter's REST argument, not claims of the
cited sources.
