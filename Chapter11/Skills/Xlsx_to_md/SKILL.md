---
name: xlsx-to-md
description: "Convert an Excel workbook (.xlsx) into plain GitHub-flavored Markdown tables using scripts/xlsx_to_md.py. Produces one .md file per sheet (named 'workbook-SheetName.md', or just 'workbook.md' for a single-sheet file), each containing only the raw table — no titles or metadata headers. Use this skill whenever the user asks to convert, export, or turn an Excel/xlsx file (or a specific sheet/table in one) into Markdown, a markdown table, a .md file, or similar phrasing like 'excel table to md' — even if they don't name this skill directly. Do NOT use for creating or editing xlsx files (use the xlsx skill for that), and do not use when the deliverable should be a Word doc, PDF, or other non-Markdown format."
author: Annegret Junker
---

# XLSX to Markdown

Converts every sheet of an Excel workbook into a plain Markdown table, one file per sheet.

## Usage

```bash
python scripts/xlsx_to_md.py <input.xlsx> [output_dir]
```

- `output_dir` defaults to the input file's own directory.
- Multi-sheet workbooks produce `<workbook-stem>-<SheetName>.md` per sheet.
- Single-sheet workbooks produce just `<workbook-stem>.md`.
- Empty sheets are skipped.
- Output is a bare GitHub-flavored Markdown table — no title, no source/date header, no extra commentary. If the user wants a title or metadata header, add it after running the script rather than modifying it, unless they ask you to change the default.
- Date-only Excel cells are rendered as `YYYY-MM-DD` (no spurious `00:00:00`).
- If a sheet has a column named "Date" (case-insensitive), rows are sorted by that column ascending before being written. Sheets without a "Date" column keep their original row order.

## Workflow

1. Confirm the input file path (check `/mnt/user-data/uploads/` if the user uploaded it).
2. Run the script, writing into `/mnt/user-data/outputs/` (or a subfolder there) so the user can access the result.
3. Present each generated `.md` file with `present_files`.

## Dependencies

`pandas` and `tabulate` (both installable via `pip install --break-system-packages` if missing; usually already present).