# Bounded Context Canvas — Media

> **Source:** Community Cooking context map (ContextMap.jpg) + Visual Glossary (VisualGlossaryEnhanced.jpg); business decisions from invariants-community-cooking-board.md; purpose lines from the pivotal-event cuts and the context cut · **Mode:** derived from the team's cut
> **Provenance:** `(given)` stated by a source · `(derived)` read off one ·
> `(proposed)` mine, confirm it · `(unknown)` nothing to derive from.

## Canvas

```mermaid
---
config:
  themeVariables:
    fontSize: 22px
    fontFamily: Arial
  flowchart:
    htmlLabels: true
    padding: 12
    nodeSpacing: 30
    rankSpacing: 30
    wrappingWidth: 380
---
%% Bounded Context Canvas — Media
%% Source: Community Cooking context map + Visual Glossary · derived 2026-09-09 · provenance in the field tables below
flowchart TB

  classDef panel fill:#FFF9D6,stroke:#C9B458,stroke-width:1px,color:#1A1A1A
  classDef head fill:#FDE68A,stroke:#B08900,stroke-width:2px,color:#1A1A1A
  classDef collab fill:#EAF2FB,stroke:#6E9BD1,stroke-width:1px,color:#1A1A1A
  classDef offboard fill:#FFFFFF,stroke:#B0B0B0,stroke-width:1px,stroke-dasharray:4 3,color:#5A5A5A
  classDef unknown fill:#F4F4F4,stroke:#B0B0B0,stroke-width:1px,color:#7A7A7A
  classDef frame fill:none,stroke:none
  classDef term fill:#FDF6C8,stroke:#C9B458,stroke-width:1px,color:#1A1A1A
  classDef termgap fill:#FFFFFF,stroke:#B0B0B0,stroke-width:1px,stroke-dasharray:4 3,color:#5A5A5A

  subgraph TOP[" "]
    direction LR

    subgraph IN["Inbound communication"]
      in_cooking_assistance["<b>Cooking Assistance</b><br/>qry · Pictures"]
      in_sharing["<b>Sharing</b><br/>qry · Pictures"]
    end

    subgraph BCX["Media"]
      direction TB
      bc["<b>Media</b><br/>Keeps the pictures a cook takes: of a catastrophe, for the helpers, and of the finished meal, for the community."]
      strat["<b>Strategic classification</b><br/>Domain — <i>unknown</i><br/>Business model — <i>unknown</i><br/>Evolution — <i>unknown</i>"]
      roles["<b>Domain roles</b><br/><i>none argued — a store, not a behaviour</i>"]
      subgraph LANG["Ubiquitous language"]
        t_picture["<b>Picture</b><br/><i>map: Pictures</i>"]
        t_help_request["<b>Help Request</b><br/><i>borrowed — Cooking Assistance</i>"]
        t_help["<b>Help</b><br/><i>borrowed — Cooking Assistance</i>"]
        t_thanks["<b>Thanks</b><br/><i>borrowed — Sharing</i>"]
        t_catastrophe_pictures["<b>Catastrophe Pictures</b><br/><i>undefined — the story's name for the first sense</i>"]
        t_help_request -->|"contains 0..10"| t_picture
        t_help -->|"contains 0..10"| t_picture
        t_thanks -->|"contains 0..10"| t_picture
        t_catastrophe_pictures -.->|"is ?"| t_picture
      end
      rules["<b>Business decisions</b><br/>A picture is of something <i>(INV-MEDIA-01)</i><br/>Only the taker publishes <i>(INV-MEDIA-02)</i><br/><i>…and 1 more (see below)</i>"]
      bc ~~~ strat
      strat ~~~ roles
      roles ~~~ LANG
      LANG ~~~ rules
    end

    subgraph OUT["Outbound communication"]
      out_none["<i>nothing drawn on the map</i>"]
    end

    IN --> BCX
    BCX --> OUT
  end

  subgraph META["Assumptions · Verification metrics · Open questions"]
    assume["<b>Assumptions</b><br/>Both arrows are read as pulls by the readers, not pushes by Media<br/>The OHS boxes at both arrowheads belong to the readers' side of the map's convention"]
    metrics["<b>Verification metrics</b><br/><i>none supplied</i>"]
    quest["<b>Open questions</b><br/>One Picture or two — evidence and trophy?<br/>Who checks guest consent on the pictures sent to strangers?<br/><i>…and 1 more (see below)</i>"]
    assume ~~~ metrics
    metrics ~~~ quest
  end

  TOP ~~~ META

  class bc head
  class strat,roles,rules,assume,quest panel
  class t_picture term
  class t_help_request,t_help,t_thanks,t_catastrophe_pictures termgap
  class metrics unknown
  class in_cooking_assistance,in_sharing collab
  class out_none unknown
  class TOP frame
```

## Purpose  (derived)

Keeps the pictures a cook takes — of a catastrophe, so a helper can see what
went wrong, and of the finished meal, so the community can see the result.

Derived from the one event on the bubble and its two readers. Every prior
analysis found the two purposes to be **two concepts under one word**; the map
keeps one bubble and one word.

## Strategic classification  (unknown)

| | |
|---|---|
| Domain | `(unknown)` — the pivotal-event re-run called pictures "media… available off the shelf" and the story cut said "supporting, tending generic" for sharing them. Hunches, not a chart. |
| Business model | `(unknown)` |
| Evolution | `(unknown)` — photo storage is a commodity in general; whether *this* is, nobody said. |

## Domain roles  (derived)

**None argued.** One event (*Pictures taken*), one object, two readers, no
decision. Nothing on the bubble predicts behaviour, and tagging it *Gateway*
or *Audit model* would be decoration. That is itself a finding: a context that
only stores and serves may be a component of the contexts that use it (see
open questions).

## Inbound communication  (derived)

| Collaborator | Message | Type | What it carries | Evidence |
|---|---|---|---|---|
| Cooking Assistance | Pictures | qry | the catastrophe pictures attached to a help request | yellow Pictures sticky on the arrow from Media; green Pictures read model inside Cooking Assistance |
| Sharing | Pictures | qry | the pictures of the finished meal, to show | yellow Pictures sticky on the arrow from Media |

**Two readers, two senses.** The same query name from two collaborators is a
fact about coupling — and here, about language: Cooking Assistance wants
diagnostic evidence with one audience; Sharing wants a trophy shot for the
whole community. See ubiquitous language.

## Outbound communication  (derived)

**Nothing is drawn.** *Pictures taken* crosses no border; nobody is told a picture exists — both readers must already know to ask.



## Ubiquitous language  (given — from the Visual Glossary; map spelling noted)

The panel above draws this table; it is repeated here because the picture caps what fits and a borrowed sense needs a sentence.

| Term | What it means *here* | Owned or borrowed | Cardinality (glossary) |
|---|---|---|---|
| Picture | an image a cook took; the map spells it *Pictures* | **owned** — the bubble's business object | Help Request contains **0..10** · Help contains **0..10** · Thanks contains **0..10** |
| Help Request · Help · Thanks | the three things a picture can be attached to | **borrowed** — Cooking Assistance (two) and Sharing (one) | — |
| Catastrophe Pictures | the first sense, named by the domain story (as *Catastrophy Pictures*) and the prototype | **undefined** — on no map bubble and in no glossary | — |

**The glossary defines one *Picture* with three owners of attachments and no
distinction between them.** The story cut's table is the borrowed-sense
statement this canvas needs: evidence (short-lived, one responder, "is the
damage legible?") versus result (indefinite, whole community, "is it
flattering, who is in it?"). One name for both "will pull the two contexts
back together" (pivotal-event cut §7).

## Business decisions  (derived — from the invariants sheet)

From the invariants sheet (`INV-MEDIA-*`):

1. **A picture is of something** — a catastrophe, a step, or a finished meal; "which meal is this?" `INV-MEDIA-01` — the board's trophy shot reads *nothing*, i.e. floats free.
2. **Only the taker publishes** — "these are not your pictures". `INV-MEDIA-02`

From the glossary: **at most ten pictures per request, answer or thanks**
(`0..10` on all three) — but those are rules the *owners of the attachments*
enforce, not this context.

**Not a business decision here:** `INV-MEDIA-03` — pictures showing
identifiable guests are not shared without consent. That is Consent
Management's data, and **this context has no edge to Consent Management**;
only Sharing does. The kitchen photos that go to strangers via Cooking
Assistance are checked by nobody.

## Assumptions  (derived)

- Both arrows leaving this bubble end at an OHS box on the *reader's* edge
  (Cooking Assistance, Sharing). Read as the map's arrowhead-side placement;
  the messages are queries the readers issue.
- *Pictures* (map) and *Picture* (glossary) are one term; patch list.
- *Catastrophe Pictures* is carried from the story and prototype, not from
  either input, to name the sense the glossary lacks.

## Verification metrics  (unknown)

`none supplied` — neither the map nor the glossary states one.

`(proposed)`: pictures attached to a request that are never viewed by a responder; pictures never shared.

## Open questions

1. **Is a catastrophe picture the same kind of thing as a finished-meal picture?** Different lifetime, audience and questions; one glossary term. *(Settles the ubiquitous language, and whether this bubble is one context or a piece of two.)*
2. **Who checks consent on the pictures that go to strangers via Cooking Assistance?** Media has no edge to Consent Management. *(Settles where `INV-MEDIA-03` lives — an undrawn edge.)*
3. **Is Media a context or a component?** No decision, no role, no outbound message. *(Settles whether this canvas should exist.)*

## Border reconciliation

| Message | Direction | Counterpart | Status |
|---|---|---|---|
| Pictures | in, from Cooking Assistance | `cooking-assistance.canvas.md` | matched — `qry` both sides |
| Pictures | in, from Sharing | `sharing.canvas.md` | matched — `qry` both sides |
| *(none)* | out | — | **no outbound message anywhere on this canvas** |

Both queries pair. Nothing outbound.
