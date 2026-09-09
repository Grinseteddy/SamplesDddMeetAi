# Bounded Context Canvas — Consent Management

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
%% Bounded Context Canvas — Consent Management
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
      in_cook_profile["<b>Cook Profile</b><br/><i>via SHO</i><br/>? · Consent"]
    end

    subgraph BCX["Consent Management"]
      direction TB
      bc["<b>Consent Management</b><br/>Holds and supplies consent used by the cooking/sharing flow."]
      strat["<b>Strategic classification</b><br/>Domain — <i>unknown</i><br/>Business model — <i>unknown</i><br/>Evolution — <i>unknown</i>"]
      roles["<b>Domain roles</b><br/>Approver — the context’s visible responsibility is consent, which gates what another context may do."]
      subgraph LANG["Ubiquitous language"]
        t_consent["<b>Consent</b>"]
      end
      rules["<b>Business decisions</b><br/><i>none traceable from supplied artifacts</i>"]
      bc ~~~ strat
      strat ~~~ roles
      roles ~~~ LANG
      LANG ~~~ rules
    end

    subgraph OUT["Outbound communication"]
      out_sharing["<b>Sharing</b><br/><i>via CF</i><br/>? · Consent"]
    end

    IN --> BCX
    BCX --> OUT
  end

  subgraph META["Assumptions · Verification metrics · Open questions"]
    assume["<b>Assumptions</b><br/>Consent is present on the context map but absent from the Visual Glossary."]
    metrics["<b>Verification metrics</b><br/><i>none supplied</i>"]
    quest["<b>Open questions</b><br/>Business decisions — What exactly is being consented to, by whom, and what causes refusal or revocation?<br/>Message typing — Is Consent a request, a decision result, or a fact?"]
    assume ~~~ metrics
    metrics ~~~ quest
  end

  TOP ~~~ META

  class bc head
  class strat,roles,rules,assume,quest panel
  class t_consent term
  class metrics unknown
  class in_cook_profile collab
  class out_sharing collab
  class TOP frame
```

## Purpose  (derived)

Holds and supplies consent used by the cooking/sharing flow.

## Strategic classification  (unknown)

| | |
|---|---|
| Domain | (unknown) — no Core Domain Chart supplied |
| Business model | (unknown) — no source supplied |
| Evolution | (unknown) — no Wardley/evolution source supplied |

## Domain roles  (derived)

- Approver — the context’s visible responsibility is consent, which gates what another context may do.

## Inbound communication  (derived)

| Collaborator | Message | Type | What it carries | Evidence |
|---|---|---|---|---|
| Cook Profile | Consent | ? | border payload only; fields not supplied | synchronous · via SHO; Consent sticky on the Cook Profile → Consent Management border |

## Outbound communication  (derived)

| Message | Type | Collaborator | What it carries | Evidence |
|---|---|---|---|---|
| Consent | ? | Sharing | border payload only; fields not supplied | synchronous · via CF; Consent sticky on the Consent Management → Sharing border |

## Ubiquitous language  (given / derived)

The glossary slice is partitioned by the context map's ownership/read-model notation. Exact spelling differences between map and glossary are preserved in assumptions/open questions rather than silently normalized.

| Term | What it means *here* | Owned or borrowed | Cardinality / relation |
|---|---|---|---|
| Consent | Permission/consent used by other contexts. | owned / contested where noted | no glossary definition supplied |

## Business decisions  (derived)

`(unknown)` — no context-local business decision can be traced confidently from the supplied artifacts.

## Assumptions  (derived)

- Consent is present on the context map but absent from the Visual Glossary.

## Verification metrics  (unknown)

`none supplied`

## Open questions

- Business decisions — What exactly is being consented to, by whom, and what causes refusal or revocation?
- Message typing — Is Consent a request, a decision result, or a fact?

## Border reconciliation

- `? · Consent` → **Sharing**: matched by Sharing's inbound table.
