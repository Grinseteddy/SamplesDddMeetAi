# Prompt 2026-06-28:

Create a Domain Story as preparation for a workshop session for "Ask Grandma/Grandpa" in the Wardley Map provided. Use the provided skill.

![](../assets/WardleyMapEvolution.jpg)

[Skill](../Skills/DomainStorySeederSkill/SKILL.md)
[References-worked-examples](../Skills/DomainStorySeederSkill/references/worked-examples.md)

# Answer

# Seed domain stories — Ask Grandma/Grandpa

## Source & focus

* **Source:** Cooking/recipe platform Wardley Map.
* **Focus:** "Ask Grandma/Grandpa" component.
* **Granularity:** Coarse-to-medium (capturing the core user interaction and its underlying fulfillment patterns).

### Map Reading & Mining

The **Ask Grandma/Grandpa** component sits high on the value chain under the user anchor **Better cooking skills**, placed firmly in the **Custom** evolution stage. Tracing its downstream dependencies reveals a classic **fulfillment fork**:

* A line runs down to **Specific AI** (Custom), which sits on top of **Gen AI** and **Cloud** (Commodity). This indicates an AI-powered persona or avatar experience.
* Other lines run directly to **Remote meeting** and **Chat** (Commodity), pointing to a live human-to-human connection path.

Because the user interacts directly with either the AI persona or the live human mentor, both are treated as **domain-visible actors**, while deeper technical components (*Gen AI, Cloud*) remain off-stage as infrastructure.

---

## Story 1 — Home cook consults a Grandma avatar (AI Path)

1. Home cook encounters a Cooking difficulty during a Recipe step.
2. Home cook selects a Grandma avatar from the Application.
3. Home cook sends a Question about the Cooking difficulty to the Grandma avatar.
4. Grandma avatar generates a Tailored explanation for the Cooking difficulty.
5. Home cook applies the Tailored explanation to the Dish.

## Story 2 — Home cook consults a live Grandma volunteer (Human Path)

1. Home cook requests a Live mentor for a Recipe step.
2. Platform matches the Home cook with an available Grandma volunteer.
3. Home cook initiates a Remote meeting with the Grandma volunteer.
4. Home cook demonstrates the Cooking difficulty to the Grandma volunteer via Video.
5. Grandma volunteer provides Real-time guidance to the Home cook.

## Story 3 — Avatar gets stuck, escalates to a human (Escalation Variant)

1. Home cook sends a Complex question to the Grandma avatar.
2. Grandma avatar fails the Automated answer for the Complex question.
3. Grandma avatar offers a Live connection to the Home cook.
4. Home cook accepts the Live connection from the Grandma avatar.
5. Grandma avatar routes the Complex question to a Chat session with a Grandma volunteer.
6. Grandma volunteer resolves the Complex question for the Home cook via Chat.

---

## For the session

* **Assumptions to confirm:**
* Is "Grandma/Grandpa" a curated product persona (an AI character), or does the user actually connect to their own family members? (We assume a productized persona/volunteer network).
* Does the platform rely on a pool of real-world volunteers for the live track, or are they paid staff culinary mentors?
* Does the "Specific AI" trigger proactively when a user gets stuck, or must the user explicitly invoke it?


* **Candidate groups / bounded contexts:**
* **Mentor Interaction Context:** (Story 1 & Story 2) Handling the live session UI, chat routing, and video streaming.
* **AI Assistance Context:** (Story 1 & Story 3) Managing the specialized prompts, persona boundaries, and fallback triggers.
* **Volunteer Dispatch Context:** (Story 2 & Story 3) Handling availability, matching logic, and volunteer notifications.


* **Questions for the room:**
* What is the threshold for the AI avatar failing? How does it gracefully realize it "doesn't know" the answer before frustrating the user?
* If no human Grandma volunteers are online during the escalation path (Story 3), what is our fallback experience? Do we create an offline ticket or route them to "Ask chef"?