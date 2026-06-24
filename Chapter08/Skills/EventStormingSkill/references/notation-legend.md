# EventStorming notation legend

The canonical palette and grammar, after Alberto Brandolini (*Introducing EventStorming*). Use this to reconcile what a given board actually does against the standard, and to read notes whose role is ambiguous from color alone.

## Contents
- The sticky palette (color → role)
- The grammar sentence (how the colors connect)
- Icons commonly seen on tooling boards
- Arrows, timeline, and pivotal events
- The three flavours (and what to expect on each)
- Tool-variation caveats (read this before trusting color)

## The sticky palette

| Color | Role | What it is | Text reads like |
|---|---|---|---|
| **Orange** | **Domain Event** | A fact that happened in the domain. The backbone of every board; placed on a left-to-right timeline. | Past tense: *Book purchased*, *Order shipped*, *Payment failed* |
| **Light blue** | **Command** | The intention / decision / action that causes an event. The imperative form of an event. | Imperative: *Purchase book*, *Ship order*, *Retry payment* |
| **Small yellow** | **Actor / User / Persona** | The person (role) who issues a command. | A role noun: *Librarian*, *Member*, *Customer* |
| **Large pale yellow** | **Aggregate** (a.k.a. *System*) | The cluster of data + rules that receives commands and emits events — the consistency boundary. Becomes an entity. | A domain noun: *Book*, *Catalog*, *Order* |
| **Lilac / purple** | **Policy / Process** | Reactive logic: "*whenever* \<event\> *then* \<command\>". May be manual ("someone notices and reacts") or automated. | A rule/automation: *Notification center*, *Auto-reorder*, *Book shelf automate* |
| **Green** | **Read Model** | The information / view a user (or a policy) reads in order to decide on the next command. Becomes a query/projection/screen. | A view noun: *Catalog entry*, *Search criteria*, *Book shelf* |
| **Large pink / salmon** | **External System** | A system outside our boundary that we call out to or receive from. Becomes an integration. | A system name: *Payment gateway*, *Email provider*, *Retailer catalog* |
| **Red / hot pink** (often rotated 45°) | **Hotspot** | A problem, conflict, inconsistency, risk, bottleneck, or open question — "we don't agree / we don't know / this hurts". | A worry: *Who owns this?*, *Slow*, *Duplicate charge?* |
| **Lime / bright green** (Improve/Envision flavour) | **Opportunity** | An upside paired with a hotspot — a place to do better. | An idea: *Upsell here*, *Automate this* |

## The grammar sentence

A complete slice always reads in this fixed order. Reconstruct it for every event:

```
   ACTOR ──issues──▶ COMMAND ──to──▶ AGGREGATE ──produces──▶ DOMAIN EVENT
  (small yellow)     (blue)        (large yellow)              (orange)
        ▲                                                          │
        │                                                          ▼
   reads a READ MODEL (green) ◀── updated by ── the event ───▶ a POLICY (lilac) may
   to decide the next command                                  fire the next command
                                                               automatically
```

Two recurring shapes:
- **Human path:** Actor reads a Read Model → issues a Command → Aggregate emits an Event.
- **Reactive path:** an Event triggers a Policy → which issues the next Command (no human). When the policy spans two bounded contexts, it is effectively a **saga / process manager**, and on the board it usually appears as an **arrow** from the upstream event to the downstream command.

## Icons commonly seen on tooling boards

Digital templates (Miro, Mural, Whiteboard, DDD tools) often stamp an icon on each note. Typical mapping — treat as a *hint*, confirm against grammar:

| Icon | Usually means |
|---|---|
| 👁 eye | Read model (you look at it) |
| 💼 briefcase / box | Aggregate / business entity |
| ⌘ command glyph | Command |
| 👤 person / stick figure | Actor |
| ⚙ gear / robot | Automated policy (the machine reacts, no human) |
| ✨ sparkle / burst | Domain event marker |

## Arrows, timeline, and pivotal events

- **Timeline:** events flow **left → right in time**. Horizontal position is chronology; vertical stacking near one event is usually alternative or parallel detail.
- **Arrows between notes:** causation / triggering. The overwhelmingly common case is *event → command* across slices = a policy/saga. Record direction faithfully; it's the integration wiring.
- **Pivotal event:** a domain event drawn with a **vertical divider line** through the timeline. It marks a phase or bounded-context boundary — a natural seam between modules.

## The three flavours (set your expectations)

EventStorming is one notation used at different resolutions. Detect which one you're looking at, because it tells you how rich the brief can honestly be.

1. **Big Picture** (Improve / Envision) — coarse exploration of a whole line of business. Mostly **orange events**, plus **actors**, **external systems**, and lots of **hotspots**. Few or no commands/aggregates/read models. → Your brief leans on contexts, timeline, and hotspots; aggregates are *inferred candidates*, clearly flagged.
2. **Process Level** (Explore) — a process modelled end-to-end. Adds **commands**, **read models**, and **policies** to the events. → You can produce real commands and flows; aggregates may still be partial.
3. **Software / Design Level** (Design) — the full grammar, ready for DDD and event-driven design: the complete *command → aggregate → event → read model → policy* sentence per slice. → The board already encodes the domain model; your brief can be fully buildable. *(The library example board is essentially this level, laid out as an overview.)*

## Tool-variation caveats — read before trusting color

Real boards drift from the canonical palette. Resolve by **grammar + icon + position**, not color alone:

- **Pink is overloaded.** Canonically pink = external system, but many boards (and the library example) use pink **+ gear** for an **automated policy**. If a pink note names an automation and fires a command, it's a policy, not an external system — regardless of the exact shade.
- **Two yellows.** Aggregate (large, briefcase) and Actor (small, person) are both yellow; size and icon separate them. If both are unclear, the one that issues commands is the actor and the one that emits events is the aggregate.
- **A noun can switch roles between slices.** The same word may be a **read model** (green) where it's consulted and an **aggregate** (yellow) where it's changed — e.g. "Search criteria" or "Catalog entry". Read each occurrence in its own slice; don't force one global role.
- **Lilac vs. pink for policy.** Some teams keep policies lilac; others tint them pink; automated ones may carry a gear/robot. The signal is the "whenever-then" reactive meaning, not the hue.
- **Hand-drawn / photo boards** lose color fidelity to lighting. When unsure, transcribe the text, infer the role from the grammar, and flag the uncertainty in §1 of the brief rather than guessing silently.