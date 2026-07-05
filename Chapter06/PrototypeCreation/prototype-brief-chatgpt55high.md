# Prototype Brief — Cooking Competition

## 1. Transcribed domain story

1. Cook wants to compare meal preparation skill with other cooks.
2. Community Administrator plans competition.
3. Community Administrator selects recipe for preparation.
3. Cook registers for competition.
4. Community Administrator finds other cooks for rating.
4. Cook prepares meal and shares online.
5. Other cooks rate meal and share online.
6. Community Administrator crowns winner with highest rate.

## 2. Actors and roles

- Cook: compares skills, registers for a competition, prepares a meal, shares it online.
- Community Administrator: plans a competition, selects a recipe, finds raters, crowns the winner.
- Other cooks: rate shared meals and share their rating online.

## 3. Modules

- Overview / domain story journey
- Competitions
- Plan
- Recipe
- Register
- Prepare
- Rate
- Winner

## 4. Domain model

- Competition: title, date, status, selected recipe, participants, raters, winner.
- Recipe: title, preparation brief, skill focus.
- Meal: competition, cook, title, online link, shared flag, ratings.
- Rating: rater, value.
- Winner: cook with the highest average rate.

## 5. State machines

Competition state machine:

`Planned → Recipe selected → Registration open → Raters found → Rating active → Winner crowned`

Transitions:

- Select recipe: Community Administrator, from Planned to Recipe selected.
- Open registration: Community Administrator, from Recipe selected to Registration open.
- Register: Cook, available in Registration open.
- Find other cooks: Community Administrator, after at least one participant is registered, to Raters found.
- Share meal online: Cook, after registration and recipe selection; when raters exist, rating can become active.
- Rate and share: Other cooks, in Raters found or Rating active, updates meal rating and moves to Rating active.
- Crown winner: Community Administrator, after ratings exist, to Winner crowned.

## 6. User journey

The clickable journey follows the story order: Overview → Competitions → Plan → Recipe → Register → Prepare → Rate → Winner.

## 7. Screens and navigation

Every screen is available through the chip-style topic filter. Role-specific controls are disabled unless the role switcher is set to the actor from the story.

## 8. Open questions

- What is the official rating scale and who can see individual ratings?
- Are cooks allowed to rate competitions in which they participate?
- How are other cooks selected or invited for rating?
- What exact online sharing channel is used?
- What is the tie-break rule for equal highest rates?
- Does the administrator open registration explicitly, or is it automatic after recipe selection?
- Does anyone verify that the prepared meal follows the selected recipe?
