# Prompt 2026-08-23

Find Bounded Contexts on the provided EventStorming result. Use the provided skill.

![](../assets/EventStormingBoardWithoutIcons.jpg)

[SKILL](../Skills/FindBoundedContextsSkill/SKILL.md)

[Pivotal tests](../Skills/FindBoundedContextsSkill/references/pivotal-tests.md)

[Worked example](../Skills/FindBoundedContextsSkill/references/worked-example.md)

# Answer

# Pivotal Event Cut — Interactive Community Cooking

Analyzed the provided EventStorming board to identify pivotal events, sequence transitions, and bounded context candidate boundaries.

## 1. Event Line as Read

| # | Event | Actor / System | Aggregates | Notes |
| --- | --- | --- | --- | --- |
| 1 | Cook registered | User / Cook | User | Scope start / User onboarding

|
| 2 | Dinner planned | Cook, Guests | Menu | Planning phase start

|
| 3 | Recipes searched | Cook | Menu, Recipes | Query action

|
| 4 | Recipe selected | Cook | Recipe | Selection milestone

|
| 5 | Ingredients missing | Cook | Recipe, Ingredients | Exception / Branch

|
| 6 | Meal planning stalled | Cook | Guests, Menu | Exception / Branch

|
| 7 | Ingredients substituted | Cook | Recipe, Ingredients, Help response | Exception resolution

|
| 8 | Plan settled | Cook | Menu, Help response, Meal plan | Commitment point

|
| 9 | Meal preparation started | Cook | Recipe | Execution phase start

|
| 10 | Step unclear | Cook | Recipe | In-kitchen loop

|
| 11 | Help requested | Cook | Help request, Menu, Catastrophe | Cross-cutting trigger

|
| 12 | Help provided | Chef, Community Cook, Grandma Avatar | Help request, Help response | External support loop

|
| 13 | Catastrophe happened | Cook | Catastrophe, Recipe | In-kitchen exception

|
| 14 | Meal rescued | Cook | Help response | In-kitchen exception resolution

|
| 15 | Step completed | Cook | Recipe, Help response | Execution loop

|
| 16 | Meal prepared | Cook | Recipe | Execution phase completion

|
| 17 | Pictures taken | Cook | Pictures | Social phase start

|
| 18 | Thanks given | Cook | Pictures, Help provider, Thanks | Scope end

|

## 2. Screening

| Removed Candidate | Reason for Exclusion |
| --- | --- |
| **1, 18** | Scope boundaries (board start and end edges).

|
| **3** | Technical query / user interaction event.

|
| **5, 6, 7, 10, 11, 12, 13, 14, 15** | Exceptions, branches, or recurring loop events during cooking/help-seeking.

|
| **17** | Auxiliary UI / media capture action within the social feedback phase.

|

*Survivors scored below:* **8 (Plan settled)**, **9 (Meal preparation started)**, **16 (Meal prepared)**.

## 3. Scoring

| # | Event | Phase | Irrev. | Handover | Language | Commit | Clock | Narrow Interface | Verdict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 8 | **Plan settled** | Recipe selection → Cooking staging | Re-planning required to alter | Deliberative mindset → Staging mindset | *Recipe/Menu* → *Meal Plan* | Guests locked in, ingredients committed | Wait period before cooking starts | Settled menu ID, selected recipe IDs, guest count | **Divider**<br> |
| 9 | **Meal preparation started** | Staging → Physical execution | Ingredients opened/heated | Staging → Active kitchen work | *Meal Plan* → *Active Step/Cooking* | Food in progress | Real-time cooking clock starts | Recipe steps, ingredient list | **Milestone** (Adjacent to #8; vetoed by narrow interface redundancy)

|
| 16 | **Meal prepared** | Physical execution → Community sharing | Meal completed | Kitchen cook → Community / Guests | *Active Step* → *Finished Dish* | Service obligation fulfilled | Kitchen timer stops; social interaction opens | Dish status, final recipe ID, helper references | **Divider**<br> |

## 4. The Dividers

* **Divider 1: `Plan settled**` — Marks the transition from deliberative meal design to execution staging. State changes from evaluating choices to committing to a specific menu and schedule.


* **Divider 2: `Meal prepared**` — Marks the point where physical cooking concludes and the social/gratitude feedback phase begins.



## 5. Segments as Candidate Contexts

* **Meal Planning Context** (Events 1–8): Responsible for discovering recipes, managing guest preferences, and producing a finalized meal plan.


* *Aggregates owned:* Menu, Meal plan, Guests.


* *Actors:* User, Cook, Guests.




* **Meal Execution Context** (Events 9–16): Responsible for step-by-step cooking instructions, ingredient substitution handling, and managing in-kitchen exceptions.


* *Aggregates owned:* Recipe, Ingredients, Catastrophe.


* *Actors:* Cook.




* **Social Recognition Context** (Events 17–18): Responsible for photo sharing, community appreciation, and acknowledging external assistance.


* *Aggregates owned:* Pictures, Thanks.


* *Actors:* Cook, Help provider.





| # | Event | Candidate Context | Divider After? |
| --- | --- | --- | --- |
| 1–7 | Cook registered ... Ingredients substituted | Meal Planning | No

|
| 8 | **Plan settled** | Meal Planning | **YES**<br> |
| 9–15 | Meal preparation started ... Step completed | Meal Execution | No

|
| 16 | **Meal prepared** | Meal Execution | **YES**<br> |
| 17–18 | Pictures taken ... Thanks given | Social Recognition | No

|

## 6. Border Contracts

* **Meal Planning → Meal Execution** (on `Plan settled`):
* *Crosses:* Settled Meal Plan ID, Selected Recipe IDs, Servings/Guest Count.


* *Stays upstream:* Rejected recipe options, search query logs, ingredient missing logs during planning.


* *Relationship:* Customer/Supplier; Meal Execution consumes the settled contract.




* **Meal Execution → Social Recognition** (on `Meal prepared`):
* *Crosses:* Completed Meal ID, Recipe ID, Helper IDs who assisted during execution.


* *Stays upstream:* Specific step completion times, catastrophe details, ingredient substitutions made.


* *Relationship:* Published Language; Social Recognition consumes completed dish metrics.





## 7. What the Timeline Cannot See

* **Cross-Cutting Context (Community Assistance / Help System):** The stickies `Help requested`, `Help provided`, `Help response` and actors (`Chef`, `Community Cook`, `Grandma Avatar`) recur across both Planning and Execution. This is a standalone, cross-cutting **Community Support Context** that provides real-time help to multiple phases rather than a single segment on the line.


* **Cross-Cutting Context (User Management):** `Cook registered` at event 1 supplies user identity across all contexts.


* **Straddling Aggregate (`Recipe`):** Appears in Planning, Execution, and Recognition. Should be split into:


1. *Catalog Recipe* (Planning)


2. *Executable Recipe Run* (Execution)


3. *Prepared Dish Record* (Social Recognition)





## 8. Contested Calls & Alternatives

* **Near Miss (`Meal preparation started` vs `Plan settled`):** Both occur close together. `Plan settled` was chosen as the divider because it represents the business commitment (menu locked).


* **Coarser Cut:** Merge Meal Planning and Meal Execution into a single "Home Cooking" context. (Loses operational separation between menu design and real-time kitchen guidance).


* **Questions for Domain Experts:** Does a cook ever start cooking without a settled plan, and does the community help system operate synchronously during cooking vs asynchronously during planning?
