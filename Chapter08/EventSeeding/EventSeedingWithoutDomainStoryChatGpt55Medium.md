# Prompt 2026-08-15

Create a couple of events as start points of an EventStorming session for 'Asking for help' in a meal preparation session. Use the provided skill.

# Answer

Using the provided EventStorming seeder skill, I’m treating these strictly as **domain events**: completed business facts in past tense, with no commands, actors, aggregates, or contexts.

## What I understood

A person is preparing a meal and reaches a point where they need help.
**Trigger:** difficulty or uncertainty arose during meal preparation. **Outcome:** help was obtained and preparation could continue.
**Fine altitude** — this is one focused process.

## Events

1. **Difficulty encountered during meal preparation**
2. **Need for help recognized**
3. **Help requested**
4. **Help received**
5. **Meal preparation resumed**

## Alternatives & failures

* **Help request declined** *(branches from 3)*
* **Help request went unanswered** *(branches from 3 — time passing)*
* **Clarification requested** *(branches from 3)*
* **Alternative helper approached** *(after a decline or no response)*
* **Meal preparation abandoned** *(after help was unavailable)*

## Made up

All of these are seed candidates inferred from the short phrase “Asking for help”; the source description did not specify the actual meal-preparation situation or how help is requested. The skill explicitly recommends marking invented events rather than presenting guesses as findings.

## Questions

* What usually causes someone to ask for help: missing knowledge, physical difficulty, missing ingredients, equipment, or something else?
* Does “help received” mean advice was given, or that somebody actually joined the preparation?
* Can the request be refused or ignored?
* What happens to the meal preparation while waiting for help?

These are intentionally just starting stickies—the session should correct, replace, split, or remove them. 
