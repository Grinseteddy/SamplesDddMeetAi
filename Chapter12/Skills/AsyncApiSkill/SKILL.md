---
name: asyncapi-spec-author
description: >-
  Author AsyncAPI 3.1.0 event-driven API specifications in YAML following a
  consistent, componentized house style (the same style as the bundled Task
  Management example). Use this skill whenever the user wants to create, write,
  draft, design, scaffold, or extend an AsyncAPI specification, model an
  event-driven / message-driven / pub-sub / streaming API, describe Kafka, MQTT,
  AMQP, WebSocket, or other broker topics, channels, messages, or events, or
  produce an asyncapi.yaml file — even if they don't say "AsyncAPI 3.1.0"
  explicitly. Also use it when adding channels, operations, messages, or schemas
  to an existing spec so the additions match the house conventions, and when the
  user asks to validate or lint an AsyncAPI document. Produces a single YAML file
  that validates against the AsyncAPI 3.1.0 spec and passes Spectral linting.
---

# AsyncAPI 3.1.0 Spec Author

This skill produces AsyncAPI **3.1.0** specifications in **YAML** that follow a
consistent, heavily componentized house style and pass linting. The bundled
`assets/example-task-management.yaml` is the gold-standard reference for what
"good" looks like — when in doubt about structure or formatting, open it and
mirror it.

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
  host. Default content type is `application/json`.
- **Security** for the broker connection (default: `userPassword`).

If the user already gave a message list, a state machine, an event-storming
result, or an existing spec, extract the channels and messages from that instead
of interrogating them. Ask only what you genuinely can't infer.

### 2. Read the conventions

Before writing, read `references/conventions.md` — it is the authoritative,
detailed specification of the house style (naming, the channel/operation/message
split, the shared message-header pattern, payload schema design, security). The
summary in this file is a reminder, not a substitute.

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

The single most important convention: **componentize and reuse via `$ref`.**
Messages live under `components.messages`; their headers and payloads live under
`components.schemas`; channels reference messages; operations reference channels
and messages — exactly as in the example. Inline as little as possible.

### 4. Validate and lint

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

### 5. Present

Save the final `.yaml` to the outputs directory and present it. Briefly note the
channels, messages, and operations covered and the result of the validation run.

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

**security** — define the broker auth scheme once in
`components.securitySchemes` (default `userPassword`) and reference it from each
server's `security` list.

## Bundled resources

- `references/conventions.md` — full house-style specification. Read before writing.
- `references/validation.md` — how to install and run the AsyncAPI CLI and Spectral (and fallbacks).
- `assets/example-task-management.yaml` — gold-standard example to mirror.
- `assets/skeleton.yaml` — minimal starting template with the right structure.
- `assets/house-style.spectral.yaml` — Spectral ruleset encoding the conventions.
