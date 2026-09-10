#!/usr/bin/env python3
"""Render browsable views of a domain knowledge graph.

    python3 views.py graph.ttl --out views/
    python3 views.py graph.ttl --out views/ --lens language

Writes views/index.md: the artifact register, five Mermaid lenses (strategy,
capability, language, flow, traceability), the orphan report and a
"worth a second look" section. The .ttl stays the source of truth; this is how
a person reads it.
"""
from __future__ import annotations

import os
import sys
import argparse
from collections import defaultdict

try:
    from rdflib import Graph, RDF, RDFS, Namespace, URIRef
    from rdflib.namespace import SKOS, OWL
except ImportError:
    sys.exit("rdflib is required:  pip install rdflib --break-system-packages")

DKG = Namespace("https://w3id.org/dkg/ns#")
LENSES = ["strategy", "capability", "language", "flow", "traceability"]

BOOKKEEPING = {
    RDF.type, RDFS.label, RDFS.comment, SKOS.prefLabel, SKOS.altLabel,
    DKG.source, DKG.locator, DKG.confidence, DKG.ingestedAt, DKG.votes,
    DKG.sequence, DKG.metric, DKG.cardinality, DKG.cardinalityGiven,
    DKG.verb, DKG.hasState, DKG.sliceKind, DKG.visibility, DKG.literal,
    OWL.imports,
}


def lbl(g, n):
    for p in (SKOS.prefLabel, RDFS.label):
        v = g.value(n, p)
        if v:
            return str(v)
    return str(n).split("#")[-1]


def mid(n):
    """A Mermaid-safe node id."""
    return str(n).split("#")[-1].replace("-", "_").replace(".", "_")


def esc(s, limit=52):
    t = str(s).replace('"', "'").replace("\n", " ")
    return t if len(t) <= limit else t[: limit - 1] + "…"


def typed(g, cls):
    """Instances of cls or any of its dkg: subclasses."""
    out = set(g.subjects(RDF.type, cls))
    for sub in g.subjects(RDFS.subClassOf, cls):
        out |= typed(g, sub)
    return out


def load_vocab(g: Graph) -> Graph:
    """The vocabulary's subclass axioms, if dkg.ttl sits beside the graph."""
    here = os.path.dirname(os.path.abspath(__file__))
    for cand in (os.path.join(here, "..", "assets", "dkg.ttl"),
                 os.path.join(os.getcwd(), "dkg.ttl")):
        if os.path.exists(cand):
            try:
                g.parse(cand, format="turtle")
            except Exception as exc:  # a partial vocab load breaks subclass
                print(f"WARNING: could not load {cand}: {exc}", file=sys.stderr)
            break
    return g


# --------------------------------------------------------------------------
# lenses
# --------------------------------------------------------------------------

def edges_of(g, kinds, nodes=None):
    for p in kinds:
        for s, o in g.subject_objects(p):
            if nodes is None or (s in nodes and o in nodes):
                yield s, p, o


def flow_lens(g):
    lines = ["```mermaid", "flowchart LR"]
    events = sorted(typed(g, DKG.Occurrence),
                    key=lambda n: int(g.value(n, DKG.sequence) or 0))
    contexts = defaultdict(list)
    for e in events:
        ctxs = list(g.objects(e, DKG.inContext))
        contexts[ctxs[0] if ctxs else None].append(e)
    for ctx, evs in contexts.items():
        name = lbl(g, ctx) if ctx else "no context drawn"
        lines.append(f'  subgraph {mid(ctx) if ctx else "nocontext"}["{esc(name)}"]')
        for e in evs:
            lines.append(f'    {mid(e)}["{esc(lbl(g, e))}"]')
        lines.append("  end")
    for s, _, o in edges_of(g, [DKG.precedes], set(events)):
        lines.append(f"  {mid(s)} --> {mid(o)}")
    for s, o in g.subject_objects(DKG.triggers):
        if o in set(events):
            lines.append(f'  {mid(s)}(["{esc(lbl(g, s))}"]) -.-> {mid(o)}')
    straddlers = [n for n in set(g.subjects(DKG.inContext, None))
                  if len(set(g.objects(n, DKG.inContext))) > 1]
    lines.append("```")
    if straddlers:
        lines.append("")
        lines.append("**Straddling two contexts:** " +
                     ", ".join(sorted(f"`{lbl(g, n)}`" for n in straddlers)))
    return "\n".join(lines)


def strategy_lens(g):
    lines = ["```mermaid", "flowchart RL"]
    seen = set()
    for p in (DKG.towardsGoal, DKG.impactedActor, DKG.realises,
              DKG.supports, DKG.servesSegment):
        for s, o in g.subject_objects(p):
            for n in (s, o):
                if n not in seen:
                    seen.add(n)
                    shape = ('(["%s"])' if (n, RDF.type, DKG.Goal) in g
                             else '["%s"]')
                    lines.append(f"  {mid(n)}{shape % esc(lbl(g, n))}")
            lines.append(f"  {mid(s)} -->|{str(p).split('#')[-1]}| {mid(o)}")
    lines.append("```")
    goals = typed(g, DKG.Goal)
    nometric = [n for n in goals if not list(g.objects(n, DKG.metric))]
    if nometric:
        lines.append("")
        lines.append("**Goals with no metric:** " +
                     ", ".join(sorted(f"`{lbl(g, n)}`" for n in nometric)))
    return "\n".join(lines)


def claims_on(g, node):
    """Assertions whose rdf:subject is this node, newest artifact last."""
    out = []
    for a in typed(g, DKG.Assertion):
        if g.value(a, RDF.subject) == node:
            src = g.value(a, DKG.source)
            out.append((str(g.value(a, RDF.object)),
                        lbl(g, src) if src else "?",
                        str(g.value(src, DKG.ingestedAt) or "") if src else ""))
    return sorted(out, key=lambda t: t[2])


def capability_lens(g):
    caps = typed(g, DKG.Capability)
    if not caps:
        return "_No capabilities in the graph yet._"
    by_stage = defaultdict(list)
    for c in caps:
        st = g.value(c, DKG.evolution)
        by_stage[lbl(g, st) if st else "unplaced"].append(c)
    lines = ["```mermaid", "flowchart LR"]
    order = ["genesis", "custom-built", "product", "commodity", "unplaced"]
    for stage in [s for s in order if s in by_stage] + \
                 [s for s in by_stage if s not in order]:
        lines.append(f'  subgraph {mid(stage)}["{esc(stage)}"]')
        for c in by_stage[stage]:
            text = lbl(g, c)
            marks = {v for v, _, _ in claims_on(g, c)}
            if marks:
                text += " · " + "/".join(sorted(marks))
            lines.append(f'    {mid(c)}["{esc(text)}"]')
        lines.append("  end")
    for s, _, o in edges_of(g, [DKG.dependsOn], caps):
        lines.append(f"  {mid(s)} --> {mid(o)}")
    lines.append("```")
    moves = list(g.subject_objects(DKG.movesTo))
    if moves:
        lines.append("")
        lines.append("**Movement drawn on the map** — predictions, with a date "
                     "on them:")
        for s, o in moves:
            cur = g.value(s, DKG.evolution)
            lines.append(f"- `{lbl(g, s)}` — {lbl(g, cur) if cur else '?'} "
                         f"→ {lbl(g, o)}")
    unplaced = by_stage.get("unplaced", [])
    if unplaced:
        lines.append("")
        lines.append("`unplaced` capabilities came from a canvas or capability "
                     "map, which carry no evolution axis. Not a defect.")
    return "\n".join(lines)


def language_lens(g):
    concepts = typed(g, DKG.Concept)
    if not concepts:
        return "_No concepts in the graph yet._"
    lines = ["```mermaid", "flowchart LR"]
    for c in sorted(concepts, key=lambda n: lbl(g, n)):
        alts = sorted(str(a) for a in g.objects(c, SKOS.altLabel))
        text = esc(lbl(g, c))
        if alts:
            text += " / " + esc(" / ".join(alts))
        lines.append(f'  {mid(c)}["{text}"]')
    for rel in typed(g, DKG.Relationship):
        a, b = g.value(rel, DKG["from"]), g.value(rel, DKG.to)
        if a is None or b is None:
            continue
        card = g.value(rel, DKG.cardinality) or ""
        given = g.value(rel, DKG.cardinalityGiven)
        mark = "" if str(given).lower() == "true" else "?"
        verb = g.value(rel, DKG.verb) or ""
        lines.append(f'  {mid(a)} -->|"{esc(verb)} {esc(card)}{mark}"| {mid(b)}')
    for s, o in g.subject_objects(DKG.renamedTo):
        lines.append(f'  {mid(s)} ==>|renamed to| {mid(o)}')
    lines.append("```")
    lines.append("")
    lines.append("`?` on a cardinality means it was derived, not drawn.")
    return "\n".join(lines)


def traceability_lens(g):
    lines = ["```mermaid", "flowchart LR"]
    chain = [DKG.proposedSameAs, DKG.realises, DKG.towardsGoal,
             DKG.supports, DKG.mentions, DKG.triggers]
    seen = set()
    for p in chain:
        for s, o in g.subject_objects(p):
            for n in (s, o):
                if n not in seen:
                    seen.add(n)
                    lines.append(f'  {mid(n)}["{esc(lbl(g, n))}"]')
            style = "-.->" if p == DKG.proposedSameAs else "-->"
            lines.append(f"  {mid(s)} {style}|{str(p).split('#')[-1]}| {mid(o)}")
    lines.append("```")
    lines.append("")
    lines.append("Dotted edges are **proposed**, not confirmed.")
    return "\n".join(lines)


LENS_FN = {
    "strategy": strategy_lens, "capability": capability_lens,
    "language": language_lens, "flow": flow_lens,
    "traceability": traceability_lens,
}

LENS_BLURB = {
    "strategy": "What are we trying to change, in whom, and what would do it?",
    "capability": "What do we build, buy, and depend on?",
    "language": "What do we call things, and where does the word change?",
    "flow": "What happens, in what order, and who is involved?",
    "traceability": "Whatever happened to that idea? — and, backwards, why are we building this?",
}


# --------------------------------------------------------------------------

def register(g):
    arts = sorted(typed(g, DKG.Artifact), key=lambda a: str(g.value(a, DKG.ingestedAt) or ""))
    if not arts:
        return "_No artifacts registered._"
    counts = defaultdict(int)
    for _, _, a in g.triples((None, DKG.source, None)):
        counts[a] += 1
    rows = ["| Artifact | Type | Ingested | Nodes | Note |", "|---|---|---|---|---|"]
    for a in arts:
        t = [str(x).split("#")[-1] for x in g.objects(a, RDF.type)
             if str(x).startswith(str(DKG)) and str(x) != str(DKG.Artifact)]
        note = g.value(a, RDFS.comment) or ""
        rows.append(f"| {esc(lbl(g, a), 80)} | {', '.join(t)} | "
                    f"{g.value(a, DKG.ingestedAt) or '—'} | {counts[a]} | {esc(note, 200)} |")
    total = sum(counts.values()) or 1
    top = max(counts.items(), key=lambda kv: kv[1])
    share = 100 * top[1] / total

    nodes = [s for s in set(g.subjects(DKG.source, None))
             if s not in typed(g, DKG.Assertion)]
    single = [n for n in nodes if len(set(g.objects(n, DKG.source))) == 1]
    rows.append("")
    rows.append(f"> **{len(single)} of {len(nodes)} nodes rest on a single "
                f"artifact.** Read every lens with that in mind: a graph is "
                f"only as corroborated as this line says it is.")
    if share > 60:
        rows.append(">")
        rows.append(f"> **{share:.0f}% of all node-mentions come from "
                    f"{lbl(g, top[0])}.** This is one artifact with garnish, "
                    "not yet a domain map.")
    return "\n".join(rows)


def findings(g):
    out = []
    vocab = (set(g.subjects(RDF.type, OWL.Ontology))
             | set(g.subjects(RDF.type, OWL.Class))
             | set(g.subjects(RDF.type, OWL.ObjectProperty))
             | set(g.subjects(RDF.type, OWL.DatatypeProperty)))
    nodes = [s for s in set(g.subjects())
             if isinstance(s, URIRef) and not str(s).startswith(str(DKG))
             and s not in vocab and s not in typed(g, DKG.Artifact)]

    orphans = []
    for n in nodes:
        o = [p for p in g.predicates(n, None) if p not in BOOKKEEPING]
        i = [p for p in g.predicates(None, n) if p not in BOOKKEEPING]
        if not o and not i:
            orphans.append(n)
    if orphans:
        out.append("### Orphans\n\nNothing in the graph references these, and "
                   "they reference nothing. Each absence means something.\n")
        for n in sorted(orphans, key=lambda x: lbl(g, x)):
            srcs = ", ".join(sorted(lbl(g, s) for s in g.objects(n, DKG.source)))
            out.append(f"- `{lbl(g, n)}` — only in {srcs}")

    ideas = typed(g, DKG.Idea)
    dropped = []
    for i in ideas:
        picked_up = (list(g.subjects(DKG.proposedSameAs, i))
                     or list(g.objects(i, DKG.proposedSameAs))
                     or list(g.objects(i, DKG.realises))
                     or list(g.objects(i, DKG.supports)))
        if not picked_up:
            dropped.append(i)
    if dropped:
        out.append("\n### Ideas nothing picked up\n\nBrainstormed, and no "
                   "later artifact references them. Dropped on purpose, or "
                   "dropped by accident?\n")
        for i in sorted(dropped, key=lambda x: -int(g.value(x, DKG.votes) or 0)):
            v = g.value(i, DKG.votes)
            out.append(f"- `{lbl(g, i)}`" + (f" — {v} vote{'s' if int(v) != 1 else ''}" if v else ""))

    assertions = typed(g, DKG.Assertion)
    if assertions:
        by_subj = defaultdict(list)
        for a in assertions:
            by_subj[g.value(a, RDF.subject)].append(a)
        out.append("\n### Claims on record\n\nAsserted by an artifact, with a "
                   "date. Where a subject carries two different claims, both "
                   "stand and the dates are the story.\n")
        for subj, aa in sorted(by_subj.items(), key=lambda kv: lbl(g, kv[0])):
            vals = claims_on(g, subj)
            changed = len({v for v, _, _ in vals}) > 1
            trail = "; ".join(f"**{v}** ({src}{', ' + d if d else ''})"
                              for v, src, d in vals)
            flag = " ← **changed**" if changed else ""
            out.append(f"- `{lbl(g, subj)}` — {trail}{flag}")

    pend = list(g.subject_objects(DKG.proposedSameAs))
    if pend:
        out.append("\n### Merges awaiting a decision\n")
        for s, o in pend:
            why = g.value(s, RDFS.comment) or ""
            out.append(f"- `{lbl(g, s)}` ~ `{lbl(g, o)}` — {esc(why, 300)}")

    contra = list(g.subject_objects(DKG.contradicts))
    if contra:
        out.append("\n### Contradictions\n\nBoth sides stand. The room settles these.\n")
        for s, o in contra:
            out.append(f"- {esc(lbl(g, s), 120)} **vs** {esc(lbl(g, o), 120)}")

    # worth a second look
    one_src_high_degree, many_src_low_degree = [], []
    for n in nodes:
        deg = len([p for p in g.predicates(n, None) if p not in BOOKKEEPING]) + \
              len([p for p in g.predicates(None, n) if p not in BOOKKEEPING])
        srcs = len(set(g.objects(n, DKG.source)))
        if srcs == 1 and deg >= 4:
            one_src_high_degree.append((n, deg))
        if srcs >= 3 and deg <= 1:
            many_src_low_degree.append((n, srcs))
    if one_src_high_degree or many_src_low_degree:
        out.append("\n### Worth a second look\n")
        for n, d in sorted(one_src_high_degree, key=lambda t: -t[1]):
            out.append(f"- `{lbl(g, n)}` — {d} edges, but only one artifact "
                       "says it exists")
        for n, s in sorted(many_src_low_degree, key=lambda t: -t[1]):
            out.append(f"- `{lbl(g, n)}` — {s} artifacts mention it, nothing "
                       "builds on it")
    return "\n".join(out) if out else "_No findings._"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("graph")
    ap.add_argument("--out", default="views")
    ap.add_argument("--lens", choices=LENSES, help="render one lens only")
    args = ap.parse_args()

    g = Graph()
    g.parse(args.graph, format="turtle")
    load_vocab(g)

    os.makedirs(args.out, exist_ok=True)
    title = next((str(g.value(o, RDFS.label)) for o in g.subjects(RDF.type, OWL.Ontology)
                  if g.value(o, RDFS.label)), "Domain knowledge graph")

    parts = [f"# {title}\n", "## Register\n", register(g), ""]
    for name in ([args.lens] if args.lens else LENSES):
        parts += [f"\n## Lens — {name}\n", f"*{LENS_BLURB[name]}*\n",
                  LENS_FN[name](g), ""]
    parts += ["\n## Findings\n", findings(g), "",
              "\n---\n\nA graph shows what the artifacts *said*. It has no "
              "opinion on whether they were right, and a densely connected "
              "node is popular, not important.\n"]

    path = os.path.join(args.out, "index.md")
    with open(path, "w") as fh:
        fh.write("\n".join(parts))
    print(f"wrote {path}")


if __name__ == "__main__":
    main()