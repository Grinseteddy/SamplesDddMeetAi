#!/usr/bin/env python3
"""Cut one bounded context out of a Domain Knowledge Graph (dkg Turtle) as JSON.

Usage:
  python3 context_slice.py graph.ttl --list-contexts
  python3 context_slice.py graph.ttl --context "Cook Assistance" [--out slice.json]

Exit codes: 0 ok · 2 context name ambiguous · 3 context not found · 4 graph unreadable.
Needs rdflib (pip install rdflib --break-system-packages).
"""
import argparse, difflib, json, re, sys
from collections import defaultdict

try:
    from rdflib import Graph, Namespace, URIRef, Literal
    from rdflib.namespace import RDF, RDFS, SKOS
except ImportError:
    sys.exit("rdflib missing: pip install rdflib --break-system-packages")

DKG = Namespace("https://w3id.org/dkg/ns#")

def cls(*names):
    return {DKG[n] for n in names}

CTX = cls("BoundedContext", "Lane", "Swimlane")
ACTOR = cls("Actor", "CustomerSegment", "KeyPartner", "ExternalSystem")
COMMAND = cls("Activity", "Command", "KeyActivity")
EVENT = cls("Occurrence", "DomainEvent", "PivotalEvent")
RULE = cls("Rule", "Policy")
TERM = cls("Concept", "Term", "Entity", "ValueObject", "AggregateRoot",
           "WorkObject", "Aggregate", "BusinessObject", "ReadModel")
QUESTION = cls("Question", "Hotspot")
STRATEGY = cls("Goal", "UserNeed", "Impact", "Deliverable", "Idea",
               "ValueProposition", "CustomerSegment", "Capability", "Component")
# Edges walked upward from the context towards the "why"
UP = [DKG.realises, DKG.supports, DKG.towardsGoal, DKG.impactedActor,
      DKG.servesSegment, DKG.proposedSameAs, DKG.mentions, DKG.dependsOn]
SKIP_PRED = {RDF.type, SKOS.prefLabel, SKOS.altLabel, RDFS.label, RDFS.comment,
             DKG.source, DKG.locator, DKG.confidence}

def local(u):
    s = str(u)
    return re.split(r"[#/]", s)[-1]

def norm(s):
    return re.sub(r"[^a-z0-9]", "", s.lower())

def label(g, n):
    for p in (SKOS.prefLabel, RDFS.label):
        v = g.value(n, p)
        if v is not None:
            return str(v)
    return local(n)

def types(g, n):
    return sorted(local(t) for t in g.objects(n, RDF.type))

def is_a(g, n, group):
    return any(t in group for t in g.objects(n, RDF.type))

def contexts_of(g, n):
    return sorted(label(g, c) for c in g.objects(n, DKG.inContext))

def describe(g, n):
    d = {"iri": local(n), "label": label(g, n), "types": types(g, n)}
    alts = sorted(str(a) for a in g.objects(n, SKOS.altLabel))
    if alts: d["altLabels"] = alts
    d["sources"] = sorted(label(g, a) for a in g.objects(n, DKG.source))
    locs = sorted(str(x) for x in g.objects(n, DKG.locator))
    if locs: d["locators"] = locs
    conf = g.value(n, DKG.confidence)
    d["confidence"] = local(conf) if conf is not None else None
    d["contexts"] = contexts_of(g, n)
    for p in ("sequence", "sliceKind", "hasState", "messageKind", "carries",
              "verb", "cardinality", "cardinalityGiven", "status",
              "decidedOption", "metric", "domainRole", "votes"):
        vals = [v.toPython() if isinstance(v, Literal) else local(v)
                for v in g.objects(n, DKG[p])]
        if vals: d[p] = vals if len(vals) > 1 else vals[0]
    comments = [str(c) for c in g.objects(n, RDFS.comment)]
    if comments: d["comment"] = comments
    out, inc = [], []
    for p, o in g.predicate_objects(n):
        if p in SKIP_PRED or p in (DKG.inContext,) or not isinstance(o, URIRef):
            continue
        if o.startswith(str(DKG)):  # vocabulary value, e.g. evolution stage
            out.append({"pred": local(p), "value": local(o)}); continue
        out.append({"pred": local(p), "to": local(o), "toLabel": label(g, o),
                    "toTypes": types(g, o), "toContexts": contexts_of(g, o)})
    for s, p in g.subject_predicates(n):
        if p in SKIP_PRED or p == DKG.inContext or p in (RDF.subject, RDF.object):
            continue
        inc.append({"pred": local(p), "from": local(s), "fromLabel": label(g, s),
                    "fromTypes": types(g, s), "fromContexts": contexts_of(g, s)})
    if out: d["out"] = sorted(out, key=lambda e: (e["pred"], e.get("to", "")))
    if inc: d["in"] = sorted(inc, key=lambda e: (e["pred"], e["from"]))
    return d

def all_contexts(g):
    seen = set()
    for t in CTX:
        for c in g.subjects(RDF.type, t):
            seen.add(c)
    return sorted(seen, key=lambda c: label(g, c))

def resolve(g, name):
    want = norm(name)
    ctxs = all_contexts(g)
    hits = [c for c in ctxs
            if want in {norm(label(g, c)), norm(local(c)), norm(local(c).split("_", 1)[-1])}
            | {norm(str(a)) for a in g.objects(c, SKOS.altLabel)}]
    return hits, ctxs

def slice_context(g, ctx):
    # the context itself plus anything the graph says is the same / renamed context
    family = {ctx}
    pending = []
    for p in (DKG.proposedSameAs, DKG.renamedTo):
        for o in list(g.objects(ctx, p)) + list(g.subjects(p, ctx)):
            if is_a(g, o, CTX):
                (family.add(o) if p == DKG.renamedTo else pending.append(o))
    members = set()
    for c in family:
        members |= set(g.subjects(DKG.inContext, c))
    # one hop out: slices, rules, sentences, questions, messages hanging off members
    attached = set()
    for m in members:
        for p, o in g.predicate_objects(m):
            if isinstance(o, URIRef) and p not in SKIP_PRED and not str(o).startswith(str(DKG)) \
                    and o not in members and not is_a(g, o, CTX):
                attached.add(o)
        for s, p in g.subject_predicates(m):
            if p in SKIP_PRED or p in (RDF.subject, RDF.object) or s in members:
                continue
            if (s, RDF.type, DKG.Assertion) in g:
                continue
            attached.add(s)
    # walk upward for the "why" (impacts, goals, value propositions, capabilities)
    why, frontier = {}, set(members | attached)
    for depth in range(1, 4):
        nxt = set()
        for n in frontier:
            for p in UP:
                for o in list(g.objects(n, p)) + ([s for s in g.subjects(p, n)] if p in (DKG.realises, DKG.proposedSameAs, DKG.impactedActor) else []):
                    if isinstance(o, URIRef) and is_a(g, o, STRATEGY) and o not in why:
                        why[o] = depth; nxt.add(o)
        frontier = nxt
    # questions and the decisions answering them
    qs = {n for n in members | attached if is_a(g, n, QUESTION)}
    for n in members | attached:
        qs |= {q for q in g.objects(n, DKG.raises)}
    decisions = {d for q in qs for d in g.subjects(DKG.answers, q)}
    # contradictions and straddlers
    nodes = members | attached
    contra = []
    for a in g.subjects(RDF.type, DKG.Assertion):
        s, o = g.value(a, RDF.subject), g.value(a, RDF.object)
        if s in nodes or o in nodes:
            for b in g.objects(a, DKG.contradicts):
                contra.append({"assertion": local(a), "subject": label(g, s),
                               "predicate": local(g.value(a, RDF.predicate)),
                               "object": label(g, o) if o is not None else None,
                               "source": [label(g, x) for x in g.objects(a, DKG.source)],
                               "contradicts": local(b)})
    straddlers = [label(g, m) for m in members if len(set(g.objects(m, DKG.inContext))) > 1]
    pending_merges = [{"a": label(g, s), "b": label(g, o)}
                      for s, o in g.subject_objects(DKG.proposedSameAs) if s in nodes or o in nodes]

    def bucket(group):
        return [describe(g, n) for n in sorted(members, key=lambda n: label(g, n)) if is_a(g, n, group)]

    grouped = set()
    for grp in (ACTOR, COMMAND, EVENT, RULE, TERM, QUESTION):
        grouped |= {n for n in members if is_a(g, n, grp)}
    return {
        "context": describe(g, ctx),
        "contextFamily": [label(g, c) for c in family if c != ctx],
        "pendingContextMerges": [label(g, c) for c in pending],
        "actors": bucket(ACTOR),
        "commands": bucket(COMMAND),
        "events": bucket(EVENT),
        "rules": bucket(RULE),
        "terms": bucket(TERM),
        "otherMembers": [describe(g, n) for n in sorted(members - grouped, key=lambda n: label(g, n))],
        "attached": [describe(g, n) for n in sorted(attached, key=lambda n: label(g, n))],
        "why": [dict(describe(g, n), hops=d) for n, d in sorted(why.items(), key=lambda x: (x[1], label(g, x[0])))],
        "questions": [describe(g, q) for q in sorted(qs, key=lambda n: label(g, n))],
        "decisions": [describe(g, d) for d in sorted(decisions, key=lambda n: label(g, n))],
        "contradictions": contra,
        "straddlers": straddlers,
        "pendingMerges": pending_merges,
        "counts": {"members": len(members), "attached": len(attached), "why": len(why)},
    }

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("graph")
    ap.add_argument("--context")
    ap.add_argument("--list-contexts", action="store_true")
    ap.add_argument("--out")
    a = ap.parse_args()
    g = Graph()
    try:
        g.parse(a.graph, format="turtle")
    except Exception as e:
        print(f"cannot parse {a.graph}: {e}", file=sys.stderr); sys.exit(4)
    if a.list_contexts or not a.context:
        rows = [{"label": label(g, c), "iri": local(c), "types": types(g, c),
                 "altLabels": sorted(str(x) for x in g.objects(c, SKOS.altLabel)),
                 "members": len(set(g.subjects(DKG.inContext, c))),
                 "sources": sorted(label(g, s) for s in g.objects(c, DKG.source))}
                for c in all_contexts(g)]
        print(json.dumps(rows, indent=2, ensure_ascii=False)); return
    hits, ctxs = resolve(g, a.context)
    if len(hits) > 1:
        print(json.dumps({"ambiguous": [label(g, c) for c in hits]}, indent=2)); sys.exit(2)
    if not hits:
        names = [label(g, c) for c in ctxs]
        print(json.dumps({"notFound": a.context,
                          "closest": difflib.get_close_matches(a.context, names, n=4, cutoff=0.3),
                          "allContexts": names}, indent=2, ensure_ascii=False)); sys.exit(3)
    data = slice_context(g, hits[0])
    text = json.dumps(data, indent=2, ensure_ascii=False, default=str)
    if a.out:
        open(a.out, "w", encoding="utf-8").write(text)
        print(f"wrote {a.out}: {data['counts']}")
    else:
        print(text)

if __name__ == "__main__":
    main()
