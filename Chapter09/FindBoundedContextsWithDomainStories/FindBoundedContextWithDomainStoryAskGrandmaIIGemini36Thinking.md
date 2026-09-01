# Prompt 2026-08-31

Create Bounded Contexts based on the provided Domain Story. Use the provided skill.

![](../assets/AskGrandmaII.jpg)

[Skill](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/SKILL.md)

[Reference Context Map and Classification](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/context-map-and-classification.md)

[Reference Cut Signals](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/cut-signals.md)

[Reference Naming and Granuraliry](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/naming-and-granularity.md)

[Reference Worked Example](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/worked-example.md)

# Answer

# Context Cut — Ask Grandma II

**Mode:** Propose a cut (no lanes or groups drawn in the input story).

---

## 1. Story transcription

1. Cook prepares Meal


2. Cook needs Help for Meal Preparation Step


3. Cook asks Grandma Avatar for Help with Meal Preparation Step


4. Grandma Avatar provides Help with Meal Preparation Step


5. Cook prepares Meal and takes Pictures


6. Cook thanks Grandma Avatar and shares Pictures with Community



---

## 2. Signal table

| # | Actor | Activity | Work objects | What it is *for* (purpose in domain terms) | Sense of the shared nouns here |
| --- | --- | --- | --- | --- | --- |
| 1 | Cook | prepares | Meal | Executing/starting a culinary recipe

| *Meal* = active cooking process / dish in progress

|
| 2 | Cook | needs | Help, Meal Preparation Step | Identifying a gap or obstacle in a cooking step

| *Help* = technical culinary guidance needed

|
| 3 | Cook | asks | Grandma Avatar, Help, Meal Preparation Step | Requesting targeted advice from an expert assistant

| *Help* = prompt/query; *Grandma Avatar* = interactive domain assistant

|
| 4 | Grandma Avatar | provides | Help, Meal Preparation Step | Resolving a culinary obstacle with instructions

| *Help* = instructional guidance delivered

|
| 5 | Cook | prepares, takes | Meal, Pictures | Completing the dish and recording visual output

| *Meal* = finished dish; *Pictures* = captured visual media asset

|
| 6 | Cook | thanks, shares | Grandma Avatar, Pictures, Community | Expressing feedback and publishing work to peers

| *Pictures* = social content post; *Community* = social audience

|

---

## 3. Proposed contexts

### Meal Execution

* **Responsibility:** Manages the step-by-step lifecycle of cooking a meal from initiation to finished dish and media capture.


* **Sentences:** 1, 5 (non-contiguous, wrapping around the assistance loop).


* **Actors:** Cook.


* **Terms owned:** `Meal`, `Pictures` (as visual capture of completed meal).


* **Terms borrowed:** None.


* **Justification:** Purpose change (executing a recipe vs. seeking advice) and language shift (`Meal` progresses from process to finished product).



### Culinary Assistance

* **Responsibility:** Resolves cooking obstacles by providing targeted, step-specific guidance through an avatar persona.


* **Sentences:** 2, 3, 4.


* **Actors:** Cook, Grandma Avatar.


* **Terms owned:** `Help`, `Meal Preparation Step`, `Grandma Avatar` (as assistance provider).


* **Terms borrowed:** `Meal` (referenced as context for the step).


* **Justification:** High work-object cohesion (`Help` + `Meal Preparation Step` + `Grandma Avatar`), distinct purpose (problem-solving), and actor change (Grandma Avatar).



### Community Engagement

* **Responsibility:** Facilitates social interaction, community sharing, and user recognition/feedback.


* **Sentences:** 6.


* **Actors:** Cook, Grandma Avatar, Community.


* **Terms owned:** `Community`.


* **Terms borrowed:** `Pictures` (shifted from local media capture to published social post), `Grandma Avatar` (as recipient of feedback/thanks).


* **Justification:** Language shift (`Pictures` as social feed item), actor change (`Community`), and purpose change (social distribution and community engagement).



---

## 4. Lane assignment (Redraw Spec)

| # | Sentence | Lane |
| --- | --- | --- |
| 1 | Cook prepares Meal | Meal Execution |
| 2 | Cook needs Help for Meal Preparation Step | Culinary Assistance |
| 3 | Cook asks Grandma Avatar for Help with Meal Preparation Step | Culinary Assistance |
| 4 | Grandma Avatar provides Help with Meal Preparation Step | Culinary Assistance |
| 5 | Cook prepares Meal and takes Pictures | Meal Execution |
| 6 | Cook thanks Grandma Avatar and shares Pictures with Community | Community Engagement |

---

## 5. Context map

| From | To | What flows | Pattern | Note |
| --- | --- | --- | --- | --- |
| **Meal Execution** | **Culinary Assistance** | Meal context & specific step requiring help | Customer/Supplier | Assistance context relies on step info from ongoing meal execution.

|
| **Culinary Assistance** | **Meal Execution** | Help instructions / step resolution | Customer/Supplier | Meal execution resumes once guidance is received.

|
| **Meal Execution** | **Community Engagement** | Completed meal media (`Pictures`) | Customer/Supplier | Meal Execution produces the photo assets; Community Engagement publishes them.

|

---

## 6. Subdomain classification

* **Culinary Assistance — Core:** Interactive, instant step guidance ("Ask Grandma") is the key differentiating feature of the domain.


* **Meal Execution — Supporting:** Necessary operational logic to track recipe progress and completion.


* **Community Engagement — Supporting / Generic:** Standard social feed, media sharing, and interaction features.



---

## 7. Contested calls & alternative cuts

* **Sentence 5 (`and takes Pictures`)**: Could sit in *Community Engagement* if photo capture is strictly a step in the post-publishing workflow. Placed in *Meal Execution* because taking photos of the finished dish marks the completion of cooking before any sharing action takes place.


* **Sentence 2 (`Cook needs Help`)**: Could be seen as an internal state within *Meal Execution*. Placed in *Culinary Assistance* because it acts as the explicit trigger for the help-seeking workflow.


* **Coarser Cut (2 Contexts):** Merge *Culinary Assistance* and *Meal Execution* into a single **Meal & Assistance Management** context. This simplifies the model but mixes execution tracking with interactive assistance logic.


* **Finer Cut (4 Contexts):** Split *Community Engagement* into **Social Feed** (sharing photos with Community) and **Assistant Feedback** (thanking Grandma Avatar).


* **Settling Question:** Is "Grandma Avatar" an AI-driven system agent within the assistance flow, or a distinct user/entity profile that receives separate social notifications and feedback?