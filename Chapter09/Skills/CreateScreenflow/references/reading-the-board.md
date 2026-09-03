# Reading an Event Model off images

Read this at Step 1, and again at Step 2 whenever a wireframe's identity is
ambiguous. Everything downstream — every screen, every arrow — inherits the
quality of this read, so it is worth being slow here and fast later.

## 1. What an Event Model looks like, at low resolution

The layout is stable enough to navigate a blurry photo:

- **A wireframe row along the top** — hand-drawn boxes with lines for fields,
  or actual mockups. These are the screens.
- **Blue commands** immediately below a wireframe, and **orange events**
  immediately right of a command. A wireframe with a blue sticky under it is a
  State Change; the user does something there.
- **Green read models** feeding *upward* into a wireframe. A wireframe fed by
  green is a State View; the user reads something there.
- **Lilac policies / hexagons**, or a small machine icon on a sticky, sitting
  between an event and a later command. Automation. No user, therefore no
  screen.
- **An event in one lane wired to a command in another** — Translation.
  Usually invisible to the user.
- **Swimlanes** as horizontal bands, labelled at the left edge of the first
  image only. This is why lane labels must be resolved from the image where
  they're legible.

Colour is the fastest signal but not a reliable one on a phone photo of a
whiteboard under warm light. When colour is doubtful, use position: commands
sit under wireframes, events sit right of commands, read models sit under
events and feed up.

**Tense settles the blue/orange question.** "Submit claim" is a command;
"Claim submitted" is an event. If a sticky's colour is unreadable, its verb
tense usually isn't.

## 2. Stitching several images

An Event Model's spine is one continuous left-to-right timeline, so multiple
images are the norm, not an edge case.

**Order.** Establish it from filenames, timestamps, or the lane content, and
state the order you used in output §0. If two images could plausibly go either
way, ask — a reversed pair inverts the whole flow.

**Lane correspondence.** Before matching any sticky, confirm the swimlanes
recur identically: same names, same top-to-bottom order, same count. If image
2 appears to have one lane fewer, the lane is probably empty there rather than
absent — check the vertical positions against image 1 before concluding
anything.

**Seams.** Classify each junction explicitly:

| Seam type | How it looks | What to do |
|---|---|---|
| **Overlap** | The last column of image *N* reappears as the first column of *N+1* | Dedupe. Match on sticky text *and* lane position, not text alone |
| **Abutment** | *N* ends cleanly, *N+1* opens on the next fact | Nothing lost; say so |
| **Gap** | Daylight — *N* ends mid-workflow and *N+1* opens somewhere unrelated | A hole in the *capture*. Flag it; never bridge it with an invented slice |

**Lines leaving frame.** A connector running off the right edge of image *N*
is unresolved. It may continue, or it may end just outside the crop. Record it
as an open question rather than wiring it to the nearest plausible target in
image *N+1*.

**Repeated workflows across images.** The same six-sticky block appearing in
two images is usually **one workflow reachable from two triggers**, not two
workflows. Match on content *and* lane position. This distinction is the
single highest-leverage call in the stitch: getting it wrong doubles a whole
region of the screen flow, and the duplicate looks plausible enough that
nobody catches it later.

## 3. Wireframe → screen: the ambiguous cases

Step 2 of the skill gives the decision table. These are the cases that
actually come up, and how to reason about them.

**The form and the list.** A State Change wireframe (`Search invoices` with a
query field) followed immediately by a State View wireframe (`Invoices`
showing results) is almost always **one screen** — a search page. Merge them
unless the board draws a navigation step between, or the read model is fed by
events from a different phase entirely.

**The same list, before and after.** A list wireframe drawn at three points on
the timeline, each time showing more items, is **one screen**. The Event Model
redraws it to prove information completeness at each step; that is a property
of the method, not evidence of three pages.

**The detail page.** A wireframe fed by a read model keyed to a single item,
where an earlier wireframe shows many, is **two screens** — list and detail.
Cardinality in the read model name (`Invoices` vs `Selected invoice`) is the
tell.

**The trigger with no command.** An event with no blue sticky above it and no
policy behind it (`Payment failed`, `Approval overdue`) has **no
screen and no honest edge**. Something raises it and the board doesn't say
what. Draw the downstream screen, draw the inbound edge as `auto: <unstated
trigger>`, and put the question in §5. Do not invent a button.

**The automation with a wireframe.** Occasionally a board draws a waiting or
status screen for an automation — "waiting for a responder". That *is* a
screen (hexagon, `auto` class), because the user is looking at it. Rare;
require the wireframe to actually be drawn.

**The lane with no wireframes.** Notification lanes, integration lanes and
machine-responder lanes usually carry only events and policies. They
contribute **edges** to the flow and no nodes. State this in §0 rather than
letting the reader wonder whether you dropped a lane.

## 4. Before you leave Step 1

Publish a transcription strip and, on a non-trivial board, get it confirmed:

```
# | Lane      | Wireframe (caption) | Command         | Event             | Read model | Image
1 | Invoicing | Search              | Search invoices | Invoices searched | Invoices   | 1
2 | Invoicing | Invoices            | —               | —                 | Invoices   | 1
```

Two columns of that table decide everything afterwards: the wireframe caption
(which becomes the screen name) and the image number (which is how anyone
checks your read against the photo). Fill both even when the caption is a
guess — an italic `(caption illegible; named from the command)` is worth more
than a confident invention.