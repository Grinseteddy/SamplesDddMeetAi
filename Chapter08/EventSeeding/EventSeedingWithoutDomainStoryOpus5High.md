# Prompt 2026-08-15

Create a couple of events as start points of an EventStorming sessin for 'Asking for help' in a meal preparation session.

# Answer

# Seed domain events — asking for help during meal preparation

## What I understood
Several people preparing a meal together; somewhere mid-session one of them can't get a step done alone. Trigger: a cook runs into something they can't handle. Outcome: the step is done (by them, by someone else, or abandoned) and cooking continues. **Coarse** — the whole flow of a session, since no single sub-process was named.

## Events
1. Meal preparation session started
2. Preparation step taken on by a cook
3. Cook got stuck on a preparation step
4. Help requested
5. Help request heard
6. Helper stepped over to the workstation
7. Help given
8. Preparation step completed
9. Cook returned to their own task
10. Dish handed to the table

## Alternatives & failures
- Help offered before it was asked for *(3–4 — human judgement)*
- Recipe consulted instead of asking *(3 — branch)*
- Help sought outside the kitchen — phoned, searched *(4 — the outside world)*
- Help request went unheard *(4–5 — time passing)*
- Help request repeated *(after unheard)*
- Help declined by the person asked *(5 — rejection)*
- Help promised but not yet given *(5–6 — time passing)*
- Helper called away mid-help *(7 — reversal)*
- Task handed over for good *(7 — the help never came back)*
- Helper took over uninvited *(7 — the "too many cooks" fact)*
- Advice turned out to be wrong *(7 — rejection)*
- Dish spoiled while help was waited for *(4–8 — time passing)*
- Missing ingredient discovered *(3 — a different kind of stuck)*
- Cook abandoned the step *(3–8 — reversal)*
- Serving time passed with the dish unfinished *(10 — threshold)*
- Two help requests raised at once *(4 — bulk)*

## Made up
Nearly all of it — the input was one phrase, so the bracketing (session start to dish served), the split between *Help requested* / *Help request heard* / *Help given*, and every alternative above are my invention. *Serving time passed* assumes the session is under a clock; *Helper took over uninvited* assumes people work at their own stations.

## Questions
- What actually starts this: someone speaking up, or someone visibly floundering until noticed? Is a silent glance already the request?
- Is "help" advice, a second pair of hands, or a handover of the task? Those may be three different facts.
- Is this a home kitchen, a cooking class, or a professional line? The failure events differ a lot between them.
- Does anyone track that help was given — does it matter later who helped whom?
- What is the fact when nobody responds and the cook simply carries on badly?

Throw these at the wall and let the room tear them up — the arguments over *Help request went unheard* and *Helper took over uninvited* are usually where the real domain shows itself. If you'd like, once the session's done and the board is corrected, I can read a photo of it back into a buildable brief.