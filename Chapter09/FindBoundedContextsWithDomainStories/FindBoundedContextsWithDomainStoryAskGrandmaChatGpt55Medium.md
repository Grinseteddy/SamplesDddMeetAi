# Prompt 2026-08-31

Create Bounded Contexts based on the provided Domain Story. Use the provided skill.

![](../assets/AskGrandma.jpg)

[Skill](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/SKILL.md)

[Reference Context Map and Classification](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/context-map-and-classification.md)

[Reference Cut Signals](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/cut-signals.md)

[Reference Naming and Granuraliry](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/naming-and-granularity.md)

[Reference Worked Example](../../Chapter06/Skills/ProposeBoundedContextsForADomainStory/references/worked-example.md)

# Answer

# Context Cut — Ask Grandma

**Mode: propose.** The story has no bounded-context lanes drawn, so I’m proposing a cut. I’m treating bounded contexts as **language/responsibility boundaries rather than chronological phases**; the supplied skill explicitly warns against slicing a story simply into consecutive steps.

## 1. Story transcription

I read the diagram as:

1. **Cook prepares Meal**
2. **Cook burns Meal and takes Pictures**
3. **Cook asks Grandma Avatar for Help with Catastrophy Pictures**
4. **Grandma Avatar provides Help to rescue Meal**
5. **Cook rescues Meal and takes Pictures**
6. **Cook thanks Grandma Avatar and shares Pictures with Community**

There are no existing group labels, so this is a fresh cut.

## 2. Signal table

The main evidence is purpose, shifts in the meaning/use of shared nouns, actor/expertise changes, handoffs, and work-object cohesion. The supplied method recommends looking for at least a couple of signals for cohesion and roughly three for a credible boundary.

| # | Actor          | Activity       | Work objects                               | Domain purpose                                                    | Meaning of shared nouns                                                   |
| - | -------------- | -------------- | ------------------------------------------ | ----------------------------------------------------------------- | ------------------------------------------------------------------------- |
| 1 | Cook           | prepares       | Meal                                       | Produce the intended meal                                         | **Meal** = food being prepared                                            |
| 2 | Cook           | burns, takes   | Meal, Pictures                             | Detect/document that cooking went wrong                           | **Meal** = failed cooking outcome; **Pictures** = evidence of the problem |
| 3 | Cook           | asks           | Grandma Avatar, Help, Catastrophy Pictures | Obtain expert guidance for the cooking problem                    | **Pictures** = diagnostic input; **Help** = requested advice              |
| 4 | Grandma Avatar | provides       | Help, Meal                                 | Give advice enabling recovery                                     | **Help** = actionable cooking guidance; **Meal** = object to be recovered |
| 5 | Cook           | rescues, takes | Meal, Pictures                             | Recover the meal and document the result                          | **Meal** = recovered outcome; **Pictures** = evidence/result              |
| 6 | Cook           | thanks, shares | Grandma Avatar, Pictures, Community        | Close the assistance interaction and publish the outcome socially | **Pictures** = shareable story/content rather than diagnostic evidence    |

A particularly useful signal is **Pictures**. The same object changes meaning across the story: diagnostic evidence in steps 2–3, evidence of the recovered outcome in step 5, and community content in step 6. That is exactly the kind of language/model shift the skill treats as the strongest boundary evidence.

## 3. Proposed bounded contexts

### A. Meal Recovery

**Responsibility:** Manage the cooking attempt from preparation through failure and successful recovery.

**Sentences:** **1, 2, 5**

**Actors:** Cook

**Owns:**

* **Meal** as a cooking outcome
* its state such as being prepared, burned, or rescued
* the actions required to recover it

**Borrows:**

* **Help** from Grandma Assistance
* potentially the result of that advice, without owning the advice model itself

**Why this is a context:** Steps 1, 2, and 5 have one continuous business purpose: *produce a viable meal despite something going wrong*. Importantly, the context recurs after the assistance interaction rather than forming one uninterrupted timeline block. That recurrence is a healthy bounded-context signal rather than a phase diagram.

I would use **Meal Recovery** rather than simply *Meal*, because the naming guidance says a context should name a domain responsibility, not merely an entity.

---

### B. Grandma Assistance

**Responsibility:** Interpret a cooking problem and provide actionable guidance for rescuing the meal.

**Sentences:** **3, 4**, plus the **“thanks Grandma Avatar”** portion of 6 conceptually.

**Actors:** Cook, Grandma Avatar

**Owns:**

* **Help** / assistance request
* Grandma Avatar's advice or recommendation
* the assistance interaction

**Borrows with shifted meaning:**

* **Catastrophy Pictures** — not general photographs, but diagnostic evidence used to understand the problem
* **Meal** — not something this context cooks; it is the subject/problem the advice concerns

**Why this is separate:** There is a clear purpose and expertise change. The Cook stops performing cooking work and asks a different role, Grandma Avatar, for specialized assistance. Step 4 then hands actionable help back to the cooking activity. Actor/expertise change alone is not enough for a cut, but combined with purpose, vocabulary, and a responsibility handoff it becomes strong evidence.

---

### C. Community Sharing

**Responsibility:** Turn the cooking outcome into content that can be shared with the community.

**Sentence:** **6**, specifically *shares Pictures with Community*.

**Actors:** Cook, Community

**Owns:**

* the act/publication of **sharing**
* the audience/community relationship
* **Pictures** as published/community content

**Borrows with shifted meaning:**

* **Pictures** from the earlier cooking/assistance flow, but now as social content rather than evidence used to diagnose the meal

**Why this is separate:** The business purpose has changed completely. The meal is no longer being prepared or rescued and nobody is diagnosing it. The concern is now communicating the outcome to other people. The meaning of **Pictures** shifts most strongly here.

The naming follows the supplied recommendation to use a noun phrase expressing a recognizable domain responsibility and to avoid technical/channel names.

### Proposed cut

So my primary proposal is **three bounded contexts**:

**Meal Recovery** ↔ **Grandma Assistance** → **Community Sharing**

The important feature is that **Meal Recovery contains 1, 2, and 5**, not simply the first two steps. That avoids the timeline-slicing trap called out explicitly in the skill.

## 4. Lane assignment — redraw specification

| # | Sentence                                                      | Lane                   |
| - | ------------------------------------------------------------- | ---------------------- |
| 1 | Cook prepares Meal                                            | **Meal Recovery**      |
| 2 | Cook burns Meal and takes Pictures                            | **Meal Recovery**      |
| 3 | Cook asks Grandma Avatar for Help with Catastrophy Pictures   | **Grandma Assistance** |
| 4 | Grandma Avatar provides Help to rescue Meal                   | **Grandma Assistance** |
| 5 | Cook rescues Meal and takes Pictures                          | **Meal Recovery**      |
| 6 | Cook thanks Grandma Avatar and shares Pictures with Community | **Community Sharing*** |

* Step 6 is intentionally marked as contested below because its first clause closes the Grandma Assistance interaction while its second clause clearly belongs to Community Sharing.

This lane-assignment table is the part the supplied skill treats as the actual **redraw specification** for putting the story back into lanes.

## 5. Context map

| From                   | To                     | What flows                                 | Relationship                         | Rationale                                                               |
| ---------------------- | ---------------------- | ------------------------------------------ | ------------------------------------ | ----------------------------------------------------------------------- |
| **Meal Recovery**      | **Grandma Assistance** | Problem description + Catastrophy Pictures | Customer/Supplier                    | Assistance needs evidence about the failed meal                         |
| **Grandma Assistance** | **Meal Recovery**      | Help / rescue advice                       | Customer/Supplier                    | Meal Recovery consumes the guidance to recover the meal                 |
| **Meal Recovery**      | **Community Sharing**  | Result Pictures / recovered outcome        | Customer/Supplier                    | Community Sharing publishes evidence produced by the cooking experience |
| **Grandma Assistance** | **Community Sharing**  | Association with Grandma / acknowledgement | Weak handoff; possibly separate ways | Only the “thanks Grandma” part of step 6 links them                     |

The context-map guidance says to make the seam concrete by identifying **what flows, in which direction, and the relationship pattern**.  Customer/supplier is the best initial fit where one context produces something another consumes and there is room for the downstream need to influence the upstream interface.

A simple conceptual map is:

```text
                    catastrophe pictures
        ┌─────────────────────────────────────►
        │
┌─────────────────┐                    ┌───────────────────┐
│  Meal Recovery  │                    │ Grandma Assistance│
│   #1 #2 #5      │◄───────────────────│      #3 #4        │
└────────┬────────┘      rescue help   └───────────────────┘
         │
         │ result pictures
         ▼
┌───────────────────┐
│ Community Sharing │
│        #6         │
└───────────────────┘
```

## 6. Contested calls & alternative cuts

**Step 2 — “burns Meal and takes Pictures.”** The cooking failure itself clearly belongs in **Meal Recovery**, while taking the pictures exists partly because the next context needs diagnostic material. I would keep the complete sentence in Meal Recovery because that is where the pictures originate and because splitting a context merely for “taking pictures” would create a technical/CRUD-like responsibility rather than a domain boundary.

**Step 4 — “provides Help to rescue Meal.”** This could be interpreted as part of Meal Recovery because its purpose is ultimately rescuing the meal. I keep it in **Grandma Assistance** because Grandma owns the advice, while the Cook performs the actual rescue in step 5. This gives a crisp handoff: *advice supplied → advice acted upon*.

**Step 6 — “thanks Grandma Avatar and shares Pictures with Community.”** This is the strongest contested sentence. “Thanks Grandma Avatar” naturally closes **Grandma Assistance**; “shares Pictures with Community” clearly starts/continues **Community Sharing**. If the modeling tool permits, I would actually split this into two numbered activities:

> 6a. Cook thanks Grandma Avatar
> 6b. Cook shares Pictures with Community

That would make the boundary substantially cleaner. I have **not** silently rewritten the original story; this is a suggested refinement because the current sentence bundles two purposes.

### Coarser cut: two contexts

If you want fewer implementation boundaries, merge **Community Sharing** into **Grandma Assistance** and call the resulting context something like **Grandma Interaction**.

You would then have:

**Meal Recovery** — 1, 2, 5
**Grandma Interaction** — 3, 4, 6

I would not prefer this cut. Step 6 introduces **Community**, and Pictures become publishing content rather than diagnostic evidence. The combined context would therefore have to explain both personal cooking assistance **and** social publication, which is a warning sign: the naming guidance says a context whose purpose effectively needs an “and” is often too broad.

### Finer cut: four contexts

A future, richer story might separate:

1. **Meal Preparation**
2. **Meal Recovery**
3. **Grandma Assistance**
4. **Community Sharing**

I would **not split Preparation from Recovery yet**. This six-step story gives too little evidence that preparing a meal and rescuing a failed meal use genuinely different models, teams, change cycles, or consistency requirements. The reference guidance warns against creating tiny contexts that share the same purpose, actors, vocabulary, and lifecycle.

### Questions that would settle the remaining boundaries

The most useful domain-expert questions would be:

* Are the pictures taken after burning the meal **specifically created as input for Grandma**, or would the Cook take them anyway?
* Is **Help** a conversation/advice lifecycle of its own—for example requested, interpreted, answered, accepted—or is Grandma effectively just a feature inside the cooking process?
* Is sharing successful rescue stories with the **Community** a significant product capability with comments, reputation, discovery, etc., or merely a final “share” button?
* Does thanking Grandma affect anything in the domain—reputation, rating, rewards—or is it only conversational behavior?

Those answers could move the design from **2 contexts to 4**, but given only the supplied story, **three bounded contexts is the most defensible cut: Meal Recovery, Grandma Assistance, and Community Sharing.**
