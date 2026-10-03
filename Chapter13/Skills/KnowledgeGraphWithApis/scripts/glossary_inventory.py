#!/usr/bin/env python3
"""Inventory a Visual Glossary model file, and draft its (re)ingest.

The model file is what visual-glossary-updater renders from — terms with a
colour-group key, edges with verb / cardinality / kind, and a contexts legend.
A refined per-context glossary is therefore machine-readable, and this script
does for it what api_inventory.py does for a spec.

    python3 glossary_inventory.py model.yaml
        The transcription (Step 2): terms by colour group, edges verbatim.

    python3 glossary_inventory.py model.yaml --labels labels.txt
        One term label per line, for  check_graph.py graph.ttl --overlap ...

    python3 glossary_inventory.py model.yaml --ttl \\
        --artifact Art_Glossary_RackManagement_2026_10 --context Ctx_RackManagement \\
        --owned rack --date 2026-10-06 \\
        [--revision-of Art_Glossary] [--partition] [--spec Art_OpenAPI_RackManagement_2026_10] \\
        [--reviewed] [--graph graph.ttl] > ingest.ttl
        A Turtle skeleton. With --graph and --revision-of it is the REVISION
        DIFF (reingest-revision.md §2): every term and edge binned unchanged /
        changed / added / removed against the nodes sourced to the earlier
        glossary, with the right triples per bin. Removed and added are also
        printed side by side on stderr — a pair there is usually one rename,
        which is the one bin this script will not decide.

Owned colour → dkg:inContext this context. Referenced colour → somebody
else's term: source added on the owner's node, or a Question when nobody
owns it. --spec names the spec artifact whose Schemas the added terms were
lifted from (dkg:derivedFrom); --reviewed says the room accepted the redraw,
so added terms are on-artifact and their cardinalities given.

Requires pyyaml; rdflib only for --graph.
"""
from __future__ import annotations

import argparse
import os
import re
import sys

try:
    import yaml
except ImportError:  # pragma: no cover
    yaml = None


# --------------------------------------------------------------------------

def load_model(path: str) -> dict:
    if yaml is None:
        sys.exit("pyyaml is required:  pip install pyyaml --break-system-packages")
    with open(path, encoding="utf-8") as fh:
        m = yaml.safe_load(fh)
    if not isinstance(m, dict) or "terms" not in m or "edges" not in m:
        sys.exit("not a glossary model file: needs 'terms' and 'edges'")
    names = {t["name"] for t in m["terms"]}
    for e in m["edges"]:
        for end in ("from", "to"):
            if e.get(end) not in names:
                sys.exit(f"edge {e} refers to {e.get(end)!r}, which is not a term — fix the model first")
    return m


def owned_key(m: dict, flag: str | None) -> tuple[str, str]:
    ctxs = m.get("contexts") or {}
    if flag:
        if flag not in ctxs and ctxs:
            sys.exit(f"--owned {flag!r} is not a contexts key; keys are {', '.join(ctxs)}")
        return flag, "--owned"
    for k, v in ctxs.items():
        leg = str((v or {}).get("legend", "")).lower()
        if "owned" in leg or "described" in leg:
            return k, f"legend says {(v or {}).get('legend')!r}"
    if ctxs:
        k = next(iter(ctxs))
        return k, f"first contexts key — CHECK; pass --owned if wrong"
    return "", "model has no contexts block — every term treated as owned"


def normalise(s: str) -> str:
    return "".join(c for c in s.lower() if c.isalnum())


def loose(s: str) -> str:
    """Lineage-loose: case, punctuation and singular/plural fold."""
    k = normalise(s)
    if k.endswith("ies") and len(k) > 4:
        return k[:-3] + "y"
    if k.endswith("s") and not k.endswith("ss") and len(k) > 3:
        return k[:-1]
    return k


def camel(s: str) -> str:
    words = re.sub(r"[^A-Za-z0-9]+", " ", s).split()
    return "".join(w[:1].upper() + w[1:] for w in words)


def lit(s) -> str:
    s = str(s).replace("\\", "\\\\").replace('"', '\\"').replace("\n", " ")
    return f'"{s}"'


# --------------------------------------------------------------------------

def md(m: dict, owned: str, why: str) -> str:
    ctxs = m.get("contexts") or {}
    L = [f"# Inventory — {m.get('title', 'Visual Glossary')}", ""]
    if m.get("subtitle"):
        L.append(f"- subtitle: {m['subtitle']}")
    L.append(f"- owned colour group: `{owned or '(all)'}` ({why})")
    for k, v in ctxs.items():
        L.append(f"- group `{k}`: {(v or {}).get('legend', '')}  "
                 f"{'— OWNED' if k == owned else '— referenced'}")
    L.append(f"\n## Terms ({len(m['terms'])})\n")
    L.append("| # | Term | Group | Owned? | Subtitle (enum values) |")
    L.append("|---|---|---|---|---|")
    for i, t in enumerate(m["terms"], 1):
        g = t.get("context", "")
        L.append(f"| {i} | {t['name']} | {g} | {'yes' if (not owned or g == owned) else 'referenced'} | "
                 f"{t.get('subtitle', '')} |")
    L.append(f"\n## Edges ({len(m['edges'])})\n")
    L.append("| # | From | Verb | Cardinality | To | Kind |")
    L.append("|---|---|---|---|---|---|")
    for i, e in enumerate(m["edges"], 1):
        L.append(f"| {i} | {e['from']} | {e.get('label', '')} | {e.get('card', '—')} | {e['to']} | "
                 f"{e.get('kind', 'assoc')} |")
    L.append("")
    L.append("Everything above is verbatim from the model file. Compare it against the SVG once: "
             "the model is what rendered, but a hand edit to the SVG would not be in it.")
    return "\n".join(L)


# --------------------------------------------------------------------------

class Model:
    def __init__(self, path: str):
        try:
            from rdflib import Graph, RDF, RDFS, Namespace
            from rdflib.namespace import SKOS
        except ImportError:
            sys.exit("rdflib is required for --graph:  pip install rdflib --break-system-packages")
        self.RDF, self.RDFS, self.SKOS = RDF, RDFS, SKOS
        self.DKG = Namespace("https://w3id.org/dkg/ns#")
        g = Graph()
        g.parse(path, format="turtle")
        here = os.path.dirname(os.path.abspath(__file__))
        vocab = os.path.join(here, "..", "assets", "dkg.ttl")
        if os.path.exists(vocab):
            g.parse(vocab, format="turtle")
        self.g = g
        self.concepts = self.typed(self.DKG.Concept)
        self.rels = self.typed(self.DKG.Relationship)

    def typed(self, cls):
        out = set(self.g.subjects(self.RDF.type, cls))
        for sub in self.g.subjects(self.RDFS.subClassOf, cls):
            if sub != cls:
                out |= self.typed(sub)
        return out

    def by_suffix(self, suffix: str):
        for s in self.g.subjects():
            if str(s).endswith("#" + suffix):
                return s
        return None

    def labels(self, n) -> list[str]:
        out = [str(v) for v in self.g.objects(n, self.SKOS.prefLabel)]
        out += [str(v) for v in self.g.objects(n, self.SKOS.altLabel)]
        if not out:
            v = self.g.value(n, self.RDFS.label)
            if v:
                out.append(str(v))
        return out

    def label(self, n) -> str:
        ls = self.labels(n)
        return ls[0] if ls else str(n).split("#")[-1]

    def curie(self, n) -> str:
        return ":" + str(n).split("#")[-1]

    def taken(self, suffix: str) -> bool:
        return self.by_suffix(suffix) is not None

    def contexts_of(self, n):
        return list(self.g.objects(n, self.DKG.inContext))

    def sourced_to(self, art):
        return set(self.g.subjects(self.DKG.source, art))


# --------------------------------------------------------------------------

def emit(m: dict, args, owned: str, model: Model | None) -> tuple[str, list[str]]:
    L: list[str] = []
    ledger: list[str] = []
    art, ctx, date = args.artifact, args.context, args.date
    rev = args.revision_of
    conf_added = "dkg:OnArtifact" if args.reviewed else "dkg:Implied"
    given_added = "true" if args.reviewed else "false"

    old_art = old_terms = old_rels = None
    spec_schemas: dict[str, str] = {}
    if model is not None:
        if rev:
            old_art = model.by_suffix(rev)
            if old_art is None:
                ledger.append(f"LINEAGE  --revision-of {rev} is not in the graph — ingest the earlier glossary first, "
                              "or drop --revision-of and treat this as a first glossary")
            else:
                mine = model.sourced_to(old_art)
                old_terms = {}
                for n in mine & model.concepts:
                    for lb in model.labels(n):
                        old_terms.setdefault(loose(lb), n)
                old_rels = {}
                for r in mine & model.rels:
                    f, t = model.g.value(r, model.DKG["from"]), model.g.value(r, model.DKG.to)
                    v = str(model.g.value(r, model.DKG.verb) or "")
                    if f is not None and t is not None:
                        old_rels[(loose(model.label(f)), normalise(v), loose(model.label(t)))] = r
        if args.spec:
            spec_art = model.by_suffix(args.spec)
            if spec_art is None:
                ledger.append(f"SPEC     --spec {args.spec} is not in the graph; no dkg:derivedFrom drafted")
            else:
                for n in model.sourced_to(spec_art) & model.typed(model.DKG.Schema):
                    key = model.label(n)
                    stem = key
                    for suf in ("Create", "Update", "Request", "Response", "Payload", "Header", "List"):
                        if key.endswith(suf) and len(key) > len(suf):
                            stem = key[: -len(suf)]
                            break
                    spec_schemas.setdefault(loose(stem), model.curie(n))

    L.append(f"\n########## Visual Glossary (refined) — {m.get('title', '')} — ingested {date} ##########")
    L.append("# Drafted by glossary_inventory.py from the updater's model file. Bins: unchanged / changed /")
    L.append("# added / removed. RENAMES are not decided here — see the stderr pairing and the ledger.\n")
    L.append(f":{art} a dkg:VisualGlossary ;")
    L.append(f"    rdfs:label {lit(m.get('title', art))} ;")
    comments = [f"redrawn by visual-glossary-updater from the context's schema; owned colour group {owned!r}"]
    if m.get("subtitle"):
        comments.append(str(m["subtitle"]))
    if args.partition:
        comments.append("partition: one piece of the domain-wide glossary — removal pending until all pieces are in")
    if args.reviewed:
        comments.append(f"redraw reviewed and accepted by the room on {date}: added terms on-artifact, cardinalities given")
    L.append("    rdfs:comment " + " ,\n                 ".join(lit(c) for c in comments) + " ;")
    if rev:
        L.append(f"    dkg:revisionOf :{rev} ;")
    L.append(f'    dkg:ingestedAt "{date}"^^xsd:date .\n')

    # ---- terms --------------------------------------------------------
    iri: dict[str, str] = {}       # term name → IRI suffix
    matched_old = set()
    counts = {"unchanged": 0, "added": 0, "borrowed": 0, "removed": 0, "changed": 0}
    L.append("# --- terms ---------------------------------------------------------------")
    for i, t in enumerate(m["terms"], 1):
        name = t["name"]
        is_owned = (not owned) or t.get("context") == owned
        loc = f"term {i}"
        existing = None
        if old_terms is not None and loose(name) in old_terms:
            existing = old_terms[loose(name)]
            matched_old.add(existing)
        elif model is not None:
            # cross-artifact: exact label only (merging.md), any Concept
            for n in model.concepts:
                if any(normalise(lb) == normalise(name) for lb in model.labels(n)):
                    existing = n
                    break
        if existing is not None:
            iri[name] = str(existing).split("#")[-1]
            pref = model.label(existing)
            ctxs = model.contexts_of(existing)
            if is_owned:
                counts["unchanged"] += 1
                L.append(f":{iri[name]} dkg:source :{art} ; dkg:locator {lit(loc)} ;")
                if ctx and all(str(c).split('#')[-1] != ctx for c in ctxs) and ctxs:
                    L.append(f"    rdfs:comment {lit('refined glossary draws it OWNED here; graph owner is ' + ', '.join(model.label(c) for c in ctxs))} .")
                    L.append(f"# PROPOSE ownership move: reify  :{iri[name]} dkg:inContext :{ctx}  as a dated dkg:Assertion sourced to :{art}")
                    ledger.append(f"OWNERSHIP {name!r} owned here, but the graph has it in "
                                  f"{', '.join(model.label(c) for c in ctxs)} — referenced → owned move, or a straddle; ask")
                else:
                    L.append(f"    dkg:inContext :{ctx} .")
                if pref != name:
                    L.append(f"# NOTE spelling: graph says {pref!r}, this glossary {name!r} — lineage-loose match; "
                             "if the rename is deliberate, move prefLabel and keep the old as altLabel")
                    ledger.append(f"SPELLING {name!r} vs existing {pref!r} ({model.curie(existing)}) — same node; decide which is prefLabel")
            else:
                counts["borrowed"] += 1
                L.append(f":{iri[name]} dkg:source :{art} ; dkg:locator {lit(loc)} .   # referenced colour: borrowed, no inContext here")
                if not ctxs:
                    ledger.append(f"OWNER    {name!r} is referenced here and the graph has no owner for it — raise a Question")
        else:
            base = "Con_" + camel(name)
            suffix = base
            if model is not None and model.taken(base):
                suffix = f"{base}_{ctx.replace('Ctx_', '')}"
                ledger.append(f"IRI      :{base} is taken by a non-Concept node — minted :{suffix}")
            iri[name] = suffix
            counts["added"] += 1
            derived = spec_schemas.get(loose(name))
            L.append(f":{suffix} a dkg:Term ; skos:prefLabel {lit(name)} ;")
            if t.get("subtitle"):
                L.append(f"    rdfs:comment {lit('values: ' + str(t['subtitle']))} ;")
            if is_owned:
                L.append(f"    dkg:inContext :{ctx} ;")
            if derived:
                L.append(f"    dkg:derivedFrom {derived} ;")
            conf = conf_added if is_owned else "dkg:OnArtifact"
            L.append(f"    dkg:source :{art} ; dkg:locator {lit(loc)} ; dkg:confidence {conf} .")
            if not is_owned:
                q = "Q_WhoOwns" + camel(name)
                L.append(f":{q} a dkg:Question ; skos:prefLabel {lit('Which context owns ' + name + '?')} ;")
                L.append(f"    dkg:mentions :{suffix} ; dkg:source :{art} ; dkg:locator {lit(loc)} ; dkg:confidence dkg:Implied .")
                L.append(f":{ctx} dkg:raises :{q} .")
                ledger.append(f"OWNER    {name!r} referenced, nobody in the graph owns it — Question raised")
            ledger.append(f"ADDED    {name!r}" + (f" — derivedFrom {derived}" if derived else
                          " — no Schema of --spec matches; on-artifact if the room drew it, else say where it came from"))
    L.append("")

    # ---- edges --------------------------------------------------------
    L.append("# --- relationships ------------------------------------------------------")
    matched_rels = set()
    for i, e in enumerate(m["edges"], 1):
        f, t, verb, card = e["from"], e["to"], str(e.get("label", "")), e.get("card")
        kind = e.get("kind", "assoc")
        fi, ti = iri[f], iri[t]
        loc = f"edge {i}"
        if kind == "is-a":
            L.append(f":{fi} rdfs:subClassOf :{ti} .   # {loc}: {f} is a {t}")
            L.append(f":{fi} dkg:mentions :{ti} .")
            continue
        key = (loose(f), normalise(verb), loose(t))
        old = old_rels.get(key) if old_rels else None
        base = f"Rel_{camel(f)}{camel(verb)}{camel(t)}"
        if old is not None:
            matched_rels.add(old)
            old_card = str(model.g.value(old, model.DKG.cardinality) or "")
            if (card is None and not old_card) or str(card) == old_card:
                counts["unchanged"] += 1
                L.append(f"{model.curie(old)} dkg:source :{art} .   # {loc}: unchanged")
                continue
            counts["changed"] += 1
            new = f"{base}_{date.replace('-', '_')}"
            L.append(f":{new} a dkg:Relationship ; skos:prefLabel {lit(f + ' ' + verb + ' ' + t)} ;")
            L.append(f"    dkg:from :{fi} ; dkg:verb {lit(verb)} ; dkg:to :{ti} ;")
            L.append(f"    dkg:cardinality {lit(card or '')} ; dkg:cardinalityGiven {given_added} ;")
            L.append(f"    dkg:supersedes {model.curie(old)} ; rdfs:comment {lit('was ' + (old_card or '(none)'))} ;")
            L.append(f"    dkg:inContext :{ctx} ; dkg:source :{art} ; dkg:locator {lit(loc)} ; dkg:confidence {conf_added} .")
            ledger.append(f"CHANGED  {f} —{verb}→ {t}: {old_card or '(none)'} → {card} — supersedes; drawn or lifted?")
            continue
        counts["added"] += 1
        suffix = base
        if model is not None and model.taken(base):
            suffix = f"{base}_{date.replace('-', '_')}"
        L.append(f":{suffix} a dkg:Relationship ; skos:prefLabel {lit(f + ' ' + verb + ' ' + t)} ;")
        L.append(f"    dkg:from :{fi} ; dkg:verb {lit(verb)} ; dkg:to :{ti} ;")
        if card is not None:
            L.append(f"    dkg:cardinality {lit(card)} ; dkg:cardinalityGiven {given_added} ;")
        if kind == "cross":
            L.append(f"    rdfs:comment \"crosses a context boundary: this context's claim about a term it does not own\" ;")
        L.append(f"    dkg:inContext :{ctx} ; dkg:source :{art} ; dkg:locator {lit(loc)} ; dkg:confidence {conf_added} .")
        L.append(f":{fi} dkg:mentions :{ti} .")
    L.append("")

    # ---- removed ------------------------------------------------------
    removed_terms, removed_rels = [], []
    if old_terms is not None:
        removed_terms = sorted({n for n in old_terms.values() if n not in matched_old}, key=str)
        removed_rels = sorted({r for r in old_rels.values() if r not in matched_rels}, key=str)
        counts["removed"] = len(removed_terms) + len(removed_rels)
        L.append("# --- removed (absent from this revision) ----------------------------------")
        if args.partition:
            L.append(f"# partition: removal pending — nothing emitted. Candidates: "
                     + ", ".join(model.label(n) for n in removed_terms + removed_rels))
        else:
            for n in removed_terms + removed_rels:
                L.append(f"{model.curie(n)} dkg:absentFrom :{art} .   # {model.label(n)}")
        L.append("")

    # ---- stderr pairing ----------------------------------------------
    added = [r for r in ledger if r.startswith("ADDED")]
    if old_terms is not None:
        ledger.append("")
        ledger.append(f"BINS     unchanged {counts['unchanged']} · changed {counts['changed']} · added {counts['added']} "
                      f"· borrowed {counts['borrowed']} · removed {counts['removed']}"
                      + (" (pending — partition)" if args.partition else ""))
        if removed_terms and added:
            ledger.append("PAIRING  removed ↔ added — a pair here may be ONE RENAME; decide in the ledger, "
                          "then: existing node keeps its IRI, new prefLabel, old label as altLabel, no absentFrom")
            for n in removed_terms:
                ledger.append(f"           - {model.label(n)}")
            for r in added:
                ledger.append(f"           + {r.split(chr(39))[1]}")
    return "\n".join(L), ledger


# --------------------------------------------------------------------------

def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("model")
    ap.add_argument("--owned", help="the contexts key drawn in the OWNED colour (default: read from the legend)")
    ap.add_argument("--labels", metavar="FILE", help="write one term label per line for --overlap")
    ap.add_argument("--ttl", action="store_true")
    ap.add_argument("--artifact")
    ap.add_argument("--context", help="IRI suffix of the owning dkg:BoundedContext")
    ap.add_argument("--date")
    ap.add_argument("--revision-of", dest="revision_of", help="IRI suffix of the earlier glossary artifact")
    ap.add_argument("--partition", action="store_true", help="one piece of a domain-wide glossary: no absentFrom")
    ap.add_argument("--spec", help="IRI suffix of the spec artifact the redraw was derived from (for dkg:derivedFrom)")
    ap.add_argument("--reviewed", action="store_true", help="the room accepted the redraw: added terms on-artifact")
    ap.add_argument("--graph", help="existing graph.ttl")
    args = ap.parse_args()

    m = load_model(args.model)
    owned, why = owned_key(m, args.owned)

    if args.labels:
        with open(args.labels, "w", encoding="utf-8") as fh:
            fh.write("\n".join(t["name"] for t in m["terms"]) + "\n")
        print(f"wrote {len(m['terms'])} labels to {args.labels}", file=sys.stderr)
        return 0

    if args.ttl:
        missing = [k for k in ("artifact", "context", "date") if not getattr(args, k)]
        if missing:
            sys.exit("--ttl needs " + ", ".join("--" + k for k in missing))
        model = Model(args.graph) if args.graph else None
        ttl, ledger = emit(m, args, owned, model)
        print(ttl)
        print(f"# owned colour group: {owned!r} ({why})", file=sys.stderr)
        if ledger:
            print("\n# ===== ledger draft (stderr) =====", file=sys.stderr)
            for row in ledger:
                print("  " + row, file=sys.stderr)
        return 0

    print(md(m, owned, why))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
