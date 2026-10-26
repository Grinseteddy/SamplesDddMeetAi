# Example Mapping: US-CA-01 Help with Menu

Source board: `BookDddMeetsAi/chapters/part5/15-From-Example-Mapping-to-Exact-Coding/assets/fig6-llm-review-q2.png`.
Questions Q1 and Q2 and the scope were clarified with the domain expert on 2026-10-26.

## Story (yellow)

As a Cook, I want to ask a Chef to propose a Menu for my Occasion, so that I get help planning and preparing my meals. This is a premium feature.

- **Occasion:** why and when the Cook is cooking: date, guests, dietary needs, budget, tastes.
- **Menu:** one meal with several courses and a recipe for each, proposed by the Chef.

## Cast

Lena, premium Cook in Munich · Tom, free plan · Chefs Marco and Ana, Munich · Chef Sam, New York (CET−6).

## Rules (blue) and Examples (green)

### R1: A request covers exactly one Occasion
- Lena asks Marco for a Menu for "Mother-in-law visiting", Sat 19:00, 4 guests, vegetarian, 60 € → Marco gets the Occasion, no courses
- Lena needs Menus for Saturday dinner and Sunday lunch → two separate requests

### R2: The Chef proposes the courses and a recipe for each; Chefs never handle substitutions
- Marco proposes starter, main and dessert → 3 courses, 3 recipes
- Marco proposes main and dessert only → valid 2-course Menu
- Lena asks Marco for a crème fraîche substitute → nothing goes to Marco; separate request to Community or Grandma Avatar

### R3: Only a premium Cook can request; an open request closes when premium ends
- Tom (free plan) asks Marco → no request, upgrade offered
- Lena's premium ended yesterday → no request, upgrade offered
- Lena's premium ends Tuesday, request open → closed Tuesday, Marco told

### R4: A Cook picks a Chef by name and can have several open requests
- Lena picks Marco → only Marco receives it
- Lena asks Marco and Ana, accepts Marco's Menu → Marco's closed, Ana's stays open
- Lena closes Ana's request herself → closed, Ana told

### R5: Only Chefs who can answer in time can be selected, in their own time zone
- Deadline Fri 19:00 CET; Sam in New York works Thu until 18:00 local → Sam is listed
- Sam is away until Saturday → Sam is not listed

### R6: A Chef can review any Menu from the Grandma Avatar
- Grandma proposed "Lasagne and salad"; Lena asks Marco to review → Marco gets it with Grandma's Menu attached
- Lena has no Grandma Menu → review option not offered

### R7: An Answer is the complete Menu; the Cook accepts, rejects or leaves it
- Lena accepts Marco's Menu → request closed
- Lena never responds → Menu stays with her, nothing expires
- Lena rejects Wed 18:00 → stays open; Marco may answer again until Fri 18:00
- Lena rejects and passes it to Ana → Ana gets it, doesn't see Marco's Menu
- Lena tries to pass it to Sam, away next week → Sam not offered
- Lena rejects Fri 21:00, Occasion Sat 19:00 → deadline Sun 21:00, note "22 h to the Occasion"
- Lena cancels before any answer → withdrawn, Marco told

### R8: First request times out after 48 h or 24 h before the Occasion, whichever is earlier; every reset gives 48 h
- Asked Mon 18:00 → deadline Wed 18:00
- Asked Thu 10:00 → deadline Fri 19:00
- Marco silent until Wed 18:00 → times out, Ana suggested
- Lena takes Ana Wed 18:00 → Ana's deadline Fri 18:00
- Lena takes Ana Fri 19:00 → deadline Sun 19:00, note "24 h to the Occasion"
- Marco answers Wed 17:58 → answer counts, no timeout

### R9: A new request needs at least 48 h before the Occasion
- Asked Thu 19:00 for Sat 19:00 → sent, deadline Fri 19:00
- Asked Thu 20:00 for Sat 19:00 → refused, 48 h needed

## Questions (red), resolved 2026-10-26

- **Q1 · R2:** Must the Chef's Menu respect the Occasion's dietary needs and budget?
  **Answer: yes, both.** A Menu that breaks a dietary need (a beef main for a vegetarian guest) or goes over the budget is invalid, not just rejected by the Cook.
- **Q2 · R7, R8 (from LLM review):** May a reset push the deadline past the Occasion?
  **Answer: no cap; add a note.** Lena rejects Fri 21:00, Occasion Sat 19:00 → deadline Sun 21:00 with the note "22 h to the Occasion".

## Scope, decided 2026-10-26

In scope for this story: the Chef answers with a Menu, the Cook accepts, rejects or passes it on, the Cook withdraws or closes a request, open requests close when premium ends. "Marco told" and "Ana told" are returned as notices to the Chef; delivering them belongs to I-03. The separate request to Community or Grandma Avatar belongs to other stories.
