# Bounded Context Canvas — Meal Preparation

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
%% Bounded Context Canvas — Meal Preparation
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
      in_meal_planning["<b>Meal Planning</b><br/><i>via SHO</i><br/>? · Recipe<br/>? · Menu"]
      in_cooking_assistance["<b>Cooking Assistance</b><br/><i>via OHS</i><br/>? · Help response"]
    end

    subgraph BCX["Meal Preparation"]
      direction TB
      bc["<b>Meal Preparation</b><br/>Runs the meal-preparation process, including starting preparation, dealing with catastrophes and unclear steps."]
      strat["<b>Strategic classification</b><br/>Domain — <i>unknown</i><br/>Business model — <i>unknown</i><br/>Evolution — <i>unknown</i>"]
      roles["<b>Domain roles</b><br/>Execution model — carries out meal preparation in real time."]
      subgraph LANG["Ubiquitous language"]
        t_meal_preparation_catastrophe["<b>Meal Preparation Catastrophe</b>"]
        t_help_for_meal_preparation_step["<b>Help for Meal Preparation Step</b>"]
        t_menu["<b>Menu</b><br/><i>borrowed</i>"]
        t_recipe["<b>Recipe</b><br/><i>borrowed</i>"]
        t_step["<b>Step</b><br/><i>borrowed</i>"]
      end
      rules["<b>Business decisions</b><br/><i>none traceable from supplied artifacts</i>"]
      bc ~~~ strat
      strat ~~~ roles
      roles ~~~ LANG
      LANG ~~~ rules
    end

    subgraph OUT["Outbound communication"]
      out_cooking_assistance["<b>Cooking Assistance</b><br/><i>via OHS</i><br/>qry · Menu<br/>qry · Recipe"]
      out_sharing["<b>Sharing</b><br/><i>via SHO</i><br/>? · Help response"]
    end

    IN --> BCX
    BCX --> OUT
  end

  subgraph META["Assumptions · Verification metrics · Open questions"]
    assume["<b>Assumptions</b><br/>The orange event “Catastrophe happened” is matched to glossary Meal Preparation Catastrophe; “Step unclear” is matched to Help for Meal Preparation Step.<br/>No yellow owned business object is drawn inside Meal Preparation; ownership of the two specialized Help Request terms is therefore provisional."]
    metrics["<b>Verification metrics</b><br/><i>none supplied</i>"]
    quest["<b>Open questions</b><br/>Ubiquitous language — Which context owns Step and the specialized Help Request types?<br/>Message typing — Why are Menu and Recipe sent/read both through Meal Planning and Cooking Assistance?"]
    assume ~~~ metrics
    metrics ~~~ quest
  end

  TOP ~~~ META

  class bc head
  class strat,roles,rules,assume,quest panel
  class t_meal_preparation_catastrophe,t_help_for_meal_preparation_step term
  class t_menu,t_recipe,t_step termgap
  class metrics unknown
  class in_meal_planning,in_cooking_assistance collab
  class out_cooking_assistance,out_sharing collab
  class TOP frame
```

## Purpose  (derived)

Runs the meal-preparation process, including starting preparation, dealing with catastrophes and unclear steps.

## Strategic classification  (unknown)

| | |
|---|---|
| Domain | (unknown) — no Core Domain Chart supplied |
| Business model | (unknown) — no source supplied |
| Evolution | (unknown) — no Wardley/evolution source supplied |

## Domain roles  (derived)

- Execution model — carries out meal preparation in real time.

## Inbound communication  (derived)

| Collaborator | Message | Type | What it carries | Evidence |
|---|---|---|---|---|
| Meal Planning | Recipe | ? | border payload only; fields not supplied | synchronous · via SHO; Recipe sticky on the long Meal Planning → Meal Preparation border |
| Meal Planning | Menu | ? | border payload only; fields not supplied | synchronous · via SHO; Menu sticky on the long Meal Planning → Meal Preparation border |
| Cooking Assistance | Help response | ? | border payload only; fields not supplied | synchronous · via OHS; Help response sticky on the downward Cooking Assistance → Meal Preparation border |

## Outbound communication  (derived)

| Message | Type | Collaborator | What it carries | Evidence |
|---|---|---|---|---|
| Menu | qry | Cooking Assistance | border payload only; fields not supplied | synchronous · via OHS; green Menu read-model sticky on the upward Meal Preparation → Cooking Assistance border |
| Recipe | qry | Cooking Assistance | border payload only; fields not supplied | synchronous · via OHS; green Recipe read-model sticky on the upward Meal Preparation → Cooking Assistance border |
| Help response | ? | Sharing | border payload only; fields not supplied | synchronous · via SHO; Help response sticky on the Meal Preparation → Sharing border |

## Ubiquitous language  (given / derived)

The glossary slice is partitioned by the context map's ownership/read-model notation. Exact spelling differences between map and glossary are preserved in assumptions/open questions rather than silently normalized.

| Term | What it means *here* | Owned or borrowed | Cardinality / relation |
|---|---|---|---|
| Meal Preparation Catastrophe | A catastrophe arising during meal preparation. | owned / contested where noted | is a Help Request subtype |
| Help for Meal Preparation Step | Help concerning a preparation step. | owned / contested where noted | is a Help Request subtype |
| Menu | Menu used during preparation; owned by Meal Planning. | borrowed | borrowed |
| Recipe | Recipe used during preparation; owned by Recipe Catalog. | borrowed | borrowed |
| Step | A step in a Recipe; glossary-defined, no exact owning object drawn in this context. | borrowed | borrowed/owner unresolved |

## Business decisions  (derived)

`(unknown)` — no context-local business decision can be traced confidently from the supplied artifacts.

## Assumptions  (derived)

- The orange event “Catastrophe happened” is matched to glossary Meal Preparation Catastrophe; “Step unclear” is matched to Help for Meal Preparation Step.
- No yellow owned business object is drawn inside Meal Preparation; ownership of the two specialized Help Request terms is therefore provisional.

## Verification metrics  (unknown)

`none supplied`

## Open questions

- Ubiquitous language — Which context owns Step and the specialized Help Request types?
- Message typing — Why are Menu and Recipe sent/read both through Meal Planning and Cooking Assistance?
- Business decisions — What makes a preparation step “unclear” or a situation a “catastrophe”?

## Border reconciliation

- `qry · Menu` → **Cooking Assistance**: matched by Cooking Assistance's inbound table.
- `qry · Recipe` → **Cooking Assistance**: matched by Cooking Assistance's inbound table.
- `? · Help response` → **Sharing**: matched by Sharing's inbound table.
