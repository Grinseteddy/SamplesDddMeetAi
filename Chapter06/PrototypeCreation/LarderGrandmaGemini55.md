# # Prompt 2026-07-11:

Create a prototype based on the Domain Story and the provided screenshot. The Domain Story is part of the Larder application. Use the provided skills.

![](../assets/LarderStyleExample.jpg)
![](../assets/DomainStoryGrandma.jpg)

[Prototype Skill](../Skills/PrototypeSkill/SKILL.md)
[Domain Story Skill](../Skills/DomainStorytellingSkill/SKILL.md)
[Refererences Domain Story Skill Pictographic Language](../Skills/DomainStorytellingSkill/references/pictographic-language.md)
[References Domain Story Skill Worked Examples](../Skills/DomainStorytellingSkill/references/worked-examples.md)
[Webapp Style Extractor Skill](../Skills/WebappStyleExtractorSkill/SKILL.md)
[References Webapp Extractor Skill](../Skills/WebappStyleExtractorSkill/references/schema.md)

# Answer:

Here is the prototype brief based on your provided domain story and UI screenshot. I have applied the Domain Storytelling interpretation methodology to extract the application structure and the web-app style extractor schema to pull design tokens.

As an AI, I cannot run pixel-probing scripts directly on the image, so the styling JSON contains visually estimated values grounded in standard web UI proportions.

---

## Prototype Brief — Larder "Grandma Rescue" Feature

### 1. Story transcription

Based on the grammar of domain-story sentences, here is the exact sequence of events:

* 1. Cook prepares Meal.




* 2. Cook burns Meal and takes Pictures.




* 3. Cook asks Grandma Avatar for Help with Pictures.




* 4. Grandma Avatar provides Help to rescue Meal.




* 5. Cook rescues Meal and takes Pictures.




* 6. Cook thanks Grandma and shares Pictures with Community.





### 2. Actors & roles

* **Cook** (person): The primary end user interacting with the app.


* **Grandma Avatar / Grandma** (system): The virtual assistant or persona providing the rescue help.


* **Community** (group): Other users on the platform viewing shared recipes and rescue stories.



### 3. Modules (bounded contexts)

While no explicit lanes are drawn in the diagram, we can infer two distinct feature areas:

* **Cooking & Rescue Module:** Handles the meal's state and interaction with the Grandma Avatar.


* **Community Discover Module:** Handles sharing the resulting pictures with other users.



### 4. Domain model

* **Entities:** **Meal** (tracks status), **Pictures** (visual proof of the before/after state), **Help** (the instructions/chat log provided by the avatar).


* **UI Surfaces:** An implied camera interface, a chat/help interface, and a community feed.


* **Physical Objects:** The actual Meal being cooked.



### 5. State machines

The same noun appearing with different adjectives indicates an entity moving through states.

* **Meal Lifecycle:** Prepared $\rightarrow$ Burned $\rightarrow$ Rescued.


* **Triggers:** Cook *prepares*, Cook *burns*, Cook *rescues* (after receiving help).



### 6. Use cases & user journey

* Log active meal preparation.


* Report burned meal and upload a picture.


* Request help from Grandma Avatar using the uploaded picture.


* Receive and review rescue instructions.


* Mark meal as rescued and upload a new picture.


* Share the before/after rescue story with the Community.



### 7. Screens & navigation

* **Active Cooking Screen:** Shows the current recipe with an "I messed up / Burned it" action button.


* **Grandma Rescue Interface:** A chat-like UI to upload the picture and converse with the Grandma Avatar.


* **Rescue Resolution:** A screen to upload the final "rescued" picture, complete with a "Share to Community" button.


* **Community Discover Feed:** The homepage (matching the screenshot) where shared meals are displayed.



### 8. Open questions & assumptions

Because a domain story only depicts a single concrete path without conditionals, the prototype must address these unstated rules:

* What happens if the meal cannot be rescued? (Needs an alternative "Ruined" state).


* How does the Grandma Avatar analyze the picture? Is it purely AI-driven or does it require specific user prompts?


* Are "Grandma" and "Grandma Avatar" strictly the same entity in the system backend?



---

## UI Styling (Larder Style Example)

Following the standard output schema, here are the design tokens extracted from the reference screenshot.

```json
{
  "meta": {
    "source": "LarderStyleExample.jpg",
    "scaleFactor": 1,
    "scaleFactorBasis": "Assumed 1x based on standard desktop typography",
    "units": "css-px",
    "notes": [
      "Colors and sizes are visually estimated as pixel-probing scripts were unavailable.",
      "Font families are visual guesses; verify against the real stack.",
      "Only default states are captured; hover/focus states are unknown."
    ]
  },
  "colors": {
    "roles": {
      "background": "#FDFBF7",
      "primary": "#FF6B4A",
      "primaryText": "#FFFFFF",
      "text": "#222222",
      "textMuted": "#888888",
      "surfaceCard1": "#FFE5E0",
      "surfaceCard2": "#DDF3EE",
      "surfaceCard3": "#FFF2DE"
    },
    "palette": [
      { "hex": "#FDFBF7", "coverage": 0.70, "role": "background" },
      { "hex": "#FF6B4A", "coverage": 0.05, "role": "primary" }
    ]
  },
  "typography": {
    "styles": {
      "h1": { "fontFamily": "Poppins, system-ui, sans-serif", "fontSize": 42, "fontWeight": 800, "lineHeight": 48, "color": "#222222" },
      "h3": { "fontFamily": "Poppins, system-ui, sans-serif", "fontSize": 18, "fontWeight": 700, "lineHeight": 24, "color": "#222222" },
      "body": { "fontFamily": "Inter, system-ui, sans-serif", "fontSize": 14, "fontWeight": 400, "lineHeight": 20, "color": "#888888" },
      "button": { "fontFamily": "Inter, system-ui, sans-serif", "fontSize": 14, "fontWeight": 600, "lineHeight": 20, "color": "#FFFFFF" }
    }
  },
  "spacing": {
    "baseUnit": 4,
    "scale": [4, 8, 16, 24, 32, 48],
    "observed": [
      { "context": "button paddingX", "px": 16 },
      { "context": "card gap", "px": 24 },
      { "context": "card internal padding", "px": 24 }
    ]
  },
  "radii": {
    "md": 16,
    "pill": 9999
  },
  "components": {
    "button.primary": {
      "background": "#FF6B4A",
      "color": "#FFFFFF",
      "fontSize": 14,
      "fontWeight": 600,
      "paddingX": 16,
      "paddingY": 8,
      "borderRadius": 9999,
      "border": "none"
    },
    "card.recipe": {
      "background": "#FFFFFF",
      "borderRadius": 16,
      "padding": 0,
      "shadow": "0 4px 12px rgba(0,0,0,0.05)"
    }
  }
}

```