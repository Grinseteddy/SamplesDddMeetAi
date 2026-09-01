# Prompt 2026-08-31


/domain-story-context-finder

Create Bounded Contexts based on the provided Domain Story.

![](../assets/AskGrandmaII.jpg)

# Answer

**Propose mode** — no lanes drawn, so this is a proposed cut. Three lanes for six sentences:

- **Meal Preparation** — 1, 2, 5a
- **Cooking Help** — 3, 4, 6a
- **Meal Sharing** — 5b, 6b

Two of three recur across the sequence, and the two compound sentences (5 and 6) are the boundary crossings.

Four things worth your attention:

**This is a different scenario from the story already in the project.** That one was the catastrophe variant; this is the stuck-on-a-step variant. Same shape, milder trouble. Having both is the most useful thing here — two independent stories producing the identical sequence is the positive evidence that "one help context drawn twice" was missing.

**The story names *Meal Preparation Step*.** Every prior analysis in the project reached the same gap from a different direction — a board stretch with no business object, ten INV-PREP rules with nowhere to live, a lane whose *Meal* is stateful and unnamed. This is the first work object that lane has ever had. Half the missing aggregate; a step is still not a session.

**Nothing creates a step.** It's referenced three times and originated nowhere, and there's no Recipe anywhere in the story. That's the fork: if steps belong to a Recipe, one grandma answer serves every cook stuck at the same point and the value collects in Meal Preparation. If steps belong to a preparation, answers are disposable and Cooking Help is the whole product. Ask this first.

**"Community Help" is the wrong name.** The earlier cut proposed it because it says who answers. Two stories in, no community member has answered anything — the only responder is the avatar, and the Community appears once, at the end, as an audience. Use one of the board's own names.

Two contested calls are argued both ways in §7: sentence 2 (*needs Help*, placed in Preparation, which is what makes the missing policy visible) and 6a (*thanks*, placed in Help). Also flagged: *needs* isn't an activity, and sentence 5 repeats sentence 1 verbatim — so as drawn, the story cannot tell you whether the help worked.

[Context cut](./claude/context-cut-grandma-II.md)