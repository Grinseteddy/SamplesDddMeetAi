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
    OWL.imports,
}

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

    # near-duplicate labels that were not merged or proposed
    labels = defaultdict(list)
    for n in nodes:
        labels[label_of(g, n)].append(n)
    known = set()
    for s, _, o in g.triples((None, DKG.proposedSameAs, None)):
        known.add(frozenset({s, o}))
    for s, _, o in g.triples((None, DKG.contradicts, None)):
        known.add(frozenset({s, o}))
    # Sentences and slices are labelled with whole sentences; they are not
    # duplicate candidates and would otherwise drown the report.
    skip = typed(g, DKG.Sentence) | typed(g, DKG.Slice)
    fam_cache: dict = {}

    def family(n):
        if n not in fam_cache:
            fam_cache[n] = next(
                (f for f in (DKG.Occurrence, DKG.Activity, DKG.Actor,
                             DKG.Capability, DKG.Goal, DKG.Deliverable,
                             DKG.BoundedContext, DKG.Question, DKG.Rule,
                             DKG.Concept)
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


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("graph")
    ap.add_argument("--sparql", help="run a SPARQL query file against the graph")
    ap.add_argument("--quiet", action="store_true", help="errors only")
    args = ap.parse_args()

    g = load(args.graph)

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