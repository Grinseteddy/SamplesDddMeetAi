# Prompt 2026-08-31

Create Bounded Contexts based on the provided Domain Story. Use the provided skill.

![](../assets/AskGrandma.jpg)

[Skill](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/SKILL.md)

[Reference Context Map and Classification](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/context-map-and-classification.md)

[Reference Cut Signals](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/cut-signals.md)

[Reference Naming and Granuraliry](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/naming-and-granularity.md)

[Reference Worked Example](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/worked-example.md)


# Context Cut — Ask Grandma

## 1. Story transcription

1. Cook prepares Meal


2. Cook burns Meal and takes Pictures


3. Cook asks Grandma Avatar for Help with Catastrophy Pictures


4. Grandma Avatar provides Help to rescue Meal


5. Cook rescues Meal and takes Pictures


6. Cook thanks Grandma Avatar and shares Pictures with Community



---

## 2. Signal table

| # | Actor | Activity | Work objects | What it is *for* | Sense of the shared nouns here |
| --- | --- | --- | --- | --- | --- |
| 1 | Cook

| prepares

| Meal

| Executing standard cooking workflow

| *Meal* = active culinary task / raw preparation

|
| 2 | Cook

| burns, takes

| Meal, Pictures

| Capturing a kitchen mishap / status failure

| *Meal* = damaged dish; *Pictures* = local state evidence

|
| 3 | Cook

| asks

| Grandma Avatar, Help, Catastrophy Pictures

| Requesting targeted advice for dish recovery

| *Help* = advice ticket; *Catastrophy Pictures* = visual diagnostic payload

|
| 4 | Grandma Avatar

| provides

| Help, Meal

| Generating actionable recovery steps

| *Help* = step-by-step fix; *Meal* = rescue target entity

|
| 5 | Cook

| rescues, takes

| Meal, Pictures

| Executing recovery and capturing success

| *Meal* = restored edible dish; *Pictures* = final outcome media

|
| 6 | Cook

| thanks, shares

| Grandma Avatar, Pictures, Community

| Expressing gratitude and publishing outcome

| *Pictures* = public content post; *Community* = social audience

|

---

## 3. Proposed contexts

**Culinary Execution**

* **Responsibility:** Manages physical dish preparation, tracks meal state changes, and carries out recovery tasks.


* **Sentences:** 1, 2, 5


* **Actors:** Cook


* **Terms owned:** *Meal* (in-progress, burnt, or restored dish state), *Pictures* (local progress/result snapshots).


* **Terms borrowed:** None.


* **Justifying signals:** Language shift (*Meal* transitions from raw to burnt to rescued), purpose change (physical execution vs. advice requesting).



**Culinary Advisory**

* **Responsibility:** Evaluates kitchen catastrophe evidence and provides expert intervention instructions.


* **Sentences:** 3, 4


* **Actors:** Cook, Grandma Avatar


* **Terms owned:** *Help*, *Catastrophy Pictures*.


* **Terms borrowed:** *Meal* (referenced target of rescue instructions).


* **Justifying signals:** Actor/expertise change (Grandma Avatar intervention), cohesive noun cluster (*Help* + *Catastrophy Pictures*), purpose change (expert consultation).



**Community & Social Engagement**

* **Responsibility:** Handles public content sharing, community interactions, and advisor recognition.


* **Sentences:** 6


* **Actors:** Cook, Grandma Avatar, Community


* **Terms owned:** *Community*, *Thanks*.


* **Terms borrowed:** *Pictures* (borrowed as publishable content media), *Grandma Avatar* (as recipient of feedback).


* **Justifying signals:** Purpose change (social sharing vs. problem solving), actor change (*Community* introduced).



---

## 4. Lane assignment

| # | Sentence | Lane |
| --- | --- | --- |
| 1 | Cook prepares Meal

| Culinary Execution |
| 2 | Cook burns Meal and takes Pictures

| Culinary Execution |
| 3 | Cook asks Grandma Avatar for Help with Catastrophy Pictures

| Culinary Advisory |
| 4 | Grandma Avatar provides Help to rescue Meal

| Culinary Advisory |
| 5 | Cook rescues Meal and takes Pictures

| Culinary Execution |
| 6 | Cook thanks Grandma Avatar and shares Pictures with Community

| Community & Social Engagement |

---

## 5. Context map

| From | To | What flows | Pattern | Note |
| --- | --- | --- | --- | --- |
| Culinary Execution | Culinary Advisory | *Catastrophy Pictures* / Help Request

| Customer/Supplier | Advisory consumes catastrophe evidence to formulate rescue advice.

|
| Culinary Advisory | Culinary Execution | *Help* (Rescue instructions)

| Customer/Supplier | Execution applies recovery steps to the *Meal* entity.

|
| Culinary Execution | Community & Social Engagement | Rescued *Pictures*<br> | Customer/Supplier | Social context accepts completed dish media for public feed creation.

|
| Community & Social Engagement | Culinary Advisory | Gratitude / Advisor Rating (*Thanks*)

| Customer/Supplier | Feeds feedback to the *Grandma Avatar* advisor profile.

|

---

## 6. Subdomain classification

* **Culinary Advisory — Core:** Differentiating business model (on-demand visual diagnosis and rescue advice from specialized personas/avatars).


* **Culinary Execution — Supporting:** Necessary workflow domain to track cooking state and execute fixes.


* **Community & Social Engagement — Supporting:** Drives engagement, retention, and viral loops via photo sharing and advisor feedback.



---

## 7. Contested calls & alternative cuts

* **Sentence 2 (Cook burns Meal and takes Pictures):** Could sit in *Culinary Advisory* if taking catastrophe pictures is viewed strictly as initiating a help ticket. It is placed in *Culinary Execution* because burning the meal represents a physical state transition during active cooking, while sentence 3 explicitly performs the handoff to advisory.


* **Sentence 6 (Cook thanks Grandma Avatar and shares Pictures with Community):** Performs two acts: thanking the advisor and sharing media with the audience. It could be split, but grouping it in *Community & Social Engagement* keeps post-rescue public interactions cohesive.


* **Coarser cut (2 contexts):** Merge *Culinary Advisory* and *Community & Social Engagement* into a single *Advisor & Community Network* context. This reduces context boundaries but mixes direct assistance workflows with social feed interactions.
* **Finer cut (4 contexts):** Split *Community & Social Engagement* into *Advisor Feedback* (thanking/rating Grandma Avatar) and *Social Feed* (publishing outcome media to the community).


* **Settling Question:** Ask domain experts: *"Is 'Help' generated as an automated response based on catastrophe photo tags, or does it open an active interactive advice session between the Cook and Grandma Avatar?"* If session-based, *Culinary Advisory* should be modeled with explicit session lifecycles.