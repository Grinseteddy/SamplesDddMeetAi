---
name: "asyncapi-spec-author"
description: >-
  Author AsyncAPI 3.1.0 event-driven API specs in YAML in a componentized house
  style (mirrors the bundled Task Management example). Use to create, draft,
  design, extend, validate or lint an AsyncAPI spec or asyncapi.yaml, or to
  model event-driven, pub-sub or streaming APIs — Kafka, MQTT, AMQP, WebSocket
  topics, channels, messages, events — even without saying "AsyncAPI". Applies
  SOLID-based quality requirements (ISO/IEC 25010): backward compatibility,
  additive evolution with versioned, deprecated channels, one bounded context
  per spec — also when evolving an existing spec. Output validates and passes
  Spectral linting and a quality review.
author: Annegret Junker
---

# AsyncAPI 3.1.0 Spec Author

This skill produces AsyncAPI **3.1.0** specifications in **YAML** that follow a
consistent, heavily componentized house style and pass linting. Two bundled
gold-standard examples show what "good" looks like — when in doubt about
structure or formatting, open the one for your protocol and mirror it:

- `assets/example-task-management.yaml` — **Kafka** (`kafka` bindings:
  schema registry on the server, partitions/replicas/retention on the
  channel, `clientId`/`groupId` on operations, partition `key` on messages).
- `assets/example-task-management-amqp.yaml` — **RabbitMQ / AMQP 0-9-1**
  (`amqp` bindings: exchange + routing-key channel for publishing, queue
  channel for consuming, delivery mode / ack on operations, `messageType`
  on messages).

The conventions below are **strong defaults**: apply them unless the user's
domain clearly calls for something different (a different protocol, a different
security scheme, a request-reply pattern instead of fire-and-forget events).
Adapt thoughtfully rather than forcing every rule, but keep the spec internally
consistent.

## The 3.1.0 mental model (read this first)

AsyncAPI 3.1.0 is **not** OpenAPI and is **not** AsyncAPI 2.x. Three root
sections do the work, and keeping them decoupled is the whole point of the
version:

- **`channels`** — *where* messages flow (a Kafka topic, an MQTT topic, a queue,
  a WebSocket route). A channel declares its `address` and the set of `messages`
  that can appear on it. Channels are reusable and say nothing about direction.
- **`operations`** — *what the application does* with a channel: `action: send`
  or `action: receive`. Operations live at the **root**, not nested under
  channels, and point at a channel via `$ref`.
- **`components.messages`** — the message definitions (`headers` + `payload`),
  referenced by channels.

**`action` is from the perspective of the application described by this
document**, and this is the most common mistake: `send` means *this application
sends/publishes the message to the channel*; `receive` means *this application
consumes/subscribes to the message from the channel*. (This replaces the
confusing 2.x `publish`/`subscribe`, where the verb described the consumer, not
the application.) If the document describes a producer service, its operations
are `send`; for a consumer service, `receive`.

## Workflow

Follow these steps in order. Don't skip validation.

### 1. Understand the event API

Establish, from the conversation or by asking briefly:
- The **events / messages** the system emits or consumes, and the **lifecycle**
  they represent (e.g. `Created → Assigned → Processed → Completed`).
- The **channels** (topics/queues) those messages travel on, and which messages
  share a channel.
- **Direction** for the application being described — does it `send` (produce) or
  `receive` (consume) each message? This decides the `action` on each operation.
- The **protocol** and broker (Kafka, MQTT, AMQP, WebSocket, …) and the server
  host. Default content type is `application/json`. Also establish the
  **broker-level facts that go into `bindings`** (`references/conventions.md`
  §12): for Kafka the partition count, replication, retention, consumer group
  and partition key; for RabbitMQ the exchange name and type, the routing
  keys, and the queue names this application consumes from.
- **Security** for the broker connection (default: `userPassword`).
- **Qualities**: which qualities matter is a business decision. If the
  conversation, ADRs or architecture principles prioritize any, use them;
  otherwise apply the defaults in `references/quality-requirements.md` and say
  so. Don't quiz the user about ISO characteristics.
- **New or evolving?** If an earlier version of the spec exists, this is an
  evolution: the Liskov and Open/closed rules become mandatory — nothing a
  consumer relies on may break; a breaking change ships as a new versioned
  channel running alongside the deprecated one.

If the user already gave a message list, a state machine, an event-storming
result, or an existing spec, extract the channels and messages from that instead
of interrogating them. Ask only what you genuinely can't infer.

### 2. Read the conventions and quality requirements

Before writing, read `references/conventions.md` — it is the authoritative,
detailed specification of the house style (naming, the channel/operation/message
split, the shared message-header pattern, payload schema design, security). The
summary in this file is a reminder, not a substitute.

Then read `references/quality-requirements.md` — the SOLID principles carried
over to event-driven APIs, each mapped to ISO/IEC 25010 qualities and turned
into rules for channels, operations and messages. They shape design decisions
(which events this context publishes, how it evolves), so read them before
writing, not only at review.

### 3. Write the spec

Start from `assets/skeleton.yaml` and build outward. Write the file in this order
so structure stays clean:

1. `asyncapi: 3.1.0` + `info` (title, version, description, `contact`).
2. `servers` (host, protocol, description, `security` → a scheme `$ref`).
3. `defaultContentType: application/json`.
4. `channels` — each with an `address` and a `messages` map of `$ref`s to
   `components.messages`.
5. `operations` — one per message flow, each with `action` (`send`/`receive`), a
   `channel` `$ref`, and a `messages` list of `$ref`s.
6. `components` — `messages`, `schemas` (a shared `MessageHeader` plus one
   payload schema per message), `securitySchemes`.
7. `bindings.<protocol>` on the server (Kafka only), every channel, every
   operation and every message, pinned to the binding version (`kafka`
   `"0.5.0"`, `amqp` `"0.3.0"`). Kafka: `partitions`/`replicas`/
   `topicConfiguration`, `clientId` or `groupId`, message `key`. AMQP:
   `is: routingKey` + `exchange` or `is: queue` + `queue`, `deliveryMode`/
   `mandatory` for `send` and `ack` for `receive`, `messageType`; a message
   this application both publishes and consumes gets two channels.

The single most important convention: **componentize and reuse via `$ref`.**
Messages live under `components.messages`; their headers and payloads live under
`components.schemas`; channels reference messages; operations reference channels
and messages — exactly as in the example. Inline as little as possible.

### 4. Review against the quality requirements

Walk the spec through the checklist in `references/quality-requirements.md` §9
(Liskov, Open/closed, Interface segregation, Dependency inversion, Single
responsibility). Fix what you can; keep a list of what stays open. When a
previous version exists, run both compatibility checks from
`references/validation.md` — `asyncapi diff` for channels/operations and
`scripts/payload_compat.py` for payloads and headers (direction-aware) — and
compare changed descriptions by hand: tools see structure, not meaning.

### 5. Validate and lint

Always validate the finished spec. Read `references/validation.md` for the exact
commands. Two complementary tools:

```bash
# Spec conformance (AsyncAPI 3.1.0) — the official parser:
npm install -g @asyncapi/cli
asyncapi validate <spec>.yaml

# House-style governance — Spectral with the bundled ruleset:
npm install -g @stoplight/spectral-cli
spectral lint <spec>.yaml --ruleset assets/house-style.spectral.yaml
```

Fix every **error** and review **warnings** (resolve them unless there's a
deliberate reason). Re-run until clean. If neither tool can be installed, fall
back to the Python structural check described in `references/validation.md`.

### 6. Present

Save the final `.yaml` to the outputs directory and present it. Briefly note the
channels, messages, and operations covered and the result of the validation run,
plus a short **Quality** section: one line per SOLID principle with its
ISO/IEC 25010 characteristics, met or open, and what the user must decide for
open items (`references/quality-requirements.md` §10).

## House style at a glance

A compressed reminder — `references/conventions.md` has the full detail.

**Naming**
- Channel keys, operation keys, message component keys: `camelCase`
  (`taskEvents`, `publishTaskCreated`, `taskCreated`).
- Operation keys are verb + noun: a `send` operation reads as `publish…`, a
  `receive` operation as `consume…`/`on…`.
- Channel `address`: broker-style path, `kebab-case` segments
  (`task-management/tasks`).
- Schema component keys: `PascalCase`, payloads suffixed `Payload`
  (`TaskCreatedPayload`); the shared header schema is `MessageHeader`.
- Message `name`: `PascalCase` (`TaskCreated`); `title`: human-readable
  ("Task Created Event"). Schema properties: `camelCase`.

**info** — `title`, semver `version`, a `description` (`|` block: what the API
does and who uses it), and a `contact` (`name`, `url`, `email`).

**channels** — each declares an `address` and a `messages` map whose keys mirror
the message component keys, each value a `$ref` into `components.messages`. Group
related lifecycle events on one channel when they share a topic.

**operations** — root-level, one per message flow. Each has `action`
(`send` | `receive`), a `channel` `$ref`, and a `messages` list of `$ref`s to the
specific messages it acts on. **Reference those messages _through the channel_**
(`#/channels/<key>/messages/<msg>`), not directly via `#/components/messages/...`
— the latter fails `asyncapi validate`. Add a per-operation `security` block only
when it narrows the server default.

**messages** — each has `name`, `title`, `summary`, `headers` (`$ref` to the
shared `MessageHeader`), and `payload` (`$ref` to its payload schema).

**schemas** — a shared `MessageHeader` (`correlationId`, `messageId`, `source`)
reused by every message, plus one `…Payload` schema per message. Every object
schema is `type: object` with `required` + `properties`. Add constraints
liberally: `format` (`uuid`, `date-time`, `uri`, `email`), `minLength`/
`maxLength`, `minimum`/`maximum`, and `enum` for status fields. Model a state
machine by giving each event's `status` a single-value `enum`
(`NEW`, `ASSIGNED`, `IN_PROGRESS`, `DONE`).

**descriptions & examples** — every payload/header property has a
`description` saying what the value means; every operation a `description` of
when it fires; every message `examples` (headers + payload).

**security** — define the broker auth scheme once in
`components.securitySchemes` (default `userPassword`) and reference it from each
server's `security` list.

**bindings** — every document carries them for its protocol; a spec without
bindings describes message shapes but not a working integration.

**Kafka** — server `bindings.kafka.schemaRegistryUrl`; channel `partitions`,
`replicas`, `topicConfiguration` (`cleanup.policy`, `retention.ms`); `send`
ops `clientId`, `receive` ops `groupId`; messages a partition `key` (the
aggregate id) and `schemaIdLocation: header`.

**AMQP / RabbitMQ** — `protocol: amqp`/`amqps`, `protocolVersion: "0.9.1"`;
publisher channels are routing keys on an exchange (`is: routingKey`,
dotted address like `task.{eventType}`), consumer channels are queues
(`is: queue`); persist state-changing events (`deliveryMode: 2`), ack
manually (`ack: true`); no server binding — `vhost` sits on the exchange/queue.
Full detail in `references/conventions.md` §12.

**Quality (SOLID → ISO/IEC 25010)** — full rules in
`references/quality-requirements.md`:
- *Liskov* (compatibility, replaceability): direction decides — for `send`
  messages never remove/narrow properties, change type or meaning, rename
  channels/messages or change the partition key; for `receive` messages never
  add required properties or tighten constraints. Retained topics are replayed,
  so compatibility runs both ways.
- *Open/closed* (modifiability, co-existence): additive only within a version;
  a new business requirement is a new event, not a new version; a breaking
  change is a new `….v2` channel with dual publishing, old parts marked
  `x-deprecated` + `x-sunset` with the successor named, and
  `deprecation`/`sunset` in `MessageHeader`; `info.description` asks consumers
  to ignore unknown message types, properties and enum values; no
  `additionalProperties: false`.
- *Interface segregation* (functional appropriateness, user error protection):
  one shared event set for all consumers — no consumer-specific channels or
  fields, no generic "Changed" events; constrained payloads.
- *Dependency inversion* (adaptability, testability): consumers depend on the
  spec (addresses and broker details live in it); notification events carry
  `…Link` URIs; descriptions and examples seed contract tests.
- *Single responsibility* (modularity, reusability, self-descriptiveness): one
  Bounded Context per spec, publishing only its own events; keep a lifecycle
  together.

## Bundled resources

- `references/conventions.md` — full house-style specification. Read before writing.
- `references/quality-requirements.md` — SOLID principles applied to
  event-driven APIs, mapped to ISO/IEC 25010, with rules, the versioned-channel
  and deprecation pattern, the review checklist and sources. Read before writing.
- `references/validation.md` — how to install and run the AsyncAPI CLI and
  Spectral (and fallbacks), plus the compatibility checks for evolving specs.
- `scripts/payload_compat.py` — direction-aware payload/header compatibility
  check between two spec versions (fills the gap `asyncapi diff` leaves).
- `assets/example-task-management.yaml` — gold-standard Kafka example (bindings) to mirror.
- `assets/example-task-management-amqp.yaml` — gold-standard RabbitMQ/AMQP example (bindings) to mirror.
- `assets/skeleton.yaml` — minimal starting template with the right structure (AMQP binding blocks commented in).
- `assets/house-style.spectral.yaml` — Spectral ruleset encoding the conventions, including the Kafka and AMQP binding rules.
