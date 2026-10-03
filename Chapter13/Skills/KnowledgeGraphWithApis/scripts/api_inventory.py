#!/usr/bin/env python3
"""Inventory an API specification, and draft its ingest.

    python3 api_inventory.py spec.yaml
        The transcription (Step 2): info, operations, channels, messages,
        schemas — as Markdown tables, verbatim. Show this to the user.

    python3 api_inventory.py spec.yaml --labels labels.txt
        One label per line, for  check_graph.py graph.ttl --overlap ...
        (Step 0: is this spec a revision of one already in the register?)

    python3 api_inventory.py spec.yaml --ttl \\
        --artifact Art_OpenAPI_RackManagement_2026_10 \\
        --context Ctx_RackManagement --date 2026-10-03 \\
        [--graph graph.ttl] [--base https://w3id.org/dkg/graph/<project>#] > ingest.ttl
        A Turtle SKELETON for Step 4: the Api, its Operations, Channels,
        Messages and Schemas with every literal verbatim and on-artifact.
        With --graph, it also drafts the links INTO the model — but only the
        exact-after-normalisation ones, as direct dkg:specifies / dkg:mentions
        (dkg:Implied). Everything looser is printed as a commented PROPOSE line
        and a ledger row on stderr. The ingester decides; the script never
        writes to the graph.

Handles OpenAPI 3.x and AsyncAPI 3.x in YAML or JSON. protobuf and GraphQL
are transcribed by hand (references/ingest-api-specification.md §"Other
synchronous formats"); the Turtle shape is the same.

Requires pyyaml; rdflib only for --graph.
"""
from __future__ import annotations

import argparse
import difflib
import json
import os
import re
import sys
from collections import OrderedDict

try:
    import yaml
except ImportError:  # pragma: no cover
    yaml = None

ROLE_SUFFIXES = [
    ("Create", "create"), ("Update", "update"), ("Request", "request"),
    ("Response", "response"), ("Payload", "payload"), ("Header", "header"),
    ("Headers", "header"), ("Error", "error"), ("List", "read"),
    ("Summary", "read"), ("Detail", "read"), ("Details", "read"),
]
MUTATING = {"POST", "PUT", "PATCH", "DELETE"}
PAST_TENSE = re.compile(r"(ed|en|t|wn|ne|un|ome|one|ought|aught|eft|ent|ept)$", re.I)


# --------------------------------------------------------------------------
# loading
# --------------------------------------------------------------------------

def load_spec(path: str) -> dict:
    with open(path, encoding="utf-8") as fh:
        text = fh.read()
    if path.lower().endswith(".json"):
        return json.loads(text)
    if yaml is None:
        sys.exit("pyyaml is required for YAML specs:  pip install pyyaml --break-system-packages")
    return yaml.safe_load(text)


def kind_of(spec: dict) -> str:
    if "openapi" in spec:
        return "openapi"
    if "asyncapi" in spec:
        return "asyncapi"
    if "swagger" in spec:
        return "openapi"
    sys.exit("not an OpenAPI or AsyncAPI document (no 'openapi' / 'asyncapi' key)")


def deref(spec: dict, node):
    """Follow a local $ref chain. Returns (resolved, last_ref_name_or_None)."""
    name = None
    seen = 0
    while isinstance(node, dict) and "$ref" in node and seen < 20:
        ref = node["$ref"]
        seen += 1
        if not ref.startswith("#/"):
            return node, name
        name = ref.split("/")[-1]
        cur = spec
        for part in ref[2:].split("/"):
            part = part.replace("~1", "/").replace("~0", "~")
            if not isinstance(cur, dict) or part not in cur:
                return node, name
            cur = cur[part]
        node = cur
    return node, name


def schema_ref_name(spec: dict, schema) -> str | None:
    """The components.schemas key a schema (or array of) points at."""
    if not isinstance(schema, dict):
        return None
    if "$ref" in schema:
        ref = schema["$ref"]
        if "/schemas/" in ref:
            return ref.split("/")[-1]
        resolved, _ = deref(spec, schema)
        return schema_ref_name(spec, resolved)
    if schema.get("type") == "array" and isinstance(schema.get("items"), dict):
        return schema_ref_name(spec, schema["items"])
    for key in ("allOf", "oneOf", "anyOf"):
        if key in schema and isinstance(schema[key], list):
            for s in schema[key]:
                n = schema_ref_name(spec, s)
                if n:
                    return n
    return None


def content_schema_name(spec: dict, obj) -> str | None:
    obj, _ = deref(spec, obj)
    if not isinstance(obj, dict):
        return None
    content = obj.get("content") or {}
    for _, media in content.items():
        n = schema_ref_name(spec, (media or {}).get("schema"))
        if n:
            return n
    return None


# --------------------------------------------------------------------------
# inventory model
# --------------------------------------------------------------------------

def inventory(spec: dict, path: str) -> dict:
    kind = kind_of(spec)
    info = spec.get("info") or {}
    inv = {
        "kind": kind,
        "file": os.path.basename(path),
        "format": f"OpenAPI {spec.get('openapi') or spec.get('swagger')}" if kind == "openapi"
                  else f"AsyncAPI {spec.get('asyncapi')}",
        "style": "sync" if kind == "openapi" else "async",
        "title": info.get("title") or os.path.basename(path),
        "version": info.get("version"),
        "description": (info.get("description") or "").strip(),
        "apiId": info.get("x-api-id"),
        "audience": info.get("x-audience"),
        "protocols": [],
        "operations": [],
        "channels": [],
        "messages": [],
        "schemas": [],
        "root_scopes": [],
    }

    servers = spec.get("servers") or {}
    if isinstance(servers, list):
        for s in servers:
            url = (s or {}).get("url", "")
            proto = (s or {}).get("protocol") or (url.split("://")[0] if "://" in url else "")
            if proto and proto not in inv["protocols"]:
                inv["protocols"].append(proto)
    elif isinstance(servers, dict):
        for s in servers.values():
            proto = (s or {}).get("protocol")
            if proto and proto not in inv["protocols"]:
                inv["protocols"].append(proto)

    comps = spec.get("components") or {}

    # schemas
    for key, sch in (comps.get("schemas") or {}).items():
        role, stem = "other", key
        for suf, r in ROLE_SUFFIXES:
            if key.endswith(suf) and len(key) > len(suf):
                role, stem = r, key[: -len(suf)]
                break
        if role == "other" and key not in ("Error", "MessageHeader"):
            role = "read"
        if key == "Error":
            role, stem = "error", "Error"
        if key == "MessageHeader":
            role, stem = "header", "MessageHeader"
        desc = (sch or {}).get("description") or (sch or {}).get("title") or ""
        inv["schemas"].append({"key": key, "role": role, "stem": stem,
                               "description": str(desc).strip().split("\n")[0]})

    if kind == "openapi":
        for sec in spec.get("security") or []:
            for scopes in (sec or {}).values():
                inv["root_scopes"] += list(scopes or [])
        for p, item in (spec.get("paths") or {}).items():
            if not isinstance(item, dict):
                continue
            for method, op in item.items():
                if method.lower() not in ("get", "post", "put", "patch", "delete", "head", "options"):
                    continue
                if not isinstance(op, dict):
                    continue
                scopes = []
                if "security" in op:
                    for sec in op["security"] or []:
                        for sc in (sec or {}).values():
                            scopes += list(sc or [])
                else:
                    scopes = list(inv["root_scopes"])
                req = content_schema_name(spec, op.get("requestBody")) if op.get("requestBody") else None
                resp = None
                for code in ("200", "201", "202", 200, 201, 202):
                    if code in (op.get("responses") or {}):
                        resp = content_schema_name(spec, op["responses"][code])
                        if resp:
                            break
                m = method.upper()
                inv["operations"].append({
                    "id": op.get("operationId") or f"{method}{p}",
                    "method": m, "path": p,
                    "summary": (op.get("summary") or "").strip(),
                    "tags": list(op.get("tags") or []),
                    "scopes": scopes, "request": req, "response": resp,
                    "kind": "qry" if m in ("GET", "HEAD") else "cmd",
                    "action": None, "channel": None, "messages": [],
                })
        # resource nouns from paths: the last non-parameter segment
        for o in inv["operations"]:
            segs = [s for s in o["path"].split("/") if s and not s.startswith("{")]
            o["resource"] = segs[-1] if segs else ""

    else:  # asyncapi
        chan_msgs: dict[str, list[str]] = {}
        for ckey, ch in (spec.get("channels") or {}).items():
            ch = ch or {}
            msgs = []
            for mkey, mref in (ch.get("messages") or {}).items():
                _, name = deref(spec, mref)
                msgs.append(name or mkey)
            chan_msgs[ckey] = msgs
            inv["channels"].append({"key": ckey, "address": ch.get("address", ""),
                                    "messages": msgs,
                                    "description": (ch.get("description") or "").strip().split("\n")[0]})
        for mkey, msg in (comps.get("messages") or {}).items():
            msg = msg or {}
            inv["messages"].append({
                "key": mkey, "name": msg.get("name") or mkey, "title": msg.get("title") or "",
                "summary": (msg.get("summary") or "").strip(),
                "payload": schema_ref_name(spec, msg.get("payload")),
                "headers": schema_ref_name(spec, msg.get("headers")),
            })
        for okey, op in (spec.get("operations") or {}).items():
            op = op or {}
            chref = (op.get("channel") or {}).get("$ref", "")
            ckey = chref.split("/")[-1] if chref else None
            msgs = []
            for mref in op.get("messages") or []:
                ref = (mref or {}).get("$ref", "")
                # #/channels/<ch>/messages/<key>  or  #/components/messages/<key>
                mk = ref.split("/")[-1]
                resolved, name = deref(spec, mref)
                msgs.append(name or mk)
            if not msgs and ckey:
                msgs = list(chan_msgs.get(ckey, []))
            names = []
            for mk in msgs:
                found = next((m["name"] for m in inv["messages"] if m["key"] == mk or m["name"] == mk), mk)
                names.append(found)
            inv["operations"].append({
                "id": okey, "method": None, "path": None,
                "summary": (op.get("summary") or op.get("title") or "").strip(),
                "tags": [], "scopes": [], "request": None, "response": None,
                "kind": None, "action": op.get("action"), "channel": ckey,
                "messages": names, "resource": "",
            })
            # an AsyncAPI operation on past-tense messages is an event flow
            if names and all(is_past_tense(n) for n in names):
                inv["operations"][-1]["kind"] = "evt"
    return inv


def is_past_tense(name: str) -> bool:
    words = decamel(name).split()
    return bool(words) and bool(PAST_TENSE.search(words[-1]))


# --------------------------------------------------------------------------
# labels and normalisation
# --------------------------------------------------------------------------

def decamel(s: str) -> str:
    s = re.sub(r"([a-z0-9])([A-Z])", r"\1 \2", s)
    s = re.sub(r"([A-Z]+)([A-Z][a-z])", r"\1 \2", s)
    return s.replace("-", " ").replace("_", " ").strip()


def normalise(s: str) -> str:
    return "".join(c for c in s.lower() if c.isalnum())


def camel(s: str) -> str:
    words = re.sub(r"[^A-Za-z0-9]+", " ", decamel(s)).split()
    return "".join(w[:1].upper() + w[1:] for w in words)


def op_label(o: dict) -> str:
    """summary when the spec wrote one, else the key verbatim (matching de-camels anyway)."""
    return o["summary"] or o["id"]


def labels_of(inv: dict) -> list[str]:
    out = [op_label(o) for o in inv["operations"]]
    out += [m["name"] for m in inv["messages"]]
    out += [c["key"] for c in inv["channels"]]
    out += [s["key"] for s in inv["schemas"]]
    return out


# --------------------------------------------------------------------------
# markdown transcription
# --------------------------------------------------------------------------

def md(inv: dict) -> str:
    L = [f"# Inventory — {inv['title']}", ""]
    L.append(f"- file: `{inv['file']}` · format: {inv['format']} · style: **{inv['style']}**")
    L.append(f"- version: {inv['version'] or '—'} · x-api-id: {inv['apiId'] or '—'} · "
             f"x-audience: {inv['audience'] or '—'} · protocol(s): {', '.join(inv['protocols']) or '—'}")
    if inv["description"]:
        L.append(f"- description: {inv['description'].splitlines()[0]}")
    if inv["root_scopes"]:
        L.append(f"- root security scopes: {', '.join(inv['root_scopes'])}")
    L.append("")
    if inv["kind"] == "openapi":
        L.append(f"## Operations ({len(inv['operations'])})\n")
        L.append("| # | Method | Path | operationId | Summary | Tag | Scopes | Request | Response | kind |")
        L.append("|---|---|---|---|---|---|---|---|---|---|")
        for i, o in enumerate(inv["operations"], 1):
            L.append(f"| {i} | {o['method']} | `{o['path']}` | `{o['id']}` | {o['summary']} | "
                     f"{', '.join(o['tags'])} | {', '.join(o['scopes'])} | "
                     f"{o['request'] or '—'} | {o['response'] or '—'} | {o['kind']} |")
    else:
        L.append(f"## Channels ({len(inv['channels'])})\n")
        L.append("| # | Key | Address | Messages |")
        L.append("|---|---|---|---|")
        for i, c in enumerate(inv["channels"], 1):
            L.append(f"| {i} | `{c['key']}` | `{c['address']}` | {', '.join(c['messages'])} |")
        L.append(f"\n## Operations ({len(inv['operations'])})\n")
        L.append("| # | Key | Action | Channel | Messages | Summary | kind |")
        L.append("|---|---|---|---|---|---|---|")
        for i, o in enumerate(inv["operations"], 1):
            L.append(f"| {i} | `{o['id']}` | **{o['action']}** | `{o['channel']}` | "
                     f"{', '.join(o['messages'])} | {o['summary']} | {o['kind'] or '—'} |")
        L.append(f"\n## Messages ({len(inv['messages'])})\n")
        L.append("| # | Key | Name | Title | Payload schema | Summary |")
        L.append("|---|---|---|---|---|---|")
        for i, m in enumerate(inv["messages"], 1):
            L.append(f"| {i} | `{m['key']}` | {m['name']} | {m['title']} | {m['payload'] or '—'} | {m['summary']} |")
    L.append(f"\n## Schemas ({len(inv['schemas'])})\n")
    L.append("| # | Key | Role (implied) | Stem | Description |")
    L.append("|---|---|---|---|---|")
    for i, s in enumerate(inv["schemas"], 1):
        L.append(f"| {i} | `{s['key']}` | {s['role']} | {s['stem']} | {s['description']} |")
    L.append("")
    L.append("`kind` is read off the method / tense and is dkg:Implied; everything else is verbatim.")
    return "\n".join(L)


# --------------------------------------------------------------------------
# graph matching (optional)
# --------------------------------------------------------------------------

class Model:
    """What the existing graph holds, indexed by normalised label."""

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
        self.by_class: dict[str, list] = {}
        for fam in ("Concept", "Activity", "Occurrence", "Message", "BoundedContext",
                    "Actor", "Api"):
            self.by_class[fam] = sorted(self.typed(self.DKG[fam]), key=str)

    def typed(self, cls):
        out = set(self.g.subjects(self.RDF.type, cls))
        for sub in self.g.subjects(self.RDFS.subClassOf, cls):
            if sub != cls:
                out |= self.typed(sub)
        return out

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
        s = str(n)
        return ":" + s.split("#")[-1] if "#" in s else f"<{s}>"

    def context_of(self, n):
        return list(self.g.objects(n, self.DKG.inContext))

    def match(self, label: str, families: list[str]):
        """(exact, near) lists of (node, family, their_label, ratio)."""
        key = normalise(decamel(label))
        exact, near = [], []
        for fam in families:
            for n in self.by_class.get(fam, []):
                for their in self.labels(n):
                    tk = normalise(their)
                    if not tk:
                        continue
                    if tk == key:
                        exact.append((n, fam, their, 1.0))
                        break
                    r = difflib.SequenceMatcher(None, key, tk).ratio()
                    contained = (len(key) >= 5 and (key in tk or tk in key))
                    if r >= 0.8 or contained:
                        near.append((n, fam, their, r))
                        break
        return exact, near

    def placement(self, n, fam: str, ctx: str | None) -> str:
        """Where the matched node sits relative to the ingesting context, as a note."""
        suffix = lambda c: str(c).split("#")[-1]  # noqa: E731
        if fam == "Message":
            frm, to = self.g.value(n, self.DKG["from"]), self.g.value(n, self.DKG.to)
            if ctx and to is not None and suffix(to) == ctx:
                return f" (canvas: inbound {self.label(frm) if frm else '?'} → this context)"
            if ctx and frm is not None and suffix(frm) == ctx:
                return f" (canvas: outbound this context → {self.label(to) if to else '?'})"
            return (f" (canvas: {self.label(frm) if frm else '?'} → {self.label(to) if to else '?'}"
                    " — NEITHER END IS THIS CONTEXT: check)")
        ctxs = self.context_of(n)
        if not ctxs:
            return " (no context drawn)"
        names = ", ".join(self.label(c) for c in ctxs)
        if ctx and all(suffix(c) != ctx for c in ctxs):
            return f" (in {names} — ANOTHER CONTEXT: a receive across a border, or this API exposing another context's model)"
        return f" (in {names})"

    def api_for(self, ctx_suffix: str, style: str):
        """An Api is identified by context × interaction style (a canvas may have minted it)."""
        for a in self.by_class["Api"]:
            if str(self.g.value(a, self.DKG.interactionStyle) or "") != style:
                continue
            if any(str(c).split("#")[-1] == ctx_suffix for c in self.context_of(a)):
                return a
        return None

    def exposed_by(self, api, cls) -> dict:
        """normalised label → node, for nodes of cls that an existing Api exposes."""
        out = {}
        for n in self.g.subjects(self.DKG.exposedBy, api):
            if (n, self.RDF.type, self.DKG[cls]) in self.g:
                for lb in self.labels(n):
                    out.setdefault(normalise(decamel(lb)), n)
        return out

    def by_suffix(self, suffix: str):
        for s in self.g.subjects():
            if str(s).endswith("#" + suffix):
                return s
        return None

    def taken(self, suffix: str) -> bool:
        return self.by_suffix(suffix) is not None

    def api_with_id(self, api_id: str):
        for a in self.by_class["Api"]:
            if str(self.g.value(a, self.DKG.apiId) or "") == api_id:
                return a
        return None


# --------------------------------------------------------------------------
# turtle
# --------------------------------------------------------------------------

def lit(s) -> str:
    s = str(s).replace("\\", "\\\\").replace('"', '\\"').replace("\n", " ")
    return f'"{s}"'


def emit_ttl(inv: dict, art: str, ctx: str, date: str, base: str | None,
             model: Model | None, ctx_label: str | None) -> tuple[str, list[str]]:
    L: list[str] = []
    ledger: list[str] = []
    cls = "dkg:SyncApiSpec" if inv["style"] == "sync" else "dkg:AsyncApiSpec"
    revision_of = None
    if model is not None and inv["apiId"]:
        revision_of = model.api_with_id(inv["apiId"])
    if revision_of is not None:
        ledger.append(f"REVISION  x-api-id {inv['apiId']} already on {model.curie(revision_of)} "
                      f"({model.label(revision_of)}) — this is a REVISION, not a grow. "
                      "Do not append this skeleton; read reingest-revision.md and diff against the existing nodes.")
        L.append(f"# !!! REVISION of {model.curie(revision_of)} (same x-api-id). This skeleton is for DIFFING,")
        L.append("# !!! not appending: unchanged nodes get the new artifact as a second dkg:source; changed")
        L.append("# !!! paths/methods become dkg:supersedes pairs; dropped ones get dkg:absentFrom.")
    existing_api = model.api_for(ctx, inv["style"]) if model is not None else None
    api = "Api_" + camel(inv["title"])
    if existing_api is not None:
        api = str(existing_api).split("#")[-1]
        srcs = ", ".join(model.label(a) for a in model.g.objects(existing_api, model.DKG.source))
        ledger.append(f"ENRICH   :{api} already is the {inv['style']} API of :{ctx} (from {srcs}) — "
                      "enriched, not re-minted; operations / messages / schemas merge by exact label")
    elif model is not None and model.taken(api):
        api = f"{api}_{date.replace('-', '_')}"
        if revision_of is None:
            ledger.append(f"IRI      an Api with this title already exists — minted :{api}; "
                          "if it is the same API at a new version, this is a REVISION")
    if base:
        L.append(f"@prefix : <{base}> .")
    L.append(f"\n########## {inv['format']} — {inv['title']} — ingested {date} ##########")
    L.append(f"# {inv['file']}: {len(inv['operations'])} operations, {len(inv['channels'])} channels, "
             f"{len(inv['messages'])} messages, {len(inv['schemas'])} schemas. Drafted by api_inventory.py;")
    L.append("# every literal is verbatim. PROPOSE lines are suggestions — decide them in the ledger.\n")

    # artifact
    L.append(f":{art} a {cls} ;")
    L.append(f"    rdfs:label {lit(inv['title'] + ' — ' + inv['format'])} ;")
    L.append(f"    rdfs:comment {lit(inv['file'])} ;")
    L.append(f"    dkg:specFormat {lit(inv['format'])} ;")
    L.append(f'    dkg:ingestedAt "{date}"^^xsd:date .\n')

    # context confidence
    ctx_conf, ctx_note = "dkg:Inferred", "context named by the ingester; the spec does not say"
    if model is not None:
        from rdflib import URIRef  # noqa
        ctx_node = None
        for c in model.by_class["BoundedContext"]:
            if str(c).split("#")[-1] == ctx:
                ctx_node = c
                break
        if ctx_node is None:
            ledger.append(f"CONTEXT  :{ctx} is not a dkg:BoundedContext in the graph — a canvas-less, "
                          "board-less context exists only because a spec needed one; mint it dkg:Inferred and report it first")
        else:
            ctx_label = ctx_label or model.label(ctx_node)
    if ctx_label:
        hay = (inv["title"] + " " + inv["description"]).lower()
        if ctx_label.lower() in hay:
            ctx_conf, ctx_note = "dkg:Implied", f"info names the context: {ctx_label!r}"

    # api
    if existing_api is not None:
        L.append(f":{api}   # existing {inv['style']} API of :{ctx} — enriched by this spec")
        L.append(f"    skos:altLabel {lit(inv['title'])} ;")
    else:
        L.append(f":{api} a dkg:Api ;")
        L.append(f"    skos:prefLabel {lit(inv['title'])} ;")
    if inv["description"]:
        L.append(f"    rdfs:comment {lit(inv['description'].splitlines()[0])} ;")
    L.append(f"    dkg:interactionStyle {lit(inv['style'])} ;")
    if inv["version"]:
        L.append(f"    dkg:apiVersion {lit(inv['version'])} ;")
    if inv["apiId"]:
        L.append(f"    dkg:apiId {lit(inv['apiId'])} ;")
    if inv["audience"]:
        L.append(f"    dkg:audience {lit(inv['audience'])} ;")
    for p in inv["protocols"]:
        L.append(f"    dkg:protocol {lit(p)} ;")
    if existing_api is None:
        L.append(f"    dkg:inContext :{ctx} ;   # {ctx_note}")
        L.append(f"    dkg:source :{art} ; dkg:locator \"info\" ; dkg:confidence dkg:OnArtifact .")
    else:
        L.append(f"    dkg:source :{art} ; dkg:locator \"info\" .")
    if ctx_conf != "dkg:OnArtifact" and existing_api is None:
        L.append(f"# NOTE the Api node itself is on-artifact; its dkg:inContext is {ctx_conf} — {ctx_note}.")
        L.append(f"# If the room disputes the context, reify that one edge as a dkg:Assertion; do not weaken the node.")
    L.append("")

    # IRIs. Two specs legitimately name the same thing — Billing's AsyncAPI
    # RECEIVES the BicycleReturned that Rack management's SENDS — and those are
    # two nodes from two artifacts, so a taken IRI gets the context as suffix
    # (ontology.md §2, collisions) rather than silently merging into the other.
    def fresh(iri: str) -> str:
        if model is not None and model.taken(iri):
            alt = f"{iri}_{ctx.replace('Ctx_', '')}"
            if revision_of is None:
                ledger.append(f"IRI      :{iri} already exists in the graph (another spec's node) — "
                              f"minted :{alt}; link the two with dkg:specifies / dkg:proposedSameAs only if they are the same contract")
            return alt
        return iri

    L_pending: list[str] = []   # PROPOSE lines against canvas stickies, appended at the end
    have = {cls: (model.exposed_by(existing_api, cls) if existing_api is not None else {})
            for cls in ("Schema", "Operation", "ApiMessage", "Channel")}
    merged: set[str] = set()   # IRI suffixes that already exist: emit enrichment, not a new node

    def near_existing(cls: str, label: str, suffix: str):
        """A canvas sticky this spec element nearly matches: propose, never merge."""
        key = normalise(decamel(label))
        for k, n in have[cls].items():
            r = difflib.SequenceMatcher(None, key, k).ratio()
            if r >= 0.8 or (len(key) >= 5 and (key in k or k in key)):
                ledger.append(f"PROPOSE  {cls} {label!r} ~ existing :{str(n).split('#')[-1]} "
                              f"({model.label(n)!r}, ratio {r:.2f}) — a canvas sticky and a spec element for one "
                              "thing? if confirmed, collapse onto the spec's IRI and keep the sticky's wording as altLabel")
                L_pending.append(f"# PROPOSE :{suffix} dkg:proposedSameAs {model.curie(n)} .   # {cls} {model.label(n)!r}, ratio {r:.2f}")

    def reuse(cls: str, label: str, fallback: str) -> str:
        n = have[cls].get(normalise(decamel(label)))
        if n is not None:
            suf = str(n).split("#")[-1]
            merged.add(suf)
            ledger.append(f"MERGE    {cls} {label!r} = existing :{suf} ({model.label(n)!r}) — exact label, same class; "
                          "second source added")
            return suf
        iri = fresh(fallback)
        near_existing(cls, label, iri)
        return iri

    schema_iri = {s["key"]: reuse("Schema", s["key"], "Sch_" + camel(s["key"])) for s in inv["schemas"]}
    msg_iri = {m["name"]: reuse("ApiMessage", m["name"], "Amsg_" + camel(m["name"])) for m in inv["messages"]}
    for m in inv["messages"]:
        msg_iri.setdefault(m["key"], msg_iri[m["name"]])
    chan_iri = {c["key"]: reuse("Channel", c["key"], "Ch_" + camel(c["key"])) for c in inv["channels"]}
    op_iri = {}
    for o in inv["operations"]:
        # a canvas sticky is matched on the summary first, then the key
        n = have["Operation"].get(normalise(decamel(op_label(o)))) or \
            have["Operation"].get(normalise(decamel(o["id"])))
        if n is not None:
            suf = str(n).split("#")[-1]
            merged.add(suf)
            ledger.append(f"MERGE    operation {op_label(o)!r} = existing :{suf} ({model.label(n)!r}) — "
                          "exact label, same class; the spec adds method/path and a second source")
            op_iri[o["id"]] = suf
        else:
            op_iri[o["id"]] = fresh("Op_" + camel(o["id"]))
            near_existing("Operation", op_label(o), op_iri[o["id"]])

    def tail(suffix: str, locator: str) -> str:
        if suffix in merged:
            return f"    dkg:source :{art} ; dkg:locator {lit(locator)} ."
        return f"    dkg:exposedBy :{api} ; dkg:source :{art} ; dkg:locator {lit(locator)} ; dkg:confidence dkg:OnArtifact ."

    def head(suffix: str, cls: str, label: str) -> str:
        """Opening line of a node block: a new node, or an enrichment of an existing one."""
        if suffix in merged:
            alt = f" skos:altLabel {lit(label)} ;" if normalise(label) != normalise(model.label(model.by_suffix(suffix))) else ""
            return f":{suffix}{alt}   # existing {cls}, enriched"
        return f":{suffix} a dkg:{cls} ; skos:prefLabel {lit(label)} ;"

    def link(subj_iri: str, label: str, families: list[str], pred: str, what: str,
             require_ctx: str | None = None):
        """Append direct edges for exact matches; PROPOSE lines for near ones."""
        if model is None:
            return []
        exact, near = model.match(label, families)
        lines = []
        for n, fam, their, _ in exact:
            where = model.placement(n, fam, require_ctx)
            lines.append(f":{subj_iri} {pred} {model.curie(n)} .   # exact: {what} {label!r} = {fam} {their!r}{where}")
            ledger.append(f"LINK     {what} {label!r} {pred.split(':')[-1]} {model.curie(n)} ({fam} {their!r}{where}) — exact after normalisation, dkg:Implied")
        for n, fam, their, r in near:
            lines.append(f"# PROPOSE :{subj_iri} {pred} {model.curie(n)} .   # {fam} {their!r}, ratio {r:.2f} — ledger")
            ledger.append(f"PROPOSE  {what} {label!r} ~ {fam} {their!r} ({model.curie(n)}, ratio {r:.2f}) — "
                          f"if confirmed: a dkg:Assertion with rdf:predicate {pred}, dkg:Inferred")
        if not exact and not near:
            ledger.append(f"NONE     {what} {label!r} matches nothing in {'/'.join(families)} — the model never named it")
        return lines

    # schemas first: operations and messages point at them
    schema_targets: dict[str, list[str]] = {}
    L.append("# --- schemas (API nouns) ------------------------------------------------")
    for s in inv["schemas"]:
        iri = schema_iri[s["key"]]
        L.append(head(iri, "Schema", s["key"]))
        L.append(f"    dkg:schemaRole {lit(s['role'])} ;")
        if s["description"]:
            L.append(f"    rdfs:comment {lit(s['description'])} ;")
        L.append(tail(iri, 'components.schemas.' + s['key']))
        if s["role"] not in ("header", "error", "payload"):
            # a payload schema's stem is an event name; the ApiMessage links to the event
            got = link(iri, s["stem"], ["Concept"], "dkg:specifies", "schema")
            L += got
            schema_targets[s["key"]] = [ln.split()[2] for ln in got if not ln.startswith("#")]
    L.append("")

    if inv["kind"] == "openapi":
        L.append("# --- operations (synchronous) -------------------------------------------")
        for o in inv["operations"]:
            iri = op_iri[o["id"]]
            label = op_label(o)
            L.append(head(iri, "Operation", label))
            if o["summary"] and normalise(o["summary"]) != normalise(decamel(o["id"])):
                L.append(f"    skos:altLabel {lit(o['id'])} ;")
            L.append(f"    dkg:operationId {lit(o['id'])} ; dkg:httpMethod {lit(o['method'])} ; dkg:path {lit(o['path'])} ;")
            L.append(f"    dkg:messageKind {lit(o['kind'])} ;   # implied from the method")
            for sc in o["scopes"]:
                L.append(f"    dkg:requiresScope {lit(sc)} ;")
            if o["request"] and o["request"] in schema_iri:
                L.append(f"    dkg:requestSchema :{schema_iri[o['request']]} ;")
            if o["response"] and o["response"] in schema_iri:
                L.append(f"    dkg:responseSchema :{schema_iri[o['response']]} ;")
            L.append(tail(iri, o['method'] + ' ' + o['path']))
            # Occurrence is deliberately in the list: a sync operation that matches an
            # EVENT is the style mismatch check_graph.py exists to catch.
            fams = ["Activity", "Message", "Occurrence"]
            got = link(iri, label, fams, "dkg:specifies", "operation", require_ctx=ctx)
            if not got and o["summary"] and normalise(o["summary"]) != normalise(decamel(o["id"])):
                got = link(iri, decamel(o["id"]), fams, "dkg:specifies", "operationId", require_ctx=ctx)
            L += got
            # mentions: closed over the schemas it carries (mechanical), then the path noun
            mentioned = set()
            for key in (o["request"], o["response"]):
                for tgt in schema_targets.get(key or "", []):
                    if tgt not in mentioned:
                        mentioned.add(tgt)
                        L.append(f":{iri} dkg:mentions {tgt} .   # via schema {key}")
            seen = set()
            for noun in [o["resource"]] + o["tags"]:
                if not noun or normalise(noun) in seen:
                    continue
                seen.add(normalise(noun))
                for ln in link(iri, decamel(noun), ["Concept"], "dkg:mentions", "resource noun"):
                    if not any(t in ln for t in mentioned):
                        L.append(ln)
            L.append("")
    else:
        L.append("# --- channels -------------------------------------------------------------")
        for c in inv["channels"]:
            iri = chan_iri[c["key"]]
            L.append(head(iri, "Channel", c["key"]))
            L.append(f"    dkg:address {lit(c['address'])} ;")
            for m in c["messages"]:
                if m in msg_iri:
                    L.append(f"    dkg:carriesMessage :{msg_iri[m]} ;")
            L.append(tail(iri, 'channels.' + c['key']))
        L.append("")
        L.append("# --- messages (contracts for facts) ---------------------------------------")
        for m in inv["messages"]:
            iri = msg_iri[m["name"]]
            L.append(head(iri, "ApiMessage", m["name"]))
            if m["title"]:
                L.append(f"    skos:altLabel {lit(m['title'])} ;")
            if m["summary"]:
                L.append(f"    rdfs:comment {lit(m['summary'])} ;")
            if m["payload"] and m["payload"] in schema_iri:
                L.append(f"    dkg:payloadSchema :{schema_iri[m['payload']]} ;")
            L.append(tail(iri, 'components.messages.' + m['key']))
            L += link(iri, m["name"], ["Occurrence", "Message"], "dkg:specifies", "message", require_ctx=ctx)
            if not is_past_tense(m["name"]):
                ledger.append(f"TENSE    message {m['name']!r} is not past tense — a state or a command on an event "
                              "channel? dkg:messageKind left off; ask")
        L.append("")
        L.append("# --- operations (asynchronous: send / receive from this context) ----------")
        for o in inv["operations"]:
            iri = op_iri[o["id"]]
            L.append(head(iri, "Operation", op_label(o)))
            L.append(f"    dkg:operationId {lit(o['id'])} ; dkg:action {lit(o['action'] or '?')} ;")
            if o["kind"]:
                L.append(f"    dkg:messageKind {lit(o['kind'])} ;   # implied: past-tense message names")
            if o["channel"] in chan_iri:
                L.append(f"    dkg:onChannel :{chan_iri[o['channel']]} ;")
            for m in o["messages"]:
                if m in msg_iri:
                    L.append(f"    dkg:carriesMessage :{msg_iri[m]} ;")
            L.append(tail(iri, 'operations.' + o['id']))
            if o["action"] == "receive":
                L.append(f"# NOTE :{iri} RECEIVES — this context consumes {', '.join(o['messages'])}; "
                         "whoever produces it is on the other side of a border.")
        L.append("")
    if L_pending:
        L.append("# --- near-matches against the canvas's stickies (decide in the ledger) ----")
        L += L_pending
        L.append("")
    return "\n".join(L), ledger


# --------------------------------------------------------------------------

def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("spec")
    ap.add_argument("--labels", metavar="FILE", help="write one label per line for check_graph.py --overlap")
    ap.add_argument("--ttl", action="store_true", help="emit a Turtle skeleton on stdout")
    ap.add_argument("--artifact", help="IRI suffix for the dkg:Artifact, e.g. Art_OpenAPI_RackManagement_2026_10")
    ap.add_argument("--context", help="IRI suffix of the existing dkg:BoundedContext, e.g. Ctx_RackManagement")
    ap.add_argument("--context-label", help="the context's label, to test whether the spec's info names it")
    ap.add_argument("--date", help="ingest date YYYY-MM-DD")
    ap.add_argument("--base", help="project base IRI, emitted as the ':' prefix (omit when appending to graph.ttl)")
    ap.add_argument("--graph", help="existing graph.ttl — draft dkg:specifies / dkg:mentions links against it")
    args = ap.parse_args()

    spec = load_spec(args.spec)
    inv = inventory(spec, args.spec)

    if args.labels:
        with open(args.labels, "w", encoding="utf-8") as fh:
            fh.write("\n".join(labels_of(inv)) + "\n")
        print(f"wrote {len(labels_of(inv))} labels to {args.labels}", file=sys.stderr)

    if args.ttl:
        missing = [k for k in ("artifact", "context", "date") if not getattr(args, k)]
        if missing:
            sys.exit("--ttl needs " + ", ".join("--" + m for m in missing))
        model = Model(args.graph) if args.graph else None
        ttl, ledger = emit_ttl(inv, args.artifact, args.context, args.date, args.base, model, args.context_label)
        print(ttl)
        if ledger:
            print("\n# ===== ledger draft (stderr) =====", file=sys.stderr)
            for row in ledger:
                print("  " + row, file=sys.stderr)
            n_prop = sum(1 for r in ledger if r.startswith("PROPOSE"))
            n_none = sum(1 for r in ledger if r.startswith("NONE"))
            print(f"\n  {n_prop} proposals to decide, {n_none} API elements the model never named. "
                  "Both go in the report; neither is fixed by inventing a node.", file=sys.stderr)
        elif model is None:
            print("# no --graph given: no links drafted. Add them by hand per ingest-api-specification.md §Map.",
                  file=sys.stderr)
        return 0

    if not args.labels:
        print(md(inv))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
