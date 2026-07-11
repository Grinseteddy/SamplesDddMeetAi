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

# Create the clickable prototype

Here is the single-file, clickable HTML prototype that brings the Larder "Grandma Rescue" flow to life. It uses the visual styles extracted from the UI screenshot and implements the user journey derived from the domain story.

You can save this code as an `.html` file (e.g., `larder-prototype.html`) and open it in any web browser to click through the entire flow.

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Larder - Grandma Rescue Prototype</title>
  <style>
    /* Design Tokens from Extracted JSON[cite: 1, 2] */
    :root {
      --bg-color: #FDFBF7;
      --primary: #FF6B4A;
      --primary-hover: #E55A39;
      --text-main: #222222;
      --text-muted: #888888;
      --card-bg-1: #FFE5E0;
      --card-bg-2: #DDF3EE;
      --card-bg-3: #FFF2DE;
      --white: #FFFFFF;
      --radius-md: 16px;
      --radius-pill: 9999px;
      --shadow: 0 4px 12px rgba(0,0,0,0.05);
      
      --font-heading: 'Poppins', system-ui, sans-serif;
      --font-body: 'Inter', system-ui, sans-serif;
    }

    * { box-sizing: border-box; margin: 0; padding: 0; }
    
    body {
      background-color: var(--bg-color);
      color: var(--text-main);
      font-family: var(--font-body);
      line-height: 1.5;
    }

    h1, h2, h3 { font-family: var(--font-heading); color: var(--text-main); }
    h1 { font-size: 42px; font-weight: 800; margin-bottom: 24px; }
    h3 { font-size: 18px; font-weight: 700; margin-bottom: 8px; }
    p { color: var(--text-muted); font-size: 14px; margin-bottom: 16px; }

    /* Navigation */
    nav {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 48px;
      background: var(--white);
      border-bottom: 1px solid #EAEAEA;
    }
    
    .nav-left { display: flex; align-items: center; gap: 32px; }
    .nav-left h2 { color: var(--primary); font-weight: 800; cursor: pointer; }
    .nav-links { display: flex; gap: 24px; font-weight: 600; color: var(--text-muted); cursor: pointer; }
    .nav-links .active { color: var(--text-main); border-bottom: 2px solid var(--primary); padding-bottom: 4px; }
    
    .nav-right { display: flex; align-items: center; gap: 16px; }
    
    /* Buttons */
    button {
      background: var(--primary);
      color: var(--white);
      border: none;
      padding: 8px 16px;
      border-radius: var(--radius-pill);
      font-family: var(--font-body);
      font-weight: 600;
      font-size: 14px;
      cursor: pointer;
      transition: background 0.2s;
    }
    button:hover { background: var(--primary-hover); }
    button.secondary { background: var(--text-main); }
    
    /* Layout */
    .container {
      max-width: 1000px;
      margin: 48px auto;
      padding: 0 24px;
    }

    .screen { display: none; }
    .screen.active { display: block; animation: fadeIn 0.3s ease-in; }

    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

    /* Discover Feed (Screen 1) */
    .grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 24px;
      margin-top: 32px;
    }
    
    .card {
      background: var(--white);
      border-radius: var(--radius-md);
      box-shadow: var(--shadow);
      overflow: hidden;
      cursor: pointer;
      transition: transform 0.2s;
    }
    .card:hover { transform: translateY(-4px); }
    
    .card-img { height: 160px; display: flex; align-items: center; justify-content: center; font-size: 48px; }
    .bg-1 { background-color: var(--card-bg-1); }
    .bg-2 { background-color: var(--card-bg-2); }
    .bg-3 { background-color: var(--card-bg-3); }
    
    .card-content { padding: 24px; }

    /* Active / Chat Screens */
    .focus-container {
      background: var(--white);
      padding: 48px;
      border-radius: var(--radius-md);
      box-shadow: var(--shadow);
      text-align: center;
      max-width: 600px;
      margin: 0 auto;
    }
    
    .chat-box {
      text-align: left;
      background: #F9F9F9;
      border-radius: 8px;
      padding: 24px;
      margin-bottom: 24px;
      height: 300px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    
    .msg { padding: 12px 16px; border-radius: 8px; max-width: 80%; }
    .msg.grandma { background: var(--card-bg-2); align-self: flex-start; color: var(--text-main); }
    .msg.cook { background: var(--primary); align-self: flex-end; color: var(--white); }
    
    .emoji-huge { font-size: 80px; margin-bottom: 24px; }
  </style>
</head>
<body>

  <nav>
    <div class="nav-left">
      <h2 onclick="navTo('screen-discover')">L Larder</h2>
      <div class="nav-links">
        <span class="active">Discover</span>
        <span>My Kitchen</span>
      </div>
    </div>
    <div class="nav-right">
      <span>👤 Amara (you)</span>
      <button>+ Share recipe</button>
    </div>
  </nav>

  <div class="container">
    
    <!-- Screen 1: Discover Feed -->
    <div id="screen-discover" class="screen active">
      <p>Fresh from the community</p>
      <h1>What are we cooking tonight?</h1>
      <p>Real recipes shared by home cooks — try one, then rate it so the next cook knows it's a keeper.</p>
      
      <div class="grid">
        <div class="card" onclick="navTo('screen-cooking')">
          <div class="card-img bg-1">🍋🍗</div>
          <div class="card-content">
            <h3>Charred Lemon Chicken</h3>
            <p>5.0 • 1 rating</p>
          </div>
        </div>
        <div class="card" onclick="navTo('screen-cooking')">
          <div class="card-img bg-2">🍲</div>
          <div class="card-content">
            <h3>Weeknight Red Dhal</h3>
            <p>4.0 • 1 rating</p>
          </div>
        </div>
        <div class="card" onclick="navTo('screen-cooking')">
          <div class="card-img bg-3">🍞</div>
          <div class="card-content">
            <h3>Grandma's Banana Bread</h3>
            <p>4.5 • 2 ratings</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Screen 2: Active Cooking (Step 1 & 2 of Domain Story)[cite: 3, 5] -->
    <div id="screen-cooking" class="screen">
      <div class="focus-container">
        <div class="emoji-huge">👨‍🍳</div>
        <h1>Cooking in Progress</h1>
        <p>You are currently preparing: <strong>Grandma's Banana Bread</strong></p>
        <p>Follow the steps and enjoy your meal!</p>
        <br><br>
        <div style="display: flex; gap: 16px; justify-content: center;">
          <button onclick="navTo('screen-discover')" class="secondary">Cancel</button>
          <button onclick="navTo('screen-burned')">Oh no, I burned it! 🔥</button>
        </div>
      </div>
    </div>

    <!-- Screen 3: Ask Grandma for Help (Step 3 of Domain Story)[cite: 3, 5] -->
    <div id="screen-burned" class="screen">
      <div class="focus-container">
        <div class="emoji-huge">⬛🍞</div>
        <h1>Oops! Things got a little crispy.</h1>
        <p>You took a picture of the burned meal. Don't throw it away just yet!</p>
        <br><br>
        <button onclick="startRescueChat()">Ask Grandma Avatar for Help 👵</button>
      </div>
    </div>

    <!-- Screen 4: Grandma Rescue Chat (Step 4 of Domain Story)[cite: 3, 5] -->
    <div id="screen-chat" class="screen">
      <div class="focus-container">
        <h3>👵 Grandma Avatar</h3>
        <p>Helping you rescue your dish...</p>
        
        <div class="chat-box" id="chatHistory">
          <!-- Chat messages injected via JS -->
        </div>

        <button id="btn-rescued" style="display:none;" onclick="navTo('screen-rescued')">I followed the steps — It's Rescued! ✨</button>
      </div>
    </div>

    <!-- Screen 5: Rescued & Share (Steps 5 & 6 of Domain Story)[cite: 3, 5] -->
    <div id="screen-rescued" class="screen">
      <div class="focus-container">
        <div class="emoji-huge">📸🍞</div>
        <h1>Meal Rescued!</h1>
        <p>You saved it! You've taken the final pictures showing the before and after.</p>
        <p>Share your rescue journey to help others in the community!</p>
        <br><br>
        <button onclick="shareToCommunity()">Share with Community 🌍</button>
      </div>
    </div>

  </div>

  <script>
    // Simple routing system to switch screens
    function navTo(screenId) {
      document.querySelectorAll('.screen').forEach(el => el.classList.remove('active'));
      document.getElementById(screenId).classList.add('active');
      window.scrollTo(0,0);
    }

    // Simulating the Avatar Chat Experience[cite: 3]
    function startRescueChat() {
      navTo('screen-chat');
      const chat = document.getElementById('chatHistory');
      const btn = document.getElementById('btn-rescued');
      
      chat.innerHTML = `<div class="msg cook">Here is a picture of my burned banana bread... can you help? 😭</div>`;
      btn.style.display = "none";

      setTimeout(() => {
        chat.innerHTML += `<div class="msg grandma">Oh sweetie, don't worry! Let me look at the picture. I see the top is quite charred.</div>`;
        chat.scrollTop = chat.scrollHeight;
      }, 1500);

      setTimeout(() => {
        chat.innerHTML += `<div class="msg grandma">Here's how we rescue this: Carefully slice off the burned top crust. Then, make a simple lemon glaze to pour over the top to keep the exposed bread moist! 🍋</div>`;
        chat.scrollTop = chat.scrollHeight;
        btn.style.display = "inline-block";
      }, 4000);
    }

    // Share back to the discover feed[cite: 3]
    function shareToCommunity() {
      alert("Success! You've thanked Grandma and shared your pictures with the community.");
      navTo('screen-discover');
    }
  </script>
</body>
</html>

```