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

**Mode: propose.** The Domain Story has no existing lanes, so I’m proposing candidate subdomains and, by default, one bounded context per subdomain. The skill treats bounded contexts as **language/model boundaries rather than timeline phases**, so recurring steps may belong to the same context even when they are separated in the sequence.  A Domain Story lane is most naturally a problem-space subdomain; implementing one bounded context per lane is a useful default, but still a design decision.

## 1. Story transcription

I read the diagram as:

1. **Cook prepares Meal**
2. **Cook needs Help with Ingredients**
3. **Cook asks Community for Help with Ingredients**
4. **Community provides Help with Ingredient Substitutes**
5. **Cook prepares Meal and takes Pictures**
6. **Cook thanks Community and shares Pictures with Community**

The important detail is that steps 5 and 6 introduce **Pictures**, while steps 2–4 revolve around the lifecycle of **Help**. Per the supplied method, unnumbered arrows continue the numbered sentence rather than becoming independent steps.

## 2. Signal table

| # | Actor     | Activity         | Work objects                 | Domain purpose                             | Important noun sense                                             |
| - | --------- | ---------------- | ---------------------------- | ------------------------------------------ | ---------------------------------------------------------------- |
| 1 | Cook      | prepares         | Meal                         | Produce the meal                           | **Meal** = cooking outcome                                       |
| 2 | Cook      | needs            | Help, Ingredients            | Resolve a cooking problem                  | **Help** = unmet assistance need                                 |
| 3 | Cook      | asks             | Community, Help, Ingredients | Obtain outside knowledge                   | **Help** = request for assistance                                |
| 4 | Community | provides         | Help, Ingredient Substitutes | Supply a way around the ingredient problem | **Help** = advice/solution; **Ingredients** = replaceable inputs |
| 5 | Cook      | prepares / takes | Meal, Pictures               | Finish the meal and record the result      | **Meal** = finished outcome; **Pictures** = record of result     |
| 6 | Cook      | thanks / shares  | Community, Pictures          | Close the social loop and show the result  | **Pictures** = community-facing contribution                     |

The strongest cut signals here are **purpose change**, **work-object cohesion**, and the differing lifecycle of the shared nouns. Those are explicitly among the method's principal boundary signals.

## 3. Proposed bounded contexts

### A. Meal Preparation

**Responsibility:** Turn available ingredients and substitutions into a prepared meal.

**Sentences:** **1, 5**

**Actors:** Cook

**Owns / models:**

* Meal
* preparation state
* ingredients actually used

**Borrows:**

* Ingredient Substitutes supplied by Community Assistance

**Why this boundary:** Steps 1 and 5 share exactly the same domain purpose and vocabulary: the Cook is producing the **Meal**. Importantly, they recur rather than forming one contiguous time slice—exactly the kind of evidence the skill says to look for.

---

### B. Community Assistance

**Responsibility:** Turn a cooking-help need into usable ingredient-substitution advice from the community.

**Sentences:** **2, 3, 4**

**Actors:** Cook, Community

**Owns / models:**

* Help need / Help request
* Help response
* Ingredient Substitutes
* association between a request and its answers

**Borrows:**

* Ingredients, but specifically as the *subject of a problem*, rather than as the ingredients currently being manipulated during meal preparation.

**Why this boundary:** These three steps form a clear lifecycle:

**need → request → response**

The business purpose is not “prepare the meal”; it is “resolve the cook's ingredient problem through community knowledge.” Step 4 is also a handoff back toward Meal Preparation because the resulting **Ingredient Substitutes** can now be used there.

---

### C. Community Sharing & Appreciation

**Responsibility:** Let the cook acknowledge community help and share the successful outcome back with the community.

**Sentence:** **6**
Step **5** touches this context through creation of the Pictures, which makes it the main contested sentence.

**Actors:** Cook, Community

**Owns / models:**

* Thank-you / acknowledgement
* shared contribution
* Pictures *as community-facing content*

**Borrows:**

* Pictures of the Meal
* possibly the previous Help interaction being acknowledged

**Why separate:** The purpose has shifted again. The cook is no longer solving an ingredient problem and is no longer primarily preparing food. The concern is now **reciprocity and sharing with the community**.

A single-sentence context is not automatically wrong: the supplied naming/granularity guidance explicitly allows it when the sentence represents a genuinely distinct concern with its own language and purpose.

### Proposed context structure

```text
┌───────────────────────┐
│   Meal Preparation    │
│       #1, #5          │
└──────────┬────────────┘
           │ cooking problem
           ▼
┌───────────────────────┐
│ Community Assistance  │
│      #2, #3, #4       │
└──────────┬────────────┘
           │ ingredient substitutes
           ▼
┌───────────────────────┐
│   Meal Preparation    │
│          #5           │
└──────────┬────────────┘
           │ pictures / outcome
           ▼
┌──────────────────────────────┐
│ Community Sharing &          │
│ Appreciation                 │
│             #6               │
└──────────────────────────────┘
```

The recurrence of **Meal Preparation** is intentional; contexts should not merely correspond to chronological phases.

## 4. Lane assignment — redraw specification

| # | Sentence                                                 | Bounded context                      |
| - | -------------------------------------------------------- | ------------------------------------ |
| 1 | Cook prepares Meal                                       | **Meal Preparation**                 |
| 2 | Cook needs Help with Ingredients                         | **Community Assistance**             |
| 3 | Cook asks Community for Help with Ingredients            | **Community Assistance**             |
| 4 | Community provides Help with Ingredient Substitutes      | **Community Assistance**             |
| 5 | Cook prepares Meal and takes Pictures                    | **Meal Preparation** ★               |
| 6 | Cook thanks Community and shares Pictures with Community | **Community Sharing & Appreciation** |

That assignment is the form intended to be directly redrawable as lanes in the Domain Story.

## 5. Context map

| Upstream             | Downstream                       | What crosses the boundary             | Likely relationship                         |
| -------------------- | -------------------------------- | ------------------------------------- | ------------------------------------------- |
| Meal Preparation     | Community Assistance             | Ingredient problem / need for help    | Customer–Supplier                           |
| Community Assistance | Meal Preparation                 | Ingredient Substitutes / advice       | Customer–Supplier                           |
| Meal Preparation     | Community Sharing & Appreciation | Pictures / finished-meal outcome      | Customer–Supplier                           |
| Community Assistance | Community Sharing & Appreciation | Prior help interaction to acknowledge | Customer–Supplier, if explicitly referenced |

The relationship terminology follows the supplied context-map guidance: identify **what flows**, its direction, and then the relationship pattern.

I would **not** introduce a Shared Kernel here. These contexts can exchange small representations—e.g. substitute suggestions or picture references—without jointly owning the same domain model. The reference cautions that a Shared Kernel creates significant coupling and should only cover a genuinely small, stable jointly owned model.

## 6. Contested calls and alternatives

### ★ Step 5 is the main contested sentence

**“Cook prepares Meal and takes Pictures.”**

There are two different purposes inside one numbered sentence:

* **prepares Meal** clearly belongs to **Meal Preparation**;
* **takes Pictures** exists largely because those Pictures are consumed in step 6 by **Community Sharing & Appreciation**.

I would keep step 5 in **Meal Preparation** because the sentence's primary activity repeats step 1 exactly and because the picture records the outcome produced there. Community Sharing then consumes that artifact.

However, this is also a sign that the Domain Story itself could be made more expressive. In a future workshop I would ask whether “takes Pictures” is genuinely part of cooking or whether it is the first action of sharing the outcome. I would **not silently split it into 5a/5b**, because the supplied skill explicitly warns against inventing steps that the story did not contain.

### Coarser alternative: 2 contexts

You could merge **Community Assistance** and **Community Sharing & Appreciation** into:

**Community Interaction — #2, #3, #4, #6**

That gives:

* **Meal Preparation:** 1, 5
* **Community Interaction:** 2, 3, 4, 6

This is viable if requests, answers, thanks, and picture sharing are all treated as one community-conversation model.

The cost is that **Community Interaction** now has two purposes: *solve a cooking problem* **and** *socially acknowledge/share the result*. The granularity guidance treats an “and” in the responsibility statement as a warning that the lane may be too broad.

### Finer alternative: 4 contexts

If the product grows substantially, I could imagine:

* **Meal Preparation**
* **Help Request Management**
* **Community Advice**
* **Community Sharing**

I would **not make that cut yet**. With only six sentences, separating requesting from answering would likely produce contexts that share the same purpose, vocabulary, lifecycle, and consistency needs. The supplied guidance considers that evidence that the contexts are too small.

## Recommended cut

For this Domain Story, I would start with **three bounded contexts**:

**Meal Preparation** → prepares the meal
**Community Assistance** → obtains and supplies ingredient-substitution help
**Community Sharing & Appreciation** → closes the community loop by thanking and sharing the outcome

The key domain seam is not **Cook vs. Community**. The same actors participate across boundaries. It is the change in meaning and purpose from **making the meal → resolving a help request → sharing/acknowledging the result**. That follows the skill's central principle that contexts should be based on domain language and responsibility rather than actors or chronology. 
