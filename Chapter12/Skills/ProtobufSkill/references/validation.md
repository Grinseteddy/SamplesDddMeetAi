# Validating and linting protobuf

Always verify before presenting. A `.proto` that doesn't compile is worthless,
and duplicate field numbers are easy to introduce and invisible on reading.

## Primary: buf

`buf` is the standard toolchain. It installs cleanly from npm:

```bash
npm install -g @bufbuild/buf
export PATH="$PATH:$(npm root -g)/../bin"
buf --version
```

Run all three from the directory containing `buf.yaml`:

```bash
buf build      # compile — the real correctness check
buf lint       # house conventions
buf format -d  # canonical formatting; -d shows a diff, -w rewrites in place
```

**What each catches:**

- **`buf build`** — syntax errors, unresolved imports, duplicate field numbers,
  duplicate message or field names, use of reserved numbers, illegal
  combinations like `optional repeated`. Exit code 0 and no output means it
  compiled. This is the check that must pass.
- **`buf lint`** — with `STANDARD` + `COMMENTS`: package/directory mismatch,
  missing version suffix, naming violations, missing `_UNSPECIFIED = 0`, missing
  enum value prefixes, and any undocumented message, field, enum, or enum value.
- **`buf format -d`** — prints a diff if the file isn't canonically formatted.
  Empty output means clean; run `buf format -w` to fix.

**Configuration.** Copy `assets/example-book-catalog/buf.yaml` alongside the
generated files:

```yaml
version: v2
modules:
  - path: .
lint:
  use:
    - STANDARD
    - COMMENTS
breaking:
  use:
    - FILE
```

**Module dependencies.** If the model imports `google.type.*` or
`buf.validate.*`, add them under `deps:` and run `buf dep update` — which needs
network access to `buf.build`. If that isn't available, either drop the
dependency and model the fields explicitly, or say clearly that the file will
compile only in an environment that can resolve it. Types under
`google.protobuf.*` are built in and need no dependency.

**Checking compatibility.** When editing an existing `.proto`, verify you
haven't broken the wire contract:

```bash
buf breaking --against '.git#branch=main'
```

## Fallback: protoc

If npm isn't available, `protoc` comes bundled with `grpcio-tools`:

```bash
pip install grpcio-tools --break-system-packages
python -m grpc_tools.protoc -I. --descriptor_set_out=/dev/null \
  $(find . -name '*.proto')
```

This compiles and reports errors, which covers the `buf build` case. It does no
linting, so check the conventions in `references/conventions.md` by hand — in
practice the ones most often missed are the `_UNSPECIFIED = 0` zero value, enum
value prefixes, and the package/directory match.

## If neither installs

Say so rather than presenting unverified output, and do a manual pass over the
things a compiler would have caught:

- every field number unique within its message, none in `19000–19999`, none
  colliding with a `reserved` range;
- every `import` present and every referenced type either imported, defined in
  the file, or in the same package;
- no `optional` on a `repeated` field;
- every enum has a zero value named `<ENUM_NAME>_UNSPECIFIED`;
- the package matches the directory path and ends in a version suffix.