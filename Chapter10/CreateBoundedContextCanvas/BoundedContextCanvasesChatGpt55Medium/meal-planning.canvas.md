# Bounded Context Canvas — Meal Planning

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
%% Bounded Context Canvas — Meal Planning
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
      in_recipe_catalog["<b>Recipe Catalog</b><br/><i>via OHS</i><br/>? · Recipe"]
      in_cooking_assistance["<b>Cooking Assistance</b><br/><i>via OHS</i><br/>? · Help response"]
    end

    subgraph BCX["Meal Planning"]
      direction TB
      bc["<b>Meal Planning</b><br/>Turns recipes and available ingredients into a settled dinner plan, handling search, selection, missing ingredients and substitutions."]
      strat["<b>Strategic classification</b><br/>Domain — <i>unknown</i><br/>Business model — <i>unknown</i><br/>Evolution — <i>unknown</i>"]
      roles["<b>Domain roles</b><br/>Draft — holds a meal plan while it is searched, selected and settled.<br/>Execution model — performs the planning lifecycle in real time."]
      subgraph LANG["Ubiquitous language"]
        t_menu["<b>Menu</b>"]
        t_ingredients["<b>Ingredients</b>"]
        t_recipe["<b>Recipe</b><br/><i>borrowed</i>"]
      end
      rules["<b>Business decisions</b><br/>A Menu contains one or more Meals. (given — Visual Glossary)<br/>A Menu has one or more Courses. (given — Visual Glossary)"]
      bc ~~~ strat
      strat ~~~ roles
      roles ~~~ LANG
      LANG ~~~ rules
    end

    subgraph OUT["Outbound communication"]
      out_cooking_assistance["<b>Cooking Assistance</b><br/><i>via OHS</i><br/>? · Menu<br/>? · Ingredients<br/>? · Recipe"]
      out_meal_preparation["<b>Meal Preparation</b><br/><i>via SHO</i><br/>? · Recipe<br/>? · Menu"]
    end

    IN --> BCX
    BCX --> OUT
  end

  subgraph META["Assumptions · Verification metrics · Open questions"]
    assume["<b>Assumptions</b><br/>The map writes “Ingredients” in both Recipe Catalog and Meal Planning; this is treated as contested/dual ownership until the team distinguishes the senses.<br/>Recipe is borrowed because it is a green read model here and a yellow object in Recipe Catalog."]
    metrics["<b>Verification metrics</b><br/><i>none supplied</i>"]
    quest["<b>Open questions</b><br/>Ubiquitous language — What is the difference between Recipe Catalog’s Ingredients and Meal Planning’s Ingredients?<br/>Message typing — Are Menu, Ingredients and Recipe pushed to Cooking Assistance or requested by it?"]
    assume ~~~ metrics
    metrics ~~~ quest
  end

  TOP ~~~ META

  class bc head
  class strat,roles,rules,assume,quest panel
  class t_menu,t_ingredients term
  class t_recipe termgap
  class metrics unknown
  class in_recipe_catalog,in_cooking_assistance collab
  class out_cooking_assistance,out_meal_preparation collab
  class TOP frame
```

## Purpose  (derived)

Turns recipes and available ingredients into a settled dinner plan, handling search, selection, missing ingredients and substitutions.

## Strategic classification  (unknown)

| | |
|---|---|
| Domain | (unknown) — no Core Domain Chart supplied |
| Business model | (unknown) — no source supplied |
| Evolution | (unknown) — no Wardley/evolution source supplied |

## Domain roles  (derived)

- Draft — holds a meal plan while it is searched, selected and settled.
- Execution model — performs the planning lifecycle in real time.

## Inbound communication  (derived)

| Collaborator | Message | Type | What it carries | Evidence |
|---|---|---|---|---|
| Recipe Catalog | Recipe | ? | border payload only; fields not supplied | synchronous · via OHS; Recipe sticky on the Recipe Catalog → Meal Planning border |
| Cooking Assistance | Help response | ? | border payload only; fields not supplied | synchronous · via OHS; Help response sticky on the upward Cooking Assistance → Meal Planning border |

## Outbound communication  (derived)

| Message | Type | Collaborator | What it carries | Evidence |
|---|---|---|---|---|
| Menu | ? | Cooking Assistance | border payload only; fields not supplied | synchronous · via OHS; Menu sticky on the downward Meal Planning → Cooking Assistance border |
| Ingredients | ? | Cooking Assistance | border payload only; fields not supplied | synchronous · via OHS; Ingredients sticky on the downward Meal Planning → Cooking Assistance border |
| Recipe | ? | Cooking Assistance | border payload only; fields not supplied | synchronous · via OHS; Recipe read-model sticky on the downward Meal Planning → Cooking Assistance border |
| Recipe | ? | Meal Preparation | border payload only; fields not supplied | synchronous · via SHO; Recipe sticky on the long Meal Planning → Meal Preparation border |
| Menu | ? | Meal Preparation | border payload only; fields not supplied | synchronous · via SHO; Menu sticky on the long Meal Planning → Meal Preparation border |

## Ubiquitous language  (given / derived)

The glossary slice is partitioned by the context map's ownership/read-model notation. Exact spelling differences between map and glossary are preserved in assumptions/open questions rather than silently normalized.

| Term | What it means *here* | Owned or borrowed | Cardinality / relation |
|---|---|---|---|
| Menu | The planned set of food for the diner. | owned / contested where noted | has 1..* Course; contains 1..* Meal |
| Ingredients | Ingredients as used/adjusted by planning. | owned / contested where noted | spelling differs from glossary Ingredient |
| Recipe | Recipe definition selected during planning; owned by Recipe Catalog. | borrowed | borrowed |

## Business decisions  (derived)

- A Menu contains one or more Meals. (given — Visual Glossary)
- A Menu has one or more Courses. (given — Visual Glossary)

## Assumptions  (derived)

- The map writes “Ingredients” in both Recipe Catalog and Meal Planning; this is treated as contested/dual ownership until the team distinguishes the senses.
- Recipe is borrowed because it is a green read model here and a yellow object in Recipe Catalog.

## Verification metrics  (unknown)

`none supplied`

## Open questions

- Ubiquitous language — What is the difference between Recipe Catalog’s Ingredients and Meal Planning’s Ingredients?
- Message typing — Are Menu, Ingredients and Recipe pushed to Cooking Assistance or requested by it?
- Business decisions — What makes a meal plan “settled” and what conditions stall planning?

## Border reconciliation

- `? · Menu` → **Cooking Assistance**: matched by Cooking Assistance's inbound table.
- `? · Ingredients` → **Cooking Assistance**: matched by Cooking Assistance's inbound table.
- `? · Recipe` → **Cooking Assistance**: matched by Cooking Assistance's inbound table.
- `? · Recipe` → **Meal Preparation**: matched by Meal Preparation's inbound table.
- `? · Menu` → **Meal Preparation**: matched by Meal Preparation's inbound table.
