# Prompt 2026-08-15

Create a couple of events as start points of an EventStorming session based on the provided Domain Stories. Use the provided skills.

![](../assets/AskCommunity.jpg)
![](../assets/AskGrandma.jpg)

[Event Seeder with Domain Story skill](../Skills/EventSeederWithDomainStorySkill/SKILL.md)

[Event Seeder skill](../Skills/EventSeederSkill/SKILL.md)

[Event Seeder skill Worked Examples](../Skills/EventSeederSkill/references/worked-examples.md)

[Domain Story Interpreter Skill](../../Chapter06/Skills/DomainStorytellingSkill/SKILL.md)

[Domain Story Interpreter Worked Examples](../../Chapter06/Skills/DomainStorytellingSkill/references/worked-examples.md)

[Domain Story Interpreter Pictographic Language](../../Chapter06/Skills/DomainStorytellingSkill/references/pictographic-language.md)

# Answer

# Seed domain events — Community-Assisted Cooking & Meal Rescue



## Sources

A — `AskCommunity.jpg`: Cook asks the community for ingredient substitutions while preparing a meal.

B — `AskGrandma.jpg`: Cook burns a meal and requests emergency rescue guidance from Grandma Avatar.

How they relate: Variants/branches of the same core meal-preparation process.

## What I understood

The process covers a cook preparing a meal, seeking assistance when stuck, resolving the issue, and sharing the result.

**Trigger:** Cook begins preparing a meal.

**Outcome:** Meal is completed or rescued, helper is acknowledged, and photos are shared with the community.

**Altitude:** Fine (one cooking workflow, step-by-step facts).

## Events

1. Meal preparation started (A1, B1)


2. Help requested for ingredient substitution (A2–A3)


3. Ingredient substitution provided (A4)


4. Meal preparation completed (A5)


5. Meal photo captured (A5)


6. Assistance acknowledged (A6)


7. Meal photo shared with community (A6)



## Alternatives & failures

* Meal burning incident recorded (branches from 1) (B2)


* Mishap photo captured (B2)


* Emergency cooking advice requested (B3)


* Meal rescue plan provided (B4)


* Meal rescued (B5)


* Rescued meal photo shared (B6)


* Help request timed out (—)


* Provided substitute rejected (—)


* Meal declared unsalvageable (—)


* Community advice flagged as inappropriate (—)



## Made up

* Splitting photo taking and photo sharing into separate events (`Meal photo captured` vs `Meal photo shared`) (—).


* `Help request timed out` (time passing) (—).


* `Provided substitute rejected` (rejection) (—).


* `Meal declared unsalvageable` (reversal/failure) (—).


* `Community advice flagged as inappropriate` (human judgement/moderation) (—).



## Questions

* Is "Grandma Avatar" an automated AI assistant or a specific expert role/person within the community?


* What happens if no community member or Grandma Avatar responds before the food is ruined or burns further?


* Can a cook request ingredient substitutes and emergency rescue advice within the same cooking session?


* Is taking/attaching a photo mandatory when asking for emergency help, or can a text prompt be used?