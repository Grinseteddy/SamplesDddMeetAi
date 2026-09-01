# Prompt 2026-08-31

Create Bounded Contexts based on the provided Domain Story. Use the provided skill.

![](../assets/AskChef.jpg)

[Skill](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/SKILL.md)

[Reference Context Map and Classification](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/context-map-and-classification.md)

[Reference Cut Signals](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/cut-signals.md)

[Reference Naming and Granuraliry](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/naming-and-granularity.md)

[Reference Worked Example](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/worked-example.md)

# Answer

# Context Cut — AskChef

## 1. Story transcription

1. Cook plans Dinner with Parents in Law


2. Cook needs Help for Meal Planning


3. Cook asks Chef for Help


4. Cook/Chef interaction where Chef provides Help to plan Dinner


5. Cook prepares Meal and takes Pictures


6. Cook thanks Chef and shares Pictures with Community



## 2. Signal table

| # | Actor | Activity | Work objects | Purpose in domain terms | Sense of shared nouns |
| --- | --- | --- | --- | --- | --- |
| 1 | Cook | plans | Dinner, Parents in Law | Define upcoming event constraints and dietary goals

| *Dinner* = planned occasion with social constraints |
| 2 | Cook | needs | Help, Meal Planning | Identify knowledge gap for the planned occasion

| *Help* = need for culinary guidance |
| 3 | Cook | asks | Chef, Help | Request specialized advice from an expert

| *Chef* = professional advisor; *Help* = consultation request |
| 4 | Chef | provides | Help, Dinner | Deliver customized advice or recipe recommendations

| *Help* = actionable advice/menu; *Dinner* = topic of advice |
| 5 | Cook | prepares / takes | Meal, Pictures | Execute the recipe and capture media evidence

| *Meal* = physical food prepared; *Pictures* = media artifact |
| 6 | Cook | thanks / shares | Chef, Pictures, Community | Acknowledge assistance and publish results to peers

| *Pictures* = social post content; *Community* = public feed |

## 3. Proposed contexts

**Meal Event Planning**

* **Responsibility:** Capturing meal occasions, dietary requirements, guest preferences, and planning goals.
* **Sentences:** 1, 2
* **Actors:** Cook
* **Terms owned:** *Dinner* (as an event model), *Parents in Law*, *Meal Planning*.
* **Signals:** Language shift (*Dinner* as a planned occasion), Purpose change (defining constraints vs. requesting help).

**Chef Consultation**

* **Responsibility:** Managing advisory interactions, expert matching, and advice delivery between cooks and chefs.
* **Sentences:** 3, 4
* **Actors:** Cook, Chef
* **Terms owned:** *Chef*, *Help* / *Advice* (consultation model).
* **Terms borrowed:** *Dinner* (referenced as the target subject of advice).
* **Signals:** Actor/expertise change (Chef), Purpose change (seeking expert solutions), Work-object cohesion (*Chef* + *Help*).

**Meal Execution**

* **Responsibility:** Handling physical cooking preparation workflows and progress artifact generation.
* **Sentences:** 5
* **Actors:** Cook
* **Terms owned:** *Meal* (physical dish execution), *Pictures* (raw media capture).
* **Signals:** Handoff / pivotal moment (transition from advice to physical kitchen work), Language shift (*Dinner* event becomes *Meal* dish).

**Community Engagement**

* **Responsibility:** Managing public feeds, peer sharing, expert appreciation, and social interaction.
* **Sentences:** 6
* **Actors:** Cook, Chef, Community
* **Terms owned:** *Community*, *Social Post* / *Appreciation*.
* **Terms borrowed:** *Pictures* (re-modeled as shared social content), *Chef* (re-modeled as recipient of feedback/thanks).
* **Signals:** Actor change (Community), Purpose change (social proof and relationship closure).

## 4. Lane assignment

| # | Sentence | Lane |
| --- | --- | --- |
| 1 | Cook plans Dinner with Parents in Law

| Meal Event Planning |
| 2 | Cook needs Help for Meal Planning

| Meal Event Planning |
| 3 | Cook asks Chef for Help

| Chef Consultation |
| 4 | Chef provides Help to plan Dinner

| Chef Consultation |
| 5 | Cook prepares Meal and takes Pictures

| Meal Execution |
| 6 | Cook thanks Chef and shares Pictures with Community

| Community Engagement |

## 5. Context map

| From | To | What flows | Pattern | Note |
| --- | --- | --- | --- | --- |
| Meal Event Planning | Chef Consultation | Planning requirements (occasion, guest rules) | Customer/Supplier | Upstream establishes the context for advice |
| Chef Consultation | Meal Execution | Advice / Menu plan (instructions) | Customer/Supplier | Downstream executes advice provided by expert |
| Meal Execution | Community Engagement | Media artifact (completed dish picture) | Customer/Supplier | Raw photo transformed into a social asset |
| Community Engagement | Chef Consultation | Feedback & Gratitude | Separate Ways / Event-driven | Simple feedback loop back to expert |

## 6. Subdomain classification

* **Chef Consultation — Core:** The primary value proposition and differentiator connecting home cooks directly to expert chefs.
* **Meal Event Planning — Supporting:** Necessary context builder that structures user requests prior to expert engagement.
* **Meal Execution — Supporting:** Captures real-world completion and enables proof of success.
* **Community Engagement — Generic / Supporting:** Standard social sharing and feedback functionality.

## 7. Contested calls & alternative cuts

* **Sentence 2 (*needs Help for Meal Planning*):** Placed in *Meal Event Planning* because it captures the intent gap within the planning phase. **Alternative:** Move to *Chef Consultation* as the initiating trigger event of the advisory session.
* **Sentence 6 (*thanks Chef and shares Pictures with Community*):** Combines two distinct activities: direct chef gratitude and public social broadcasting. Placed in *Community Engagement* to anchor the social feed aspect. **Alternative:** Split into two separate steps (Step 6a: *Cook thanks Chef* in *Chef Consultation*, Step 6b: *Cook shares Pictures* in *Community Engagement*).
* **Coarser cut (3 Contexts):** Merge *Meal Event Planning* and *Meal Execution* into a single *Meal Management* context covering pre-cook and post-cook phases.
* **Finer cut (5 Contexts):** Split *Community Engagement* to decouple private expert feedback (*Chef Ratings & Feedback*) from public social activity (*Community Feed*).
* **Settling Questions:**
1. Is "Help" provided as a structured digital asset (like a generated recipe/shopping list) or an unstructured advisory conversation?
2. Is thanking the Chef tied directly to rating/tipping the expert, or is it purely a social gesture on the public feed?