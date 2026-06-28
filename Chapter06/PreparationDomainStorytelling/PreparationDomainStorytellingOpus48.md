# Prompt 2026-06-28:

Create a Domain Story as preparation for a workshop session for "Ask Grandma/Grandpa" in the Wardley Map provided. Use the provided skill.

![](../assets/WardleyMapEvolution.jpg)

# Answer

# Seed domain stories — "Ask Grandma/Grandpa" (cooking platform)

*Disposable strawman scaffolding for a Domain Storytelling session — concrete, plausible, deliberately a little wrong in spots so the room corrects it. Not ground truth.*

## Source & focus

- **Input:** the cooking-platform Wardley Map (anchor users: Young families, Singles, Home cook, Prospect; needs include *Better cooking skills* and *Support cooking*).
- **Focus component:** **Ask Grandma/Grandpa** — treated as a proposed sibling of the map's existing **Ask chef** and **Ask community** components (Visible, Custom).
- **Why these stories:** the focus component depends *downward* on **Chat / Remote meeting → Specific AI → Gen AI → Cloud**. That dependency on **Specific AI** (a tuned persona) means the same need is fulfillable two ways — a **human mentor** or an **AI "Grandma" avatar** — so the seed is a *fulfillment fork* plus the *escalation* between the paths.
- **Granularity:** coarse-to-medium. **Primary actor:** Home cook.

---

## Story 1 — Home cook asks a Grandma avatar *(AI path — Chat → Specific AI)*

1. Home cook gets stuck on a Step of the Recipe
2. Home cook opens a Chat with the Grandma avatar
3. Home cook asks the Grandma avatar about the Step
4. Grandma avatar explains the Technique to the Home cook
5. Home cook applies the Technique to the Dish
6. Home cook rates the answer from the Grandma avatar

## Story 2 — Home cook asks a human Grandma mentor *(live path — Remote meeting)*

1. Home cook requests a Grandma mentor for the Recipe
2. Platform matches the Home cook with a Grandma volunteer
3. Home cook joins a Remote meeting with the Grandma volunteer
4. Home cook shows the Step to the Grandma volunteer over the Remote meeting
5. Grandma volunteer demonstrates the Technique to the Home cook
6. Home cook thanks the Grandma volunteer for the Recipe

## Story 3 — …the avatar can't help, hand off to a human *(escalation — the key variant)*

1. Home cook asks the Grandma avatar about the Step
2. Grandma avatar cannot answer the Question
3. Grandma avatar offers a Grandma volunteer to the Home cook
4. Home cook accepts the Grandma volunteer
5. Home cook continues the Question with the Grandma volunteer over a Remote meeting

---

## For the session

**The fork that matters (validate this first).** Is "Ask Grandma/Grandpa" a *human* service, an *AI persona*, or *avatar-first with human escalation* (Story 3)? The Specific-AI dependency forces this decision; everything else follows from it.

**Assumptions to confirm** (each is a guess — these are your best discussion prompts):
- "Grandma/Grandpa" is a **persona/brand** for "experienced home cook," **not the user's own family member**. The downstream Specific-AI link is what implies this.
- The **avatar** is a tuned persona (Specific AI on Gen AI), so its voice and personality are a **product choice**, not an accident.
- **Human mentors are community volunteers** (related to the *Ask community* component), as opposed to *Ask chef*, which may be paid professionals — so "Ask Grandma" might be the *unpaid, peer* flavour of advice.
- **Remote meeting** is the live channel; **Chat** is the asynchronous one.
- Rating the answer (Story 1, step 6) feeds the **Rating Database** and the *Rate content* need.

**Candidate groups / bounded contexts:**
- *Advice request & matching* (request, persona pick, volunteer match)
- *AI persona answering* (Chat + Specific AI)
- *Live mentoring session* (Remote meeting)
- *Feedback & rating* (rate the answer → Rating Database)

**Questions for the room:**
- Is "Ask Grandma/Grandpa" a genuinely distinct capability, or the **same advice capability** as *Ask chef* / *Ask community* with a friendlier persona? (If the latter, you may not need a new component at all.)
- Who **vets or curates** the Grandma volunteers, and how do they opt in?
- Does the avatar draw on the platform's **Recipe database**, or only on general knowledge?
- On escalation (Story 3), **what context hands off** to the human — just the Question, or the Step, the Recipe, and the chat history?
- Where does **Free hand control** fit — does a cook ask hands-free mid-cooking, changing who initiates the conversation?