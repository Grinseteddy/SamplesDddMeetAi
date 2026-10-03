#!/usr/bin/env python3
"""Render browsable views of a domain knowledge graph.

    python3 views.py graph.ttl --out views/
    python3 views.py graph.ttl --out views/ --lens language

Writes views/index.md: the artifact register, six Mermaid lenses (strategy,
capability, language, flow, interface, traceability), the orphan report and a
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
LENSES = ["strategy", "capability", "language", "flow", "interface", "traceability"]

BOOKKEEPING = {
    RDF.type, RDFS.label, RDFS.comment, SKOS.prefLabel, SKOS.altLabel,
    DKG.source, DKG.locator, DKG.confidence, DKG.ingestedAt, DKG.votes,
    DKG.sequence, DKG.metric, DKG.cardinality, DKG.cardinalityGiven,
    DKG.verb, DKG.hasState, DKG.sliceKind, DKG.visibility, DKG.literal,
    DKG.messageKind, DKG.carries, DKG.domainRole, DKG.businessModel,
    DKG.status, DKG.decidedOption, DKG.consideredOption, DKG.decidedAt,
    DKG.decidedBy,
    DKG.specFormat, DKG.interactionStyle, DKG.apiId, DKG.apiVersion,
    DKG.audience, DKG.protocol, DKG.operationId, DKG.httpMethod, DKG.path,
    DKG.action, DKG.requiresScope, DKG.address, DKG.schemaRole,
    DKG.architecturePattern, DKG.contact, DKG.parameter,
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
    drifting = (set(g.subjects(SKOS.altLabel, None))
                | set(g.subjects(DKG.renamedTo, None))
                | set(g.objects(None, DKG.renamedTo)))
    # a spec's own spellings (operationId, message title) belong to the
    # interface lens; here they would read as vocabulary drift
    for cls in (DKG.Api, DKG.Operation, DKG.Channel, DKG.ApiMessage, DKG.Schema):
        drifting -= typed(g, cls)
    concepts = typed(g, DKG.Concept) | drifting
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


def interface_lens(g):
    """Per context: what can be called (sync) and what is published or consumed
    (async), and which model element each contract specifies."""
    apis = typed(g, DKG.Api)
    if not apis:
        return ("_No API specification in the graph yet. When one is ingested, this lens "
                "shows each context's synchronous operations and asynchronous channels "
                "beside the commands, events and canvas messages they specify._")
    lines = ["```mermaid", "flowchart LR",
             "  %% rectangles = sync operations · hexagons = channels · rounded = messages · "
             "dotted = specifies"]
    by_ctx = defaultdict(list)
    for a in apis:
        ctxs = list(g.objects(a, DKG.inContext))
        by_ctx[ctxs[0] if ctxs else None].append(a)
    model_nodes = set()
    for ctx, aa in by_ctx.items():
        name = lbl(g, ctx) if ctx else "no context"
        lines.append(f'  subgraph {mid(ctx) if ctx else "nocontext"}["{esc(name)}"]')
        for a in sorted(aa, key=lambda n: lbl(g, n)):
            style = g.value(a, DKG.interactionStyle) or "?"
            proto = ", ".join(sorted(str(p) for p in g.objects(a, DKG.protocol)))
            lines.append(f'    subgraph {mid(a)}["{esc(lbl(g, a))} · {style}{" · " + proto if proto else ""}"]')
            for op in sorted(g.subjects(DKG.exposedBy, a), key=lambda n: lbl(g, n)):
                if (op, RDF.type, DKG.Operation) in g:
                    m, p = g.value(op, DKG.httpMethod), g.value(op, DKG.path)
                    act = g.value(op, DKG.action)
                    text = f"{m} {p}" if m else f"{act or ''} {lbl(g, op)}".strip()
                    lines.append(f'      {mid(op)}["{esc(text)}"]')
                    for ch in g.objects(op, DKG.onChannel):
                        if str(act) == "receive":
                            lines.append(f"      {mid(ch)} --> {mid(op)}")
                        else:
                            lines.append(f"      {mid(op)} --> {mid(ch)}")
                elif (op, RDF.type, DKG.Channel) in g:
                    lines.append(f'      {mid(op)}{{{{"{esc(g.value(op, DKG.address) or lbl(g, op))}"}}}}')
                    for msg in g.objects(op, DKG.carriesMessage):
                        lines.append(f'      {mid(msg)}(["{esc(lbl(g, msg))}"])')
                        lines.append(f"      {mid(op)} --- {mid(msg)}")
            lines.append("    end")
        lines.append("  end")
    for cls in (DKG.Operation, DKG.ApiMessage):
        for n in typed(g, cls):
            for tgt in g.objects(n, DKG.specifies):
                model_nodes.add(tgt)
                lines.append(f"  {mid(n)} -.->|specifies| {mid(tgt)}")
    for n in sorted(model_nodes, key=lambda x: lbl(g, x)):
        kind = g.value(n, DKG.messageKind)
        tag = f" ({kind})" if kind else ""
        lines.append(f'  {mid(n)}>"{esc(lbl(g, n))}{tag}"]')
    lines.append("```")

    # the table underneath: coverage, per context
    lines.append("")
    lines.append("| Context | Sync | Async | Commands / inbound rows without an operation | Events / outbound evt rows without a message |")
    lines.append("|---|---|---|---|---|")
    specified = set()
    for cls in (DKG.Operation, DKG.ApiMessage, DKG.Schema):
        for n in typed(g, cls):
            specified |= set(g.objects(n, DKG.specifies))
    cmds, events, msgs = typed(g, DKG.Activity), typed(g, DKG.Occurrence), typed(g, DKG.Message)
    for ctx, aa in sorted(by_ctx.items(), key=lambda kv: lbl(g, kv[0]) if kv[0] else ""):
        if ctx is None:
            continue
        sync = [a for a in aa if str(g.value(a, DKG.interactionStyle)) == "sync"]
        asyn = [a for a in aa if str(g.value(a, DKG.interactionStyle)) == "async"]
        n_ops = sum(1 for a in sync for o in g.subjects(DKG.exposedBy, a) if (o, RDF.type, DKG.Operation) in g)
        n_ch = sum(1 for a in asyn for o in g.subjects(DKG.exposedBy, a) if (o, RDF.type, DKG.Channel) in g)
        n_msg = sum(1 for a in asyn for o in g.subjects(DKG.exposedBy, a) if (o, RDF.type, DKG.ApiMessage) in g)
        my_cmd = [x for x in cmds if ctx in set(g.objects(x, DKG.inContext))] + \
                 [m for m in msgs if g.value(m, DKG.to) == ctx and str(g.value(m, DKG.messageKind) or "") != "evt"]
        my_ev = [x for x in events if ctx in set(g.objects(x, DKG.inContext))] + \
                [m for m in msgs if g.value(m, DKG["from"]) == ctx and str(g.value(m, DKG.messageKind) or "") == "evt"]
        miss_c = sorted(f"`{lbl(g, x)}`" for x in my_cmd if x not in specified)
        miss_e = sorted(f"`{lbl(g, x)}`" for x in my_ev if x not in specified)
        def cell(missing, modelled, has_api):
            if not has_api:
                return "— (no API of this style)"
            if not modelled:
                return "— (none modelled)"
            return ", ".join(missing) if missing else "none missing"
        lines.append(f"| {lbl(g, ctx)} | {len(sync)} API, {n_ops} operations | {len(asyn)} API, {n_ch} channels, {n_msg} messages | "
                     f"{cell(miss_c, my_cmd, sync)} | {cell(miss_e, my_ev, asyn)} |")
    qrs = typed(g, DKG.QualityRequirement)
    if qrs:
        lines.append("")
        lines.append("**Quality requirements** (from API Product Canvases):")
        for q in sorted(qrs, key=lambda n: lbl(g, n)):
            tgt = ", ".join(sorted(f"`{lbl(g, t)}`" for t in g.objects(q, DKG.constrains))) or "—"
            lines.append(f"- {esc(lbl(g, q), 140)} — constrains {tgt}")
    lines.append("")
    lines.append("A contract with no model element behind it, and a model element with no contract, "
                 "are both findings; `check_graph.py` lists them one by one.")
    return "\n".join(lines)


def traceability_lens(g):
    lines = ["```mermaid", "flowchart LR"]
    chain = [DKG.proposedSameAs, DKG.realises, DKG.towardsGoal,
             DKG.supports, DKG.mentions, DKG.triggers, DKG.answers, DKG.specifies,
             DKG.derivedFrom, DKG.unrepresentedIn]
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


# --------------------------------------------------------------------------
# patches — the reverse direction: what one artifact should change
# --------------------------------------------------------------------------

def typed_classes(g, cls):
    """cls and its dkg: subclasses."""
    out = {cls}
    for sub in g.subjects(RDFS.subClassOf, cls):
        out |= typed_classes(g, sub)
    return out


def find_artifact(g, key):
    arts = typed(g, DKG.Artifact)
    for a in arts:
        if str(a).split("#")[-1].split("/")[-1] == key or str(a) == key:
            return a
    for a in arts:
        if key.lower() in lbl(g, a).lower():
            return a
    return None


def latest_revision(g, art):
    """Walk dkg:revisionOf forwards to the newest artifact in the lineage."""
    seen = {art}
    while True:
        newer = [a for a in g.subjects(DKG.revisionOf, art) if a not in seen]
        if not newer:
            return art
        art = newer[0]
        seen.add(art)


def patches(g, key):
    art = find_artifact(g, key)
    if art is None:
        return f"_No artifact matches `{key}`. Run without --patches to see the register._"
    newest = latest_revision(g, art)
    mine = {n for n in g.subjects(DKG.source, newest)
            if n not in typed(g, DKG.Assertion)}
    if not mine:
        return f"_`{lbl(g, art)}` has no nodes sourced to it._"
    out = [f"# Patch list — {lbl(g, newest)}", ""]
    if newest != art:
        out.append(f"_Newest revision in this lineage; you asked for "
                   f"`{lbl(g, art)}`._\n")
    out.append("What the rest of the graph says this artifact should change. "
               "Each row names its source; nothing here is a decision the "
               "graph made on its own.\n")

    def src_of(n):
        return ", ".join(sorted(lbl(g, s) for s in g.objects(n, DKG.source)))

    # 1. decisions that answer questions mentioning this artifact's nodes,
    #    or that mention them directly
    rows = []
    for d in typed(g, DKG.Decision):
        hits = set()
        for q in g.objects(d, DKG.answers):
            hits |= {m for m in g.objects(q, DKG.mentions) if m in mine}
            for q2 in g.objects(q, DKG.proposedSameAs):
                hits |= {m for m in g.objects(q2, DKG.mentions) if m in mine}
        hits |= {m for m in g.objects(d, DKG.mentions) if m in mine}
        if hits:
            rows.append((d, hits))
    out.append("## 1. Decisions about its terms\n")
    if rows:
        out.append("| Decision | Status | Decided | Terms here | Source |")
        out.append("|---|---|---|---|---|")
        for d, hits in sorted(rows, key=lambda t: str(g.value(t[0], DKG.decidedAt) or "")):
            out.append(f"| `{lbl(g, d)}` | {g.value(d, DKG.status) or '—'} | "
                       f"{esc(g.value(d, DKG.decidedOption) or '', 90)} | "
                       f"{', '.join(sorted('`'+lbl(g, h)+'`' for h in hits))} | {src_of(d)} |")
    else:
        out.append("_None. No Decision in the graph touches a term this artifact holds._")
    out.append("")

    # 2. spellings: nodes here whose prefLabel differs from a label this
    #    artifact used (an altLabel exists), or that were renamedTo
    out.append("## 2. Spellings the graph has moved on from\n")
    rows = []
    for n in mine:
        alts = sorted(str(a) for a in g.objects(n, SKOS.altLabel))
        if alts:
            rows.append((n, alts))
    for s_, o in g.subject_objects(DKG.renamedTo):
        if s_ in mine:
            rows.append((s_, [f"renamed to {lbl(g, o)}"]))
    if rows:
        out.append("| Term as held | Other spellings on record | Sources |")
        out.append("|---|---|---|")
        for n, alts in sorted(rows, key=lambda t: lbl(g, t[0])):
            out.append(f"| `{lbl(g, n)}` | {' / '.join(esc(a, 40) for a in alts)} | {src_of(n)} |")
        out.append("\nA spelling in the second column that a Decision above chose "
                   "is a rename to apply; one with no Decision is a drift to "
                   "settle, not to copy.")
    else:
        out.append("_None._")
    out.append("")

    # 3. relationships between this artifact's terms drawn only elsewhere
    out.append("## 3. Relationships between its terms that it does not draw\n")
    rows = []
    for rel in typed(g, DKG.Relationship):
        a, b = g.value(rel, DKG["from"]), g.value(rel, DKG.to)
        if a in mine and b in mine and newest not in set(g.objects(rel, DKG.source)):
            rows.append(rel)
    # also relationships from a merge-candidate of a held term
    if rows:
        out.append("| From | Verb | To | Cardinality | Drawn by |")
        out.append("|---|---|---|---|---|")
        for rel in sorted(rows, key=lambda r: lbl(g, r)):
            given = g.value(rel, DKG.cardinalityGiven)
            mark = "" if str(given).lower() == "true" else " (derived)"
            out.append(f"| `{lbl(g, g.value(rel, DKG['from']))}` | {g.value(rel, DKG.verb) or ''} | "
                       f"`{lbl(g, g.value(rel, DKG.to))}` | {g.value(rel, DKG.cardinality) or '—'}{mark} | {src_of(rel)} |")
    else:
        out.append("_None._")
    out.append("")

    # 4. superseded statements this artifact still carries
    out.append("## 4. Statements of its own that a later artifact superseded\n")
    rows = [(new, old) for new, old in g.subject_objects(DKG.supersedes)
            if old in mine or old in typed(g, DKG.Assertion) and g.value(old, RDF.subject) in mine]
    if rows:
        for new, old in rows:
            out.append(f"- `{lbl(g, old)}` → superseded by `{lbl(g, new)}` ({src_of(new)})")
    else:
        out.append("_None._")
    out.append("")

    # 5. open questions about its terms with no decision
    out.append("## 5. Questions about its terms nobody has closed\n")
    rows = []
    for q in typed(g, DKG.Question):
        if not {m for m in g.objects(q, DKG.mentions) if m in mine}:
            continue
        answered = list(g.subjects(DKG.answers, q)) or \
            any(list(g.subjects(DKG.answers, q2)) for q2 in g.objects(q, DKG.proposedSameAs)) or \
            any(list(g.subjects(DKG.answers, q2)) for q2 in g.subjects(DKG.proposedSameAs, q))
        if not answered:
            rows.append(q)
    if rows:
        for q in sorted(rows, key=lambda x: lbl(g, x)):
            out.append(f"- {esc(lbl(g, q), 160)} — raised by {src_of(q)}")
        out.append("\nThese are not patches; they are the reason a patch may be premature.")
    else:
        out.append("_None._")
    out.append("")

    # 6. things this artifact dropped in a revision
    dropped = [n for n in set(g.subjects(DKG.absentFrom, None))
               if newest in set(g.objects(n, DKG.absentFrom))]
    if dropped:
        out.append("## 6. Dropped in this revision\n")
        for n in sorted(dropped, key=lambda x: lbl(g, x)):
            out.append(f"- `{lbl(g, n)}`")
        out.append("")

    # 7. the interface side — both directions
    is_spec = any((newest, RDF.type, c) in g for c in typed_classes(g, DKG.ApiSpecification))
    if is_spec:
        apis = [a for a in mine if (a, RDF.type, DKG.Api) in g]
        ctxs = {c for a in apis for c in g.objects(a, DKG.inContext)}
        styles = {str(g.value(a, DKG.interactionStyle) or "") for a in apis}
        specified = set()
        for n in mine:
            specified |= set(g.objects(n, DKG.specifies))
            for m in g.objects(n, DKG.carriesMessage):
                specified |= set(g.objects(m, DKG.specifies))
        rows = []
        for c in ctxs:
            if "sync" in styles:
                rows += [(x, "command") for x in typed(g, DKG.Activity)
                         if c in set(g.objects(x, DKG.inContext)) and x not in specified]
                rows += [(x, f"inbound {g.value(x, DKG.messageKind) or '?'} row") for x in typed(g, DKG.Message)
                         if g.value(x, DKG.to) == c and str(g.value(x, DKG.messageKind) or "") != "evt"
                         and x not in specified]
            if "async" in styles:
                rows += [(x, "event") for x in typed(g, DKG.Occurrence)
                         if c in set(g.objects(x, DKG.inContext)) and x not in specified]
                rows += [(x, "outbound evt row") for x in typed(g, DKG.Message)
                         if g.value(x, DKG["from"]) == c and str(g.value(x, DKG.messageKind) or "") == "evt"
                         and x not in specified]
        out.append("## 7. Model elements of its context this spec does not cover\n")
        if rows:
            out.append("| Element | Kind | Sources |")
            out.append("|---|---|---|")
            for x, kind in sorted(rows, key=lambda t: lbl(g, t[0])):
                out.append(f"| `{lbl(g, x)}` | {kind} | {src_of(x)} |")
            out.append("\nNot every command needs an endpoint and not every event leaves the context. "
                       "Each row is a question for the API's owner, not a missing operation.")
        else:
            out.append("_None — every modelled command/event of its context has a contract here._")
        out.append("")
        unlinked = [n for n in mine if (n, RDF.type, DKG.Operation) in g or (n, RDF.type, DKG.ApiMessage) in g
                    or ((n, RDF.type, DKG.Schema) in g and str(g.value(n, DKG.schemaRole) or "") not in ("header", "error", "payload"))]
        unlinked = [n for n in unlinked if not list(g.objects(n, DKG.specifies)) and not list(g.objects(n, DKG.mentions))
                    and not any(list(g.objects(m, DKG.specifies)) for m in g.objects(n, DKG.carriesMessage))]
        out.append("## 8. Its own elements the model never named\n")
        if unlinked:
            for n in sorted(unlinked, key=lambda x: lbl(g, x)):
                out.append(f"- `{lbl(g, n)}`")
            out.append("\nThe first the workshops hear of these — or a spelling the ledger missed. "
                       "Either way, the model side needs a sticky before the graph can link them.")
        else:
            out.append("_None._")
        out.append("")
    else:
        rows = []
        for n in mine:
            for spec in g.subjects(DKG.specifies, n):
                rows.append((n, spec))
        if rows:
            out.append("## 7. Contracts already written for its elements\n")
            out.append("| Element here | Contract | Style | In |")
            out.append("|---|---|---|---|")
            for n, spec in sorted(rows, key=lambda t: lbl(g, t[0])):
                api = g.value(spec, DKG.exposedBy)
                style = g.value(api, DKG.interactionStyle) if api else "?"
                out.append(f"| `{lbl(g, n)}` | `{lbl(g, spec)}` | {style} | {src_of(spec)} |")
            out.append("\nRenaming or retyping one of these elements changes a published contract; "
                       "say so in the room before redrawing.")
            out.append("")

    out.append("---\n\nThis list says *what* and *why*. Redrawing the artifact is "
               "the job of its own skill or the room; a graph that edits its "
               "sources can no longer be checked against them.")
    return "\n".join(out)


LENS_FN = {
    "strategy": strategy_lens, "capability": capability_lens,
    "language": language_lens, "flow": flow_lens,
    "interface": interface_lens, "traceability": traceability_lens,
}

LENS_BLURB = {
    "strategy": "What are we trying to change, in whom, and what would do it?",
    "capability": "What do we build, buy, and depend on?",
    "language": "What do we call things, and where does the word change?",
    "flow": "What happens, in what order, and who is involved?",
    "interface": "Per context: what can be called, what is published — and does the contract match the model?",
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
        rev = g.value(a, DKG.revisionOf)
        if rev is not None:
            note = f"revision of {lbl(g, rev)}. {note}"
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

    sup = [(a, b) for a, b in g.subject_objects(DKG.supersedes)
           if a not in typed(g, DKG.Decision)]
    if sup:
        out.append("\n### Changed between revisions\n\nOne artifact changing its mind. Both statements stand.\n")
        for a, b in sup:
            out.append(f"- `{lbl(g, b)}` → `{lbl(g, a)}`")

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
    ap.add_argument("--patches", metavar="ARTIFACT",
                    help="reverse direction: what one artifact (IRI suffix or "
                         "label fragment) should change, given the graph")
    args = ap.parse_args()

    g = Graph()
    g.parse(args.graph, format="turtle")
    load_vocab(g)

    if args.patches:
        text = patches(g, args.patches)
        os.makedirs(args.out, exist_ok=True)
        path = os.path.join(args.out, "patches.md")
        with open(path, "w") as fh:
            fh.write(text + "\n")
        print(text)
        print(f"\nwrote {path}")
        return

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
