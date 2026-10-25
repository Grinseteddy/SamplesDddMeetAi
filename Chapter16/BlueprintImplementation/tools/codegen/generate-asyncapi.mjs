// ADR-CA-011 (amended) · message payload types from the approved AsyncAPI 3.1.0 spec — generated, never edited.
// @asyncapi/parser checks that the spec parses; json-schema-to-typescript turns every named component
// schema into one exported type. modelina was replaced: it reads AsyncAPI only up to 3.0.0.
import { mkdir, readFile, rm, writeFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { Parser } from "@asyncapi/parser";
import { compile } from "json-schema-to-typescript";
import { parse } from "yaml";

const spec = fileURLToPath(new URL("../../contracts/asyncapi/cook-assistance.asyncapi.yaml", import.meta.url));
const outDir = fileURLToPath(new URL("../../src/generated/asyncapi", import.meta.url));
const source = await readFile(spec, "utf8");

const { document, diagnostics } = await new Parser().parse(source);
const errors = diagnostics.filter((diagnostic) => diagnostic.severity === 0);
if (document === undefined || errors.length > 0) {
  for (const error of errors) console.error(`  ${error.path.join(".")}: ${error.message}`);
  throw new Error(`the AsyncAPI spec does not parse (${errors.length} errors)`);
}

// The raw component schemas keep their names; the parser's view would inline every $ref.
const schemas = parse(source).components.schemas;
const $defs = JSON.parse(JSON.stringify(schemas).replaceAll("#/components/schemas/", "#/$defs/"));
const names = Object.keys($defs);
const root = {
  title: "CookAssistanceMessageSchemas",
  type: "object",
  additionalProperties: false,
  properties: Object.fromEntries(names.map((name) => [name, { $ref: `#/$defs/${name}` }])),
  $defs,
};

const types = await compile(root, "CookAssistanceMessageSchemas", {
  bannerComment: `/* Generated from contracts/asyncapi/cook-assistance.asyncapi.yaml by json-schema-to-typescript — do not edit. */`,
  additionalProperties: false,
  unreachableDefinitions: true,
  maxItems: -1, // array limits are checked at runtime against the spec (ajv); as tuples they only bloat the types
});
const anonymous = types.match(/export (?:interface|type) (?:Anonymous|.*\d+\b)/g) ?? [];
if (anonymous.length > 0) throw new Error(`generated anonymous types: ${anonymous.join(", ")} — name the schemas in the spec`);

await rm(outDir, { recursive: true, force: true });
await mkdir(outDir, { recursive: true });
await writeFile(`${outDir}/cook-assistance.ts`, types);
const exported = types.match(/^export (?:interface|type) \w+/gm) ?? [];
console.log(`json-schema-to-typescript: ${exported.length} types from ${names.length} component schemas → ${outDir}`);
