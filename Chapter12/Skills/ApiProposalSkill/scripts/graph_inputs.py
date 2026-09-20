#!/usr/bin/env python3
"""Read a domain knowledge graph and print what context-map-api-proposer needs.

    python3 graph_inputs.py graph.ttl                # Markdown to stdout
    python3 graph_inputs.py graph.ttl --json out.json
    python3 graph_inputs.py graph.ttl --context "Rack management"   # one context's borders only

A domain knowledge graph (from `domain-knowledge-graph`, optionally grown by
`adr-and-principles-ingester`) has no "context map" node. The map is IMPLICIT:
it is whatever the graph's canvases, boards, event models, stories and
glossaries say crosses from one bounded context to another. This script makes
that implicit map explicit, and never adds to it:

  B1  a canvas Message (dkg:Message, dkg:from / dkg:to)         typed cmd/qry/evt
  B2  an Event Model translation slice spanning two swimlanes    typed ?
  B3  a board/model edge whose two ends sit in different contexts
        Policy  reactsTo  Event      -> evt, event's context -> policy's
        Command triggers  Event      -> cmd, command's       -> event's
        X       reads     ReadModel  -> qry, reader's        -> read model's
  B4  a story handoff: sentence n in one lane, n+1 in another    typed ?
  B5  a glossary arrow from a context's own term to a term
      another context owns                                       typed ?

B1 is a drawn border. B2-B3 are borders the artifacts imply. B4-B5 say that a
border exists and what noun sits on it, but not what kind of message it is.
The rung is printed on every row so the reader can tell the difference.

Nothing here decides sync vs async and nothing here names an operation. It
collects evidence; SKILL.md Steps 1-2 do the judging.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from collections import defaultdict

try:
    from rdflib import Graph, RDF, RDFS, Namespace, URIRef, Literal, BNode
    from rdflib.namespace import SKOS
except ImportError:
    sys.exit("rdflib is required:  pip install rdflib --break-system-packages")

DKG = Namespace("https://w3id.org/dkg/ns#")

# The base vocabulary's subclass tree, so the script works on a graph that does
# not carry dkg.ttl inline. Anything the graph itself declares is added on top.
SUBCLASSES = {
    "BoundedContext": {"BoundedContext", "Lane", "Swimlane"},
    "Actor": {"Actor", "CustomerSegment", "KeyPartner", "ExternalSystem"},
    "Occurrence": {"Occurrence", "DomainEvent", "PivotalEvent"},
    "Activity": {"Activity", "Command"},
    "Concept": {"Concept", "Term", "Entity", "ValueObject", "AggregateRoot",
                "WorkObject", "Aggregate", "ReadModel", "BusinessObject"},
    "Rule": {"Rule", "Policy"},
    "Question": {"Question", "Hotspot"},
}
CONF = {DKG.OnArtifact: "on-artifact", DKG.Implied: "implied", DKG.Inferred: "inferred"}
DEAD_STATUS = {"superseded", "deprecated", "rejected", "proposed", "draft"}


def norm(s: str) -> str:
    return re.sub(r"[^a-z0-9]+", " ", s.lower()).strip()


class G:
    def __init__(self, path: str):
        self.g = Graph()
        self.g.parse(path, format="turtle")
        self._types = defaultdict(set)
        for s, o in self.g.subject_objects(RDF.type):
            if isinstance(o, URIRef) and str(o).startswith(str(DKG)):
                self._types[s].add(str(o)[len(str(DKG)):])
        # subclass declarations the graph itself carries
        self.sub = {k: set(v) for k, v in SUBCLASSES.items()}
        changed = True
        while changed:
            changed = False
            for c, p in self.g.subject_objects(RDFS.subClassOf):
                if not (str(c).startswith(str(DKG)) and str(p).startswith(str(DKG))):
                    continue
                cn, pn = str(c)[len(str(DKG)):], str(p)[len(str(DKG)):]
                for members in self.sub.values():
                    if pn in members and cn not in members:
                        members.add(cn)
                        changed = True

    # -- basics -----------------------------------------------------------
    def types(self, n):
        return self._types.get(n, set())

    def isa(self, n, spine: str) -> bool:
        return bool(self.types(n) & self.sub.get(spine, {spine}))

    def all_of(self, spine: str):
        return sorted({n for n, ts in self._types.items() if ts & self.sub.get(spine, {spine})},
                      key=lambda n: self.label(n))

    def label(self, n) -> str:
        if isinstance(n, Literal):
            return str(n)
        for p in (SKOS.prefLabel, RDFS.label):
            v = self.g.value(n, p)
            if v is not None:
                return str(v)
        s = str(n)
        return re.sub(r"^[A-Za-z]+_", "", s.rsplit("#", 1)[-1].rsplit("/", 1)[-1])

    def qname(self, n) -> str:
        s = str(n)
        return ":" + s.rsplit("#", 1)[-1] if "#" in s else s

    def alts(self, n):
        return sorted({str(v) for v in self.g.objects(n, SKOS.altLabel)})

    def comments(self, n):
        return [str(v) for v in self.g.objects(n, RDFS.comment)]

    def contexts_of(self, n):
        return sorted(set(self.g.objects(n, DKG.inContext)), key=self.label)

    def conf(self, n) -> str:
        return CONF.get(self.g.value(n, DKG.confidence), "?")

    def where(self, n) -> str:
        """'<artifact label> — <locator>' for every source, newest spelling kept."""
        srcs = [self.label(a) for a in self.g.objects(n, DKG.source)]
        loc = self.g.value(n, DKG.locator)
        out = "; ".join(sorted(srcs)) or "(no source)"
        return f"{out} — {loc}" if loc else out

    def dropped(self, n) -> bool:
        """A revision of one of the node's own sources no longer shows it."""
        return (n, DKG.absentFrom, None) in self.g

    def source_kinds(self, n):
        kinds = set()
        for a in self.g.objects(n, DKG.source):
            kinds |= self.types(a)
        return kinds


# ---------------------------------------------------------------------------
# contexts
# ---------------------------------------------------------------------------
def collect_contexts(k: G):
    rows = []
    for c in k.all_of("BoundedContext"):
        same = sorted({k.label(o) for o in k.g.objects(c, DKG.proposedSameAs)} |
                      {k.label(s) for s in k.g.subjects(DKG.proposedSameAs, c)})
        rows.append({
            "iri": k.qname(c), "label": k.label(c), "alt": k.alts(c),
            "kind": sorted(k.types(c)), "confidence": k.conf(c),
            "sources": sorted(k.label(a) for a in k.g.objects(c, DKG.source)),
            "proposedSameAs": same, "dropped": k.dropped(c),
            "has_canvas": any("BoundedContextCanvas" in k.types(a) for a in k.g.objects(c, DKG.source)),
            "roles": sorted(str(v) for v in k.g.objects(c, DKG.domainRole)),
        })
    return rows


# ---------------------------------------------------------------------------
# borders
# ---------------------------------------------------------------------------
def endpoint(k: G, n):
    """(label, offboard?) — a Message end may be a context or an actor/system."""
    return k.label(n), not k.isa(n, "BoundedContext")


def concept_of(k: G, n):
    """Concepts a node is about, with glossary kind where one exists."""
    out = []
    for c in list(k.g.objects(n, DKG.mentions)) + list(k.g.objects(n, DKG.actsOn)) + \
             list(k.g.objects(n, DKG.produces)):
        if k.isa(c, "Concept"):
            kind = next((t for t in ("AggregateRoot", "Entity", "ValueObject") if t in k.types(c)), None)
            out.append({"label": k.label(c), "kind": kind or "concept", "iri": k.qname(c)})
    seen, uniq = set(), []
    for c in out:
        if c["iri"] not in seen:
            seen.add(c["iri"])
            uniq.append(c)
    return uniq


def collect_borders(k: G):
    borders = []

    def add(rung, frm, to, crossing, kind, node, note="", concepts=None):
        f_lab, f_off = endpoint(k, frm)
        t_lab, t_off = endpoint(k, to)
        borders.append({
            "rung": rung, "from": f_lab, "to": t_lab,
            "from_offboard": f_off, "to_offboard": t_off,
            "crossing": crossing, "type": kind,
            "confidence": k.conf(node), "evidence": f"{k.qname(node)} · {k.where(node)}",
            "comments": k.comments(node), "note": note,
            "concepts": concepts if concepts is not None else concept_of(k, node),
            "dropped": k.dropped(node), "stories": [], "corroborated_by": [],
        })

    # B1 — canvas messages -------------------------------------------------
    msgs = [m for m in k.all_of("Message") if k.g.value(m, DKG["from"]) and k.g.value(m, DKG.to)]
    paired = set()
    for m in msgs:
        if m in paired:
            continue
        frm, to = k.g.value(m, DKG["from"]), k.g.value(m, DKG.to)
        owner = k.g.value(m, DKG.inContext)
        twin = next((o for o in msgs if o is not m and o not in paired
                     and k.g.value(o, DKG["from"]) == frm and k.g.value(o, DKG.to) == to
                     and norm(k.label(o)) == norm(k.label(m))
                     and k.g.value(o, DKG.inContext) != owner), None)
        other = to if owner == frm else frm
        other_has_canvas = any(k.g.value(x, DKG.inContext) == other for x in msgs)
        if twin is not None:
            paired.add(twin)
            note = "on both canvases"
            kt, km = str(k.g.value(twin, DKG.messageKind) or "?"), str(k.g.value(m, DKG.messageKind) or "?")
            if kt != km:
                note += f" — BUT typed {km} on one and {kt} on the other"
        elif not k.isa(other, "BoundedContext"):
            note = "other end is off-board (no canvas expected)"
        elif other_has_canvas:
            note = f"only on {k.label(owner)}'s canvas — {k.label(other)}'s canvas does not claim it"
        else:
            note = f"only on {k.label(owner)}'s canvas — {k.label(other)} has no canvas in the graph"
        add("B1", frm, to, k.label(m), str(k.g.value(m, DKG.messageKind) or "?"), m, note)
        if k.g.value(m, DKG.carries):
            borders[-1]["carries"] = str(k.g.value(m, DKG.carries))
        if twin is not None:
            borders[-1]["evidence"] += f"  +  {k.qname(twin)} · {k.where(twin)}"
        paired.add(m)

    def ctx1(n):
        cs = k.contexts_of(n)
        return cs[0] if len(cs) == 1 else None   # straddlers are not border evidence

    # B2 — translation slices ---------------------------------------------
    for s in k.all_of("Slice"):
        if "translation" not in norm(str(k.g.value(s, DKG.sliceKind) or "")):
            continue
        members = sorted(set(k.g.subjects(DKG.mentions, s)), key=lambda n: int(k.g.value(n, DKG.sequence) or 0))
        lanes = []
        for mbr in members:
            c = ctx1(mbr)
            if c is not None and c not in lanes:
                lanes.append(c)
        if len(lanes) >= 2:
            add("B2", lanes[0], lanes[1], k.label(s), "?", s,
                note="translation slice; direction read from member order — confirm",
                concepts=[c for mbr in members for c in concept_of(k, mbr)])

    # B3 — typed edges across contexts ------------------------------------
    for pol, ev in k.g.subject_objects(DKG.reactsTo):
        a, b = ctx1(ev), ctx1(pol)
        if a is not None and b is not None and a != b:
            add("B3", a, b, k.label(ev), "evt", ev,
                note=f"policy '{k.label(pol)}' in {k.label(b)} reacts to it")
    for cmd, ev in k.g.subject_objects(DKG.triggers):
        a, b = ctx1(cmd), ctx1(ev)
        if a is not None and b is not None and a != b:
            kind = "cmd" if k.isa(cmd, "Activity") else "?"
            add("B3", a, b, k.label(cmd), kind, cmd,
                note=f"triggers '{k.label(ev)}' inside {k.label(b)}")
    for reader, rm in k.g.subject_objects(DKG.reads):
        a, b = ctx1(reader), ctx1(rm)
        if a is not None and b is not None and a != b:
            add("B3", a, b, k.label(rm), "qry", rm,
                note=f"read by '{k.label(reader)}' in {k.label(a)}")

    # B5 — glossary arrows onto another context's term ---------------------
    for r in k.all_of("Relationship"):
        f, t = k.g.value(r, DKG["from"]), k.g.value(r, DKG.to)
        if f is None or t is None:
            continue
        a, b = (k.g.value(r, DKG.inContext) or ctx1(f)), ctx1(t)
        if a is not None and b is not None and a != b:
            card = k.g.value(r, DKG.cardinality)
            given = k.g.value(r, DKG.cardinalityGiven)
            add("B5", a, b, f"{k.label(f)} {k.g.value(r, DKG.verb) or '—'} {k.label(t)}"
                + (f" [{card}{'' if str(given).lower() == 'true' else ', derived'}]" if card else ""),
                "?", r, note=f"{k.label(a)} holds a reference to a term {k.label(b)} owns",
                concepts=[{"label": k.label(t), "iri": k.qname(t),
                           "kind": next((x for x in ("AggregateRoot", "Entity", "ValueObject")
                                         if x in k.types(t)), "concept")}])

    # fold lower rungs into a B1 row that says the same thing ---------------
    keep = []
    for b in sorted(borders, key=lambda x: x["rung"]):
        host = next((h for h in keep if h["rung"] < b["rung"] and h["from"] == b["from"]
                     and h["to"] == b["to"] and
                     (norm(h["crossing"]) == norm(b["crossing"]) or
                      ({c["iri"] for c in h["concepts"]} & {c["iri"] for c in b["concepts"]}
                       and b["rung"] != "B5" and h["type"] == b["type"]))), None)
        if host is not None:
            host["corroborated_by"].append(f"{b['rung']}: {b['evidence']}")
        else:
            keep.append(b)
    return keep


# ---------------------------------------------------------------------------
# stories -> journeys, and B4
# ---------------------------------------------------------------------------
def collect_journeys(k: G, borders):
    journeys = []
    stories = [a for a, ts in k._types.items() if "DomainStory" in ts]
    for st in sorted(stories, key=k.label):
        sens = [s for s in k.all_of("Sentence") if (s, DKG.source, st) in k.g]
        sens.sort(key=lambda s: (int(k.g.value(s, DKG.sequence) or 0), k.label(s)))
        steps, prev_ctx = [], None
        for s in sens:
            actor = k.g.value(s, DKG.performedBy)
            ctxs, via = k.contexts_of(s), "sentence"
            if not ctxs and actor is not None:
                ctxs, via = k.contexts_of(actor), "actor's lane"
            ctx = ctxs[0] if len(ctxs) == 1 else None
            step = {"n": str(k.g.value(s, DKG.sequence) or "?"), "text": k.label(s),
                    "actor": k.label(actor) if actor is not None else "?",
                    "context": k.label(ctx) if ctx is not None else
                    ("(straddles: " + ", ".join(k.label(c) for c in ctxs) + ")" if ctxs else "(no lane)"),
                    "context_via": via, "crosses": None}
            if ctx is not None and prev_ctx is not None and ctx != prev_ctx:
                step["crosses"] = f"{k.label(prev_ctx)} → {k.label(ctx)}"
                ref = f"{k.label(st)} #{step['n']}"
                hits = [b for b in borders if {b["from"], b["to"]} == {k.label(prev_ctx), k.label(ctx)}]
                for b in hits:
                    b["stories"].append(ref)
                if not hits:
                    borders.append({
                        "rung": "B4", "from": k.label(prev_ctx), "to": k.label(ctx),
                        "from_offboard": False, "to_offboard": False,
                        "crossing": step["text"], "type": "?", "confidence": k.conf(s),
                        "evidence": f"{k.qname(s)} · {k.where(s)}", "comments": [],
                        "note": "story handoff only — no canvas, board or model draws this border",
                        "concepts": concept_of(k, s), "dropped": False,
                        "stories": [ref], "corroborated_by": []})
            if ctx is not None:
                prev_ctx = ctx
            steps.append(step)
        touched = []
        for s in steps:
            if not s["context"].startswith("(") and s["context"] not in touched:
                touched.append(s["context"])
        journeys.append({
            "story": k.label(st), "iri": k.qname(st), "sentences": len(steps),
            "contexts_in_order": touched, "crossings": sum(1 for s in steps if s["crosses"]),
            "has_lanes": any(not s["context"].startswith("(no lane") for s in steps),
            "needs_arazzo": sum(1 for s in steps if s["crosses"]) > 0, "steps": steps})
    return journeys


# ---------------------------------------------------------------------------
# glossary
# ---------------------------------------------------------------------------
def collect_glossary(k: G):
    crossing_concepts = set()
    terms = []
    for c in k.all_of("Concept"):
        ts = k.types(c)
        gl = ts & {"Term", "Entity", "ValueObject", "AggregateRoot"}
        kind = ("aggregate root" if "AggregateRoot" in ts else "entity" if "Entity" in ts
        else "value object" if "ValueObject" in ts else "term (unmarked)" if "Term" in ts
        else "/".join(sorted(ts)) + " — not a glossary term")
        ctxs = k.contexts_of(c)
        terms.append({
            "label": k.label(c), "iri": k.qname(c), "kind": kind, "in_glossary": bool(gl),
            "alt": k.alts(c), "owner": [k.label(x) for x in ctxs],
            "straddler": len(ctxs) > 1,
            "renamedTo": [k.label(o) for o in k.g.objects(c, DKG.renamedTo)],
            "proposedSameAs": sorted({k.label(o) for o in k.g.objects(c, DKG.proposedSameAs)} |
                                     {k.label(s) for s in k.g.subjects(DKG.proposedSameAs, c)}),
            "n_sources": len(set(k.g.objects(c, DKG.source))), "dropped": k.dropped(c)})
    rels = []
    for r in k.all_of("Relationship"):
        f, t = k.g.value(r, DKG["from"]), k.g.value(r, DKG.to)
        if f is None or t is None:
            continue
        given = k.g.value(r, DKG.cardinalityGiven)
        rels.append({
            "from": k.label(f), "verb": str(k.g.value(r, DKG.verb) or ""), "to": k.label(t),
            "cardinality": str(k.g.value(r, DKG.cardinality) or ""),
            "given": (str(given).lower() == "true") if given is not None else None,
            "contested": (r, DKG.contradicts, None) in k.g or (None, DKG.contradicts, r) in k.g,
            "superseded": (None, DKG.supersedes, r) in k.g,
            "evidence": f"{k.qname(r)} · {k.where(r)}"})
    isa = [{"sub": k.label(s), "super": k.label(o)} for s, o in k.g.subject_objects(RDFS.subClassOf)
           if k.isa(s, "Concept") and k.isa(o, "Concept")]
    return {"terms": terms, "relationships": rels, "is_a": isa}


# ---------------------------------------------------------------------------
# decisions, principles, questions, rules
# ---------------------------------------------------------------------------
def collect_architecture(k: G, ctx_labels):
    decisions = []
    for d in k.all_of("Decision"):
        status = str(k.g.value(d, DKG.status) or "")
        decisions.append({
            "id": k.qname(d), "label": k.label(d), "status": status,
            "current": norm(status) not in DEAD_STATUS and (None, DKG.supersedes, d) not in k.g,
            "decided": str(k.g.value(d, DKG.decidedOption) or ""),
            "answers": [k.label(q) for q in k.g.objects(d, DKG.answers)],
            "honors": [k.label(p) for p in k.g.objects(d, DKG.honors)],
            "overrides": [k.label(p) for p in k.g.objects(d, DKG.overrides)],
            "names_contexts": [c for c in ctx_labels
                               if norm(c) and norm(c) in norm(k.label(d) + " " + str(k.g.value(d, DKG.decidedOption) or "")
                                                              + " " + " ".join(k.label(q) for q in k.g.objects(d, DKG.answers)))]})
    principles = []
    for p in k.all_of("Principle"):
        principles.append({
            "id": str(k.g.value(p, DKG.principleId) or k.qname(p)), "label": k.label(p),
            "statement": str(k.g.value(p, DKG.statement) or ""),
            "scope": str(k.g.value(p, DKG.scope) or ""),
            "testable": str(k.g.value(p, DKG.testable) or "").lower() == "true"})
    answered = set(k.g.objects(None, DKG.answers))
    questions = []
    for q in k.all_of("Question"):
        raised_by = [k.label(s) for s in k.g.subjects(DKG.raises, q)]
        questions.append({"label": k.label(q), "iri": k.qname(q), "open": q not in answered,
                          "raised_by": raised_by, "where": k.where(q),
                          "contexts": [k.label(c) for c in k.contexts_of(q)]})
    rules = []
    for r in k.all_of("Rule"):
        if "Policy" in k.types(r):
            continue
        rules.append({"label": k.label(r), "contexts": [k.label(c) for c in k.contexts_of(r)],
                      "where": k.where(r), "confidence": k.conf(r)})
    return {"decisions": decisions, "principles": principles, "questions": questions, "rules": rules}


# ---------------------------------------------------------------------------
# coverage — what the graph CANNOT supply
# ---------------------------------------------------------------------------
def coverage(k: G, contexts, borders, journeys, glossary):
    kinds = defaultdict(list)
    for a, ts in k._types.items():
        if (a, DKG.ingestedAt, None) in k.g or ts & {"DomainStory", "VisualGlossary", "EventStormingBoard",
                                                     "EventModel", "BoundedContextCanvas", "ADRLog",
                                                     "PrinciplesDocument"}:
            for t in ts - {"Artifact"}:
                kinds[t].append(k.label(a))
    gaps = []
    live = [c for c in contexts if not c["dropped"]]
    if len(live) < 2:
        gaps.append("FEWER THAN TWO bounded contexts in the graph — there is no border to propose an API for. "
                    "Ingest a board, an Event Model, laned stories or canvases first, or fall back to artifact mode.")
    if not borders:
        gaps.append("NO border evidence on any rung (B1–B5). The graph names contexts but nothing crosses between them. "
                    "Ask for the context map as an image/text and work in mixed mode.")
    elif not any(b["rung"] == "B1" for b in borders):
        gaps.append("No canvas Messages (B1) — every border is implied (B2–B5), none is drawn. "
                    "Every crossing's cmd/qry/evt type is weaker than a context map would give.")
    if not journeys:
        gaps.append("No Domain Story in the graph — Step 2's story rung is silent and Step 6 has no journeys. "
                    "Ask for stories (image or text), or mark every operation (no story evidence).")
    elif not any(j["has_lanes"] for j in journeys):
        gaps.append("Stories are in the graph but none has lanes — sentences cannot be placed in a context, "
                    "so no journey can be shown to cross a border.")
    if not any(t["in_glossary"] for t in glossary["terms"]):
        gaps.append("No Visual Glossary terms in the graph — names fall back to canvas/board wording and every "
                    "field shape is (unconfirmed shape).")
    gaps.append("Staleness windows are not part of the graph vocabulary — Step 2's staleness rung is silent unless "
                "a Message's rdfs:comment happens to state one (printed under the border when it does).")
    bordered = {b["from"] for b in borders} | {b["to"] for b in borders}
    for c in live:
        if c["label"] not in bordered:
            gaps.append(f"Context '{c['label']}' has no border on any rung — isolated, or its crossings were never drawn.")
        if c["proposedSameAs"]:
            gaps.append(f"Context '{c['label']}' is a PENDING MERGE with {', '.join(c['proposedSameAs'])} — "
                        f"a border between them may not be a border at all. Ask before sketching it.")
        if c["confidence"] == "inferred":
            gaps.append(f"Context '{c['label']}' is dkg:Inferred — it exists only because an analysis or canvas needed it.")
    return {"artifacts": {t: sorted(v) for t, v in sorted(kinds.items())}, "gaps": gaps}


# ---------------------------------------------------------------------------
# render
# ---------------------------------------------------------------------------
def cell(s) -> str:
    return str(s).replace("|", "\\|").replace("\n", " ")


def render(data) -> str:
    o = []
    w = o.append
    w(f"# Graph inputs — {data['file']}\n")
    w("## 0. What the graph holds\n")
    for t, labs in data["coverage"]["artifacts"].items():
        w(f"- **{t}** ({len(labs)}): {', '.join(labs)}")
    w("\n### What it cannot supply — carry every line into the report's Coverage gaps\n")
    for gp in data["coverage"]["gaps"]:
        w(f"- {gp}")

    w("\n## 1. Bounded contexts\n")
    w("| Context | Also spelled | Class | Confidence | Canvas? | Pending merge with | Sources |")
    w("|---|---|---|---|---|---|---|")
    for c in data["contexts"]:
        w(f"| {cell(c['label'])}{' *(dropped in a revision)*' if c['dropped'] else ''} | {cell(', '.join(c['alt']) or '—')} | "
          f"{'/'.join(c['kind'])} | {c['confidence']} | {'yes' if c['has_canvas'] else 'no'} | "
          f"{cell(', '.join(c['proposedSameAs']) or '—')} | {cell('; '.join(c['sources']))} |")

    w("\n## 2. Border candidates — the implicit context map\n")
    w("Rung: B1 canvas message · B2 translation slice · B3 cross-context board/model edge · "
      "B4 story handoff only · B5 glossary reference only. `?` = the graph does not type it; do not guess here.\n")
    w("| # | From | To | Crossing | Type | Rung | Graph confidence | Concepts (glossary kind) | Story sentences at this pair of contexts | Note |")
    w("|---|---|---|---|---|---|---|---|---|---|")
    for i, b in enumerate(data["borders"], 1):
        f = b["from"] + (" *(off-board)*" if b["from_offboard"] else "")
        t = b["to"] + (" *(off-board)*" if b["to_offboard"] else "")
        cons = ", ".join(f"{c['label']} ({c['kind']})" for c in b["concepts"]) or "—"
        w(f"| {i} | {cell(f)} | {cell(t)} | {cell(b['crossing'])} | {b['type']} | {b['rung']} | {b['confidence']} | "
          f"{cell(cons)} | {cell(', '.join(b['stories']) or '(no story evidence)')} | "
          f"{cell(b['note'])}{' · DROPPED in a later revision' if b['dropped'] else ''} |")
    w("\n### Evidence per border\n")
    for i, b in enumerate(data["borders"], 1):
        w(f"{i}. `{b['evidence']}`")
        if b.get("carries"):
            w(f"   - carries: {b['carries']}")
        for cm in b["comments"]:
            w(f"   - comment: {cm}")
        for cb in b["corroborated_by"]:
            w(f"   - corroborated by {cb}")

    w("\n## 3. Journeys — one per Domain Story in the graph\n")
    if not data["journeys"]:
        w("*(none)*")
    for j in data["journeys"]:
        verdict = "candidate Arazzo file" if j["needs_arazzo"] else "single context or unlaned — NO Arazzo file"
        w(f"### {j['story']} — {j['crossings']} crossing(s), {' → '.join(j['contexts_in_order']) or '(no lanes)'} — {verdict}\n")
        w("| # | Actor | Sentence | Context | Crosses |")
        w("|---|---|---|---|---|")
        for s in j["steps"]:
            ctx = s["context"] + ("" if s["context_via"] == "sentence" or s["context"].startswith("(") else " *(via actor's lane)*")
            w(f"| {s['n']} | {cell(s['actor'])} | {cell(s['text'])} | {cell(ctx)} | {cell(s['crosses'] or '')} |")
        w("")

    gl = data["glossary"]
    w("## 4. Terminology and shape\n")
    w("| Term | Kind | Owner context | Also spelled | Renamed across a border to | Pending merge | Sources |")
    w("|---|---|---|---|---|---|---|")
    for t in gl["terms"]:
        owner = ", ".join(t["owner"]) or "—"
        if t["straddler"]:
            owner += " **(straddler)**"
        w(f"| {cell(t['label'])}{' *(dropped)*' if t['dropped'] else ''} | {t['kind']} | {cell(owner)} | "
          f"{cell(', '.join(t['alt']) or '—')} | {cell(', '.join(t['renamedTo']) or '—')} | "
          f"{cell(', '.join(t['proposedSameAs']) or '—')} | {t['n_sources']} |")
    w("\n| From | Verb | To | Cardinality | Drawn or derived | Flags |")
    w("|---|---|---|---|---|---|")
    for r in gl["relationships"]:
        given = {True: "drawn", False: "DERIVED", None: "not recorded"}[r["given"]]
        flags = ", ".join(x for x, on in (("contested", r["contested"]), ("superseded", r["superseded"])) if on) or "—"
        w(f"| {cell(r['from'])} | {cell(r['verb'])} | {cell(r['to'])} | {r['cardinality'] or '—'} | {given} | {flags} |")
    for e in gl["is_a"]:
        w(f"\n- {e['sub']} **is-a** {e['super']}")

    a = data["architecture"]
    w("\n## 5. Decisions and principles\n")
    w("| Decision | Status | Current? | Decided | Answers | Names contexts | Honors / overrides |")
    w("|---|---|---|---|---|---|---|")
    for d in a["decisions"]:
        ho = "; ".join(filter(None, ["honors " + ", ".join(d["honors"]) if d["honors"] else "",
                                     "OVERRIDES " + ", ".join(d["overrides"]) if d["overrides"] else ""])) or "—"
        w(f"| {d['id']} {cell(d['label'])} | {d['status'] or '—'} | {'yes' if d['current'] else 'NO — history only'} | "
          f"{cell(d['decided'])} | {cell('; '.join(d['answers']) or '—')} | {cell(', '.join(d['names_contexts']) or '—')} | {cell(ho)} |")
    w("\n| Principle | Statement | Scope | Testable |")
    w("|---|---|---|---|")
    for p in a["principles"]:
        w(f"| {p['id']} {cell(p['label'])} | {cell(p['statement'])} | {cell(p['scope'] or '—')} | "
          f"{'yes' if p['testable'] else 'NO — settles nothing'} |")

    w("\n## 6. Open questions the graph already holds\n")
    for q in a["questions"]:
        if q["open"]:
            w(f"- {q['label']}  — raised by {', '.join(q['raised_by']) or '—'} · {q['where']}")
    w("\n## 7. Rules per context — candidates for later `onFailure` actions\n")
    for r in a["rules"]:
        w(f"- [{', '.join(r['contexts']) or 'no context'}] {r['label']}  ({r['confidence']}; {r['where']})")
    return "\n".join(o) + "\n"


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("graph")
    ap.add_argument("--json", help="also write the full extraction as JSON")
    ap.add_argument("--context", help="keep only borders touching this context label")
    a = ap.parse_args()

    k = G(a.graph)
    contexts = collect_contexts(k)
    borders = collect_borders(k)
    journeys = collect_journeys(k, borders)          # may append B4 rows
    glossary = collect_glossary(k)
    arch = collect_architecture(k, [c["label"] for c in contexts] + [x for c in contexts for x in c["alt"]])
    cov = coverage(k, contexts, borders, journeys, glossary)
    pending = {c["label"]: c["proposedSameAs"] for c in contexts if c["proposedSameAs"]}
    for b in borders:
        for end, other in ((b["from"], b["to"]), (b["to"], b["from"])):
            if end in pending and "NOT A BORDER" not in b["note"]:
                b["note"] += (f" · NOT A BORDER if '{end}' = '{other}' is confirmed" if other in pending[end]
                              else f" · '{end}' is a pending merge with {', '.join(pending[end])} — "
                                   f"this may be that context's border")
    if a.context:
        want = norm(a.context)
        borders = [b for b in borders if want in (norm(b["from"]), norm(b["to"]))]
    data = {"file": a.graph, "contexts": contexts, "borders": borders, "journeys": journeys,
            "glossary": glossary, "architecture": arch, "coverage": cov}
    if a.json:
        with open(a.json, "w") as fh:
            json.dump(data, fh, indent=2, ensure_ascii=False)
    sys.stdout.write(render(data))


if __name__ == "__main__":
    main()