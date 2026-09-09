# Bounded Context Canvas — Cooking Assistance

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
%% Bounded Context Canvas — Cooking Assistance
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
      in_meal_planning["<b>Meal Planning</b><br/><i>via OHS</i><br/>? · Menu<br/>? · Ingredients<br/>? · Recipe"]
      in_grandma_avatar_ai["<b>Grandma Avatar AI</b><br/><i>via ACL</i><br/>? · Help response"]
      in_media["<b>Media</b><br/><i>via SHO</i><br/>? · Pictures"]
      in_meal_preparation["<b>Meal Preparation</b><br/><i>via OHS</i><br/>qry · Menu<br/>qry · Recipe"]
    end

    subgraph BCX["Cooking Assistance"]
      direction TB
      bc["<b>Cooking Assistance</b><br/>Coordinates cooking help: accepts help needs, supplies help responses, and uses menu, ingredient, recipe and picture information while cooking."]
      strat["<b>Strategic classification</b><br/>Domain — <i>unknown</i><br/>Business model — <i>unknown</i><br/>Evolution — <i>unknown</i>"]
      roles["<b>Domain roles</b><br/>Execution model — handles help requests and produces help during cooking.<br/>Gateway — coordinates multiple providers and collaborators around a help interaction."]
      subgraph LANG["Ubiquitous language"]
        t_help_request["<b>Help Request</b>"]
        t_help["<b>Help</b>"]
        t_menu["<b>Menu</b><br/><i>borrowed</i>"]
        t_ingredient["<b>Ingredient</b><br/><i>borrowed</i>"]
        t_recipe["<b>Recipe</b><br/><i>borrowed</i>"]
        t_picture["<b>Picture</b><br/><i>borrowed</i>"]
      end
      rules["<b>Business decisions</b><br/>A Help contains 1..3 Ingredient Substitutes. (given — Visual Glossary)<br/>A Help contains exactly 1 Preparation Step Explanation. (given — Visual Glossary)<br/>A Help contains 1..10 Steps to Mitigate Catastrophe. (given — Visual Glossary)<br/><i>…and 1 more (see below)</i>"]
      bc ~~~ strat
      strat ~~~ roles
      roles ~~~ LANG
      LANG ~~~ rules
    end

    subgraph OUT["Outbound communication"]
      out_meal_planning["<b>Meal Planning</b><br/><i>via OHS</i><br/>? · Help response"]
      out_notification["<b>Notification</b><br/><i>via OHS</i><br/>? · Help request<br/>? · Help response"]
      out_grandma_avatar_ai["<b>Grandma Avatar AI</b><br/><i>via ACL</i><br/>? · Help request"]
      out_meal_preparation["<b>Meal Preparation</b><br/><i>via OHS</i><br/>? · Help response"]
    end

    IN --> BCX
    BCX --> OUT
  end

  subgraph META["Assumptions · Verification metrics · Open questions"]
    assume["<b>Assumptions</b><br/>Glossary Help Request is treated as the model behind map payload “Help request”; glossary Help is treated as the model behind “Help response”.<br/>Ingredient/Picture singular glossary terms are matched to plural map labels Ingredients/Pictures."]
    metrics["<b>Verification metrics</b><br/><i>none supplied</i>"]
    quest["<b>Open questions</b><br/>Message typing — Confirm the cmd/qry/evt type of every Help request/Help response crossing.<br/>Ubiquitous language — Does “Help response” equal glossary Help, or is it a distinct envelope?"]
    assume ~~~ metrics
    metrics ~~~ quest
  end

  TOP ~~~ META

  class bc head
  class strat,roles,rules,assume,quest panel
  class t_help_request,t_help term
  class t_menu,t_ingredient,t_recipe,t_picture termgap
  class metrics unknown
  class in_meal_planning,in_grandma_avatar_ai,in_media,in_meal_preparation collab
  class out_meal_planning,out_notification,out_grandma_avatar_ai,out_meal_preparation collab
  class TOP frame
```

## Purpose  (derived)

Coordinates cooking help: accepts help needs, supplies help responses, and uses menu, ingredient, recipe and picture information while cooking.

## Strategic classification  (unknown)

| | |
|---|---|
| Domain | (unknown) — no Core Domain Chart supplied |
| Business model | (unknown) — no source supplied |
| Evolution | (unknown) — no Wardley/evolution source supplied |

## Domain roles  (derived)

- Execution model — handles help requests and produces help during cooking.
- Gateway — coordinates multiple providers and collaborators around a help interaction.

## Inbound communication  (derived)

| Collaborator | Message | Type | What it carries | Evidence |
|---|---|---|---|---|
| Meal Planning | Menu | ? | border payload only; fields not supplied | synchronous · via OHS; Menu sticky on the downward Meal Planning → Cooking Assistance border |
| Meal Planning | Ingredients | ? | border payload only; fields not supplied | synchronous · via OHS; Ingredients sticky on the downward Meal Planning → Cooking Assistance border |
| Meal Planning | Recipe | ? | border payload only; fields not supplied | synchronous · via OHS; Recipe read-model sticky on the downward Meal Planning → Cooking Assistance border |
| Grandma Avatar AI | Help response | ? | border payload only; fields not supplied | asynchronous · via ACL; Help response sticky on the dashed return border |
| Media | Pictures | ? | border payload only; fields not supplied | synchronous · via SHO; Pictures sticky on the Media → Cooking Assistance border |
| Meal Preparation | Menu | qry | border payload only; fields not supplied | synchronous · via OHS; green Menu read-model sticky on the upward Meal Preparation → Cooking Assistance border |
| Meal Preparation | Recipe | qry | border payload only; fields not supplied | synchronous · via OHS; green Recipe read-model sticky on the upward Meal Preparation → Cooking Assistance border |

## Outbound communication  (derived)

| Message | Type | Collaborator | What it carries | Evidence |
|---|---|---|---|---|
| Help response | ? | Meal Planning | border payload only; fields not supplied | synchronous · via OHS; Help response sticky on the upward Cooking Assistance → Meal Planning border |
| Help request | ? | Notification | border payload only; fields not supplied | asynchronous · via OHS; Help request sticky on the dashed Cooking Assistance → Notification border |
| Help response | ? | Notification | border payload only; fields not supplied | asynchronous · via OHS; Help response sticky on the dashed Cooking Assistance → Notification border |
| Help request | ? | Grandma Avatar AI | border payload only; fields not supplied | asynchronous · via ACL; Help request sticky on the dashed upward border |
| Help response | ? | Meal Preparation | border payload only; fields not supplied | synchronous · via OHS; Help response sticky on the downward Cooking Assistance → Meal Preparation border |

## Ubiquitous language  (given / derived)

The glossary slice is partitioned by the context map's ownership/read-model notation. Exact spelling differences between map and glossary are preserved in assumptions/open questions rather than silently normalized.

| Term | What it means *here* | Owned or borrowed | Cardinality / relation |
|---|---|---|---|
| Help Request | A request for help, specialized by the kind of cooking problem. | owned / contested where noted | may contain 0..10 Picture |
| Help | The help supplied for a request. | owned / contested where noted | contains 1..3 Ingredient Substitute; 1 Preparation Step Explanation; 1..10 Steps to Mitigate Catastrophe; 1..3 Menu proposal |
| Menu | Menu information used while assisting; owned by Meal Planning. | borrowed | borrowed |
| Ingredient | Ingredient information used while assisting; map says Ingredients. | borrowed | borrowed |
| Recipe | Recipe information used while assisting; owned by Recipe Catalog. | borrowed | borrowed |
| Picture | Pictures used as context; owned by Media. | borrowed | borrowed |

## Business decisions  (derived)

- A Help contains 1..3 Ingredient Substitutes. (given — Visual Glossary)
- A Help contains exactly 1 Preparation Step Explanation. (given — Visual Glossary)
- A Help contains 1..10 Steps to Mitigate Catastrophe. (given — Visual Glossary)
- A Help contains 1..3 Menu proposals. (given — Visual Glossary)

## Assumptions  (derived)

- Glossary Help Request is treated as the model behind map payload “Help request”; glossary Help is treated as the model behind “Help response”.
- Ingredient/Picture singular glossary terms are matched to plural map labels Ingredients/Pictures.

## Verification metrics  (unknown)

`none supplied`

## Open questions

- Message typing — Confirm the cmd/qry/evt type of every Help request/Help response crossing.
- Ubiquitous language — Does “Help response” equal glossary Help, or is it a distinct envelope?
- Boundary — Why does Meal Preparation query Menu and Recipe through Cooking Assistance when Meal Planning also exposes them directly?

## Border reconciliation

- `? · Help response` → **Meal Planning**: matched by Meal Planning's inbound table.
- `? · Help request` → **Notification**: matched by Notification's inbound table.
- `? · Help response` → **Notification**: matched by Notification's inbound table.
- `? · Help request` → **Grandma Avatar AI**: matched by Grandma Avatar AI's inbound table.
- `? · Help response` → **Meal Preparation**: matched by Meal Preparation's inbound table.
