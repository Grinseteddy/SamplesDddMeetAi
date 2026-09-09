# Bounded Context Canvas — Recipe Catalog

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
%% Bounded Context Canvas — Recipe Catalog
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

    subgraph BCX["Recipe Catalog"]
      direction TB
      bc["<b>Recipe Catalog</b><br/>Holds the recipe catalogue used by planning, including recipes and their ingredients."]
      strat["<b>Strategic classification</b><br/>Domain — <i>unknown</i><br/>Business model — <i>unknown</i><br/>Evolution — <i>unknown</i>"]
      roles["<b>Domain roles</b><br/>Specification model — holds recipe definitions that planning executes against."]
      subgraph LANG["Ubiquitous language"]
        t_recipe["<b>Recipe</b>"]
        t_ingredient["<b>Ingredient</b>"]
        t_recipe -->|"contains 1..*"| t_ingredient
      end
      rules["<b>Business decisions</b><br/>A Recipe contains one or more Ingredients. (given — Visual Glossary)<br/>A Recipe contains one or more Steps. (given — Visual Glossary)"]
      bc ~~~ strat
      strat ~~~ roles
      roles ~~~ LANG
      LANG ~~~ rules
    end

    subgraph OUT["Outbound communication"]
      out_meal_planning["<b>Meal Planning</b><br/><i>via OHS</i><br/>? · Recipe"]
    end

    IN --> BCX
    BCX --> OUT
  end

  subgraph META["Assumptions · Verification metrics · Open questions"]
    assume["<b>Assumptions</b><br/>Glossary term “Ingredient” is treated as the singular form of the map’s “Ingredients”; the spelling mismatch is not silently normalized."]
    metrics["<b>Verification metrics</b><br/><i>none supplied</i>"]
    quest["<b>Open questions</b><br/>Ubiquitous language — Are “Ingredient” (glossary) and “Ingredients” (map) intentionally the same concept?<br/>Message typing — Is Recipe supplied on request (qry), pushed as a command payload, or published as a fact?"]
    assume ~~~ metrics
    metrics ~~~ quest
  end

  TOP ~~~ META

  class bc head
  class strat,roles,rules,assume,quest panel
  class t_recipe,t_ingredient term
  class metrics unknown
  class in_none unknown
  class out_meal_planning collab
  class TOP frame
```

## Purpose  (derived)

Holds the recipe catalogue used by planning, including recipes and their ingredients.

## Strategic classification  (unknown)

| | |
|---|---|
| Domain | (unknown) — no Core Domain Chart supplied |
| Business model | (unknown) — no source supplied |
| Evolution | (unknown) — no Wardley/evolution source supplied |

## Domain roles  (derived)

- Specification model — holds recipe definitions that planning executes against.

## Inbound communication  (derived)

| Collaborator | Message | Type | What it carries | Evidence |
|---|---|---|---|---|
| — | — | — | — | nothing drawn on the map |

## Outbound communication  (derived)

| Message | Type | Collaborator | What it carries | Evidence |
|---|---|---|---|---|
| Recipe | ? | Meal Planning | border payload only; fields not supplied | synchronous · via OHS; Recipe sticky on the Recipe Catalog → Meal Planning border |

## Ubiquitous language  (given / derived)

The glossary slice is partitioned by the context map's ownership/read-model notation. Exact spelling differences between map and glossary are preserved in assumptions/open questions rather than silently normalized.

| Term | What it means *here* | Owned or borrowed | Cardinality / relation |
|---|---|---|---|
| Recipe | A recipe definition used to plan a meal. | owned / contested where noted | contains 1..* Ingredient; contains 1..* Step |
| Ingredient | An ingredient required by a recipe. | owned / contested where noted | 1..* per Recipe |

## Business decisions  (derived)

- A Recipe contains one or more Ingredients. (given — Visual Glossary)
- A Recipe contains one or more Steps. (given — Visual Glossary)

## Assumptions  (derived)

- Glossary term “Ingredient” is treated as the singular form of the map’s “Ingredients”; the spelling mismatch is not silently normalized.

## Verification metrics  (unknown)

`none supplied`

## Open questions

- Ubiquitous language — Are “Ingredient” (glossary) and “Ingredients” (map) intentionally the same concept?
- Message typing — Is Recipe supplied on request (qry), pushed as a command payload, or published as a fact?

## Border reconciliation

- `? · Recipe` → **Meal Planning**: matched by Meal Planning's inbound table.
