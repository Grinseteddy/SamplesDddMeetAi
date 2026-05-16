Prompt: Create a design of a software system of a recipe sharing platform.

Answer Claude Opus 4.7:
Recipe Sharing Platform – System Design
1. Overview
   A web-based platform where users can create, share, discover, rate, and collect cooking recipes. The system supports social interaction (follows, comments, likes), search/discovery, and personalized recommendations.
2. Goals & Non-Functional Requirements
   Scalability: support millions of recipes and users
   Availability: 99.9% uptime
   Low-latency search (< 300 ms)
   Secure authentication & authorization
   Extensible (future: meal planning, shopping list, AI recommendations)
3. Core Domains (DDD-Style Bounded Contexts)
   Identity & Access: users, authentication, profiles, roles
   Recipe Management: recipes, ingredients, steps, media, categories, tags
   Social Interaction: follows, comments, likes, ratings, reviews
   Discovery & Search: full-text search, filters, recommendations
   Collections & Planning: favorites, cookbooks, meal plans
   Notifications: email, push, in-app
   Media Storage: images, videos
   Moderation: reporting, content review
4. High-Level Architecture
   Microservices behind an API Gateway, communicating via REST/gRPC (sync) and a message broker (async events).
   [Web App] [Mobile App]
   \   /
   [API Gateway] -- [Auth Service]
   |
   ┌──────┼─────────────┬──────────────┬───────────────┐
   |      |             |              |               |
   [Recipe][Social][Discovery/Search][Collections][Notification]
   |       |             |              |              |
   └──── Event Bus (Kafka/RabbitMQ) ────────────────────┘
   |       |             |              |
   [PostgreSQL][PostgreSQL][Elasticsearch][PostgreSQL]
   |
   [Object Storage (S3)] for media
   [Redis] for caching/sessions
5. Key Services
   5.1 Recipe Service
   CRUD for recipes (title, description, ingredients, steps, servings, prep/cook time, tags, images)
   Versioning of recipes
   Publishes events: RecipeCreated, RecipeUpdated, RecipeDeleted
   5.2 Identity Service
   Registration, login (OAuth2/OIDC, JWT)
   Profile management
   Roles: User, Moderator, Admin
   5.3 Social Service
   Likes, ratings (1–5), comments, reviews, follows
   Aggregates stats (avg rating, like count)
   5.4 Discovery/Search Service
   Indexes recipes in Elasticsearch via Kafka consumer
   Filters: ingredient, cuisine, diet, time, difficulty
   Recommendations based on user history & follows
   5.5 Collections Service
   Favorites, custom cookbooks, meal plans, shopping lists
   5.6 Notification Service
   Consumes events, sends email/push/in-app messages
   5.7 Media Service
   Upload, resize, transcode; stores in S3, serves via CDN
   5.8 Moderation Service
   Reports, automated checks, admin review queue
6. Domain Model (Recipe Context Example)
   Recipe (Aggregate Root)
   ├─ RecipeId
   ├─ AuthorId
   ├─ Title, Description
   ├─ Servings, PrepTime, CookTime, Difficulty
   ├─ List<Ingredient> (quantity, unit, name)
   ├─ List<Step> (order, text, optional image)
   ├─ List<Tag>
   ├─ List<MediaRef>
   └─ Status (Draft/Published/Archived)
7. Data Storage
   PostgreSQL per service (relational data)
   Elasticsearch for search
   Redis for cache, sessions, rate limiting
   S3-compatible object storage for media
   Kafka for event streaming
8. API Style
   REST/JSON externally via API Gateway
   gRPC for internal service-to-service calls
   OpenAPI specs per service
9. Security
   OAuth2 + JWT access tokens, refresh tokens
   HTTPS/TLS everywhere
   Role-based access control
   Input validation, rate limiting at gateway
   Media virus scanning before publish
10. Cross-Cutting Concerns
    Centralized logging (ELK/Loki)
    Metrics (Prometheus + Grafana)
    Tracing (OpenTelemetry + Jaeger)
    CI/CD pipelines, containerized (Docker + Kubernetes)
    Infrastructure as Code (Terraform)
11. Example User Flows
    Publish recipe: Client → API Gateway → Recipe Service → DB → emits RecipeCreated → Search indexes it → Followers notified.
    Search recipes: Client → Gateway → Discovery Service → Elasticsearch → results.
    Like recipe: Client → Social Service → DB → emits RecipeLiked → Notification to author.
12. Future Extensions
    AI-powered recipe generation & ingredient substitution
    Voice-guided cooking mode
    Nutrition analysis integration
    Multi-language translation