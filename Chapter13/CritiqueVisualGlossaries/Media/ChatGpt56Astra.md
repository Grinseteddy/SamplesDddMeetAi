# Prompt 2026-10-04

/devils-advocate
Critique Visual Glossary Media

![](../../../Chapter12/assets/ApiProductCanvas/MediaMediaVisualGlossary.jpg)

# Answer

I could read the image. **My strongest objection: the glossary looks precise about structure while leaving the meaning of its two central concepts—“media” and “links”—unresolved.** Two developers could implement different models and both claim to follow it.

These are the challenges I’d raise, based on what’s visible:

| Challenge | Why it matters | What would resolve it |
|---|---|---|
| **What is the difference between `Media` and its required `media` value?** | Is the inner value image bytes, a storage reference, or an image description? “Contains one media” doesn’t establish that boundary. | Define both terms and give a concrete example of the inner value. If only images are supported, make that scope explicit. |
| **Are `links` business associations or navigation links?** | `recipe` suggests a business relationship; `help` and `help Request` could suggest navigation or other domain concepts. A generic type-plus-URL structure may conceal different rules. | Define each link type in business language. Establish whether these links describe where an image is used or where someone can navigate. |
| **Why does Media own those links?** | If a recipe uses an image, who records that relationship? If both sides do, they could disagree after replacement or deletion. | Walk through “attach image,” “replace image,” and “delete recipe.” Identify who owns the association and who updates it. |
| **Which invariants justify the aggregate boundary?** | The legend includes invariants, but none are shown. The aggregate designation therefore has little visible support beyond containment. | State the rules that must always hold together. For example, decide whether duplicate `(type, URL)` pairs are allowed and whether links may exist before the media is usable. |
| **Does “exactly one media” hold throughout its lifecycle?** | If Media can exist during an upload or after a failed upload, the required value may be absent. Alternatively, Media might only come into existence after successful upload. | Choose and document when a Media instance begins to exist and what happens when its content is replaced or removed. |

Two smaller notation issues compound the ambiguity:

- `links` has cardinality `0..*`, so the individual concept would read more clearly as **Link**. Its URL relationship currently has no cardinality.
- `recipe`, `thanks`, `help Request`, and `help` are drawn as value objects connected by “is.” Are these allowed values of **Link Type**, distinct concepts, or examples? The notation should distinguish those interpretations.

**“We will handle videos later” is a reasonable scope choice, but it doesn’t resolve what “media” means today.** You don’t need to design video support now; you do need to identify which current rules apply specifically to images.

The failure scenario I’d test is simple: *a recipe replaces an image that is also used elsewhere.* Can two people independently use this glossary to agree on which object changes, which links remain, and whether the original Media can be deleted?

If they can—and those answers are captured in definitions and invariants—my main concerns would largely disappear.