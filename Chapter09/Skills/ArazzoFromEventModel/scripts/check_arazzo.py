#!/usr/bin/env python3
"""Cross-reference checker for Arazzo documents produced from an Event Model.

Checks the things `redocly lint` does not: that every operationId resolves in a
supplied source description, that runtime expressions point at things which
exist and are defined *earlier*, that dependsOn targets exist, that async steps
are well formed, and that every workflow and step carries x-event-model
traceability.

Usage:
    python3 check_arazzo.py <file>.arazzo.yaml [--strict] [--no-traceability]

Exit code 0 when there are no errors (and, with --strict, no warnings).
Requires PyYAML:  pip install pyyaml --break-system-packages
"""

from __future__ import annotations

import argparse
import os
import re
import sys

try:
    import yaml
except ImportError:  # pragma: no cover
    sys.exit("PyYAML is required: pip install pyyaml --break-system-packages")

ID_RE = re.compile(r"^[A-Za-z0-9_-]+$")
STEP_EXPR_RE = re.compile(r"\$steps\.([A-Za-z0-9_-]+)\.outputs\.([A-Za-z0-9._-]+)")
INPUT_EXPR_RE = re.compile(r"\$inputs\.([A-Za-z0-9._-]+)")
WORKFLOW_EXPR_RE = re.compile(
    r"\$workflows\.([A-Za-z0-9_-]+)\.(inputs|outputs)\.([A-Za-z0-9._-]+)")
SOURCE_OP_RE = re.compile(r"\$sourceDescriptions\.([A-Za-z0-9_-]+)\.([^\s'\"}#]+)")
COMPONENT_RE = re.compile(
    r"\$components\.(parameters|successActions|failureActions|inputs)\.([A-Za-z0-9._-]+)")

VALID_STATUS = {"on-board", "inferred", "proposed-operation"}
VALID_SLICE_TYPES = {"state-change", "state-view", "automation", "translation"}

errors: list[str] = []
warnings: list[str] = []


def err(where: str, msg: str) -> None:
    errors.append(f"ERROR  {where}: {msg}")


def warn(where: str, msg: str) -> None:
    warnings.append(f"WARN   {where}: {msg}")


def walk_strings(node):
    """Yield every string anywhere in a nested structure."""
    if isinstance(node, str):
        yield node
    elif isinstance(node, dict):
        for value in node.values():
            yield from walk_strings(value)
    elif isinstance(node, list):
        for item in node:
            yield from walk_strings(item)


def load_source_operations(base_dir: str, source: dict) -> set[str] | None:
    """Operation ids exposed by a source description, or None if unreadable."""
    url = source.get("url", "")
    if url.startswith(("http://", "https://")):
        return None
    path = os.path.normpath(os.path.join(base_dir, url))
    if not os.path.isfile(path):
        return None
    try:
        with open(path, encoding="utf-8") as handle:
            doc = yaml.safe_load(handle)
    except Exception:
        return None
    if not isinstance(doc, dict):
        return None

    ids: set[str] = set()
    # OpenAPI: operationIds under paths
    for path_item in (doc.get("paths") or {}).values():
        if isinstance(path_item, dict):
            for op in path_item.values():
                if isinstance(op, dict) and "operationId" in op:
                    ids.add(str(op["operationId"]))
    # AsyncAPI 3: operation keys (and any explicit operationId)
    operations = doc.get("operations")
    if isinstance(operations, dict):
        for key, op in operations.items():
            ids.add(str(key))
            if isinstance(op, dict) and "operationId" in op:
                ids.add(str(op["operationId"]))
    # Arazzo: workflowIds
    for wf in doc.get("workflows") or []:
        if isinstance(wf, dict) and "workflowId" in wf:
            ids.add(str(wf["workflowId"]))
    return ids


def check_step(step, wf_id, index, seen_steps, wf_inputs, step_ids,
               workflow_ids, sources, source_ops, components, traceability):
    step_id = step.get("stepId")
    where = f"{wf_id}/{step_id or f'step[{index}]'}"

    if not step_id:
        err(where, "stepId is required")
    elif not ID_RE.match(step_id):
        err(where, f"stepId '{step_id}' must match [A-Za-z0-9_-]+ (no dots or spaces)")

    targets = [k for k in ("operationId", "operationPath", "workflowId", "channelPath")
               if k in step]
    if not targets:
        err(where, "step has no operationId, operationPath, channelPath or workflowId")
    elif len(targets) > 1:
        err(where, f"operationId/operationPath/channelPath/workflowId are mutually "
                   f"exclusive; found {', '.join(targets)}")

    status = (step.get("x-event-model") or {}).get("status")

    # --- operation resolution -------------------------------------------------
    if "operationId" in step:
        raw = str(step["operationId"])
        match = SOURCE_OP_RE.fullmatch(raw.strip())
        if match:
            source_name, op_id = match.groups()
            if source_name not in sources:
                err(where, f"operationId references unknown sourceDescription "
                           f"'{source_name}'")
            else:
                known = source_ops.get(source_name)
                if known is not None and op_id not in known:
                    if status == "proposed-operation":
                        warn(where, f"operation '{op_id}' is not in "
                                    f"{sources[source_name].get('url')} "
                                    f"(declared as proposed-operation)")
                    else:
                        err(where, f"operation '{op_id}' not found in "
                                   f"{sources[source_name].get('url')}; either fix it "
                                   f"or mark the step x-event-model.status: "
                                   f"proposed-operation")
        else:
            if len([s for s in sources.values() if s.get("type") != "arazzo"]) > 1:
                err(where, f"operationId '{raw}' must be written "
                           f"$sourceDescriptions.<name>.<operationId> when several "
                           f"non-arazzo source descriptions exist")
            else:
                warn(where, "prefer $sourceDescriptions.<name>.<operationId> form")

    if "workflowId" in step:
        target = str(step["workflowId"])
        if not target.startswith("$") and target not in workflow_ids:
            err(where, f"workflowId '{target}' is not defined in this document")
        for param in step.get("parameters") or []:
            if isinstance(param, dict) and "in" in param:
                err(where, f"parameter '{param.get('name')}' must not set 'in' on a "
                           f"step that targets a workflowId")

    # --- async ----------------------------------------------------------------
    is_async = False
    for key in ("operationId", "channelPath", "operationPath"):
        match = SOURCE_OP_RE.search(str(step.get(key, "")))
        if match and sources.get(match.group(1), {}).get("type") == "asyncapi":
            is_async = True
    if is_async and "action" not in step:
        err(where, "an asyncapi step needs action: send or receive")
    if "action" in step:
        if step["action"] not in ("send", "receive"):
            err(where, f"action must be 'send' or 'receive', not '{step['action']}'")
        if not is_async:
            warn(where, "action is only meaningful on asyncapi steps")
        if step["action"] == "receive" and "timeout" not in step:
            warn(where, "a receive step without a timeout waits forever")
    if "correlationId" in step and step.get("action") != "receive":
        warn(where, "correlationId only applies to receive steps")

    # --- criteria and control flow -------------------------------------------
    if "successCriteria" in step and not step["successCriteria"]:
        err(where, "successCriteria, when present, needs at least one criterion")
    for crit in step.get("successCriteria") or []:
        if isinstance(crit, dict):
            if "condition" not in crit:
                err(where, "criterion is missing 'condition'")
            if crit.get("type") in ("regex", "jsonpath", "xpath") and "context" not in crit:
                err(where, f"a {crit['type']} criterion needs a 'context'")
    for kind in ("onSuccess", "onFailure"):
        for action in step.get(kind) or []:
            if not isinstance(action, dict) or "reference" in action:
                continue
            if "name" not in action or "type" not in action:
                err(where, f"{kind} action needs both name and type")
            if action.get("type") == "goto" and not (action.get("stepId")
                                                     or action.get("workflowId")):
                err(where, f"{kind} goto action needs a stepId or workflowId")
            if action.get("stepId") and action["stepId"] not in step_ids:
                err(where, f"{kind} action '{action.get('name')}' points at unknown "
                           f"step '{action['stepId']}'")
            if action.get("workflowId") and not str(action["workflowId"]).startswith("$") \
                    and action["workflowId"] not in workflow_ids:
                err(where, f"{kind} action '{action.get('name')}' points at unknown "
                           f"workflow '{action['workflowId']}'")

    for dep in step.get("dependsOn") or []:
        if isinstance(dep, str) and not dep.startswith("$") and dep not in step_ids:
            err(where, f"dependsOn references unknown step '{dep}'")

    # --- runtime expressions --------------------------------------------------
    for text in walk_strings({k: v for k, v in step.items() if k != "x-event-model"}):
        for ref_step, output in STEP_EXPR_RE.findall(text):
            if ref_step not in step_ids:
                err(where, f"expression references unknown step '{ref_step}'")
            elif ref_step not in seen_steps and ref_step != step_id:
                err(where, f"expression references step '{ref_step}', which comes later "
                           f"in the workflow (forward reference)")
            elif ref_step in seen_steps and output not in seen_steps[ref_step]:
                err(where, f"step '{ref_step}' declares no output '{output}'")
        for name in INPUT_EXPR_RE.findall(text):
            root = name.split(".")[0].split("#")[0]
            if wf_inputs is not None and root not in wf_inputs:
                err(where, f"$inputs.{root} is not declared in this workflow's inputs")
        for comp_type, key in COMPONENT_RE.findall(text):
            if key not in (components.get(comp_type) or {}):
                err(where, f"$components.{comp_type}.{key} is not defined")

    # --- traceability ---------------------------------------------------------
    if traceability:
        trace = step.get("x-event-model")
        if not isinstance(trace, dict):
            warn(where, "no x-event-model block — a step nobody can trace to the board")
        else:
            if trace.get("status") not in VALID_STATUS:
                warn(where, f"x-event-model.status should be one of "
                            f"{sorted(VALID_STATUS)}")
            if "sliceType" in trace and trace["sliceType"] not in VALID_SLICE_TYPES:
                warn(where, f"x-event-model.sliceType should be one of "
                            f"{sorted(VALID_SLICE_TYPES)}")
            if not trace.get("slice"):
                warn(where, "x-event-model has no 'slice' — say which slice this is")

    return {name for name in (step.get("outputs") or {})}


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("path")
    parser.add_argument("--strict", action="store_true",
                        help="treat warnings as errors")
    parser.add_argument("--no-traceability", action="store_true",
                        help="skip the x-event-model checks")
    args = parser.parse_args()

    with open(args.path, encoding="utf-8") as handle:
        doc = yaml.safe_load(handle)
    base_dir = os.path.dirname(os.path.abspath(args.path))

    if not isinstance(doc, dict):
        print("ERROR  document: not a YAML mapping")
        return 1

    if "arazzo" not in doc:
        err("document", "the arazzo version field is required")
    info = doc.get("info") or {}
    for field in ("title", "version"):
        if field not in info:
            err("info", f"{field} is required")

    source_list = doc.get("sourceDescriptions") or []
    if not source_list:
        err("document", "sourceDescriptions must have at least one entry")
    sources: dict[str, dict] = {}
    source_ops: dict[str, set[str] | None] = {}
    for source in source_list:
        name = source.get("name")
        if not name or not ID_RE.match(str(name)):
            err("sourceDescriptions", f"name '{name}' must match [A-Za-z0-9_-]+")
            continue
        if "url" not in source:
            err(f"sourceDescriptions/{name}", "url is required")
        sources[name] = source
        ops = load_source_operations(base_dir, source)
        source_ops[name] = ops
        if ops is None:
            warn(f"sourceDescriptions/{name}",
                 f"could not read {source.get('url')} — operation ids unverified")
        if source.get("type") == "asyncapi" and str(doc.get("arazzo", "")) < "1.1":
            err(f"sourceDescriptions/{name}",
                "asyncapi source descriptions need arazzo 1.1.0 or later")

    workflows = doc.get("workflows") or []
    if not workflows:
        err("document", "workflows must have at least one entry")
    workflow_ids = {wf.get("workflowId") for wf in workflows if isinstance(wf, dict)}
    components = doc.get("components") or {}

    called: set[str] = set()
    for wf in workflows:
        wf_id = wf.get("workflowId") or "<unnamed>"
        if not ID_RE.match(str(wf_id)):
            err(wf_id, "workflowId must match [A-Za-z0-9_-]+ (no dots or spaces)")
        inputs_schema = wf.get("inputs")
        wf_inputs = None
        if isinstance(inputs_schema, dict):
            if inputs_schema.get("type") != "object" and "properties" not in inputs_schema:
                warn(wf_id, "inputs should be a JSON Schema object with properties")
            wf_inputs = set((inputs_schema.get("properties") or {}).keys())
        steps = wf.get("steps") or []
        if not steps:
            err(wf_id, "a workflow needs at least one step")
        step_ids = [s.get("stepId") for s in steps if isinstance(s, dict)]
        if len(step_ids) != len(set(step_ids)):
            err(wf_id, "duplicate stepId in this workflow")
        if len(steps) > 12:
            warn(wf_id, f"{len(steps)} steps — probably two outcomes, not one")

        seen_steps: dict[str, set[str]] = {}
        for index, step in enumerate(steps):
            if not isinstance(step, dict):
                err(wf_id, f"step[{index}] is not a mapping")
                continue
            if "workflowId" in step:
                called.add(str(step["workflowId"]))
            outputs = check_step(step, wf_id, index, seen_steps, wf_inputs,
                                 set(step_ids), workflow_ids, sources, source_ops,
                                 components, not args.no_traceability)
            if step.get("stepId"):
                seen_steps[step["stepId"]] = outputs

        for name, expr in (wf.get("outputs") or {}).items():
            for ref_step, output in STEP_EXPR_RE.findall(str(expr)):
                if ref_step not in seen_steps:
                    err(f"{wf_id}/outputs/{name}", f"unknown step '{ref_step}'")
                elif output not in seen_steps[ref_step]:
                    err(f"{wf_id}/outputs/{name}",
                        f"step '{ref_step}' declares no output '{output}'")
        for dep in wf.get("dependsOn") or []:
            if not str(dep).startswith("$") and dep not in workflow_ids:
                err(wf_id, f"dependsOn references unknown workflow '{dep}'")
        if not args.no_traceability and not isinstance(wf.get("x-event-model"), dict):
            warn(wf_id, "no x-event-model block on the workflow")

    for text in walk_strings(doc.get("workflows")):
        for wf_ref, _field, _name in WORKFLOW_EXPR_RE.findall(text):
            if wf_ref not in workflow_ids:
                err("document", f"expression references unknown workflow '{wf_ref}'")

    for line in errors + warnings:
        print(line)
    if not errors and not warnings:
        print(f"OK  {args.path}: no cross-reference or traceability problems.")
    else:
        print(f"\n{len(errors)} error(s), {len(warnings)} warning(s).")
    return 1 if errors or (args.strict and warnings) else 0


if __name__ == "__main__":
    sys.exit(main())