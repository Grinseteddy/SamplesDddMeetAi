# Bounded Context Canvases

Derived from the supplied **Context Map** and **Visual Glossary** on 2026-09-09.

## Roster

| Context | Canvas |
|---|---|
| Recipe Catalog | [recipe-catalog.canvas.md](recipe-catalog.canvas.md) |
| Cook Profile | [cook-profile.canvas.md](cook-profile.canvas.md) |
| Meal Planning | [meal-planning.canvas.md](meal-planning.canvas.md) |
| Grandma Avatar AI | [grandma-avatar-ai.canvas.md](grandma-avatar-ai.canvas.md) |
| Notification | [notification.canvas.md](notification.canvas.md) |
| Cooking Assistance | [cooking-assistance.canvas.md](cooking-assistance.canvas.md) |
| Media | [media.canvas.md](media.canvas.md) |
| Consent Management | [consent-management.canvas.md](consent-management.canvas.md) |
| Meal Preparation | [meal-preparation.canvas.md](meal-preparation.canvas.md) |
| Sharing | [sharing.canvas.md](sharing.canvas.md) |

## Message ledger

The ledger is intentionally conservative about type. A noun-only border payload that the map does not clearly identify as a command, query or event is marked `?`; this avoids manufacturing coupling semantics.

| From | To | Message | Type | Mechanism | Evidence |
|---|---|---|---|---|---|
| Recipe Catalog | Meal Planning | Recipe | ? | synchronous · via OHS | Recipe sticky on the Recipe Catalog → Meal Planning border |
| Meal Planning | Cooking Assistance | Menu | ? | synchronous · via OHS | Menu sticky on the downward Meal Planning → Cooking Assistance border |
| Meal Planning | Cooking Assistance | Ingredients | ? | synchronous · via OHS | Ingredients sticky on the downward Meal Planning → Cooking Assistance border |
| Meal Planning | Cooking Assistance | Recipe | ? | synchronous · via OHS | Recipe read-model sticky on the downward Meal Planning → Cooking Assistance border |
| Cooking Assistance | Meal Planning | Help response | ? | synchronous · via OHS | Help response sticky on the upward Cooking Assistance → Meal Planning border |
| Cooking Assistance | Notification | Help request | ? | asynchronous · via OHS | Help request sticky on the dashed Cooking Assistance → Notification border |
| Cooking Assistance | Notification | Help response | ? | asynchronous · via OHS | Help response sticky on the dashed Cooking Assistance → Notification border |
| Cooking Assistance | Grandma Avatar AI | Help request | ? | asynchronous · via ACL | Help request sticky on the dashed upward border |
| Grandma Avatar AI | Cooking Assistance | Help response | ? | asynchronous · via ACL | Help response sticky on the dashed return border |
| Media | Cooking Assistance | Pictures | ? | synchronous · via SHO | Pictures sticky on the Media → Cooking Assistance border |
| Meal Planning | Meal Preparation | Recipe | ? | synchronous · via SHO | Recipe sticky on the long Meal Planning → Meal Preparation border |
| Meal Planning | Meal Preparation | Menu | ? | synchronous · via SHO | Menu sticky on the long Meal Planning → Meal Preparation border |
| Cooking Assistance | Meal Preparation | Help response | ? | synchronous · via OHS | Help response sticky on the downward Cooking Assistance → Meal Preparation border |
| Meal Preparation | Cooking Assistance | Menu | qry | synchronous · via OHS | green Menu read-model sticky on the upward Meal Preparation → Cooking Assistance border |
| Meal Preparation | Cooking Assistance | Recipe | qry | synchronous · via OHS | green Recipe read-model sticky on the upward Meal Preparation → Cooking Assistance border |
| Meal Preparation | Sharing | Help response | ? | synchronous · via SHO | Help response sticky on the Meal Preparation → Sharing border |
| Media | Sharing | Pictures | ? | synchronous · via OHS | Pictures sticky on the Media → Sharing border |
| Cook Profile | Consent Management | Consent | ? | synchronous · via SHO | Consent sticky on the Cook Profile → Consent Management border |
| Consent Management | Sharing | Consent | ? | synchronous · via CF | Consent sticky on the Consent Management → Sharing border |

## Reconciliation

All 19 ledger rows have exactly one outbound entry on the producer canvas and one inbound entry on the receiver canvas. There are no deliberately invented off-board message counterparts.

## Set-wide findings

- **Strategic classification is `(unknown)` on every canvas** because no Core Domain Chart, business-model classification or evolution source was supplied.
- **Verification metrics are `(unknown)` on every canvas** because neither supplied artifact provides them.
- The map and glossary use several spelling/model variants: **Ingredient / Ingredients**, **Picture / Pictures**, **Help / Help response**, **Help Request / Help request**, and **Grandma Avatar / Grandma Avatar AI**. These are recorded as assumptions/questions rather than normalized away.
- **Ingredients appears as a yellow owned object in both Recipe Catalog and Meal Planning.** That is treated as contested/dual ownership, not automatically reconciled.
- **Meal Preparation shows Recipe and Menu as green read models while the map also draws more than one route by which those concepts can reach it/its neighbor.** The intended source-of-truth contract should be confirmed.
- The Visual Glossary contains concepts that have no clean one-to-one owner on the map (for example Community, Chef, Diner, Course, Step and several specialized Help Request/Help contents). They are not pasted onto every canvas.

## Three questions that would fill the most boxes

1. For each noun-only border payload, what is the actual message type: `cmd`, `qry` or `evt`?
2. What are the intended ownership distinctions for **Ingredients**, **Help request/response**, **Recipe/Menu access during Meal Preparation**, and the glossary-only concepts?
3. What Core Domain Chart / business-model / evolution classification should be applied to the ten contexts?
