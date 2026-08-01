# Prompt 2026-08-01

Check the consistency of the three provided Domain Stories with the provided Visual Glossary.

![](../assets/Grandma.jpg)
![](../assets/GrandmaII.jpg)
![](../assets/VisualGlossaryGrandma.jpg)

# Answer

I'll transcribe both sides first, then diff them.**Verdict: diverged — but in one specific, fixable way.** 2 Blocking, 6 Significant, 2 grouped Minor. 9 of 15 glossary terms are exercised by name; 16 of 26 relationships.

The two that matter most:

**F1 (Blocking).** The glossary's central distinction — `Help Request` (what you post) vs. `Help` (what you get back), joined by `Help —for→ 1 Help Request` — does not exist in any story. All three write `Help` for both: "asks for Help" in sentence 3, "provides Help" in sentence 4. Same word, two objects. That single overload also drags in most of F3: the three response content types (`Ingredient Substitute`, `Preparation Step Expanation`, `Steps to Mitigate Catastrophy`) are never named, and Story C reuses the *request*-type name for the response.

**F2 (Blocking).** `Help Request —at→ 1 Community` **and** `—at→ 1 Grandma Avatar`, both mandatory — so every request must sit at both. All three stories go to exactly one. Three independent scenarios contradicting a bound is strong evidence the glossary should move: either `0..1` with an exactly-one constraint, or a `Helper` supertype (which also collapses the duplicated `provides` edges).

Three things I'd flag as judgement calls rather than facts. Story B(6) says `Grandma`, not `Grandma Avatar` — probably a slip, but it appears precisely at the *thanking* step, and "Avatar" implies someone stands behind it, so it may be a second party the glossary lacks. Stories B and C thank the Grandma Avatar but share the pictures with the Community — two acts the glossary models as one `Thanks`. And every story photographs a meal, while `Picture` can only attach to a Help Request, Help or Thanks — B(2) photographs the burnt meal before posting anything.

Two caveats on my reading: I've assumed all three stories are **to-be** (no scope marker is drawn), which is what keeps `Ingredients` and `Meal Preparation Step` filed as glossary gaps rather than story defects. And the three `Thanks` edges — the two `to` lines routed across the top-left and the long `for` line along the bottom — were the hardest to trace; confirm those before acting on F8.

[Consistency Report](ConsistencyReportOpus5.md)