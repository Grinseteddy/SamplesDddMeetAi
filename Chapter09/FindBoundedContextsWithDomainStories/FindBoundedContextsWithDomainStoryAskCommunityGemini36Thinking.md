# Prompt 2026-08-31

Create Bounded Contexts based on the provided Domain Story. Use the provided skill.

![](../assets/AskCommunity.jpg)

[Skill](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/SKILL.md)

[Reference Context Map and Classification](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/context-map-and-classification.md)

[Reference Cut Signals](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/cut-signals.md)

[Reference Naming and Granuraliry](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/naming-and-granularity.md)

[Reference Worked Example](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/worked-example.md)

# Answer

# Context Cut — Ask Community

## 1. Story transcription

1. Cook prepares Meal


2. Cook needs Help with Ingredients


3. Cook asks Community for Help with Ingredients


4. Community provides Help with Ingredients Substitutes


5. Cook prepares Meal and takes Pictures


6. Cook thanks Community and shares Pictures with Community



## 2. Signal table

| # | Actor | Activity | Work objects | Purpose | Sense of the shared nouns |
| --- | --- | --- | --- | --- | --- |
| 1 | Cook | prepares | Meal | Starting dish execution | *Meal*: Dish in active preparation

|
| 2 | Cook | needs | Help, Ingredients | Identifying missing ingredients | *Help*: State of blocked preparation; *Ingredients*: Missing recipe items

|
| 3 | Cook | asks | Community, Help, Ingredients | Solicit assistance for missing items | *Help*: Inquiry request; *Ingredients*: Query target

|
| 4 | Community | provides | Help, Ingredients Substitutes | Resolve cooking blocker | *Help*: Advice response; *Ingredients Substitutes*: Alternative ingredients

|
| 5 | Cook | prepares, takes | Meal, Pictures | Complete cooking & document result | *Meal*: Finished dish; *Pictures*: Raw captured visual media artifact

|
| 6 | Cook | thanks, shares | Community, Pictures, Community | Close feedback loop & publish outcome | *Pictures*: Social media post content; *Community*: Social audience

|

## 3. Proposed contexts

**Meal Preparation**

* **Responsibility:** Manages active cooking lifecycles, dish execution, and capturing visual media of completed dishes.
* **Sentences:** 1, 5


* **Actors:** Cook


* **Terms owned:** *Meal*, *Pictures* (as raw media artifacts).
* **Terms borrowed:** None.
* **Justifying signals:** Language shift (*Meal* as preparation vs completed product), Purpose change, Handoff (pauses when ingredients are missing, resumes when substitutes arrive), Work-object cohesion.

**Ingredient Assistance**

* **Responsibility:** Facilitates Q&A workflows for identifying and recommending ingredient substitutions during cooking.
* **Sentences:** 2, 3, 4


* **Actors:** Cook, Community


* **Terms owned:** *Help Request*, *Help Response*, *Ingredients Substitutes*.
* **Terms borrowed:** *Ingredients* (borrowed from recipe definitions, used here as missing component tokens).
* **Justifying signals:** Language shift (*Help* as a problem state vs advisory content), Purpose change (seeking/providing advice), Actor expertise (community crowd knowledge), Consistency need.

**Community Sharing**

* **Responsibility:** Handles peer recognition, social publishing, and community engagement.
* **Sentences:** 6


* **Actors:** Cook, Community


* **Terms owned:** *Social Post*, *Thank-you Note*, *Community Engagement*.
* **Terms borrowed:** *Pictures* (borrowed from Meal Preparation as published content payload).
* **Justifying signals:** Purpose change (social gratitude vs active cooking/troubleshooting), Language shift (*Pictures* shifts from raw photo file to published post content), Actor change.

## 4. Lane assignment

| # | Sentence | Lane |
| --- | --- | --- |
| 1 | Cook prepares Meal

| Meal Preparation |
| 2 | Cook needs Help with Ingredients

| Ingredient Assistance |
| 3 | Cook asks Community for Help with Ingredients

| Ingredient Assistance |
| 4 | Community provides Help with Ingredients Substitutes

| Ingredient Assistance |
| 5 | Cook prepares Meal and takes Pictures

| Meal Preparation |
| 6 | Cook thanks Community and shares Pictures with Community

| Community Sharing |

## 5. Context map

| From | To | What flows | Pattern | Note |
| --- | --- | --- | --- | --- |
| Meal Preparation | Ingredient Assistance | Missing ingredient query (*Help Request*) | Customer/Supplier | Meal preparation is paused until assistance is requested |
| Ingredient Assistance | Meal Preparation | *Ingredients Substitutes* advice | Customer/Supplier | Cooking resumes once substitution advice is received |
| Meal Preparation | Community Sharing | Finished dish photo media | Customer/Supplier | Captured media artifact passed for community sharing |
| Community Sharing | Ingredient Assistance | Gratitude / Resolution signal | Customer/Supplier | Thanking community closes the assistance loop |

## 6. Subdomain classification

* **Ingredient Assistance — Core:** Real-time, contextual crowd sourcing of ingredient substitutions is the primary product differentiator.
* **Meal Preparation — Supporting:** Cooking execution and recipe handling are essential but non-differentiating domain capabilities.
* **Community Sharing — Generic:** Standard social interaction, photo posting, and feedback mechanisms; could be implemented via standard social tools or services.

## 7. Contested calls & alternative cuts

* **Sentence 2 (*Cook needs Help with Ingredients*):** Placed in *Ingredient Assistance* as the trigger state of a help request. Alternatively, it could sit in *Meal Preparation* as an internal state transition ("cooking blocked due to missing ingredient").


* **Sentence 5 (*Cook prepares Meal and takes Pictures*):** Combines meal completion and taking pictures. Taking the picture is placed in *Meal Preparation* as a local media creation event, separating it from the act of social sharing in step 6.


* **Coarser Cut (2 Contexts):** Merge *Ingredient Assistance* and *Community Sharing* into a single *Community Engagement* context. This reduces interface boundaries but mixes real-time decision support (ingredient substitution) with general social feed interactions.
* **Finer Cut (4 Contexts):** Split *Ingredient Assistance* into *Help Request Management* (live Q&A tracking) and *Substitution Knowledge Base* (indexing crowd-sourced substitutes for automated future suggestions).
* **Questions to settle boundaries:** Ask domain experts: "Do we plan to reuse community substitution suggestions automatically for future cooks without creating a new community post?" If yes, *Substitution Knowledge Base* should be carved out as a separate bounded context.