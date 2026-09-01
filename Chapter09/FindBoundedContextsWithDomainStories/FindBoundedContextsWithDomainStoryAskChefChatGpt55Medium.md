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

**Mode: propose.** The Domain Story has no existing lanes, so I’m proposing candidate subdomains and, by default, corresponding bounded contexts. The skill treats a bounded context as a **language/model boundary rather than a slice of the timeline**, and uses language shift, purpose, actor/expertise, handoffs, work-object cohesion, and ownership as the main evidence.

## 1. Story transcription

I read the diagram as:

1. **Cook plans Dinner with Parents in Law**
2. **Cook needs Help for Meal Planning**
3. **Cook asks Chef for Help**
4. **Chef provides Help to plan Dinner**
5. **Cook prepares Meal and takes Pictures**
6. **Cook thanks Chef and shares Pictures with Community**

I am treating the unnumbered arrows such as *with*, *for*, *to plan*, and *and shares … with* as continuations of the numbered sentence, following the supplied skill.

## 2. Signal table

| # | Actor | Activity         | Work objects              | Domain purpose                                                     | Important noun sense                                                 |
| - | ----- | ---------------- | ------------------------- | ------------------------------------------------------------------ | -------------------------------------------------------------------- |
| 1 | Cook  | plans            | Dinner, Parents in Law    | Decide what dinner to provide for particular guests                | **Dinner** = planned social meal / occasion                          |
| 2 | Cook  | needs            | Help, Meal Planning       | Recognise that planning cannot be completed confidently alone      | **Help** = unmet need for planning guidance                          |
| 3 | Cook  | asks             | Chef, Help                | Obtain expert assistance                                           | **Help** = requested expert advice                                   |
| 4 | Chef  | provides         | Help, Dinner              | Supply expertise so the dinner can be planned                      | **Help** = delivered advice/guidance; **Dinner** = subject of advice |
| 5 | Cook  | prepares / takes | Meal, Pictures            | Turn the plan into an actual meal and capture the result           | **Meal** = prepared outcome; **Pictures** = record of the result     |
| 6 | Cook  | thanks / shares  | Chef, Pictures, Community | Close the interaction with the chef and expose the result socially | **Pictures** = shareable community content                           |

The most useful boundary signal here is **purpose change**. The story moves among deciding what to make, obtaining expert guidance, producing the meal, and sharing the result. Purpose is one of the strongest cut signals and is also what normally yields useful context names.

There is also a meaningful shift in **Help**: in step 2 it is a planning need, in step 3 a request made to an expert, and in step 4 advice actually supplied. That does not necessarily imply three contexts—the states may belong to one consultation lifecycle—but it strongly separates the consultation model from the dinner plan itself. The skill explicitly warns to distinguish a real language shift from states within one lifecycle.

---

# 3. Proposed bounded contexts

I would start with **four**.

### Dinner Planning

**Responsibility:** Decide what dinner should be prepared for the occasion and recognise what planning input is required.

**Primary sentences:** **1, 2**

**Actors:** Cook

**Owns:**

* Dinner Plan
* Meal Planning need
* Guest requirements/context, represented here by *Parents in Law*

**Borrows:**

* **Help** as something required to complete the plan; the detailed consultation lifecycle belongs elsewhere.

**Evidence:** shared planning purpose, common actor, cohesive *Dinner / Meal Planning* language.

This is intentionally called **Dinner Planning**, not *Dinner* or *Meal*, because context names should describe a domain responsibility rather than merely name an entity.

---

### Chef Consultation

**Responsibility:** Manage the request for expert cooking/planning guidance and the advice supplied in response.

**Primary sentences:** **3, 4**

**Actors:** Cook, Chef

**Owns:**

* Help Request
* Chef Advice / Help
* Consultation interaction

**Borrows:**

* **Dinner** as the subject on which advice is requested; it does not own the dinner plan itself.

**Evidence:** strong actor/expertise change, explicit request/response handoff, and a cohesive *asks → provides Help* lifecycle.

The actor change is useful evidence because the Chef brings a different kind of expertise, although actor change alone would not justify the boundary.  The request/response is also a clear handoff between the planning concern and the expert-guidance concern.

---

### Meal Preparation

**Responsibility:** Turn the chosen plan into the actual meal.

**Primary sentence:** **5**

**Actors:** Cook

**Owns:**

* Meal as a prepared/cooked result

**Borrows:**

* The plan produced by Dinner Planning
* Pictures are created here but become important to another concern once used socially.

**Evidence:** clear purpose shift from deciding/advising to actually producing the meal.

A one-sentence context is not automatically wrong. The granularity guidance says it is defensible when the sentence represents a genuinely distinct concern rather than merely an arbitrary step.

---

### Community Sharing

**Responsibility:** Present the cooking outcome to a wider community.

**Primary sentence:** **6**

**Actors:** Cook, Community

**Owns:**

* Shared Pictures / Post, conceptually
* Community-facing representation of the result

**Borrows:**

* **Pictures** created during meal preparation
* **Chef** as someone credited or thanked for the assistance

**Evidence:** new audience, new purpose, and a semantic change in *Pictures*: step 5 treats them as captured evidence of the prepared meal; step 6 treats them as content meant for others.

That language/purpose shift is stronger evidence for a boundary than the fact that steps 5 and 6 happen consecutively. Sequence position itself is explicitly an anti-signal.

---

## 4. Lane assignment

This is the redraw specification:

| # | Sentence                                            | Lane                  |
| - | --------------------------------------------------- | --------------------- |
| 1 | Cook plans Dinner with Parents in Law               | **Dinner Planning**   |
| 2 | Cook needs Help for Meal Planning                   | **Dinner Planning**   |
| 3 | Cook asks Chef for Help                             | **Chef Consultation** |
| 4 | Chef provides Help to plan Dinner                   | **Chef Consultation** |
| 5 | Cook prepares Meal and takes Pictures               | **Meal Preparation**  |
| 6 | Cook thanks Chef and shares Pictures with Community | **Community Sharing** |

The lane-assignment table is important because it turns the proposed boundaries directly back into something that can be redrawn as a grouped Domain Story.

---

# 5. Context map

| Upstream          | Downstream        | What flows                          | Likely pattern          | Reason                                                 |
| ----------------- | ----------------- | ----------------------------------- | ----------------------- | ------------------------------------------------------ |
| Dinner Planning   | Chef Consultation | Planning problem / request for help | Customer/Supplier       | Planning needs expertise from consultation             |
| Chef Consultation | Dinner Planning   | Advice / Help                       | Customer/Supplier       | Planning consumes the chef's guidance                  |
| Dinner Planning   | Meal Preparation  | Dinner plan                         | Customer/Supplier       | Preparation acts on the chosen plan                    |
| Meal Preparation  | Community Sharing | Pictures of prepared meal           | Customer/Supplier       | Sharing consumes an artifact created while preparing   |
| Community Sharing | Chef Consultation | Thanks / acknowledgement            | Customer/Supplier, weak | Feedback returns to the expert who supplied the advice |

The supplied context-map guidance says to identify **what crosses a boundary, its direction, and the relationship pattern**; customer/supplier is the normal starting point when one context produces something another consumes.

Conceptually:

**Dinner Planning**
↔ *request / advice* ↔ **Chef Consultation**

**Dinner Planning**
→ *plan* → **Meal Preparation**
→ *pictures* → **Community Sharing**

and Community Sharing sends *thanks/recognition* back toward Chef Consultation.

---

# 6. The most important contested calls

### Step 4 — “Chef provides Help to plan Dinner”

This is the strongest contested sentence.

**Argument for Dinner Planning:** the business outcome is a better dinner plan, and *Dinner* is the work object being affected.

**Argument for Chef Consultation:** the action is explicitly **provides Help**, completing the request/response lifecycle begun by step 3.

**My placement: Chef Consultation.** The context should own the thing it creates, and step 4 creates the expert advice. Dinner Planning consumes that advice.

The ownership signal supports this: a context that creates a work object normally owns its model, while another context may merely reference or consume it.

---

### Step 5 — “prepares Meal and takes Pictures”

This sentence actually contains **two concerns**:

* preparing the meal;
* capturing pictures that later become shareable content.

I would keep the sentence in **Meal Preparation** because preparing the meal is the dominant business purpose and the pictures originate there.

If photography/content capture grows into a richer part of the business, this sentence is the first candidate for a finer boundary.

---

### Step 6 — “thanks Chef and shares Pictures with Community”

This is even more visibly cross-cutting.

**Chef Consultation argument:** thanking the Chef closes the consultation relationship.

**Community Sharing argument:** the durable work object is the *Pictures*, and the new recipient is the *Community*.

**My placement: Community Sharing.** The community-facing purpose is the stronger differentiator, while *thanks Chef* looks like feedback across the boundary.

This sentence would be worth challenging in a Domain Storytelling workshop because its purpose effectively needs an **“and”**, one of the supplied warning signs that a lane or sentence may be spanning responsibilities.

---

## Alternative cuts

A **coarser three-context cut** would merge **Dinner Planning + Chef Consultation** into something like **Assisted Meal Planning**, leaving:

**Assisted Meal Planning · Meal Preparation · Community Sharing**

That is attractive if asking a chef is simply an implementation detail of planning and will always evolve together with it. What you lose is an explicit model for the request/advice interaction.

A **finer five-context cut** would split picture-taking from Meal Preparation into something like **Meal Documentation** or **Content Capture**. I would **not** do that yet: one partial sentence is too little evidence, and the supplied guidance warns against creating a context for every individual action.

### Questions that would settle the boundaries

The key domain-expert questions are: **Is “asking a chef” a reusable consultation capability independent of planning this particular dinner, or is it simply one activity inside meal planning?** And **are the pictures taken primarily as a personal record of the cooked meal, or specifically because the Cook intends to publish/share them?**

Those answers could legitimately move steps 4–6 without changing the rest of the cut.

### Recommended initial bounded contexts

**Dinner Planning · Chef Consultation · Meal Preparation · Community Sharing**

I would implement those as the initial solution-space bounded contexts as well, with **Chef Consultation ↔ Dinner Planning** kept as the most intentionally designed seam. The skill notes that Domain Story lanes are naturally problem-space subdomains, while making each a separate bounded context is a solution-space design choice; one bounded context per subdomain is the useful default, not a law. 
