# Prompt 2026-08-31


/domain-story-context-finder

Create Bounded Contexts based on the provided Domain Story.

![](../assets/AskGrandma.jpg)

# Answer

**Propose mode** — no lanes were drawn, so this is a fresh cut. Three contexts for the six sentences:

| Lane | Clauses | Owns |
|---|---|---|
| **Meal Preparation** | 1, 2a … 5a | the *Meal* as a stateful preparation (unnamed in the story) |
| **Community Help** | 2b, 3, 4 … 6a | *Help request*, *Help response*, *Help provider*, *Thanks*, catastrophe pictures |
| **Meal Sharing** | 5b, 6b | *Pictures* as a post, *Community* as audience |

The cut rests on one thing the diagram handed over for free: **the story already names the two senses of *Pictures*** — *Catastrophy Pictures* going into the help request, plain *Pictures* going out to the community. Different lifetimes, different audiences, different questions asked of them. That is a language boundary, not a phase.

Two of the three lanes recur across the sequence, and the two compound sentences (2 and 6) are exactly where the boundaries fall — worth splitting them on a redraw so arrows don't cross lane borders mid-sentence.

Three things I'd put to the room before anyone builds on this:

1. **There is no Recipe in this story.** The avatar rescues a meal with no stated knowledge of what was being cooked — so either a recipe reference crosses the border and the story omits it, or it's working from photographs alone.
2. **The Community answers nothing.** On the EventStorming board in this project, help comes from a Community Cook, a Chef *and* the Grandma Avatar. Here the only responder is the machine, and the community shows up once at the end as an audience. If that's deliberate, the reciprocity loop is asymmetric — the cook thanks a machine and shares with people who did nothing.
3. **Is the thank-you private, or is it the caption on the public post?** That single answer decides whether Meal Sharing is a context or a module inside Community Help.

The cut agrees independently with the pivotal-event cut and the invariants sheet already in the project on the unnamed cooking aggregate, on *Thanks* living in the help context, and on *Pictures* being one word for two concepts — which this story resolves by splitting the two senses into the lanes whose purpose each serves.

[Context cut](./claude/context-cut-cooking-rescue-story.md)

Sources: `claude/pivotal-event-cut-cooking-board.md`, `claude/invariants-community-cooking-board.md`