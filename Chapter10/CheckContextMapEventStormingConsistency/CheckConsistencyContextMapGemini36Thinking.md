# Prompt 2026-09-08

Check EventStorming board against the Context Map. Use the provided skill.

![](../assets/EventStormingBoardWithBcs.jpg)
![](../assets/ContextMap.jpg)

[Skill](../Skills/ContextBoardEventStormingConsistency/SKILL.md)
[Refrences Checks](../Skills/ContextBoardEventStormingConsistency/references/checks.md)

# Answer

## 0. Scope checked



* **Board Artifact**: EventStorming Board image (`EventStormingBoardWithBcs.jpg`). Full wall session snapshot containing explicit bounded context bubbles drawn around stickies.


* **Map Artifact**: Context Map diagram (`ContextMap.jpg`).
* **Preconditions**: Map was derived directly from this board session. Board contains explicit context boundary bubbles. Board is complete for the session scope.


* **Unchecked / Low Clarity**: Exact directional arrowheads on `Grandma Avatar AI` (drawn as dashed bidirectional flow) and abbreviated port labels on node borders (`SHO` vs `OHS`).

---

## 1. Verdict



**Consistent with drift**

* **Blocking**: 2 (`DIR-1`, `NODE-1`)


* **Significant**: 3 (`NODE-2`, `CONTRACT-1`, `EDGE-1`)


* **Minor**: 2 (`PAT-1`, `TERM-1`)


* **Resolution direction**: 4 Map Moves, 2 Back to the Wall, 1 Ask.



---

## 2. Board as read



| Context Bubble | Events | Commands | Actors / Systems | Objects Produced | Objects Consumed |
| --- | --- | --- | --- | --- | --- |
| **Cook Profile** (1x) | Cook registered | Register cook | User | Cook, User | (none) |
| **Meal Planning** (2x) | Dinner planned, Recipes searched, Recipe selected, Meal planning stalled, Ingredients missing, Ingredients substituted, Meal plan settled | Plan dinner, Search recipes, Plan meal, Substitute ingredients | Cook, Recipe Catalog (pink system) | Meal plan, Ingredients missing | Guests, Menu, Recipe Catalog, Recipe, Help response |
| **Cooking Assistance** (4x) | Help requested, Help provided | Request help, Provide help | Cook, Community Cook, Chef, Grandma Avatar (pink system) | Help request, Help response | Ingredients, Menu, Catastrophe, Recipe, Pictures |
| **Meal Preparation** (2x) | Meal preparation started, Catastrophe happened, Step completed, Meal rescued, Meal prepared | Prepare meal | Cook | Catastrophe, Meal prepared | Recipe, Step unclear, Help response |
| **Media** (2x) | Pictures taken | Take pictures | Cook | Pictures | (none) |
| **Sharing** (1x) | Thanks given | Provide thanks | Cook | Thanks | Help provider, Pictures |

---

## 3. Map as read



| Source Node | Target Node | Arrow Direction | Payload / Contract | Pattern | Mechanism |
| --- | --- | --- | --- | --- | --- |
| Recipe Catalog | Meal Planning | Down ($\downarrow$) | Recipe | OHS | Sync |
| Meal Planning | Cooking Assistance | Down ($\downarrow$) | Menu, Ingredients, Recipe, Help response | OHS | Sync |
| Cooking Assistance | Grandma Avatar AI | Bidirectional ($\uparrow\downarrow$) | Help request ($\uparrow$), Help response ($\downarrow$) | ACL | Async (dashed) |
| Cooking Assistance | Notification | Left ($\leftarrow$) | Help request, Help response | OHS / SHO | Async (dashed) |
| Cooking Assistance | Meal Preparation | Down ($\downarrow$) | Help response | OHS | Sync |
| Meal Planning | Meal Preparation | Down ($\downarrow$) | Recipe, Menu | OHS | Sync |
| Media | Cooking Assistance | Left ($\leftarrow$) | Pictures | SHO | Sync |
| Media | Sharing | Down ($\downarrow$) | Pictures | OHS | Sync |
| Cooking Assistance | Sharing | Down/Right ($\searrow$) | Help response | OHS / CF | Sync |
| Cook Profile | Consent Management | Down ($\downarrow$) | Consent | (not stated) | Sync |
| Consent Management | Meal Preparation | Down ($\downarrow$) | Consent | (not stated) | Sync |

---

## 4. Term ledger



| Term | Written by (board) | Read by (board) | Map says owned by | Verdict |
| --- | --- | --- | --- | --- |
| **Cook / User** | Cook Profile | All Contexts (via Cook actor) | Cook Profile | Consistent |
| **Recipe** | Recipe Catalog (pink) | Meal Planning, Cooking Assistance, Meal Preparation | Recipe Catalog | Consistent |
| **Help request** | Cooking Assistance | Cooking Assistance, Grandma Avatar | Cooking Assistance | Consistent |
| **Help response** | Cooking Assistance | Meal Planning, Meal Preparation | Cooking Assistance | **DIR Error on Map**<br> |
| **Meal plan** | Meal Planning | *Nobody* | Meal Planning | **Orphan Object — see GAP**<br> |
| **Pictures** | Media | Cooking Assistance, Sharing | Media | Consistent |
| **Catastrophe** | Meal Preparation | Cooking Assistance | Meal Preparation | **Undrawn Edge (`EDGE`)**<br> |
| **Consent** | *Nobody* | *Nobody* | Consent Management | **Invented Term (`NODE`)**<br> |

---

## 5. Node reconciliation



| Board Bubble(s) | Map Node | Match Rung | Status / Verdict |
| --- | --- | --- | --- |
| Cook Profile (1x) | Cook Profile | Exact | Matched |
| Meal Planning (2x) | Meal Planning | Collapse | Matched (recurrence correctly collapsed)

|
| Cooking Assistance (4x) | Cooking Assistance | Collapse | Matched (recurrence correctly collapsed)

|
| Meal Preparation (2x) | Meal Preparation | Collapse | Matched (recurrence correctly collapsed)

|
| Media (2x) | Media | Collapse | Matched (recurrence correctly collapsed)

|
| Sharing (1x) | Sharing | Exact | Matched |
| Recipe Catalog (pink sticky) | Recipe Catalog | External Promotion | Matched (valid system boundary)

|
| Grandma Avatar (pink sticky) | Grandma Avatar AI | External Promotion | Matched (valid system boundary)

|
| *(None)* | Consent Management | Unmatched | **`NODE` — Invented node**<br> |
| *(None)* | Notification | Unmatched | **`NODE` — Off-board infrastructure**<br> |

---

## 6. Findings



### **[F1] Code: `DIR` | Severity: Blocking**

* **Evidence**: Edge `Meal Planning` $\rightarrow$ `Cooking Assistance` includes `Help response` in the payload flowing downstream from `Meal Planning`. On the board, `Help response` is produced by `Cooking Assistance` (`Help provided` event) and consumed as a read model in `Meal Planning` prior to `Plan meal`.


* **Why it matters**: Inverts the upstream producer relationship, causing integrations to be designed backwards.


* **Resolution**: *Map moves*. Remove `Help response` from the `Meal Planning` $\rightarrow$ `Cooking Assistance` contract, and route it from `Cooking Assistance` to `Meal Planning`.



### **[F2] Code: `NODE` | Severity: Blocking**

* **Evidence**: `Consent Management` exists on the map as a standalone node passing `Consent` to `Meal Preparation`. The board has no events, commands, read models, or pink stickies referencing `Consent`.


* **Why it matters**: Introduces an explicit boundary and runtime coordination for a domain model unsupported by board evidence.


* **Resolution**: *Back to the wall / Map moves*. Either strip `Consent Management` from the map or add explicit consent policies and events back to the board wall.



### **[F3] Code: `NODE` | Severity: Significant**

* **Evidence**: `Notification` is drawn as a bounded context receiving asynchronous `Help request` and `Help response` payloads from `Cooking Assistance`. No `Notification` bubble or sticky exists on the board.


* **Why it matters**: Represents technical architecture injected onto a domain context map without explicit domain event triggers.


* **Resolution**: *Ask / Allowed divergence*. Mark `Notification` as a generic infrastructure node or add explicit notification policy stickies to the wall.



### **[F4] Code: `EDGE` | Severity: Significant**

* **Evidence**: `Meal Preparation` produces `Catastrophe happened` (event) and `Catastrophe` (read model). `Cooking Assistance` consumes `Catastrophe` as the input trigger for `Help requested`. The map shows no edge carrying `Catastrophe` from `Meal Preparation` to `Cooking Assistance`.


* **Why it matters**: Omits the critical upstream trigger for emergency cooking help.


* **Resolution**: *Map moves*. Add `Catastrophe` payload to an edge directed from `Meal Preparation` to `Cooking Assistance`.



### **[F5] Code: `CONTRACT` | Severity: Significant**

* **Evidence**: Contract `Meal Planning` $\rightarrow$ `Cooking Assistance` specifies `Menu`, `Ingredients`, `Recipe`. Board sticky evidence shows `Meal Planning` produces `Meal plan settled` / `Meal plan`, while `Menu` and `Ingredients` are internal query models.


* **Why it matters**: Contract specifies transient intermediate read models rather than the core aggregate outcome crossing the border.


* **Resolution**: *Map moves*. Reconcile payload names to export `Meal plan` state.



### **[F6] Code: `PAT` | Severity: Minor**

* **Evidence**: Almost every internal node on the map (`Recipe Catalog`, `Meal Planning`, `Cooking Assistance`, `Meal Preparation`, `Media`, `Sharing`) is marked with `OHS` (Open Host Service).


* **Why it matters**: Universal decoration of `OHS` dilutes pattern utility where point-to-point or conformist integrations are used.


* **Resolution**: *Map moves*. Restrict `OHS` tags to nodes explicitly serving multiple downstream consumers (e.g., `Cooking Assistance`, `Media`).



### **[F7] Code: `TERM` | Severity: Minor**

* **Evidence**: Board sticky names the pink system `Grandma Avatar`; map names the node `Grandma Avatar AI`.


* **Why it matters**: Cosmetic terminology expansion.


* **Resolution**: *Allowed divergence*. Retain as an architectural clarification.



---

## 7. Undrawn edges and missing nodes



* **Undrawn Edge**: `Meal Preparation` $\rightarrow$ `Cooking Assistance` carrying `Catastrophe`.


* **Missing Node Connection**: `Cook Profile` produces `Cook`/`User` identity consumed by all contexts, but the map shows `Cook Profile` only outputting `Consent`.



---

## 8. Allowed divergence



* **Timeline Collapse**: Collapsing 4 `Cooking Assistance` bubbles, 2 `Meal Planning` bubbles, 2 `Meal Preparation` bubbles, and 2 `Media` bubbles into single context nodes is valid and recommended context map design.


* **System Promotion**: Promoting `Recipe Catalog` and `Grandma Avatar` pink stickies to external bounded context nodes (`OHS`, `ACL`) is valid context mapping practice.


* **Protocol Details**: Drawing dashed lines for asynchronous mechanisms (e.g., to `Notification` or `Grandma Avatar AI`) elaborates technical transport details not visible on sticky notes.



---

## 9. Patch list



### **Edits to the Map**

1. Remove `Help response` from the `Meal Planning` $\rightarrow$ `Cooking Assistance` contract; add `Help response` payload to an edge originating at `Cooking Assistance` pointing to `Meal Planning`.


2. Add `Catastrophe` to the edge payload flowing from `Meal Preparation` to `Cooking Assistance`.


3. Remove `Consent Management` node and `Consent` edges until domain justification exists.


4. Update `Meal Planning` contract payload to export `Meal plan` rather than `Menu`/`Ingredients`.


5. Strip `OHS` decorations from single-consumer borders.



### **Back to the Wall (Board Edits)**

1. Draw explicit consumer stickies for `Meal plan` or mark `Meal plan` as internal to `Meal Planning`.


2. If `Consent` and `Notification` are required business boundaries, add the missing domain events (`Consent Granted`, `Notification Sent`) and policies to the wall.



---

## 10. Already consistent



* `Cook Profile` ownership of user/cook registration.


* `Recipe Catalog` supplying `Recipe` payloads to `Meal Planning`.


* `Media` producing `Pictures` consumed by `Cooking Assistance` and `Sharing`.


* `Cooking Assistance` supplying `Help response` to `Meal Preparation`.


* `Sharing` consuming `Thanks` and `Pictures`.