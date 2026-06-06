# Prompt 2026-06-06:

Criticize the Impact Map based on the Brainstorming result and Business Model Canvas.
Use the providded skill.
![](../assets/ImpactMapping.jpg)
![](../assets/BrainstormingConvergence.jpg)
![](../assets/BusinessModelCanvas.jpg)
[SKILL.md](../Skills/ImpactMappingSkill/SKILL.md)
[Canvas cross check](../Skills/ImpactMappingSkill/references/canvas-cross-check.md)
[Collaboration cross check](../Skills/ImpactMappingSkill/references/collaboration-cross-check.md)
[Coherence and tracibility](../Skills/ImpactMappingSkill/references/coherence-and-traceability.md)
[Four level checklist](../Skills/ImpactMappingSkill/references/four-level-checklist.md)

# Answer:

Here is a strategic critique of the Impact Map presented in **ImpactMapping.jpg**, analyzed through the lens of your upstream inputs: the brainstorming session in **BrainstormingConvergence.jpg** and the strategic framework in **BusinessModelCanvas.jpg**.

---

## The One-Line Read

If we read the map from left to right as a coherent story, it says:

> *"In order to build **Larder**, a **Single** will do some **Shopping**, which we will support by building **Direct ordering**."*

**The Verdict:** Right now, this map functions more like a visual product backlog or a feature breakdown structure than an actual Impact Map. It tracks *what we want to build* rather than *the business value we want to create* and *the behavioral changes required to get there*.

---

## The Biggest Risks (Load-Bearing Assumptions)

### 1. The Goal is a Product Name, Not a Business Outcome

* **The Issue:** The central goal in **ImpactMapping.jpg** is simply listed as **"Larder"**. Larder is a noun—the name of the app. This is the classic "solution wearing a goal's hat" failure mode.
* **Why it worries me:** Without a measurable business metric (e.g., *"Achieve $10k MRR by Q4"* or *"Reduce household food waste by 20% within 6 months"*), the team has no framework for saying "no." Every feature becomes a good idea, turning the project into a feature factory.
* **How to test/resolve it cheaply:** Ask the stakeholders: *"If we ship every single deliverable on the right, but make $0 and change no one's habits, is Larder a success?"* Use their answer to extract a SMART metric (Metric + Target + Deadline) to anchor the center.

### 2. Impacts are Functional Modules, Not Behavior Changes

* **The Issue:** The "Impact" column lists terms like **Shopping**, **Community**, **Budget**, and **Cooking support**. These are software modules or abstract concepts, not human behaviors.
* **Why it worries me:** Actors cannot "do" a *Cooking support*. An impact must be an observable behavior change (e.g., *Singles cook at home 3x more per week* or *Grocery stores push surplus inventory through our API*). Because these are written as feature categories, the deliverables map directly to them in a circular fashion.
* **How to test/resolve it cheaply:** Force the "Sentence Test." Rephrase every impact using an active verb: *Start doing, stop doing, do more of,* or *do differently*.

---

## Level-by-Level Breakdown

### 📊 Goal (WHY)

* **Critical Flaw:** Completely unmeasured and anchored entirely to a product release rather than a business capability.

### 👥 Actors (WHO)

* **The Good:** The map does a fantastic job of pulling in the **Key Partners** from **BusinessModelCanvas.jpg** (Kitchen ware retailer, Chefs, Grocery store, Photo book service) as actors. This correctly identifies that their behavior needs to change to make the business model work.
* **The Weakness:** The actor **"Cook"** is highly generic and sits right next to specific personas like **"Single"** and **"Young family"**. Aren't the singles and young families the ones cooking? This causes redundant, messy line intersections in the middle of the map.

### ⚡ Impacts (HOW)

* **The Flaw:** In addition to being feature modules, there are zero **negative impacts or blockers** mapped. For example, what if *Grocery stores refuse to sync inventory data*? Mapping defensive behaviors is vital to de-risking the project.

### 📦 Deliverables (WHAT)

* **The Flaw (Circular Logic):** Look at the bottom branch. The Impact is **"Cookbook publishing"** and the Deliverable is... **"Cookbook publishing"**. This is a dead giveaway that the map skipped behavioral analysis and just reverse-engineered a feature list.
* **Lack of Options:** Most impacts only branch out to one or two pre-determined solutions. True impact mapping lists multiple low-cost alternatives so you can prune the expensive ones.

---

## Cross-Check: Map vs. Strategic Sources

### 1. Fidelity to the Business Model Canvas (**BusinessModelCanvas.jpg**)

* **The Dropped Value Proposition:** The canvas lists a crucial, highly monetizable Value Proposition: **"Recipes for specialized nutrition requirements."** This is a massive differentiator for "Singles" and "Young families" who have allergies or specific diets. **This is completely missing from the Impact Map.** It has been ironed out into the generic "Easy-to-find recipes."
* **The Revenue Disconnect:** The canvas counts on **"Monthly member fees"** and **"Ads by partners."** The Impact Map does not show how any of the deliverables (like the *Timer* or *Grandma/Grandpa AI*) will actively convince a user to pay a subscription or interact with partner ads.

### 2. Fidelity to the Brainstorming Session (**BrainstormingConvergence.jpg**)

* **Premature Convergence (The "HiPPO" Effect):** The brainstorming board contains several highly innovative, specific ideas marked in blue stickies, such as **Fridge-photo recipe generation**, **Voice control**, and a **Larder tracker**.
* **The Flattening:** During synthesis, the team flattened these unique selling points into generic boxes. *Fridge-photo recipe generation* became the much less inspiring *AI proposals*, and *Voice control* was buried or dropped entirely under *Cooking support*. The team anchored on the safest, most obvious features (*Timer*, *Cooking club*) instead of the high-value differentiators.
* **Structural Messiness:** The brainstorm split *Content Creation* and *Content Rating* logically. The Impact Map sloppily crams *Content* and *Rating* as deliverables underneath the *Community* impact branch, losing the clear operational distinction established in the brainstorm.

---

## What is Strong About This Map

To be fair, the team successfully avoided an internal-only viewpoint. Including the **Photo book service** and mapping it directly to **Cookbook publishing** shows a great understanding of how a Key Partner creates an ecosystem. The lines also show an attempt to cross-utilize deliverables (e.g., *Meal Chooser* serving both *Easy-to-find recipes* and *Cooking support*), which points to a lean technical architecture.

---

## The Path Forward

To turn this into an actionable, risk-reducing document:

1. **Redefine the center:** Replace "Larder" with a real business goal (e.g., *"Convert 5% of app downloads to premium monthly subscribers by Q3"*).
2. **Fix the dropped value prop:** Pull **"Dietary & Allergen Requirements"** back out of the brainstorm graveyard and create an Impact branch for it. That is what will actually drive the business model forward.
3. **Rewrite the Impacts as Actions:** Change "Shopping" to *"Grocery stores fulfill orders directly through our app interface."* Then ask: *What is the absolute simplest piece of software we can build to test if they will actually do that?*