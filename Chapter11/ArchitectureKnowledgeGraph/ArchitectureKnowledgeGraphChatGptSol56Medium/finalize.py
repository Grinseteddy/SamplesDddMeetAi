"""Run the bundled checker and make the report's matrix/findings reflect it exactly."""
import json, os, subprocess, sys
from pathlib import Path

root = Path(__file__).parent
checker = Path('/root/.codex/skills/remote-skills/skill-6aa91922f3b48191a417729f9dcf9fc7/scripts/check_adr_graph.py')
result = subprocess.run([sys.executable, str(checker), str(root/'graph.ttl'), '--matrix', '--json', str(root/'findings.json')],
                        capture_output=True, text=True, env=os.environ.copy())
if result.returncode: raise RuntimeError(result.stdout + '\n' + result.stderr)
findings = json.loads((root/'findings.json').read_text())
if findings['errors']: raise RuntimeError(findings['errors'])
matrix = result.stdout.split('## Compliance Matrix\n', 1)[1].strip()
report = (root/'report.md').read_text()
begin = report.index('## Compliance Matrix')
end = report.index('## Conflicts, in full')
report = report[:begin] + '## Compliance Matrix\n\n' + matrix + '\n\nSilent and out-of-scope pairs have no assertion in the graph; absence is not a conflict.\n\n' + report[end:]
begin = report.index('## Governance gaps')
end = report.index('## What newly connects')
notes = ['## Governance gaps', '']
for label, key in [('No decision engages', 'dead_letter_principles'), ('Honored without an explicit citation', 'never_cited_principles'), ('Decisions with no principle citation', 'decisions_citing_no_principle')]:
    items = findings['findings'].get(key, [])
    notes.append(f'**{label} ({len(items)}):** ' + ('; '.join(items) if items else 'none') + '.')
    notes.append('')
notes += ['**Source defects:**', '']
notes += ['- ' + item for item in findings['findings'].get('source_defects', [])]
notes += ['', 'The duplicate AP0005 identifiers and ADR0001 option ambiguity merit correction before relying on these decisions as unambiguous design instructions. AP0002 concerns cross-context communication; AP0006 concerns UI communication, so their scopes do not create an asserted principle tension. SADR0004 uses terse terminology that should be confirmed in its glossary.', '']
report = report[:begin] + '\n'.join(notes) + '\n' + report[end:]
(root/'report.md').write_text(report)
print(result.stdout)
