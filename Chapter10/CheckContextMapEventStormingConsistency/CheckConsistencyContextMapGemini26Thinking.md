# Prompt 2026-09-08

Check EventStorming board against the Context Map. Use the provided skill.

![](../assets/EventStormingBoardWithBcs.jpg)
![](../assets/ContextMap.jpg)

[Skill](../Skills/ContextBoardEventStormingConsistency/SKILL.md)
[Refrences Checks](../Skills/ContextBoardEventStormingConsistency/references/checks.md)

# Answer

# Conformance Report — ContextMap.jpg × EventStormingBoardWithBcs.jpg

## 0. Scope checked

* **Artifacts:** `ContextMap.jpg` (Context Map diagram) checked against `EventStormingBoardWithBcs.jpg` (EventStorming board with Bounded Context boundaries).
* **Preconditions:**
* *Derived from board:* Yes, the map attempts to formalize the domain boundaries captured during the EventStorming session.
* *Artifact age:* The Context Map is drawn after the board.
* *Board completeness:* The board represents a domain discovery session covering meal planning through preparation and sharing. It lacks stickies for consent tracking or notification delivery. Context boundaries are drawn directly on the board.



## 1. Verdict

* **Verdict:** Diverged (The map introduces non-board nodes and alters actor/context boundaries).
* **Finding Counts:** 1 Blocking, 3 Significant, 1 Minor.
* **Resolution Direction:** Map moves: 2 | Back to the wall: 2 | Ask: 1.

## 2. Board as read

| Context Bubble (Board) | Events (Orange) | Commands (Blue) | Read Models / Objects (Green/Yellow) | Actors / Systems (Yellow/Pink) |
| --- | --- | --- | --- | --- |
| Cook Profile | Cook registered | Register cook | Cook | User |
| Meal Planning (Top) | Dinner planned, Recipes searched, Recipe selected, Ingredients missing, Meal planning stalled | Plan dinner, Search recipes, Search ingredients, Prepare meal | Guests, Menu, Recipe Catalog, Recipe, Ingredients | User, Cook |
| Cooking Assistance (Top Right) | Help requested | Request help | Ingredients, Menu, Help request | Cook |
| Cooking Assistance (Middle Left) | Help provided | Provide help | Help request, Help response | Community Cook, Chef, Grandma Avatar |
| Meal Planning (Middle) | Ingredients substituted, Meal plan settled | Substitute ingredients, Plan meal | Recipe, Menu, Help response, Meal plan | Cook |
| Meal Preparation (Middle) | Meal preparation started, Catastrophe happened, Step unclear | Prepare meal | Recipe, Menu | Cook |
| Media (Middle Right) | Pictures taken | Take pictures | Pictures | Cook |
| Cooking Help (Middle Right) | Help requested | Request help | Catastrophe, Recipe, Pictures, Help request | Cook |
| Cooking Help (Bottom Left) | Help provided | Request help, Provide help | Help request, Help response | Community Cook, Grandma Avatar |
| Meal Preparation (Bottom Middle) | Step completed, Meal rescued, Meal prepared | Prepare meal | Help response, Recipe | Cook |
| Media (Bottom Right) | Pictures taken | Take pictures | Pictures | Cook |
| Sharing (Bottom Right) | Thanks given | Provide thanks | Help provider, Pictures, Thanks | Cook |

## 3. Map as read

| Node Name (Map) | External / Off-board | Incoming Edges (Source, Payload) | Outgoing Edges (Target, Payload) |
| --- | --- | --- | --- |
| Recipe Catalog | Not stated | None | -> Meal Planning (`Recipe`) |
| Cook Profile | Internal | None | -> Meal Planning (`Cook`), -> Consent Management (`Consent`) |
| Meal Planning | Internal | <- Recipe Catalog (`Recipe`), <- Cook Profile (`Cook`), <-> Cooking Assistance (`Help request` / `Help response`) | <-> Cooking Assistance (`Help request` / `Help response`) |
| Cooking Assistance | Internal | <-> Meal Planning, <-> Meal Preparation, <- Media (`Pictures`), <-> Grandma Avatar AI | -> Notification (`Help request` / `Help response`), <-> Grandma Avatar AI |
| Grandma Avatar AI | Internal (via ACL) | <-> Cooking Assistance (`Help request` / `Help response` via ACL) | <-> Cooking Assistance |
| Notification | Internal | <- Cooking Assistance (`Help request` / `Help response`) | None |
| Consent Management | Internal | <- Cook Profile (`Consent`), <- Meal Preparation (`Consent`), <- Sharing (`Consent`) | None |
| Meal Preparation | Internal | <-> Cooking Assistance (`Help request` / `Help response`) | -> Sharing (`Help response`), -> Consent Management (`Consent`) |
| Media | Internal | None | -> Cooking Assistance (`Pictures`), -> Sharing (`Pictures`) |
| Sharing | Internal | <- Media (`Pictures`), <- Meal Preparation (`Help response`) | -> Consent Management (`Consent`) |

## 4. Term ledger

| Term | Written by (Board) | Read by (Board) | Map says owned by | Verdict |
| --- | --- | --- | --- | --- |
| Cook | Cook Profile | Meal Planning, Cooking Assistance, Meal Prep, Media, Sharing | Cook Profile | Consistent |
| Recipe Catalog | *nobody* | Meal Planning | Recipe Catalog | **Off-board upstream missing on board → NODE/EXT** |
| Recipe | *nobody* | Meal Planning, Meal Prep, Cooking Help | Recipe Catalog / Meal Planning | **Off-board upstream missing on board** |
| Help request | Cooking Assistance, Cooking Help | Cooking Assistance, Cooking Help | Cooking Assistance | **COLL + TERM** (Contexts merged) |
| Help response | Cooking Assistance, Cooking Help | Meal Planning, Meal Preparation | Cooking Assistance | **COLL + TERM** |
| Pictures | Media | Cooking Help, Sharing | Media | Consistent |
| Grandma Avatar | *nobody* (Actor sticky) | Cooking Assistance, Cooking Help | Grandma Avatar AI | **OWN / EXT** (Actor promoted to Context Node) |
| Consent | *nobody* | *nobody* | Consent Management | **GAP / NODE** (No evidence on board) |
| Notification | *nobody* | *nobody* | Notification | **GAP / NODE** (No evidence on board) |
| Thanks | Sharing | Sharing | Sharing | Consistent |

## 5. Node reconciliation

| Board Bubble(s) | Occurrences | Map Node | Verdict |
| --- | --- | --- | --- |
| Cook Profile | 1 | Cook Profile | Matched |
| Meal Planning | 2 | Meal Planning | Matched (Collapsed correctly) |
| Cooking Assistance | 2 | Cooking Assistance | Matched |
| Cooking Help | 2 | Cooking Assistance | **COLL / TERM**: Merged into `Cooking Assistance` |
| Meal Preparation | 2 | Meal Preparation | Matched (Collapsed correctly) |
| Media | 2 | Media | Matched (Collapsed correctly) |
| Sharing | 1 | Sharing | Matched |
| *(None)* | 0 | Recipe Catalog | **NODE**: Promoted from Read Model sticky |
| *(None)* | 0 | Grandma Avatar AI | **EXT / NODE**: Promoted from Actor sticky to ACL-isolated Node |
| *(None)* | 0 | Notification | **NODE**: Zero stickies on board |
| *(None)* | 0 | Consent Management | **NODE**: Zero stickies on board |

## 6. Findings

* **F1 · `OWN` / `EXT` (Blocking) · Grandma Avatar AI promoted from Actor to Bounded Context with ACL**
* *Evidence:* On the board, `Grandma Avatar` is an actor sticky inside `Cooking Assistance` and `Cooking Help`. On the map, `Grandma Avatar AI` is drawn as a distinct Bounded Context interacting via an Anti-Corruption Layer (ACL).
* *Why it matters:* Isolating an actor persona into a distinct bounded context behind an ACL implies a third-party or legacy integration with separate translation boundaries, fundamentally altering system complexity.
* *Recommendation (Ask):* Clarify whether Grandma Avatar is an external AI service requiring an ACL or an internal capability of Cooking Assistance.


* **F2 · `NODE` (Significant) · `Consent Management` and `Notification` nodes have no board evidence**
* *Evidence:* `Consent Management` and `Notification` exist as nodes on the map. No event, command, or read model sticky for consent or notifications appears on the EventStorming board.
* *Why it matters:* The map asserts system boundaries for domain areas that were never modeled or validated by the team during the discovery session.
* *Recommendation (Back to the wall):* Run a targeted EventStorming pass for consent tracking and notifications before solidifying these borders on the map.


* **F3 · `COLL` / `TERM` (Significant) · `Cooking Help` merged into `Cooking Assistance**`
* *Evidence:* The board explicitly divides help into `Cooking Assistance` (planning phase) and `Cooking Help` (real-time preparation/catastrophe phase). The map merges both into `Cooking Assistance`.
* *Why it matters:* Real-time emergency help during meal preparation has distinct availability and latency demands compared to asynchronous planning help. Merging them hides this model boundary.
* *Recommendation (Map moves):* Split `Cooking Assistance` into two nodes or document the non-functional SLAs that justify a single shared context.


* **F4 · `NODE` / `EXT` (Significant) · `Recipe Catalog` promoted from read model to upstream Node**
* *Evidence:* `Recipe Catalog` exists on the board strictly as a green Read Model sticky inside `Meal Planning`. The map draws it as a dedicated upstream Bounded Context.
* *Why it matters:* If Recipe Catalog is a separate context, its write-model and domain events were omitted from the board.
* *Recommendation (Back to the wall):* Mark `Recipe Catalog` as an external/off-board system on the map and add an external system sticky to the board.


* **F5 · `PAT` / `CONTRACT` (Minor) · Unlabeled Conformist (CF) borders without explicit contracts**
* *Evidence:* Several edges rely on generic `CF` boxes (e.g., Cook Profile -> Meal Planning) without explicit relationship pattern labels or defined payload contracts.
* *Why it matters:* Unlabeled conformist edges leave upstream/downstream guarantees ambiguous.
* *Recommendation (Map moves):* Add explicit `U`/`D` markers and document payload schemas across CF borders.



## 7. Undrawn edges and missing nodes

* **Missing Upstream (`Recipe` & `Ingredients` Supplier):** `Recipe` and `Ingredients` read models are consumed across `Meal Planning`, `Meal Preparation`, and `Cooking Help`, but no write context produces them on the board. `Recipe Catalog` supplies recipes on the map, but an ingredient master context is missing from both artifacts.

## 8. Allowed divergence

* **Timeline Collapsing:** The board draws `Meal Planning`, `Meal Preparation`, `Media`, and `Cooking Help` multiple times across the temporal flow. Collapsing these recurring appearances into single nodes on the map is correct.

## 9. Patch list

* **Edits to the Map:**
1. Distinguish between asynchronous planning assistance and real-time catastrophe intervention (`Cooking Help`).
2. Mark `Recipe Catalog` as an external/dashed upstream node.
3. Revert `Grandma Avatar AI` to an internal persona/actor within Cooking Assistance unless confirmed as an external third-party service.
4. Label `Consent Management` and `Notification` as "Proposed / Out of Scope" until backed by board evidence.


* **Back to the Wall (Edits to the Board):**
1. Add pink stickies for external systems (`Recipe Catalog`, external AI platforms).
2. Storm the event lifecycles for **Consent** (e.g., `Consent granted`) and **Notifications** (e.g., `Notification dispatched`).
3. Identify the producing context for `Ingredients` and `Recipe` write models.



## 10. Already consistent

* **Cook Profile:** Accurately reflects board events (`Cook registered`) and supplies user identity downstream.
* **Meal Planning & Preparation Core:** Core domain events (`Dinner planned`, `Meal plan settled`, `Meal preparation started`, `Catastrophe happened`) align with their respective context groupings on the map.
* **Media & Sharing:** `Media` owns image creation (`Pictures taken`), and `Sharing` handles downstream consumption (`Pictures`) and interaction (`Thanks given`).