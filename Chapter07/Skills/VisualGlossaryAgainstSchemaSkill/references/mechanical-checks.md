# Mechanical checks — parse, don't read

Read this when running Step 2, or when the script isn't available and you need to
do the pass by hand.

Every defect below is one that a careful human reading of the file *does not*
catch, because the file looks right. They are caught by parsing it and asking the
parsed structure questions. They matter to a conformance check for two reasons:

1. **They invalidate other findings.** A `$ref` missing a slash means the subtype
   inherits nothing — so the fields you were about to check against the glossary
   aren't actually there.
2. **They buy credibility.** They are objective, reproducible in seconds, and
   they demonstrate that the report came from the file rather than from an
   impression of it.

## Running it

```bash
python scripts/inspect_schema.py <file.yaml> --inventory
python scripts/inspect_schema.py <file.yaml> --json   # for programmatic use
```

Handles OpenAPI (`components.schemas`), JSON Schema (`definitions`, `$defs`), or
a bare map of schemas. YAML needs PyYAML:
`pip install pyyaml --break-system-packages`.

`--inventory` prints every type with its fields, shapes, array bounds, required
flags and `allOf` parents. That inventory is what you diff the glossary against —
work from it rather than from the raw file, because it has already resolved
inheritance.

## The finding codes

### Blocking

**`REF-BROKEN`** — a local `$ref` that doesn't start `#/`. The classic is
`'#components/schemas/Foo'`, one character from correct. Nothing resolves,
inheritance silently does nothing, and validators generally don't complain.
Almost always appears in *some* refs and not others in the same file, which is
the tell that it was hand-typed.

**`REF-MISSING`** — well-formed ref to a schema that doesn't exist. Usually a
rename that missed a call site.

**`REQ-NULL`** — `required:` written with nothing under it parses as `null`, not
as an empty list. The schema is invalid. This one *migrates*: it gets fixed in
one type and reappears in the type added next revision, which is the signal that
the file needs a lint step rather than another spot fix.

**`REQ-MAPPING`** — `required:` holding a mapping of field definitions means a
`properties:` key was omitted and the fields fell into `required`. The type ends
up with no properties at all and constrains nothing — while looking, to a reader,
completely normal.

**`REQ-UNDEFINED`** — a name in `required` that no property defines, following
`allOf` and `$ref`. Two causes: a singular/plural slip (`substitute` vs
`substitutes` — the script suggests the near match), or the property genuinely
never got written.

**`ARR-BOUNDS`** — `minItems` greater than `maxItems`.

**`DUP-KEY`** — a duplicate mapping key. YAML silently keeps the last one, so
half the definition vanishes without warning.

### Significant

**`EX-NOT-LIST`** — `examples:` that parsed as a scalar instead of a list.
Nearly always a missing space after the dash (`-2e6afe1d…`), which YAML reads as
a plain string rather than a sequence item.

**`EX-TYPE`** — an example whose parsed type contradicts the declared `type`.
Unquoted `001` becomes the integer `1`; unquoted `1.` becomes the float `1.0`.
Both then fail any string pattern, and both look correct in the file.

**`EX-PATTERN`** — an example that fails its own `pattern`. If the author's own
example doesn't validate, either the pattern or the intent is wrong, and it's
worth asking which.

**`FMT-PAIR`** — a `type`/`format` pairing outside the registry. `type: number`
with `format: int32` is the common one: `int32` belongs with `type: integer`, so
behaviour is generator-dependent — one toolchain emits an integer, another a
decimal, from the same schema. It also usually contradicts the domain, since
quantities and measurements are frequently fractional.

**`PAT-DOT`** — an unescaped `.` in a pattern. `^\d.$` reads as "a digit and a
dot" and matches `1x` and `12` just as happily. The fix is `^\d\.$`.

**`PAT-SINGLE-DIGIT`** — `^\d$` allows exactly one digit, silently capping
whatever it indexes at ten. Cross-check against any unbounded cardinality in the
glossary; this is a `CARD` finding hiding inside a `pattern`.

**`VENDOR-TYPO`** — a near-miss on a known vendor extension
(`x-extenxible-enum`). Tooling ignores unknown `x-` keys silently, so a
misspelled extension is simply absent. Usually spelled correctly elsewhere in the
same file, which makes it easy to confirm.

**`SUB-EMPTY`** — a subtype that adds nothing to its base. Indistinguishable on
the wire from the base and from its siblings.

**`SUB-NO-DISCRIMINATOR`** — two or more subtypes of one base with no
`discriminator`. A consumer cannot tell which variant it received without
guessing from field presence.

### Minor

**`ORPHAN`** — schemas nothing references. Two very different cases: entry-point
types referenced from `paths` outside this file (fine), and shared value types
that duplicate an inline definition and are used by neither (a drift hazard —
the copy and its inline twin will diverge).

## What the script cannot check

Do these by hand; each has produced a real finding.

- **Stale descriptions.** A type generalized for a second use, with its
  `description` still naming the first. The strongest available evidence for a
  `SENSE` finding, and invisible to any structural check.
- **Semantic naming.** Whether a field's name matches what it holds — an
  `ingredient` that holds an index, a `helpProviderType` described as an
  "identifier".
- **Mixed identity conventions.** `fooId` in one type and `fooIdentifier` in
  another; one required, its counterpart optional.
- **Units and code lists.** Whether `CUP`, `SPOON`, `KG`, currency codes or
  status enums mean the same thing here as in the context they'll be compared
  against. Ambiguous units (a US cup is 240 ml, a metric cup 250, a Japanese cup
  200; a tablespoon is 15 ml except in Australia, where it's 20) should carry
  their convention in the description. Keeping the informal unit is usually right
  — it's the vocabulary cooks and clerks actually use — but the convention has to
  be written down somewhere.
- **Whether a bound is a business rule.** `maxItems: 3` parses fine and may be a
  remembered screen limit. Ask what happens at the boundary.