# House-Style Conventions (full reference)

The authoritative description of the AsyncAPI 3.1.0 house style. The bundled
`assets/example-task-management.yaml` demonstrates every rule here; treat it as
the canonical example and mirror its structure and formatting.

## Contents
1. Document & info block
2. Servers
3. defaultContentType
4. Channels
5. Operations (and the `action` direction rule)
6. Messages
7. Schemas (the shared header + payload pattern)
8. Security schemes
9. Componentization (the core principle)
10. Naming cheat sheet
11. Version notes: 3.1.0, and 3.x vs 2.x

---

## 1. Document & info block

- The first line is always `asyncapi: 3.1.0`.
- The `info` block contains:
  - `title` — human-readable API name.
  - `version` — semantic version (`MAJOR.MINOR.PATCH`), e.g. `1.0.0`.
  - `description` — one or more sentences: what the API does **and** who uses it.
    Use a `|` block scalar when it spans lines.
  - `contact` — `name`, and where useful `url` and `email` (the team that owns
    the API and where to get help).

```yaml
asyncapi: 3.1.0
info:
  title: Task Management System
  version: 1.0.0
  description: |
    Async API specification for Task Management System that handles task lifecycle
    between Creators, Freelancers, and Assignees.
  contact:
    name: Task Management Team
    url: https://taskmanagement.org
    email: team@taskmanagement.org
```

A `license`, top-level `tags`, and `externalDocs` may be added when useful, but
are not required by the house style.

---

## 2. Servers

`servers` is a **map** (keyed by a logical name like `production`). Each server
declares:

- `host` — broker host and port (`events.taskmanagement.com:9092`). Do **not**
  prefix with a protocol scheme; the protocol lives in its own field.
- `protocol` — the transport: `kafka`, `mqtt`, `amqp`, `ws`, `wss`, `http`,
  `nats`, `redis`, etc.
- `description` — short human-readable note.
- `security` — a list of `$ref`s into `components.securitySchemes` (see §8).

```yaml
servers:
  production:
    host: "events.taskmanagement.com:9092"
    protocol: "kafka"
    description: "Production broker"
    security:
      - $ref: '#/components/securitySchemes/user-password'
```

Add more entries (`staging`, `development`) the same way when the domain needs
them. `protocolVersion`, `pathname`, and `bindings` are available when a protocol
requires them.

---

## 3. defaultContentType

Declare the default media type for message payloads once at the root:

```yaml
defaultContentType: application/json
```

Individual messages may override it with their own `contentType`, but the house
default is `application/json`.

---

## 4. Channels

`channels` is a **map** keyed in **camelCase** (`taskEvents`). A channel
describes *where* messages flow and is direction-agnostic. Each channel declares:

- `address` — the broker-level address (topic / queue / routing key). Use a
  broker-style path with **kebab-case** segments: `task-management/tasks`.
- `messages` — a map whose **keys mirror the message component keys** and whose
  values are `$ref`s into `components.messages`. A channel lists every message
  that can appear on it.

```yaml
channels:
  taskEvents:
    address: "task-management/tasks"
    messages:
      taskCreated:
        $ref: '#/components/messages/taskCreated'
      taskAssigned:
        $ref: '#/components/messages/taskAssigned'
      taskProcessed:
        $ref: '#/components/messages/taskProcessed'
      taskCompleted:
        $ref: '#/components/messages/taskCompleted'
```

Group related lifecycle events that share a topic onto one channel (as above).
Use separate channels when messages travel on genuinely different addresses.
Channel `parameters` (for templated addresses such as `task/{taskId}/status`)
are defined under the channel and referenced from `components.parameters`.

---

## 5. Operations (and the `action` direction rule)

`operations` is a **root-level map** (not nested under channels) keyed in
**camelCase**, verb + noun. Each operation describes one thing the application
does with a channel.

- `action` — **`send`** or **`receive`**, from the perspective of the
  application this document describes:
  - `send` → the application **produces/publishes** the message to the channel.
  - `receive` → the application **consumes/subscribes to** the message from the
    channel.
  - Name `send` operations `publish…` and `receive` operations `consume…` / `on…`
    so the key reflects the action.
- `channel` — a `$ref` to the channel the operation acts on.
- `messages` — a **list** of `$ref`s to the specific messages this operation
  handles. **These must reference messages _through the channel_**
  (`#/channels/<channelKey>/messages/<messageKey>`), **not** directly through
  `#/components/messages/...`. The AsyncAPI 3.1.0 spec requires that an
  operation's messages be a subset of the messages declared on the channel it
  refers to; the official validator (`asyncapi validate`) raises
  `asyncapi3-operation-messages-from-referred-channel` if you point straight at
  `components.messages`. One operation per message keeps the contract explicit.
- `summary` / `description` — recommended; a `|` block describing when the event
  fires and any side effects.
- `security` — include **only when it narrows** the server default.

```yaml
operations:
  publishTaskCreated:
    action: send
    channel:
      $ref: '#/channels/taskEvents'
    messages:
      - $ref: '#/channels/taskEvents/messages/taskCreated'

  publishTaskAssigned:
    action: send
    channel:
      $ref: '#/channels/taskEvents'
    messages:
      - $ref: '#/channels/taskEvents/messages/taskAssigned'
```

A document describing a **producer** has `send` operations; one describing a
**consumer** has `receive` operations. For request-reply protocols, add a
`reply` object (with its own channel and messages) to the operation.

---

## 6. Messages

`components.messages` is keyed in **camelCase** (`taskCreated`). Each message
declares:

- `name` — **PascalCase** machine name (`TaskCreated`).
- `title` — human-readable label ("Task Created Event").
- `summary` — one sentence on what the event means.
- `headers` — a `$ref` to the shared `MessageHeader` schema (§7).
- `payload` — a `$ref` to this message's payload schema (§7).

```yaml
components:
  messages:
    taskCreated:
      name: TaskCreated
      title: Task Created Event
      summary: Indicates that a new task has been created
      headers:
        $ref: '#/components/schemas/MessageHeader'
      payload:
        $ref: '#/components/schemas/TaskCreatedPayload'
```

`description`, `contentType`, `correlationId`, `tags`, and `examples` may be added
when useful. Every message reuses the **same** `MessageHeader` so tracing and
routing metadata are consistent across the system.

---

## 7. Schemas (the shared header + payload pattern)

`components.schemas` is keyed in **PascalCase**. Two kinds of schema:

### The shared message header

One `MessageHeader` reused by every message. It carries cross-cutting
tracing/routing metadata:

```yaml
    MessageHeader:
      type: object
      required:
        - correlationId
        - messageId
        - source
      properties:
        correlationId:
          type: string
          format: uuid
          description: Identifier to trace end-to-end task journey
        messageId:
          type: string
          format: uuid
          description: Unique identifier for this message
        source:
          type: string
          description: Source system generating the event
          minLength: 2
          maxLength: 20
```

### One payload schema per message

Named `<Event>Payload` (`TaskCreatedPayload`). General rules:

- Every object schema declares `type: object`, a `required` list, and
  `properties`. Properties are **camelCase**.
- Apply constraints generously — they are part of the contract:
  - `format`: `uuid`, `date-time`, `uri`, `email`, etc.
  - Strings: `minLength`, `maxLength`, and `pattern` for formatted values.
  - Numbers: `minimum`, `maximum` (e.g. a `progress` 0–100).
- **Model the lifecycle as a state machine** by giving each event's `status` a
  **single-value `enum`**, so the message type and its status agree:
  - `TaskCreatedPayload.status` → `enum: [NEW]`
  - `TaskAssignedPayload.status` → `enum: [ASSIGNED]`
  - `TaskProcessedPayload.status` → `enum: [IN_PROGRESS]`
  - `TaskCompletedPayload.status` → `enum: [DONE]`
- Carry the entity identifier (`taskId`, `format: uuid`) on every payload so
  events can be correlated to the aggregate.

```yaml
    TaskCreatedPayload:
      type: object
      required:
        - taskId
        - title
        - status
        - creatorId
      properties:
        taskId:
          type: string
          format: uuid
        title:
          type: string
          minLength: 1
          maxLength: 200
        description:
          type: string
          maxLength: 2000
        status:
          type: string
          enum: [NEW]
        dueDate:
          type: string
          format: date-time
        creatorId:
          type: string
          format: uuid

    TaskProcessedPayload:
      type: object
      required:
        - taskId
        - status
      properties:
        taskId:
          type: string
          format: uuid
        status:
          type: string
          enum: [IN_PROGRESS]
        progress:
          type: integer
          minimum: 0
          maximum: 100
```

Compose with `$ref` rather than repeating structures, and reuse sub-objects
(addresses, money, actors) as their own named schemas when they appear more than
once.

---

## 8. Security schemes

Define the broker auth scheme once in `components.securitySchemes` and reference
it from each server's `security` list. The house default is `userPassword`
(basic broker credentials):

```yaml
  securitySchemes:
    user-password:
      type: userPassword
      description: Basic authentication for event broker
```

Other valid `type`s when the protocol calls for them: `apiKey`, `X509`,
`scramSha256` / `scramSha512` (common for Kafka SASL), `oauth2`, `openIdConnect`,
`plain`, `gssapi`. Whatever the scheme, define it once and reference it — never
inline credentials in a server.

---

## 9. Componentization (the core principle)

**Inline as little as possible.** The reference chain is:

```
operations ─┬─ channel  ──$ref──▶ channels (one channel)
            └─ messages ──$ref──▶ channels.<key>.messages ──$ref──▶ components.messages ──$ref──▶ components.schemas
```

An operation references its **channel** directly, and references its **messages
through that channel** (`#/channels/<key>/messages/<msg>`). The channel's message
map in turn `$ref`s `components.messages`, whose `headers`/`payload` `$ref`
`components.schemas`. Pointing an operation straight at `components.messages`
fails validation (see §5).

`components` holds these sections, each keyed in the case noted:

- `messages` — **camelCase** keys; `name` is PascalCase.
- `schemas` — **PascalCase**; payloads suffixed `Payload`; the shared header is
  `MessageHeader`.
- `securitySchemes` — the auth scheme(s).
- `parameters` — for templated channel addresses, when used.

This keeps channels and operations readable and the contract DRY. The example
inlines essentially nothing.

---

## 10. Naming cheat sheet

| Element | Convention | Example |
|---------|-----------|---------|
| Channel key | camelCase | `taskEvents` |
| Channel address | kebab-case path segments | `task-management/tasks` |
| Operation key | camelCase verb+noun | `publishTaskCreated` |
| Message component key | camelCase | `taskCreated` |
| Message `name` | PascalCase | `TaskCreated` |
| Message `title` | human-readable | `Task Created Event` |
| Schema / component key | PascalCase | `TaskCreatedPayload` |
| Payload schema | PascalCase + `Payload` | `TaskAssignedPayload` |
| Shared header schema | fixed name | `MessageHeader` |
| Schema property | camelCase | `creatorId` |
| Status enum value | UPPER_SNAKE | `IN_PROGRESS` |
| Server key | camelCase | `production` |

---

## 11. Version notes: 3.1.0, and 3.x vs 2.x

### 3.1.0 vs 3.0.0

3.1.0 is a **non-breaking minor** release over 3.0.0. The object model, the
channel/operation/message split, naming, and every convention in this document
are identical. Moving a 3.0.0 document to 3.1.0 is just changing the version
string (`asyncapi: 3.0.0` → `asyncapi: 3.1.0`). The only spec addition in 3.1.0
is support for the **ROS 2** protocol via the bindings feature — relevant only if
you describe ROS 2 APIs; everything else is unchanged. Author new specs as
`3.1.0` (the current 3.x release) and mirror the example.

### 3.x vs 2.x — what changed

If you have a 2.x document or 2.x habits, the big differences (introduced in
3.0.0 and unchanged in 3.1.0):

- **Operations moved to the root.** In 2.x, `publish`/`subscribe` were nested
  inside each channel. In 3.x, `operations` is its own top-level map and
  references channels via `$ref`.
- **`action: send` / `action: receive` replace `publish` / `subscribe`.** And the
  meaning flipped to be intuitive: the verb now describes what *this application*
  does, not what a consumer does. `send` = this app publishes; `receive` = this
  app consumes.
- **Channels are reusable, first-class, and addressed.** A channel has an explicit
  `address` field; the channel key is just an identifier.
- **`servers` is a map**, and `host` + `protocol` are separate fields (no more
  `url`).
- **Messages are referenced, not inlined.** Define them in `components.messages`
  and `$ref` from channels.

Build new specs as 3.1.0 from the start; mirror the example.
