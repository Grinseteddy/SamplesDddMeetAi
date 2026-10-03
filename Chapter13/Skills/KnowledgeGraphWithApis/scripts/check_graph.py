#!/usr/bin/env python3
"""Check a domain knowledge graph.

    python3 check_graph.py graph.ttl
    python3 check_graph.py graph.ttl --sparql query.rq

Errors are things that make the graph untrustworthy: a node with no source
cannot be traced to a wall, a node with no confidence lets a guess pass as a
transcription. Fix those.

Warnings are findings. An orphan, a contradiction, a pending merge — these are
what the graph is FOR. Report them; do not fix them by inventing edges.
"""
from __future__ import annotations

import sys
import argparse
import difflib
from collections import defaultdict

try:
    from rdflib import Graph, RDF, RDFS, Namespace, URIRef, Literal
    from rdflib.namespace import SKOS, OWL
except ImportError:
    sys.exit("rdflib is required:  pip install rdflib --break-system-packages")

DKG = Namespace("https://w3id.org/dkg/ns#")

# Properties that do not, on their own, make a node connected to anything.
BOOKKEEPING = {
    RDF.type, RDFS.label, RDFS.comment, SKOS.prefLabel, SKOS.altLabel,
    DKG.source, DKG.locator, DKG.confidence, DKG.ingestedAt, DKG.votes,
    DKG.sequence, DKG.metric, DKG.cardinality, DKG.cardinalityGiven,
    DKG.verb, DKG.hasState, DKG.sliceKind, DKG.visibility, DKG.literal,
    DKG.messageKind, DKG.carries, DKG.domainRole, DKG.businessModel,
    DKG.status, DKG.decidedOption, DKG.consideredOption, DKG.decidedAt,
    DKG.decidedBy,
    # API specifications: literals, and the structural edges inside one spec
    DKG.specFormat, DKG.interactionStyle, DKG.apiId, DKG.apiVersion,
    DKG.audience, DKG.protocol, DKG.operationId, DKG.httpMethod, DKG.path,
    DKG.action, DKG.requiresScope, DKG.address, DKG.schemaRole,
    DKG.architecturePattern, DKG.contact, DKG.parameter,
    OWL.imports,
}

# Edges that hold a spec together but do not connect it to the MODEL. A Schema
# whose only edges are these is unlinked: an API noun no workshop ever named.
API_INTERNAL = {
    DKG.exposedBy, DKG.onChannel, DKG.carriesMessage, DKG.requestSchema,
    DKG.responseSchema, DKG.payloadSchema,
}
API_SIDE = (DKG.Api, DKG.Operation, DKG.Channel, DKG.ApiMessage, DKG.Schema)

CONFIDENCES = {DKG.OnArtifact, DKG.Implied, DKG.Inferred}


def label_of(g: Graph, node) -> str:
    for p in (SKOS.prefLabel, RDFS.label):
        v = g.value(node, p)
        if v:
            return str(v)
    return str(node).split("#")[-1]


def normalise(s: str) -> str:
    return "".join(c for c in s.lower() if c.isalnum())


def normalise_words(s: str) -> list[str]:
    cleaned = "".join(c if c.isalnum() or c.isspace() else " " for c in s.lower())
    return cleaned.split()


def load(path: str) -> Graph:
    g = Graph()
    try:
        g.parse(path, format="turtle")
    except Exception as exc:  # noqa: BLE001 - parse errors are the whole point
        sys.exit(f"PARSE FAILED\n  {exc}")
    return load_vocab(g)


def load_vocab(g: Graph) -> Graph:
    """Pull in dkg.ttl if it sits beside the graph or beside this script,
    so subclass closure works. Absent, the checks still run on direct types."""
    import os
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


def typed(g: Graph, cls) -> set:
    """Instances of cls or any dkg: subclass of it."""
    out = set(g.subjects(RDF.type, cls))
    for sub in set(g.subjects(RDFS.subClassOf, cls)):
        if sub != cls:
            out |= typed(g, sub)
    return out


def subjects_in_graph(g: Graph) -> set:
    """Everything the file makes a statement about, minus vocabulary terms."""
    out = set()
    for s in set(g.subjects()):
        if isinstance(s, URIRef) and not str(s).startswith(str(DKG)):
            out.add(s)
    return out


def check(g: Graph) -> tuple[list[str], list[str], dict]:
    errors: list[str] = []
    warnings: list[str] = []

    artifact_nodes = typed(g, DKG.Artifact)
    ontology_nodes = set(g.subjects(RDF.type, OWL.Ontology))
    reifications = set(g.subjects(RDF.type, DKG.Assertion))

    nodes = [
        s for s in subjects_in_graph(g)
        if s not in artifact_nodes and s not in ontology_nodes
        and s not in reifications
    ]

    # ---- errors -------------------------------------------------------
    for n in sorted(nodes, key=str):
        name = str(n).split("#")[-1]
        if not list(g.objects(n, RDF.type)):
            errors.append(f"{name}: no rdf:type")
        if not list(g.objects(n, SKOS.prefLabel)):
            errors.append(f"{name}: no skos:prefLabel")
        if not list(g.objects(n, DKG.source)):
            errors.append(f"{name}: no dkg:source — cannot be traced to an artifact")
        conf = list(g.objects(n, DKG.confidence))
        if not conf:
            errors.append(f"{name}: no dkg:confidence")
        elif not set(conf) <= CONFIDENCES:
            errors.append(f"{name}: dkg:confidence is not one of OnArtifact/Implied/Inferred")

    # API specifications: an Api belongs to exactly one context, and every
    # operation / channel / message / schema belongs to an Api.
    apis = typed(g, DKG.Api)
    for a in sorted(apis, key=str):
        name = str(a).split("#")[-1]
        ctxs = set(g.objects(a, DKG.inContext))
        if not ctxs:
            errors.append(f"{name}: dkg:Api with no dkg:inContext — an API that belongs "
                          "to no bounded context is the thing this ingest exists to prevent")
        style = set(str(x) for x in g.objects(a, DKG.interactionStyle))
        if not style:
            errors.append(f"{name}: dkg:Api with no dkg:interactionStyle (sync | async)")
        elif not style <= {"sync", "async"}:
            errors.append(f"{name}: dkg:interactionStyle must be 'sync' or 'async'")
    for cls in (DKG.Operation, DKG.Channel, DKG.ApiMessage, DKG.Schema):
        for n in sorted(typed(g, cls), key=str):
            if not list(g.objects(n, DKG.exposedBy)):
                errors.append(f"{str(n).split('#')[-1]}: {str(cls).split('#')[-1]} with no "
                              "dkg:exposedBy — which API defines it?")

    for a in sorted(reifications, key=str):
        name = str(a).split("#")[-1]
        for p, why in ((RDF.subject, "rdf:subject"), (RDF.predicate, "rdf:predicate"),
                       (RDF.object, "rdf:object")):
            if not list(g.objects(a, p)):
                errors.append(f"{name}: assertion with no {why}")
        if not list(g.objects(a, DKG.source)):
            errors.append(f"{name}: assertion with no dkg:source — "
                          "an unsourced claim is worse than no claim")
        if not list(g.objects(a, DKG.confidence)):
            errors.append(f"{name}: assertion with no dkg:confidence")

    for a in sorted(artifact_nodes, key=str):
        if not list(g.objects(a, RDFS.label)):
            errors.append(f"{str(a).split('#')[-1]}: artifact with no rdfs:label")

    # dangling references: pointed at, never described
    described = subjects_in_graph(g)
    for s, p, o in g:
        if isinstance(o, URIRef) and not str(o).startswith(str(DKG)) \
                and "w3.org" not in str(o) and "#" in str(o) \
                and o not in described and p not in (OWL.imports,):
            errors.append(
                f"{str(o).split('#')[-1]}: referenced by "
                f"{str(s).split('#')[-1]} but never described")

    # ---- warnings -----------------------------------------------------
    for s, _, o in g.triples((None, DKG.proposedSameAs, None)):
        warnings.append(
            f"pending merge: {label_of(g, s)!r} ~ {label_of(g, o)!r} "
            "— awaiting a human decision")

    for s, _, o in g.triples((None, DKG.contradicts, None)):
        warnings.append(
            f"contradiction: {label_of(g, s)} vs {label_of(g, o)} "
            "— keep both, report verbatim")

    # orphans: no substantive edge in either direction
    for n in nodes:
        out_edges = [p for p in g.predicates(n, None) if p not in BOOKKEEPING]
        in_edges = [p for p in g.predicates(None, n) if p not in BOOKKEEPING]
        if not out_edges and not in_edges:
            warnings.append(
                f"orphan: {label_of(g, n)!r} connects to nothing — "
                "a finding, not a defect to patch")

    warnings += api_findings(g, apis)

    # near-duplicate labels that were not merged or proposed
    labels = defaultdict(list)
    for n in nodes:
        labels[label_of(g, n)].append(n)
    known = set()
    for s, _, o in g.triples((None, DKG.proposedSameAs, None)):
        known.add(frozenset({s, o}))
    for s, _, o in g.triples((None, DKG.contradicts, None)):
        known.add(frozenset({s, o}))
    for s, _, o in g.triples((None, DKG.specifies, None)):
        known.add(frozenset({s, o}))
    for s, _, o in g.triples((None, DKG.mentions, None)):
        known.add(frozenset({s, o}))
    for s, _, o in g.triples((None, DKG.derivedFrom, None)):
        known.add(frozenset({s, o}))
    for op, _, m in g.triples((None, DKG.carriesMessage, None)):
        for t in g.objects(m, DKG.specifies):
            known.add(frozenset({op, t}))
    # An API's own nodes are compared against the MODEL only: an Operation
    # against commands and canvas messages, an ApiMessage against events, a
    # Schema against concepts. Api and Channel labels name the context or a
    # topic and would otherwise match everything in it.
    api_side = {n for c in API_SIDE for n in typed(g, c)}
    api_pair_ok = {
        DKG.Operation: {DKG.Activity, DKG.Message, DKG.Occurrence},
        DKG.ApiMessage: {DKG.Occurrence, DKG.Message},
        DKG.Schema: {DKG.Concept},
    }
    # Sentences and slices are labelled with whole sentences; they are not
    # duplicate candidates and would otherwise drown the report. Messages are
    # deliberately repeated by label across collaborators (one row per
    # collaborator, same message name) — that repetition is the finding, not
    # a transcription error, so it is skipped here too.
    skip = typed(g, DKG.Sentence) | typed(g, DKG.Slice) | typed(g, DKG.Message)
    fam_cache: dict = {}

    def family(n):
        if n not in fam_cache:
            fam_cache[n] = next(
                (f for f in (DKG.Occurrence, DKG.Activity, DKG.Actor,
                             DKG.Capability, DKG.Goal, DKG.Deliverable,
                             DKG.BoundedContext, DKG.Question, DKG.Rule,
                             DKG.Decision, DKG.Message, DKG.Operation,
                             DKG.ApiMessage, DKG.Schema, DKG.Api,
                             DKG.Channel, DKG.Concept)
                 if n in typed(g, f)), None)
        return fam_cache[n]

    names = sorted(labels)
    for i, a in enumerate(names):
        for b in names[i + 1:]:
            for x in labels[a]:
                for y in labels[b]:
                    if x in skip or y in skip:
                        continue
                    if frozenset({x, y}) & set() or frozenset({x, y}) in known:
                        continue
                    fx, fy = family(x), family(y)
                    # An event is named after the thing it happened to. That is
                    # a convention, not a duplicate.
                    if {fx, fy} == {DKG.Occurrence, DKG.Concept} or \
                       {fx, fy} == {DKG.Occurrence, DKG.Activity}:
                        continue
                    if x in api_side or y in api_side:
                        if x in api_side and y in api_side:
                            continue
                        api_n, model_n = (x, y) if x in api_side else (y, x)
                        api_fam = family(api_n)
                        if api_fam not in api_pair_ok or \
                           family(model_n) not in api_pair_ok[api_fam]:
                            continue
                    ta = [w for w in normalise_words(a)]
                    tb = [w for w in normalise_words(b)]
                    if not ta or not tb:
                        continue
                    ratio = difflib.SequenceMatcher(
                        None, " ".join(ta), " ".join(tb)).ratio()
                    short, long_ = (ta, tb) if len(ta) <= len(tb) else (tb, ta)
                    contained = (fx == fy and len(long_) <= 4
                                 and all(w in long_ for w in short))
                    if ratio > 0.88 or contained:
                        warnings.append(
                            "near-duplicate labels not merged or proposed: "
                            f"{a!r} / {b!r} — decide, and put it in the ledger")

    # confidence distribution is worth seeing
    stats = {
        "nodes": len(nodes),
        "artifacts": len(artifact_nodes),
        "assertions": len(reifications),
        "triples": len(g),
        "by_class": defaultdict(int),
        "by_confidence": defaultdict(int),
        "sources_per_node": defaultdict(int),
    }
    for n in nodes:
        for t in g.objects(n, RDF.type):
            if str(t).startswith(str(DKG)):
                stats["by_class"][str(t).split("#")[-1]] += 1
        for c in g.objects(n, DKG.confidence):
            stats["by_confidence"][str(c).split("#")[-1]] += 1
        stats["sources_per_node"][len(set(g.objects(n, DKG.source)))] += 1

    return errors, warnings, stats


def api_findings(g: Graph, apis: set) -> list[str]:
    """What the API specifications say against what the model says.

    Nothing here is an error. A board command with no operation may be
    internal; an operation with no command may be the first the team heard of
    it. Both are findings for the report, and the style mismatches are the
    graph's contradictions in the making."""
    out: list[str] = []
    if not apis:
        return out

    def style_of(n) -> str:
        for a in g.objects(n, DKG.exposedBy):
            for s in g.objects(a, DKG.interactionStyle):
                return str(s)
        return "?"

    def ctx_of(n) -> set:
        return set(g.objects(n, DKG.inContext))

    def api_ctx(n) -> set:
        out_ = set()
        for a in g.objects(n, DKG.exposedBy):
            out_ |= ctx_of(a)
        return out_

    # 1. an Api straddling contexts; an Api whose sources disagree on protocol
    PROTO_FAMILY = {"http": "http", "https": "http", "rest": "http", "restful": "http",
                    "kafka": "kafka", "amqp": "amqp", "rabbitmq": "amqp", "mqtt": "mqtt",
                    "grpc": "grpc", "ws": "ws", "wss": "ws", "websocket": "ws"}
    for a in sorted(apis, key=str):
        ctxs = ctx_of(a)
        if len(ctxs) > 1:
            out.append(f"API straddles contexts: {label_of(g, a)!r} is in "
                       + ", ".join(sorted(label_of(g, c) for c in ctxs))
                       + " — a boundary the spec ignores, or one context the graph holds twice")
        protos = [str(p) for p in g.objects(a, DKG.protocol)]
        fams = {PROTO_FAMILY.get(p.lower(), p.lower()) for p in protos}
        if len(fams) > 1:
            out.append(f"protocol disagreement: {label_of(g, a)!r} is said to run over "
                       + ", ".join(sorted(repr(p) for p in protos))
                       + " by its sources — canvas and spec do not agree; keep both, ask")
    # 1b. a context with two Apis of one style — identity is context × style
    seen_pair: dict = defaultdict(list)
    for a in apis:
        for c in ctx_of(a):
            seen_pair[(c, str(g.value(a, DKG.interactionStyle) or ""))].append(a)
    for (c, st), aa in sorted(seen_pair.items(), key=lambda kv: str(kv[0])):
        if len(aa) > 1:
            out.append(f"two {st} APIs in {label_of(g, c)!r}: "
                       + ", ".join(sorted(repr(label_of(g, a)) for a in aa))
                       + " — a canvas and a spec that should have merged, or two real APIs; say which")

    ops = typed(g, DKG.Operation)
    amsgs = typed(g, DKG.ApiMessage)
    schemas = typed(g, DKG.Schema)
    events = typed(g, DKG.Occurrence)
    cmds = typed(g, DKG.Activity)
    canvas_msgs = typed(g, DKG.Message)

    # 2. style mismatches along dkg:specifies
    for n in sorted(ops | amsgs, key=str):
        st = style_of(n)
        for tgt in g.objects(n, DKG.specifies):
            kind = str(g.value(tgt, DKG.messageKind) or "")
            is_event = tgt in events or kind == "evt"
            if st == "sync" and is_event:
                out.append(f"style mismatch: {label_of(g, n)!r} is a synchronous operation, but it "
                           f"specifies {label_of(g, tgt)!r}, which the model holds as an event — "
                           "an event exposed as a call, or a command named in the past tense")
            if st == "async" and kind == "qry":
                out.append(f"style mismatch: {label_of(g, n)!r} rides a message channel, but it "
                           f"specifies {label_of(g, tgt)!r}, which the canvas typed 'qry' — "
                           "a query on a broker, or a canvas row typed wrong")
            # a receive across a border, or an API exposing another context's model
            here, there = api_ctx(n), ctx_of(tgt)
            if here and there and not (here & there) and tgt not in canvas_msgs:
                actions = {str(x) for x in g.objects(n, DKG.action)}
                for op in g.subjects(DKG.carriesMessage, n):
                    actions |= {str(x) for x in g.objects(op, DKG.action)}
                if "receive" in actions:
                    out.append(f"border: {label_of(g, n)!r} RECEIVES {label_of(g, tgt)!r}, which "
                               f"{', '.join(sorted(label_of(g, c) for c in there))} emits — "
                               "an asynchronous border the specs now draw; is it on a canvas?")
                else:
                    out.append(f"foreign model: {label_of(g, n)!r} in "
                               f"{', '.join(sorted(label_of(g, c) for c in here))} specifies "
                               f"{label_of(g, tgt)!r}, which belongs to "
                               f"{', '.join(sorted(label_of(g, c) for c in there))} — "
                               "an API speaking for another context's model")

    # 3. unlinked API nodes: contracts the model never asked for
    for n in sorted(ops | amsgs | schemas, key=str):
        role = str(g.value(n, DKG.schemaRole) or "")
        if role in ("header", "error", "payload"):
            continue
        linked = list(g.objects(n, DKG.specifies)) or list(g.objects(n, DKG.mentions)) \
            or list(g.subjects(DKG.derivedFrom, n))   # a refined glossary lifted a term from it
        if not linked and n in ops:
            # an async operation is linked through the messages it carries
            linked = [t for m in g.objects(n, DKG.carriesMessage)
                      for t in g.objects(m, DKG.specifies)]
        if not linked:
            what = "operation" if n in ops else "message" if n in amsgs else "schema"
            out.append(f"unlinked {what}: {label_of(g, n)!r} specifies nothing the model holds — "
                       "the first the workshops hear of it, or a spelling the ledger missed")

    # 4. coverage, per context that has an API
    specified = set()
    for n in ops | amsgs | schemas:
        specified |= set(g.objects(n, DKG.specifies))
    by_ctx: dict = defaultdict(set)
    for a in apis:
        for c in ctx_of(a):
            by_ctx[c].add(str(g.value(a, DKG.interactionStyle) or "?"))
    for c in sorted(by_ctx, key=str):
        styles = by_ctx[c]
        parts = []
        if "sync" in styles:
            my_cmds = [x for x in cmds if c in ctx_of(x)]
            my_in = [m for m in canvas_msgs if g.value(m, DKG.to) == c
                     and str(g.value(m, DKG.messageKind) or "") in ("cmd", "qry", "?")]
            miss = [x for x in my_cmds + my_in if x not in specified]
            total = len(my_cmds) + len(my_in)
            parts.append(f"sync — {total - len(miss)} of {total} commands/inbound rows have an operation"
                         + (": missing " + ", ".join(sorted(repr(label_of(g, x)) for x in miss)) if miss else "")
                         if total else "sync — no command or inbound row modelled here; the spec is the "
                         "only description of this context's interface")
        if "async" in styles:
            my_evs = [x for x in events if c in ctx_of(x)]
            my_out = [m for m in canvas_msgs if g.value(m, DKG["from"]) == c
                      and str(g.value(m, DKG.messageKind) or "") == "evt"]
            miss = [x for x in my_evs + my_out if x not in specified]
            total = len(my_evs) + len(my_out)
            parts.append(f"async — {total - len(miss)} of {total} events/outbound evt rows have a message"
                         + (": missing " + ", ".join(sorted(repr(label_of(g, x)) for x in miss)) if miss else "")
                         if total else "async — no event or outbound evt row modelled here; the spec is the "
                         "only description of this context's interface")
        if parts:
            out.append(f"API coverage of {label_of(g, c)!r}: " + "; ".join(parts))
    return out


def verdict_of(share: float, cover: float) -> str:
    """share = new labels already held; cover = held labels present in new."""
    if share >= 2 / 3 and cover >= 2 / 3:
        return "REVISION — same picture, redrawn"
    if share >= 2 / 3 and cover < 2 / 3:
        return "PARTITION — a piece of it (or the new one borrows heavily)"
    if share < 1 / 3 and cover >= 2 / 3:
        return "SUPERSET — contains it and much more; revision with additions, or a merge of several"
    if share >= 1 / 3 or cover >= 1 / 3:
        return "ASK — partial overlap"
    return ""


def overlap(g: Graph, key: str, labels_path: str) -> int:
    """Is the arriving artifact a revision of an existing one? Compare the
    transcribed labels (one per line) with the nodes sourced to that artifact.
    key may be an artifact IRI suffix, a label fragment, or 'all' — which
    scores every artifact in the register and reports the best matches, so
    the ingester never has to guess which of five glossaries to test against."""
    arts = typed(g, DKG.Artifact)
    if key.lower() in ("all", "*"):
        with open(labels_path) as fh:
            new = [l.strip() for l in fh if l.strip()]
        edges = typed(g, DKG.Relationship) | typed(g, DKG.Assertion)
        scored = []
        for a in arts:
            held = {normalise(label_of(g, n)) for n in g.subjects(DKG.source, a)
                    if n not in edges}
            if not held:
                continue
            hits = sum(1 for l in new if normalise(l) in held)
            share = hits / len(new) if new else 0      # of the NEW, already held
            cover = hits / len(held)                   # of the EXISTING, present in new
            scored.append((share, cover, hits, a, len(held)))
        scored.sort(key=lambda t: -t[0])
        print(f"{len(new)} transcribed labels against {len(scored)} artifacts "
              "(share = of new labels already held · cover = of held labels present in new):\n")
        for share, cover, hits, a, size in scored[:8]:
            types = ", ".join(str(t).split("#")[-1] for t in g.objects(a, RDF.type)
                              if str(t).startswith(str(DKG)) and str(t) != str(DKG.Artifact))
            print(f"  share {share:4.0%} · cover {cover:4.0%}  ({hits}/{len(new)} of new, "
                  f"{hits}/{size} of held)   {label_of(g, a)}  [{types}]  {verdict_of(share, cover)}")
        print("\nRe-run with the best match named to see added / removed labels. "
              "Several artifacts of the same type at a third or more each usually "
              "means the new one PARTITIONS them (or they partition it) — see "
              "reingest-revision.md §0b.")
        return 0
    art = next((a for a in arts if str(a).split("/")[-1].split("#")[-1] == key), None) \
        or next((a for a in arts if key.lower() in label_of(g, a).lower()), None)
    if art is None:
        print(f"no artifact matches {key!r}")
        return 1
    # relationships and assertions are edges, not labels a transcription lists
    edges = typed(g, DKG.Relationship) | typed(g, DKG.Assertion)
    nodes_of_art = [n for n in g.subjects(DKG.source, art) if n not in edges]
    held = {normalise(label_of(g, n)) for n in nodes_of_art}
    with open(labels_path) as fh:
        new = [l.strip() for l in fh if l.strip()]
    hits = [l for l in new if normalise(l) in held]
    misses = [l for l in new if normalise(l) not in held]
    gone = sorted(l for l in {label_of(g, n) for n in nodes_of_art}
                  if normalise(l) not in {normalise(x) for x in new})
    share = len(hits) / len(new) if new else 0.0
    cover = len(hits) / len(held) if held else 0.0
    verdict = verdict_of(share, cover) or "NEW ARTIFACT"
    print(f"{label_of(g, art)}: {len(hits)} of {len(new)} transcribed labels "
          f"already sourced to it ({share:.0%}); they cover {cover:.0%} of what "
          f"it holds → {verdict}")
    if misses:
        print(f"\nnot in the existing artifact ({len(misses)}) — added, or renamed:")
        for l in misses:
            print(f"  + {l}")
    if gone:
        print(f"\nin the existing artifact, not in the transcription ({len(gone)}) "
              "— removed, or renamed:")
        for l in gone:
            print(f"  - {l}")
    print("\nA label in both lists may be one rename. Decide it in the ledger.")
    return 0


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("graph")
    ap.add_argument("--sparql", help="run a SPARQL query file against the graph")
    ap.add_argument("--overlap", nargs=2, metavar=("ARTIFACT", "LABELS_FILE"),
                    help="revision test: share of transcribed labels (one per "
                         "line) already sourced to ARTIFACT; use 'all' to "
                         "score every artifact in the register")
    ap.add_argument("--quiet", action="store_true", help="errors only")
    args = ap.parse_args()

    g = load(args.graph)

    if args.overlap:
        return overlap(g, args.overlap[0], args.overlap[1])

    if args.sparql:
        with open(args.sparql) as fh:
            q = fh.read()
        for row in g.query(q):
            print(" | ".join(str(x) for x in row))
        return 0

    errors, warnings, stats = check(g)

    print(f"parsed {stats['triples']} triples · {stats['nodes']} nodes · "
          f"{stats['artifacts']} artifacts · {stats['assertions']} assertions")
    if stats["by_class"]:
        print("\nby class")
        for k, v in sorted(stats["by_class"].items(), key=lambda kv: -kv[1]):
            print(f"  {v:4d}  {k}")
    if stats["by_confidence"]:
        print("\nby confidence")
        for k in ("OnArtifact", "Implied", "Inferred"):
            if stats["by_confidence"].get(k):
                print(f"  {stats['by_confidence'][k]:4d}  {k}")
    if stats["sources_per_node"]:
        print("\ncorroboration (artifacts per node)")
        for k in sorted(stats["sources_per_node"]):
            print(f"  {stats['sources_per_node'][k]:4d}  node(s) with {k} source(s)")

    print(f"\nERRORS ({len(errors)}) — fix these")
    for e in errors:
        print(f"  ✗ {e}")
    if not errors:
        print("  none")

    if not args.quiet:
        print(f"\nFINDINGS ({len(warnings)}) — report these, do not patch them")
        for w in warnings:
            print(f"  · {w}")
        if not warnings:
            print("  none")

    return 1 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
