# Prompt 2026-09-13

Use the knowledge graph and discuss if a monolith, a microservice or a modular monolith should be used for the project.

[Domain Knowledge Graph](../../assets/larderEnahnced4.ttl)

# Answer

**I recommend a modular monolith for Larder’s first release:** one application deployment, with clear internal modules and ownership of data. The graph identifies distinct areas—Cook Profile, Meal Planning, Meal Preparation, Cooking Assistance, Media and Sharing—so an undivided monolith would make those responsibilities too easy to mix.

Microservices would make today’s uncertainties expensive to resolve. The two EventStorming renderings disagree on whether planning-time and cooking-time help are one context. Meal Planning and Recipe Catalog both claim *Ingredient*, while the catalog has no modeled recipe creation or editing lifecycle. Those are questions about ownership that should be settled before they become network and data boundaries.

The workflows also cross several areas: a plan is settled before preparation begins; a help response can affect that preparation; pictures and thanks follow the finished meal. Within the modular monolith, a module can own each business rule and use explicit interfaces for immediate calls, while notifications and other work that can happen later use events. The graph’s proposed rule that a cook confirms a rescue is an example of a decision to keep with Meal Preparation.

The goal of **2,500 paying members in two years** establishes a business target, but the graph gives no traffic or team-size evidence that calls for independently deployed services. It also makes member management and a paywall priority-one deliverables, so I would give membership and access rules an explicit module even though the detailed canvases concentrate on cooking.

Reconsider extracting a microservice when a boundary has a settled owner **and** a measured need—for example, substantially different scaling, release cadence or reliability requirements. Until then, the modular monolith preserves those future options without requiring distributed coordination throughout the main cooking journey.
