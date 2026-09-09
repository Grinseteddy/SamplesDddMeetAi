# Bounded Context Canvas — Sharing

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
%% Bounded Context Canvas — Sharing
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
      in_meal_preparation["<b>Meal Preparation</b><br/><i>via SHO</i><br/>? · Help response"]
      in_media["<b>Media</b><br/><i>via OHS</i><br/>? · Pictures"]
      in_consent_management["<b>Consent Management</b><br/><i>via CF</i><br/>? · Consent"]
    end

    subgraph BCX["Sharing"]
      direction TB
      bc["<b>Sharing</b><br/>Handles thanks and sharing outputs, using pictures, help responses and consent."]
      strat["<b>Strategic classification</b><br/>Domain — <i>unknown</i><br/>Business model — <i>unknown</i><br/>Evolution — <i>unknown</i>"]
      roles["<b>Domain roles</b><br/>Execution model — records the act of giving thanks/sharing.<br/>Approver — potentially gates sharing on consent, although the actual rule is not drawn."]
      subgraph LANG["Ubiquitous language"]
        t_thanks["<b>Thanks</b>"]
        t_picture["<b>Picture</b><br/><i>borrowed</i>"]
        t_help["<b>Help</b><br/><i>borrowed</i>"]
        t_consent["<b>Consent</b><br/><i>borrowed</i>"]
        t_thanks -->|"contains 0..10"| t_picture
      end
      rules["<b>Business decisions</b><br/>Thanks contains at most 10 Pictures. (given — Visual Glossary)"]
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
    assume["<b>Assumptions</b><br/>The map’s “Thanks” object is matched to glossary Thanks.<br/>The presence of a Consent border is not taken as proof of a specific consent rule; that remains an open question."]
    metrics["<b>Verification metrics</b><br/><i>none supplied</i>"]
    quest["<b>Open questions</b><br/>Business decisions — Is valid Consent required before Thanks/sharing can be recorded or published?<br/>Ubiquitous language — What does Sharing mean beyond Thanks, and who/what is the recipient?"]
    assume ~~~ metrics
    metrics ~~~ quest
  end

  TOP ~~~ META

  class bc head
  class strat,roles,rules,assume,quest panel
  class t_thanks term
  class t_picture,t_help,t_consent termgap
  class metrics unknown
  class in_meal_preparation,in_media,in_consent_management collab
  class out_none unknown
  class TOP frame
```

## Purpose  (derived)

Handles thanks and sharing outputs, using pictures, help responses and consent.

## Strategic classification  (unknown)

| | |
|---|---|
| Domain | (unknown) — no Core Domain Chart supplied |
| Business model | (unknown) — no source supplied |
| Evolution | (unknown) — no Wardley/evolution source supplied |

## Domain roles  (derived)

- Execution model — records the act of giving thanks/sharing.
- Approver — potentially gates sharing on consent, although the actual rule is not drawn.

## Inbound communication  (derived)

| Collaborator | Message | Type | What it carries | Evidence |
|---|---|---|---|---|
| Meal Preparation | Help response | ? | border payload only; fields not supplied | synchronous · via SHO; Help response sticky on the Meal Preparation → Sharing border |
| Media | Pictures | ? | border payload only; fields not supplied | synchronous · via OHS; Pictures sticky on the Media → Sharing border |
| Consent Management | Consent | ? | border payload only; fields not supplied | synchronous · via CF; Consent sticky on the Consent Management → Sharing border |

## Outbound communication  (derived)

| Message | Type | Collaborator | What it carries | Evidence |
|---|---|---|---|---|
| — | — | — | — | nothing drawn on the map |

## Ubiquitous language  (given / derived)

The glossary slice is partitioned by the context map's ownership/read-model notation. Exact spelling differences between map and glossary are preserved in assumptions/open questions rather than silently normalized.

| Term | What it means *here* | Owned or borrowed | Cardinality / relation |
|---|---|---|---|
| Thanks | A thank-you associated with help/community interaction. | owned / contested where noted | contains 0..10 Picture |
| Picture | Pictures supplied by Media. | borrowed | borrowed |
| Help | Help response associated with the sharing flow. | borrowed | borrowed |
| Consent | Consent supplied by Consent Management; absent from glossary. | borrowed | borrowed |

## Business decisions  (derived)

- Thanks contains at most 10 Pictures. (given — Visual Glossary)

## Assumptions  (derived)

- The map’s “Thanks” object is matched to glossary Thanks.
- The presence of a Consent border is not taken as proof of a specific consent rule; that remains an open question.

## Verification metrics  (unknown)

`none supplied`

## Open questions

- Business decisions — Is valid Consent required before Thanks/sharing can be recorded or published?
- Ubiquitous language — What does Sharing mean beyond Thanks, and who/what is the recipient?

## Border reconciliation

- No outbound messages are drawn on the map.
