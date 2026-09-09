# Bounded Context Canvas — Media

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
%% Bounded Context Canvas — Media
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
      in_none["<i>nothing drawn on the map</i>"]
    end

    subgraph BCX["Media"]
      direction TB
      bc["<b>Media</b><br/>Captures pictures and supplies them to cooking assistance and sharing."]
      strat["<b>Strategic classification</b><br/>Domain — <i>unknown</i><br/>Business model — <i>unknown</i><br/>Evolution — <i>unknown</i>"]
      roles["<b>Domain roles</b><br/>Execution model — records pictures as they are taken.<br/>Gateway — exposes captured pictures to other contexts."]
      subgraph LANG["Ubiquitous language"]
        t_picture["<b>Picture</b>"]
      end
      rules["<b>Business decisions</b><br/><i>none traceable from supplied artifacts</i>"]
      bc ~~~ strat
      strat ~~~ roles
      roles ~~~ LANG
      LANG ~~~ rules
    end

    subgraph OUT["Outbound communication"]
      out_cooking_assistance["<b>Cooking Assistance</b><br/><i>via SHO</i><br/>? · Pictures"]
      out_sharing["<b>Sharing</b><br/><i>via OHS</i><br/>? · Pictures"]
    end

    IN --> BCX
    BCX --> OUT
  end

  subgraph META["Assumptions · Verification metrics · Open questions"]
    assume["<b>Assumptions</b><br/>Map plural “Pictures” and glossary singular “Picture” are treated as the same concept for glossary partitioning only."]
    metrics["<b>Verification metrics</b><br/><i>none supplied</i>"]
    quest["<b>Open questions</b><br/>Business decisions — What constraints, if any, does Media itself enforce on captured pictures?<br/>Message typing — Are Pictures queried synchronously by consumers or pushed from Media?"]
    assume ~~~ metrics
    metrics ~~~ quest
  end

  TOP ~~~ META

  class bc head
  class strat,roles,rules,assume,quest panel
  class t_picture term
  class metrics unknown
  class in_none unknown
  class out_cooking_assistance,out_sharing collab
  class TOP frame
```

## Purpose  (derived)

Captures pictures and supplies them to cooking assistance and sharing.

## Strategic classification  (unknown)

| | |
|---|---|
| Domain | (unknown) — no Core Domain Chart supplied |
| Business model | (unknown) — no source supplied |
| Evolution | (unknown) — no Wardley/evolution source supplied |

## Domain roles  (derived)

- Execution model — records pictures as they are taken.
- Gateway — exposes captured pictures to other contexts.

## Inbound communication  (derived)

| Collaborator | Message | Type | What it carries | Evidence |
|---|---|---|---|---|
| — | — | — | — | nothing drawn on the map |

## Outbound communication  (derived)

| Message | Type | Collaborator | What it carries | Evidence |
|---|---|---|---|---|
| Pictures | ? | Cooking Assistance | border payload only; fields not supplied | synchronous · via SHO; Pictures sticky on the Media → Cooking Assistance border |
| Pictures | ? | Sharing | border payload only; fields not supplied | synchronous · via OHS; Pictures sticky on the Media → Sharing border |

## Ubiquitous language  (given / derived)

The glossary slice is partitioned by the context map's ownership/read-model notation. Exact spelling differences between map and glossary are preserved in assumptions/open questions rather than silently normalized.

| Term | What it means *here* | Owned or borrowed | Cardinality / relation |
|---|---|---|---|
| Picture | A picture attached to help or thanks. | owned / contested where noted | Help/Help Request/Thanks may contain pictures |

## Business decisions  (derived)

`(unknown)` — no context-local business decision can be traced confidently from the supplied artifacts.

## Assumptions  (derived)

- Map plural “Pictures” and glossary singular “Picture” are treated as the same concept for glossary partitioning only.

## Verification metrics  (unknown)

`none supplied`

## Open questions

- Business decisions — What constraints, if any, does Media itself enforce on captured pictures?
- Message typing — Are Pictures queried synchronously by consumers or pushed from Media?

## Border reconciliation

- `? · Pictures` → **Cooking Assistance**: matched by Cooking Assistance's inbound table.
- `? · Pictures` → **Sharing**: matched by Sharing's inbound table.
