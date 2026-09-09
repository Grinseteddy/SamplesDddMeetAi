# Bounded Context Canvas — Notification

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
%% Bounded Context Canvas — Notification
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
      in_cooking_assistance["<b>Cooking Assistance</b><br/><i>via OHS</i><br/>? · Help request<br/>? · Help response"]
    end

    subgraph BCX["Notification"]
      direction TB
      bc["<b>Notification</b><br/>Receives help-related messages and turns them into notifications."]
      strat["<b>Strategic classification</b><br/>Domain — <i>unknown</i><br/>Business model — <i>unknown</i><br/>Evolution — <i>unknown</i>"]
      roles["<b>Domain roles</b><br/>Gateway — converts incoming help-related messages into notifications for an external recipient/channel."]
      subgraph LANG["Ubiquitous language"]
        t_notification["<b>Notification</b>"]
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
      out_none["<i>nothing drawn on the map</i>"]
    end

    IN --> BCX
    BCX --> OUT
  end

  subgraph META["Assumptions · Verification metrics · Open questions"]
    assume["<b>Assumptions</b><br/>Notification appears on the map but is not defined in the Visual Glossary."]
    metrics["<b>Verification metrics</b><br/><i>none supplied</i>"]
    quest["<b>Open questions</b><br/>Ubiquitous language — What is a Notification and who receives it?<br/>Message typing — Are Help request and Help response notifications triggered by events, commands, or another asynchronous contract?"]
    assume ~~~ metrics
    metrics ~~~ quest
  end

  TOP ~~~ META

  class bc head
  class strat,roles,rules,assume,quest panel
  class t_notification term
  class t_help_request,t_help termgap
  class metrics unknown
  class in_cooking_assistance collab
  class out_none unknown
  class TOP frame
```

## Purpose  (derived)

Receives help-related messages and turns them into notifications.

## Strategic classification  (unknown)

| | |
|---|---|
| Domain | (unknown) — no Core Domain Chart supplied |
| Business model | (unknown) — no source supplied |
| Evolution | (unknown) — no Wardley/evolution source supplied |

## Domain roles  (derived)

- Gateway — converts incoming help-related messages into notifications for an external recipient/channel.

## Inbound communication  (derived)

| Collaborator | Message | Type | What it carries | Evidence |
|---|---|---|---|---|
| Cooking Assistance | Help request | ? | border payload only; fields not supplied | asynchronous · via OHS; Help request sticky on the dashed Cooking Assistance → Notification border |
| Cooking Assistance | Help response | ? | border payload only; fields not supplied | asynchronous · via OHS; Help response sticky on the dashed Cooking Assistance → Notification border |

## Outbound communication  (derived)

| Message | Type | Collaborator | What it carries | Evidence |
|---|---|---|---|---|
| — | — | — | — | nothing drawn on the map |

## Ubiquitous language  (given / derived)

The glossary slice is partitioned by the context map's ownership/read-model notation. Exact spelling differences between map and glossary are preserved in assumptions/open questions rather than silently normalized.

| Term | What it means *here* | Owned or borrowed | Cardinality / relation |
|---|---|---|---|
| Notification | A notification emitted from help activity. | owned / contested where noted | no glossary definition supplied |
| Help Request | Help-related input originating in Cooking Assistance. | borrowed | borrowed |
| Help | Help-related response originating in Cooking Assistance. | borrowed | borrowed |

## Business decisions  (derived)

`(unknown)` — no context-local business decision can be traced confidently from the supplied artifacts.

## Assumptions  (derived)

- Notification appears on the map but is not defined in the Visual Glossary.

## Verification metrics  (unknown)

`none supplied`

## Open questions

- Ubiquitous language — What is a Notification and who receives it?
- Message typing — Are Help request and Help response notifications triggered by events, commands, or another asynchronous contract?

## Border reconciliation

- No outbound messages are drawn on the map.
