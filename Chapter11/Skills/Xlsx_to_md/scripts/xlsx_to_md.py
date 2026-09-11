#!/usr/bin/env python3
"""
Convert an Excel workbook to Markdown tables — one .md file per sheet.

Usage:
    python xlsx_to_md.py <input.xlsx> [output_dir]

- output_dir defaults to the input file's own directory.
- Output files are named "<workbook-stem>-<SheetName>.md" when there is more
  than one sheet, or "<workbook-stem>.md" when there is exactly one.
- Empty sheets are skipped.
- Tables are plain GitHub-flavored Markdown (via pandas.to_markdown), no
  extra title or metadata header.
"""

import sys
import re
from pathlib import Path

import pandas as pd


def slugify_sheet(name: str) -> str:
    """Make a sheet name safe for use in a filename."""
    name = name.strip().replace(" ", "_")
    return re.sub(r"[^A-Za-z0-9_.-]", "", name) or "Sheet"


def convert(input_path: Path, output_dir: Path) -> list[Path]:
    sheets = pd.read_excel(input_path, sheet_name=None, dtype=str)
    stem = input_path.stem
    written = []

    multi = len(sheets) > 1
    for sheet_name, df in sheets.items():
        if df.empty:
            continue
        df = df.fillna("")
        df = df.map(
            lambda v: re.sub(r"^(\d{4}-\d{2}-\d{2}) 00:00:00$", r"\1", v)
            if isinstance(v, str)
            else v
        )

        date_col = next((c for c in df.columns if c.strip().lower() == "date"), None)
        if date_col is not None:
            sort_key = pd.to_datetime(df[date_col], errors="coerce")
            df = df.assign(_sort_key=sort_key).sort_values(
                "_sort_key", kind="stable"
            ).drop(columns="_sort_key")

        table_md = df.to_markdown(index=False)

        if multi:
            out_name = f"{stem}-{slugify_sheet(sheet_name)}.md"
        else:
            out_name = f"{stem}.md"
        out_path = output_dir / out_name
        out_path.write_text(table_md + "\n", encoding="utf-8")
        written.append(out_path)

    return written


def main():
    if len(sys.argv) < 2:
        print("Usage: python xlsx_to_md.py <input.xlsx> [output_dir]", file=sys.stderr)
        sys.exit(1)

    input_path = Path(sys.argv[1]).expanduser().resolve()
    if not input_path.exists():
        print(f"Error: file not found: {input_path}", file=sys.stderr)
        sys.exit(1)

    output_dir = Path(sys.argv[2]).expanduser().resolve() if len(sys.argv) > 2 else input_path.parent
    output_dir.mkdir(parents=True, exist_ok=True)

    written = convert(input_path, output_dir)
    if not written:
        print("No non-empty sheets found — nothing written.", file=sys.stderr)
        sys.exit(1)

    for p in written:
        print(p)


if __name__ == "__main__":
    main()