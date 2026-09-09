# Bounded Context Canvas — Grandma Avatar AI

> **Source:** supplied Context Map + supplied Visual Glossary · **Mode:** derived from the team's cut  
> **Provenance:** `(given)` stated by a source · `(derived)` read off one · `(proposed)` mine, confirm it · `(unknown)` nothing to derive from.

## Canvas

```mermaid
---
config:
  themeVariables:
    fontSize: 14px
    fontFamily: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, sans-serif
  flowchart:
    htmlLabels: true
    padding: 16
    nodeSpacing: 45
    rankSpacing: 55
    wrappingWidth: 280
---
%% Bounded Context Canvas — Grandma Avatar AI
%% Source: Context Map + Visual Glossary · derived 2026-09-09
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
      in_cooking_assistance["<b>Cooking Assistance</b><br/><i>via ACL</i><br/>? · Help request"]
    end

    subgraph BCX["Grandma Avatar AI"]
      direction TB
      bc["<b>Grandma Avatar AI</b><br/>Acts as a help provider that receives cooking help requests and returns help responses through an anti-corruption layer."]
      strat["<b>Strategic classification</b><br/>Domain — <i>unknown</i><br/>Business model — <i>unknown</i><br/>Evolution — <i>unknown</i>"]
      roles["<b>Domain roles</b><br/>Gateway — routes help requests to an AI-based help provider through an ACL.<br/>Interchange — translates between the cooking model and the avatar/provider model."]
      subgraph LANG["Ubiquitous language"]
        t_grandma_avatar["<b>Grandma Avatar</b>"]
        t_help_provider["<b>Help provider</b>"]
        t_help_request["<b>Help Request</b><br/><i>borrowed</i>"]
        t_help["<b>Help</b><br/><i>borrowed</i>"]
      end
      rules["<b>Business decisions</b><br/><i>none traceable from supplied artifacts</i>"]
      bc ~~~ strat
      strat ~~~ roles
      roles ~~~ LANG
      LANG ~~~ rules
    end

    subgraph OUT["Outbound communication"]
      out_cooking_assistance["<b>Cooking Assistance</b><br/><i>via ACL</i><br/>? · Help response"]
    end

    IN --> BCX
    BCX --> OUT
  end

  subgraph META["Assumptions · Verification metrics · Open questions"]
    assume["<b>Assumptions</b><br/>The map label “Grandma Avatar AI” is matched to glossary term “Grandma Avatar”.<br/>Map payloads “Help request/Help response” are mapped to glossary Help Request/Help without changing the map spellings in communication tables."]
    metrics["<b>Verification metrics</b><br/><i>none supplied</i>"]
    quest["<b>Open questions</b><br/>Message typing — Is Help request a command and Help response an event, or is another contract intended?<br/>Ubiquitous language — Is Grandma Avatar AI a provider identity, a channel, or a separate domain concept from Grandma Avatar?"]
    assume ~~~ metrics
    metrics ~~~ quest
  end

  TOP ~~~ META

  class bc head
  class strat,roles,rules,assume,quest panel
  class t_grandma_avatar,t_help_provider term
  class t_help_request,t_help termgap
  class metrics unknown
  class in_cooking_assistance collab
  class out_cooking_assistance collab
  class TOP frame
```

## Purpose  (derived)

Acts as a help provider that receives cooking help requests and returns help responses through an anti-corruption layer.

## Strategic classification  (unknown)

| | |
|---|---|
| Domain | (unknown) — no Core Domain Chart supplied |
| Business model | (unknown) — no source supplied |
| Evolution | (unknown) — no Wardley/evolution source supplied |

## Domain roles  (derived)

- Gateway — routes help requests to an AI-based help provider through an ACL.
- Interchange — translates between the cooking model and the avatar/provider model.

## Inbound communication  (derived)

| Collaborator | Message | Type | What it carries | Evidence |
|---|---|---|---|---|
| Cooking Assistance | Help request | ? | border payload only; fields not supplied | asynchronous · via ACL; Help request sticky on the dashed upward border |

## Outbound communication  (derived)

| Message | Type | Collaborator | What it carries | Evidence |
|---|---|---|---|---|
| Help response | ? | Cooking Assistance | border payload only; fields not supplied | asynchronous · via ACL; Help response sticky on the dashed return border |

## Ubiquitous language  (given / derived)

The glossary slice is partitioned by the context map's ownership/read-model notation. Exact spelling differences between map and glossary are preserved in assumptions/open questions rather than silently normalized.

| Term | What it means *here* | Owned or borrowed | Cardinality / relation |
|---|---|---|---|
| Grandma Avatar | An avatar that is a Help provider. | owned / contested where noted | is Help provider |
| Help provider | A provider capable of supplying Help. | owned / contested where noted | Grandma Avatar and Chef are Help providers |
| Help Request | A request for cooking help; owned in Cooking Assistance’s help model. | borrowed | borrowed |
| Help | The help supplied in response; owned in Cooking Assistance’s help model. | borrowed | borrowed |

## Business decisions  (derived)

`(unknown)` — no context-local business decision can be traced confidently from the supplied artifacts.

## Assumptions  (derived)

- The map label “Grandma Avatar AI” is matched to glossary term “Grandma Avatar”.
- Map payloads “Help request/Help response” are mapped to glossary Help Request/Help without changing the map spellings in communication tables.

## Verification metrics  (unknown)

`none supplied`

## Open questions

- Message typing — Is Help request a command and Help response an event, or is another contract intended?
- Ubiquitous language — Is Grandma Avatar AI a provider identity, a channel, or a separate domain concept from Grandma Avatar?

## Border reconciliation

- `? · Help response` → **Cooking Assistance**: matched by Cooking Assistance's inbound table.
