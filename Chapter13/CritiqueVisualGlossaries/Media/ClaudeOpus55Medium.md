# Prompt 2026-10-04

/devils-advocate
Critique Visual Glossary Media

![](../../../Chapter12/assets/ApiProductCanvas/MediaMediaVisualGlossary.jpg)

# Answer

I'm treating this as a finished artifact, so I'll go straight to pressure-testing. Here are the objections I find genuinely worrying, strongest first. Push back on any of them; you know the domain and I'm reading a picture.

**1. The links branch makes Media know who uses it, and that may be backwards.**
Media contains 0..* links, and each link points out to a recipe, a thanks, a help request or help. The example URL goes into `recipe-catalog`, so these links cross into other bounded contexts. That raises some hard questions:
- **Who writes a link?** Does Recipe Catalog have to call Media to register "I use this photo"? Or does Media write it itself, and if so, how does it learn about the recipe?
- **What happens when the recipe is deleted?** A dangling link in Media is the likely result. If no one owns cleanup, links will drift out of sync.
- **Is there a simpler design?** The strongest alternative is that Media knows nothing about its consumers. Recipe, Thanks and HelpRequest each hold a `mediaId`, and Media stays a dumb, reusable store. That design removes the whole branch.

If there is a real reason Media must know its consumers (a "where is this photo used?" view, or deleting a photo only when nothing references it), that reason belongs on the board. Right now the board doesn't show why the branch exists.

**2. `url` plus `type` holds the same fact twice and ties Media to another context's routes.**
The URL already contains `recipe-catalog/recipes/…`, so the type repeats it. Nothing stops `type = thanks` from sitting next to a recipe URL. The URL also binds Media to another context's hostname and path layout, so a route change there breaks Media's data. Storing a type plus the target's id, with no URL, would give you one source of truth.

Two smaller problems in the same branch:
- The `links → url` arrow has no cardinality.
- `links` is plural while the 0..* already says "many." Elsewhere the glossary uses singular nouns, so `link` would match.

**3. `help` and `helpRequest` look like an unresolved naming disagreement.**
Two siblings this similar usually mean one of these:
- two people named the same thing differently, or
- there are two real concepts (asking for help vs. offering it?) and nobody has said which is which.

Either way, this is the term most likely to cause confusion later. It deserves a definition card or a hotspot.

**4. `media` inside `Media` is the one term that carries the most meaning and it's undefined.**
The aggregate contains a value object with its own name, and the board doesn't say what it is: a file, a storage reference, a MIME type, dimensions? The ID and the links get an example; the actual content gets none.

The pink sticky makes this sharper. If videos are coming, `media` is where they'd break the model (duration, transcoding state). So either:
- define `media` now as image-shaped and call that a deliberate choice, or
- show how it would vary for video.

**5. The legend offers invariants, and there are none.**
That may be fine, but the questions above suggest some, for example:
- Can Media exist with zero links, and if so, when is it deleted?
- Can the same link appear twice?
- Is a link immutable once set?

If the answer to all of these is "no rules," say so explicitly. Otherwise developers will make up their own.

**What would make me stop worrying:** a one-line answer to "who creates and removes a link, and why does Media need to know about it?" If that answer is solid, objections 1 and 2 mostly go away. The rest is naming, a couple of definitions and a cardinality, all cheap to fix on the board.