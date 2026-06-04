# Prompt 2026-05-30:

You are the Devil's Advocate in a Brainstorming session. We want to create and structure ideas for a recipe sharing platform. We provide our ideas as picture.
![Brainstorming](./assets/Brainstorming.jpg)

# Answer:

Alright, putting on the horns. I count roughly 35 sticky notes up there, and that's the first problem I want to drop on the table.

**You don't have a product. You have a wishlist.**

If I squint, this board contains at least five different apps fighting each other: a recipe database, a meal planner, a cooking social network, a learning platform, and a grocery commerce play. Each of those is a venture-scale company on its own (NYT Cooking, Mealime, Kitchen Stories, MasterClass, Instacart). Pick one. Which is the wedge? "Recipe sharing platform" is so generic that nothing on this board tells me why a single user would uninstall Chefkoch, Allrecipes, or Paprika to use yours.

**The personas contradict each other.**

"Become a dessert chef," "Grill master," and "Cookbook publishing" are aspirational creator features. "Necessary stuff," "Search with things I already have," and "Left over optimizations" are tired-Tuesday-night utility features. "Famous chefs as partner" is a media business. "Ask grandma" / "Grandpa shows how to do it" is a UGC sourcing fantasy. These users want different things, read different copy, pay different prices. Who is the first 1,000 users? If you can't name them, the rest of the board is decoration.

**Several of these aren't ideas, they're table stakes or someone else's product.**

- *Timer* — my phone has one. My oven has one. What's the insight?
- *Login with social account* — that's a checkbox in the auth library, not a feature.
- *Sharing on Instagram* — Instagram already lets people share photos of food. You'd be a feeder service for Meta, not a destination.
- *Order ingredients directly* — congratulations, you just signed up to compete with Amazon Fresh, Gorillas, and every supermarket app, and you need supply chain partnerships in every city. In Munich that's a different deal than in Hamburg.

**The "cute" ideas hide enormous execution problems.**

"Ask grandma" and "Grandpa shows how to do it" — where do the grandparents come from? You have a cold-start content problem on day one, and elderly UGC creators are not a demographic that organically downloads new apps. "Import handwritten notes" — handwriting OCR on recipe cards (mixed text, smudges, fractions, regional terms) is a real engineering investment for a feature maybe 2% of users will use once. "Remix of recipes" — who owns the remix? What if I "remix" Jamie Oliver's cookbook? Now you have a legal team.

**Where is the money?**

I see "Sponsor contest" and "Famous chefs as partner" gesturing toward monetization, but no subscription, no ads, no affiliate model, no premium tier explicitly on the board. "Order ingredients directly" is the only revenue-shaped sticky and it's the hardest thing here to build. If you can't draw a line from a user behavior to a euro, none of these features matter.

**Overlap audit.**

Cooking skill level + Cooking skill learning + Step-by-step cooking mode + Cooking Videos + Grandpa shows how to do it + Become a dessert chef + Grill master are all variations of "teach me to cook." That's one feature with one good UX, not seven. Same with Meal Chooser + Cooking Calendar + Shopping Lists + Budget + Dietary requirements + Necessary stuff — that's "meal planning," already a saturated category.

**The questions I'd force the room to answer before adding one more sticky:**

What does this app do that the user can't already do in Chefkoch or NYT Cooking? Who is the very first user, and what painful thing happens to them at 6:47pm on a Wednesday that this fixes? What are you willing to *cut*?

Now — what would you like me to keep attacking? I can rip into the business model specifically, or stress-test the "Ask grandma" / authenticity angle if you think that's the real differentiator.

Prompt:
Do you have better ideas?

Answer:
Sure. Taking the horns off. A few directions I'd actually defend in the room — each one is a *different product*, not a feature checklist, which is the whole point.

**1. The "what's actually in my kitchen" app.**
Open the app, point camera at fridge or pantry, AI builds your inventory. Now every recipe suggestion is filtered through what you own *right now*. Shopping list = the delta between recipe and inventory. Leftovers tracking falls out for free. Food waste reduction becomes a measurable user benefit you can talk about in marketing. This was a fantasy three years ago. With modern vision models it's a real product. Yummly tried a primitive version and gave up too early. Nobody owns this and the German/EU sustainability angle gives you a press story Munich-based you can credibly tell.

**2. The family recipe archive.**
Your "ask grandma" sticky note, but serious. The app is a private family cookbook: digitize Oma's handwritten cards (OCR + human cleanup), record her voice telling you the story behind the dish, video her hands kneading the dough one last time. Recipes become heirlooms. You charge €5/month per family. The emotional moat is enormous — people don't churn from things their dead grandmother is inside. Optional cultural-preservation angle for regional cuisines that are disappearing. This is not Allrecipes' market and Allrecipes can't pivot to it.

**3. Duolingo for cooking.**
Most apps treat users as either incompetent or expert; almost nothing handles the middle. A structured skill tree: "knife skills 1," "emulsions," "braising," "bread basics." Each lesson is a recipe, each recipe unlocks the next. You earn the right to attempt cassoulet by proving you can make a stock. Streaks, progression, mastery. This is the only model on the board where retention is built into the mechanic instead of bolted on with "Followers" and "Comments."

**4. The household cooking OS.**
Two-person household, dual-income, both tired. Who's cooking tonight, what's the budget this week, what does each person eat, what's in the fridge, who's doing the shop. A coordination layer, not a recipe site. Aimed at couples and small families — the demographic that actually pays for software because their time is expensive. Mealime is the closest competitor and it's American and weak on the coordination side.

**5. Cook-along, asynchronously.**
Cooking is intrinsically social and apps make it solitary. I cook my mother's ragù in Munich, record voice notes and a couple of photos as I go. My sister in Hamburg opens it next weekend and cooks along to my actual session, my timing, my mistakes, my voice. Recipes become *performances*, not text. This is the only way "Cooking Videos," "Sharing," and "Followers" turn into something Instagram doesn't already do better.

**6. The pro-knowledge bridge.**
Restaurants run on ratios, temperatures, and technique; home cooks copy recipes by rote. An app that teaches you the *why* — 1:1.5:0.8 for vinaigrette, 63°C for the perfect egg, why you salt the eggplant. Less recipe database, more "Salt Fat Acid Heat" as a tool. Smaller audience but they pay, and they tell their friends.

---

Each of these is a different bet on who the user is. I'd run the room through a forced ranking: if you had to ship *one* of these as a v1 in nine months with five people, which? That question kills 80% of the stickies on your board automatically, which is the goal.

Want me to stress-test whichever one the team gravitates toward — back to Devil's Advocate mode?

