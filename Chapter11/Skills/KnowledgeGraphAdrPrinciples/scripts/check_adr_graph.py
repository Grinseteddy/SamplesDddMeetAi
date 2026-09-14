#!/usr/bin/env python3
"""Check an ADR & Principles knowledge graph and derive the cross-check from it.

    python3 check_adr_graph.py graph.ttl            # errors, warnings, findings
    python3 check_adr_graph.py graph.ttl --matrix   # + compliance matrix (markdown)
    python3 check_adr_graph.py graph.ttl --json out.json

Errors make the graph untrustworthy (a Principle or Decision with no source or
confidence; an overrides edge whose reification does not say whether it was
acknowledged). Fix those.

Findings are what the graph is FOR — dead-letter principles, decisions that cite
nothing, unacknowledged overrides, principle tensions, source defects. Report
them; never fix them by inventing edges.

The base dkg.ttl and this skill's dkg-adr.ttl are loaded if they can be found
(beside the graph, in the working directory, or in the skill/asset folders) so
subclass reasoning works; the checker still runs without them.
"""
from __future__ import annotations

import argparse
import json
import sys
from collections import defaultdict
from pathlib import Path

try:
    from rdflib import Graph, RDF, RDFS, Namespace, URIRef
    from rdflib.namespace import SKOS
except ImportError:
    sys.exit("rdflib is required:  pip install rdflib --break-system-packages")

DKG = Namespace("https://w3id.org/dkg/ns#")
CONF = {DKG.OnArtifact, DKG.Implied, DKG.Inferred}

VOCAB_CANDIDATES = [
    "dkg.ttl", "dkg-adr.ttl",
    "assets/dkg.ttl", "assets/dkg-adr.ttl",
    "/mnt/skills/plugins/domain-knowledge-graph/assets/dkg.ttl",
    "/mnt/skills/user/domain-knowledge-graph/assets/dkg.ttl",
    "/mnt/skills/plugins/ddd-modeling-pipeline:domain-knowledge-graph/assets/dkg.ttl",
]


def load(graph_path: Path) -> tuple[Graph, list[str]]:
    g = Graph()
    loaded = []
    here = Path(__file__).resolve().parent.parent
    for cand in VOCAB_CANDIDATES + [str(here / "assets" / "dkg-adr.ttl")]:
        for base in (graph_path.parent, Path.cwd(), Path("/")):
            p = (base / cand) if not cand.startswith("/") else Path(cand)
            if p.exists() and str(p) not in loaded:
                try:
                    g.parse(str(p), format="turtle")
                    loaded.append(str(p))
                except Exception as e:  # pragma: no cover
                    print(f"warning: could not parse {p}: {e}", file=sys.stderr)
    g.parse(str(graph_path), format="turtle")
    return g, loaded


def label(g: Graph, n) -> str:
    for p in (SKOS.prefLabel, RDFS.label):
        v = g.value(n, p)
        if v:
            return str(v)
    return str(n).split("#")[-1]


def short(n) -> str:
    return str(n).split("#")[-1]


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("graph")
    ap.add_argument("--matrix", action="store_true", help="print the compliance matrix as markdown")
    ap.add_argument("--json", help="write findings as JSON")
    a = ap.parse_args()

    g, loaded = load(Path(a.graph))
    print(f"parsed {a.graph}: {len(g)} triples; vocab loaded: {len(loaded)} file(s)")

    principles = sorted(set(g.subjects(RDF.type, DKG.Principle)), key=short)
    decisions = sorted(set(g.subjects(RDF.type, DKG.Decision)), key=short)
    errors, findings = [], defaultdict(list)

    # --- structural errors ------------------------------------------------
    for n in principles + decisions:
        if g.value(n, DKG.source) is None:
            errors.append(f"{short(n)} has no dkg:source")
        c = g.value(n, DKG.confidence)
        if c is None or c not in CONF:
            errors.append(f"{short(n)} has no valid dkg:confidence")
    for p in principles:
        if g.value(p, DKG.testable) is None:
            errors.append(f"{short(p)} has no dkg:testable — decide, don't leave it implicit")

    # reifications, indexed by (subject, predicate, object)
    reif = {}
    for asn in g.subjects(RDF.type, DKG.Assertion):
        key = (g.value(asn, RDF.subject), g.value(asn, RDF.predicate), g.value(asn, RDF.object))
        reif[key] = asn

    # --- overrides: every one must be reified with dkg:acknowledged --------
    overrides = []
    for d, p in g.subject_objects(DKG.overrides):
        asn = reif.get((d, DKG.overrides, p))
        ack = g.value(asn, DKG.acknowledged) if asn else None
        if ack is None:
            errors.append(f"{short(d)} overrides {short(p)} but no Assertion says whether it is acknowledged")
        overrides.append((d, p, ack is not None and bool(ack), str(g.value(asn, RDFS.comment) or "") if asn else ""))

    # --- honors, with how they were cited ----------------------------------
    honors = []
    for d, p in g.subject_objects(DKG.honors):
        asn = reif.get((d, DKG.honors, p))
        how = str(g.value(asn, DKG.citedBy) or "unspecified") if asn else "unspecified"
        honors.append((d, p, how))

    cites = set(g.subject_objects(DKG.cites))

    # --- findings ------------------------------------------------------------
    touched = defaultdict(set)
    for d, p, _ in honors:
        touched[p].add(d)
    for d, p, _, _ in overrides:
        touched[p].add(d)
    for d, p in cites:
        touched[p].add(d)
    for p in principles:
        testable = g.value(p, DKG.testable)
        if testable is not None and not bool(testable):
            findings["untestable_principles"].append(short(p))
        elif not touched[p]:
            findings["dead_letter_principles"].append(short(p))
        elif not any(pp == p for _, pp in cites):
            findings["never_cited_principles"].append(short(p))

    for d in decisions:
        if not any(dd == d for dd, _ in cites):
            engaged = any(dd == d for dd, _, _ in honors) or any(dd == d for dd, _, _, _ in overrides)
            findings["decisions_citing_no_principle"].append(
                f"{short(d)} ({'engages principles incidentally' if engaged else 'engages no principle at all'})")

    for d, p, ack, cmt in overrides:
        (findings["acknowledged_exceptions"] if ack else findings["unacknowledged_overrides"]).append(
            f"{short(d)} → {short(p)}" + (f" — {cmt}" if cmt else ""))

    for p1, p2 in g.subject_objects(DKG.inTensionWith):
        findings["principle_tensions"].append(f"{short(p1)} ↔ {short(p2)}")

    for s, o in g.subject_objects(DKG.defect):
        findings["source_defects"].append(f"{short(s)}: {o}")

    # supersession & same-question contradictions among decisions
    for d1, d2 in g.subject_objects(DKG.supersedes):
        findings["supersessions"].append(f"{short(d1)} supersedes {short(d2)}")
    by_q = defaultdict(list)
    for d in decisions:
        for q in g.objects(d, DKG.answers):
            by_q[q].append(d)
    for q, ds in by_q.items():
        active = [d for d in ds if str(g.value(d, DKG.status) or "").lower() in {"adopted", "accepted"}]
        if len(active) > 1:
            opts = {str(g.value(d, DKG.decidedOption) or "") for d in active}
            if len(opts) > 1:
                findings["conflicting_active_decisions"].append(
                    f"{label(g, q)[:80]}… : " + " vs ".join(short(d) for d in active))
    for d in decisions:
        if not list(g.objects(d, DKG.answers)):
            findings["decisions_answering_no_question"].append(short(d))

    # --- output --------------------------------------------------------------
    print(f"principles: {len(principles)}  decisions: {len(decisions)}  honors: {len(honors)}  overrides: {len(overrides)}  cites: {len(cites)}")
    print(f"\nERRORS ({len(errors)})")
    for e in errors:
        print("  ✗", e)
    print("\nFINDINGS")
    for k in ("unacknowledged_overrides", "acknowledged_exceptions", "principle_tensions",
              "dead_letter_principles", "never_cited_principles", "untestable_principles",
              "decisions_citing_no_principle", "conflicting_active_decisions", "supersessions",
              "decisions_answering_no_question", "source_defects"):
        v = findings.get(k, [])
        print(f"  {k} ({len(v)})")
        for x in v:
            print("     -", x)

    if a.matrix:
        print("\n## Compliance Matrix\n")
        print("| Decision | Honors | Overrides | Cites |")
        print("|---|---|---|---|")
        for d in decisions:
            h = ", ".join(f"{short(p)} ({how})" for dd, p, how in honors if dd == d) or "—"
            o = ", ".join(f"{short(p)} ({'acknowledged' if ack else 'UNACKNOWLEDGED'})"
                          for dd, p, ack, _ in overrides if dd == d) or "—"
            c = ", ".join(short(p) for dd, p in cites if dd == d) or "—"
            print(f"| {short(d)} | {h} | {o} | {c} |")

    if a.json:
        Path(a.json).write_text(json.dumps({"errors": errors, "findings": findings,
                                            "principles": [short(p) for p in principles],
                                            "decisions": [short(d) for d in decisions]},
                                           indent=2, ensure_ascii=False))
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())