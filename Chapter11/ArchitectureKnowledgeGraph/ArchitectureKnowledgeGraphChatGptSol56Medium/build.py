from pathlib import Path
import csv, html, json, re
from datetime import date
from rdflib import Graph, Namespace, Literal, URIRef, RDF, RDFS, OWL
from rdflib.namespace import SKOS, XSD

ROOT = Path(__file__).parent
UPLOAD = ROOT.parent / 'upload'
DKG = Namespace('https://w3id.org/dkg/ns#')
EX = Namespace('https://w3id.org/dkg/larder#')
g = Graph()
for prefix, ns in [('dkg', DKG), ('', EX), ('skos', SKOS), ('rdf', RDF), ('rdfs', RDFS), ('owl', OWL), ('xsd', XSD)]:
    g.bind(prefix, ns)
g.add((EX.graph, RDF.type, OWL.Ontology))
g.add((EX.graph, OWL.imports, Namespace('https://w3id.org/dkg/ns')['']))
g.add((EX.graph, OWL.imports, Namespace('https://w3id.org/dkg/adr-ns')['']))
g.add((EX.graph, RDFS.label, Literal('Larder ADR and architectural principles graph')))

def put(s, p, v):
    g.add((s, p, Literal(v) if isinstance(v, str) and not isinstance(v, URIRef) else v))

def artifact(key, typ, filename, comment=''):
    n = EX['Art_' + key]
    put(n, RDF.type, DKG.Artifact); put(n, RDF.type, typ)
    put(n, RDFS.label, filename); put(n, DKG.ingestedAt, Literal('2026-09-15', datatype=XSD.date))
    if comment: put(n, RDFS.comment, comment)
    return n

def node(key, typ, label, src, locator):
    n = EX[key]
    put(n, RDF.type, typ); put(n, SKOS.prefLabel, label)
    put(n, DKG.source, src); put(n, DKG.locator, locator); put(n, DKG.confidence, DKG.OnArtifact)
    return n

principles_text = (UPLOAD / 'LarderArchitecturalPrinciples.md').read_text()
pa = artifact('Principles_Larder', DKG.PrinciplesDocument, 'LarderArchitecturalPrinciples.md', '9 entries; AP0005 occurs twice with different statements.')
sections = re.split(r'(?=^# AP\d{4})', principles_text, flags=re.M)
principles = {}
for sec in sections:
    head = re.search(r'^# (AP\d{4})\s*[—-]\s*(.+)$', sec, re.M)
    if not head: continue
    pid, title = head.group(1), head.group(2).strip()
    suffix = 'Testing' if pid == 'AP0005' and 'Testing' in title else 'MicroUIs' if pid == 'AP0005' else pid
    n = node('Prin_' + suffix, DKG.Principle, title, pa, pid)
    principles[suffix] = n
    put(n, DKG.principleId, pid)
    for heading, prop in [('Statement', DKG.statement), ('Rational', DKG.rationale), ('Implications', DKG.implication), ('Status', DKG.status)]:
        m = re.search(r'^\*\*' + heading + r':\*\*\s*(.+)$', sec, re.M)
        if m: put(n, prop, m.group(1).strip())
    m = re.search(r'^\*\*Last amended:\*\*\s*(\d{4}-\d\d-\d\d)', sec, re.M)
    if m: put(n, DKG.amendedAt, Literal(m.group(1), datatype=XSD.date))
    put(n, DKG.testable, Literal(True))
for k, scope in {'AP0001':'deployment and modular Bounded Contexts', 'AP0002':'communication between Bounded Contexts', 'AP0003':'design of our functional architecture', 'AP0004':'design of our technical architecture', 'MicroUIs':'Bounded Context UIs', 'AP0006':'UI communication', 'AP0008':'Domain artifacts'}.items():
    put(principles[k], DKG.scope, scope)
put(pa, DKG.defect, 'Two distinct entries carry the exact ID AP0005: Automatic Testing and Micro-UIs. Both retain dkg:principleId "AP0005"; graph IRIs disambiguate them.')
put(principles['AP0007'], DKG.defect, 'The Monitoring implications line is identical to Micro-UIs implications: "Using a large monolithic frontend would us slow down." It does not explain monitoring.')
put(principles['AP0007'], RDFS.comment, 'Reliable and fast are not quantified; a decision to omit monitoring would still falsify the monitoring requirement.')

def question(key, label, src, locator):
    return node('Q_' + key, DKG.Question, label, src, locator)

decisions = {}
artifacts = {}
for path in sorted(UPLOAD.glob('ADR*.md')):
    txt = path.read_text()
    did = re.search(r'^ADR\d{4}', txt).group()
    title = re.search(r'^# (.+)$', txt, re.M).group(1).strip()
    src = artifact('ADR_' + did, DKG.ADRDocument, path.name)
    artifacts[did] = src
    n = node('D_' + did, DKG.Decision, title, src, did)
    decisions[did] = n
    put(n, DKG.decisionId, did); put(n, DKG.sourceFormat, 'full-form')
    status_date = re.search(r'^### (\w+) (\d{4}-\d\d-\d\d)', txt, re.M)
    put(n, DKG.status, status_date.group(1)); put(n, DKG.decidedAt, Literal(status_date.group(2), datatype=XSD.date))
    put(n, DKG.decidedBy, re.search(r'^### (.+, Architect)', txt, re.M).group(1))
    dec = re.search(r'## Decision\s*\n\*\*(.*?)\*\*', txt, re.S).group(1).strip()
    put(n, DKG.decidedOption, dec)
    context = txt.split('## Context\n', 1)[1].split('## Options considered', 1)[0].strip()
    put(n, DKG.rationale, re.sub(r'!\[\]\([^)]*\)', '', context).strip()[:1200])
    options = txt.split('## Options considered', 1)[1].split('## Consequences', 1)[0]
    for heading in re.findall(r'^### (.+)$', options, re.M): put(n, DKG.consideredOption, heading.strip())
    table = txt.split('## Consequences', 1)[1].split('## Advice', 1)[0]
    chosen_column = 3 if did == 'ADR0001' else 1
    for line in table.splitlines():
        if not line.startswith('|') or re.match(r'^\|[-:| ]+\|$', line): continue
        cells = [c.strip() for c in line.strip().strip('|').split('|')]
        if len(cells) > chosen_column and cells[0] != 'Consequence' and not cells[0].startswith('Consequence'):
            put(n, DKG.consequence, cells[0] + ': ' + cells[chosen_column])
    for p in sorted(set(re.findall(r'AP\d{4}', txt))):
        target = principles['MicroUIs' if p == 'AP0005' and did == 'ADR0005' else p]
        put(n, DKG.cites, target)
    if did == 'ADR0001':
        put(n, DKG.defect, 'Decision says "We will implement Larder as Monolith", but Context defines Bounded Context modules and the Modular monolith option specifies schema isolation; the Monolith option explicitly says enhancements are difficult. The chosen deployment type is ambiguous.')
    if did == 'ADR0002':
        put(n, DKG.defect, 'Context says "modular monolith" although ADR0001 operative decision says "Monolith"; the Consequences columns are copied from deployment alternatives and do not correspond to the database options.')
    if did == 'ADR0005':
        put(n, DKG.defect, 'The Choreography option describes "Centralizing interactive user flows through a UI orchestrator" and thus does not describe choreography as a distinct alternative.')
    labels = {
        'ADR0001':'Which deployment and module shape should Larder use?',
        'ADR0002':'How should Bounded Context modules share or isolate database instances?',
        'ADR0003':'How should Cooking Assistance communicate asynchronously with Notification and Grandma Avatar?',
        'ADR0004':'How should user interfaces communicate synchronously?',
        'ADR0005':'How should Larder compose micro-UIs and manage interactive UI workflows?'
    }
    q = question(did, labels[did], src, did + ' Context')
    put(n, DKG.answers, q)

sa = artifact('SADR_Larder', DKG.ADRDocument, 'SadrsLarder.md', 'Small-ADR decision log; 7 rows.')
lines = (UPLOAD / 'SadrsLarder.md').read_text().splitlines()
rows = list(csv.reader(lines, delimiter='|'))[2:]
shared_questions = {}
for row in rows:
    cols = [x.strip() for x in row[1:-1]]
    if len(cols) != 8: raise ValueError(cols)
    dt, did, title, status, author, dec, qtext, options = cols
    n = node('D_' + did, DKG.Decision, title, sa, did)
    decisions[did] = n; artifacts[did] = sa
    for p, v in [(DKG.decisionId,did),(DKG.status,status),(DKG.decidedBy,author),(DKG.decidedOption,dec),(DKG.sourceFormat,'small-ADR')]: put(n,p,v)
    put(n, DKG.decidedAt, Literal(dt, datatype=XSD.date))
    # Preserve the complete options cell; commas and semicolons are not reliable option boundaries.
    put(n, DKG.consideredOption, options)
    qkey = 'SADR0002_0003' if did in ('SADR0002','SADR0003') else did
    if qkey not in shared_questions: shared_questions[qkey] = question(qkey, qtext, sa, did + ' Question')
    put(n, DKG.answers, shared_questions[qkey])
put(decisions['SADR0003'], DKG.supersedes, decisions['SADR0002'])
put(decisions['SADR0004'], RDFS.comment, 'The decision cell "Help response / Help request" answers the ambiguous glossary question, but its truncated wording needs confirmation of how each term is assigned.')

def verdict(did, key, relation, how, evidence, confidence=DKG.Implied):
    d, p = decisions[did], principles[key]
    put(d, relation, p)
    a = EX['As_' + did + '_' + ('honors' if relation == DKG.honors else 'overrides') + '_' + key]
    put(a, RDF.type, DKG.Assertion)
    put(a, RDF.subject, d); put(a, RDF.predicate, relation); put(a, RDF.object, p)
    put(a, DKG.source, artifacts[did]); put(a, DKG.confidence, confidence)
    put(a, RDFS.comment, evidence)
    if relation == DKG.honors: put(a, DKG.citedBy, how)
    else: put(a, DKG.acknowledged, Literal(how == 'acknowledged'))

verdict('ADR0001','AP0001',DKG.honors,'explicit','The ADR selects a deployment monolith and cites AP0001. Its modularity claim is ambiguous; see the decision defect.',DKG.OnArtifact)
verdict('ADR0001','AP0003',DKG.honors,'incidental','Context: "We defined Bounded Contexts ... Those Bounded Contexts are modules"; this applies a DDD boundary to functional structure.')
verdict('ADR0002','AP0001',DKG.honors,'explicit','The selected shared instance with Bounded Context schemas is argued as easy to operate (see AP0001).',DKG.OnArtifact)
verdict('ADR0002','AP0003',DKG.honors,'incidental','Option: decoupled schemas and schema-scoped database users per Bounded Context.')
verdict('ADR0003','AP0002',DKG.honors,'explicit','The chosen RabbitMQ option says "It is supported by principles (see AP0002)" and rejects synchronous calls as contradicting AP0002.',DKG.OnArtifact)
verdict('ADR0004','AP0006',DKG.honors,'explicit','The chosen REST API option says "It is supported by principles (see AP0006)".',DKG.OnArtifact)
verdict('ADR0004','AP0008',DKG.honors,'by-elimination','The ADR cites AP0008 against GraphQL, and the chosen REST option is rated "Fine grained" for security in Consequences.',DKG.OnArtifact)
verdict('ADR0005','MicroUIs',DKG.honors,'incidental','Decision: AppShell ingests the micro UIs of Bounded Contexts; the orchestrator option says these micro-UIs can be independent. Consequence records dependence on AppShell team.')
verdict('ADR0005','AP0003',DKG.honors,'incidental','Context identifies each interactive flow and its Bounded Context ownership.')
verdict('SADR0004','AP0003',DKG.honors,'incidental','Question distinguishes the Cook request from the supplied response; decision names Help request and Help response as separate terms.')

g.serialize(ROOT/'graph.ttl', format='turtle')

# The report and browser view are regenerated from the serialized graph.
h = Graph().parse(ROOT/'graph.ttl', format='turtle')
def val(n,p): return str(h.value(n,p) or '')
def sid(n): return str(n).split('#')[-1]
pp = sorted(h.subjects(RDF.type,DKG.Principle),key=lambda n:(val(n,DKG.principleId),sid(n)))
dd = sorted(h.subjects(RDF.type,DKG.Decision),key=lambda n:sid(n))
aa = sorted(h.subjects(RDF.type,DKG.Artifact),key=sid)
report = ['# ADR & Principles — Larder','','**Mode: seed.** One principles file, five full-form ADRs and one seven-row small-ADR log were ingested. There was no existing graph in the supplied workspace.','','## What went in',f'{len(aa)} source artifacts; {len(pp)} principles; {len(dd)} decisions; {len(list(h.subjects(RDF.type,DKG.Question)))} questions.','','## Principles Register','','| ID | Principle | Statement | Testable |','|---|---|---|---|']
for p in pp: report.append(f'| {val(p,DKG.principleId)} | {val(p,SKOS.prefLabel)} | {val(p,DKG.statement)} | {val(p,DKG.testable)} |')
report += ['','## Decision Ledger','','| ID | Title | Status | Date | Author | Decision | Source |','|---|---|---|---|---|---|---|']
for d in dd:
    decision = val(d,DKG.decidedOption).replace('|','\\|')
    report.append(f'| {val(d,DKG.decisionId)} | {val(d,SKOS.prefLabel)} | {val(d,DKG.status)} | {val(d,DKG.decidedAt)} | {val(d,DKG.decidedBy)} | {decision} | {val(d,DKG.sourceFormat)} |')
report += ['','SADR0003 explicitly supersedes SADR0002; they share a question, but only SADR0003 is active. The full-form ADRs answer distinct technical questions, so no pair of active decisions answers the same question differently.','','## Compliance Matrix','','| Decision | Honors | Overrides | Cites |','|---|---|---|---|']
assertions = list(h.subjects(RDF.type,DKG.Assertion))
for d in dd:
    def edges(rel):
        out=[]
        for p in h.objects(d,rel):
            a=next((x for x in assertions if h.value(x,RDF.subject)==d and h.value(x,RDF.predicate)==rel and h.value(x,RDF.object)==p),None)
            qualifier=val(a,DKG.citedBy) if rel==DKG.honors else ('acknowledged' if val(a,DKG.acknowledged)=='true' else 'UNACKNOWLEDGED')
            out.append(f'{val(p,DKG.principleId)} {"(Micro-UIs)" if sid(p)=="Prin_MicroUIs" else ""} ({qualifier})')
        return ', '.join(out) or '—'
    cites=', '.join(val(p,DKG.principleId) + (' (Micro-UIs)' if sid(p)=='Prin_MicroUIs' else '') for p in h.objects(d,DKG.cites)) or '—'
    report.append(f'| {sid(d)} | {edges(DKG.honors)} | {edges(DKG.overrides)} | {cites} |')
report += ['','Silent and out-of-scope pairs have no assertion in the graph; absence is not a conflict.','', '## Conflicts, in full','','No principle override is evidenced by these decisions. ADR0001 is ambiguous about *which kind* of monolith it selected; resolve its operative sentence before treating modular boundaries as settled.','','## Governance gaps','']
touch={p for d in dd for rel in (DKG.honors,DKG.overrides,DKG.cites) for p in h.objects(d,rel)}
for p in pp:
    if p not in touch: report.append(f'- **{val(p,DKG.principleId)} {val(p,SKOS.prefLabel)}:** no decision in this set engages it. This is a coverage gap, not a violation.')
report += ['- **AP0005 is duplicated:** Automatic Testing and Micro-UIs retain their source ID; disambiguate the document IDs before future references are made.','- **AP0007 Monitoring:** its implications repeat the Micro-UIs implications and do not address monitoring.','- **ADR0001:** the operative “Monolith” choice and modular-monolith analysis do not unambiguously identify the adopted option.','- **ADR0002:** its context assumes a modular monolith, and its consequences table is labelled with deployment alternatives rather than the database options.','- **ADR0005:** the Choreography option itself describes a UI orchestrator, so the alternatives are not distinct.','- **SADR0004:** the terse decision “Help response / Help request” leaves the precise glossary wording to verify.','- **AP0002 and AP0006:** asynchronous communication between Bounded Contexts and synchronous communication with UIs have different scopes. No principle tension is asserted.','','## What newly connects','','Each full-form ADR now answers a question minted from its context; all seven small-log rows answer their recorded questions. SADR0002 and SADR0003 share the naming question and are linked by supersession. No earlier domain graph or open questions were supplied, so these links cannot be joined to prior artifact questions.']
(ROOT/'report.md').write_text('\n'.join(report)+'\n')

data=[]
for n in pp+dd:
    typ='principle' if n in pp else 'decision'
    data.append({'id':sid(n),'type':typ,'label':val(n,SKOS.prefLabel),'source_id':val(n,DKG.principleId if typ=='principle' else DKG.decisionId),'status':val(n,DKG.status),'statement':val(n,DKG.statement),'decision':val(n,DKG.decidedOption),'rationale':val(n,DKG.rationale),'scope':val(n,DKG.scope),'testable':val(n,DKG.testable),'questions':[val(q,SKOS.prefLabel) for q in h.objects(n,DKG.answers)],'honors':[sid(p) for p in h.objects(n,DKG.honors)],'overrides':[sid(p) for p in h.objects(n,DKG.overrides)],'cites':[sid(p) for p in h.objects(n,DKG.cites)],'supersedes':[sid(p) for p in h.objects(n,DKG.supersedes)],'evidence':[{'relation':sid(h.value(a,RDF.predicate)),'target':sid(h.value(a,RDF.object)),'comment':val(a,RDFS.comment),'citation':val(a,DKG.citedBy)} for a in assertions if h.value(a,RDF.subject)==n],'defects':[str(x) for x in h.objects(n,DKG.defect)]})
browser='''<!doctype html><html lang="en"><meta charset="utf-8"><title>Larder ADR graph browser</title><style>body{font:16px system-ui;max-width:1100px;margin:auto;padding:32px;color:#202b3a;background:#f7f9fb}h1{margin-bottom:4px}nav{display:flex;gap:12px;flex-wrap:wrap;margin:24px 0}button,input{font:inherit;padding:9px 13px;border:1px solid #bac7d4;border-radius:8px;background:white}button{cursor:pointer}.card{background:white;border:1px solid #d8e0e7;border-radius:10px;padding:18px;margin:12px 0}small{color:#52657a}.pill{display:inline-block;background:#e6f0fa;border-radius:12px;padding:3px 9px;margin:3px}p{line-height:1.5}a{color:#145ba6}</style><h1>Larder architecture graph</h1><small>Generated from graph.ttl · 9 principles · 12 decisions</small><nav><button onclick="view('all')">Overview</button><button onclick="view('principle')">Principles</button><button onclick="view('decision')">Decisions</button><button onclick="view('gap')">Governance gaps</button><input id="search" placeholder="Filter by ID or text" oninput="render()"></nav><main id="main"></main><script id="graph-data" type="application/json">DATA</script><script>const nodes=JSON.parse(document.getElementById('graph-data').textContent),byId=Object.fromEntries(nodes.map(n=>[n.id,n]));let mode='all';const esc=s=>String(s||'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));function view(m){mode=m;render()}function links(ids){return ids.map(id=>'<span class="pill">'+esc(byId[id]?.source_id||id)+' '+esc(byId[id]?.label||'')+'</span>').join('')||'—'}function render(){let query=document.getElementById('search').value.toLowerCase(),items=nodes.filter(n=>(mode==='all'||n.type===mode||mode==='gap'&&(n.defects.length||n.type==='principle'&&!nodes.some(d=>d.type==='decision'&&[...d.honors,...d.overrides,...d.cites].includes(n.id))))&&JSON.stringify(n).toLowerCase().includes(query));document.getElementById('main').innerHTML=items.map(n=>'<section class="card"><small>'+esc(n.type)+' · '+esc(n.source_id)+' · '+esc(n.status)+'</small><h2>'+esc(n.label)+'</h2>'+(n.statement?'<p><strong>Statement:</strong> '+esc(n.statement)+'</p>':'')+(n.decision?'<p><strong>Decision:</strong> '+esc(n.decision)+'</p>':'')+(n.scope?'<p><strong>Scope:</strong> '+esc(n.scope)+'</p>':'')+(n.type==='principle'?'<p><strong>Testable:</strong> '+esc(n.testable)+'</p>':'')+(n.rationale?'<p><strong>Rationale:</strong> '+esc(n.rationale)+'</p>':'')+(n.type==='decision'?'<p><strong>Honors:</strong> '+links(n.honors)+'</p><p><strong>Overrides:</strong> '+links(n.overrides)+'</p><p><strong>Cites:</strong> '+links(n.cites)+'</p><p><strong>Supersedes:</strong> '+links(n.supersedes)+'</p><p><strong>Answers:</strong> '+esc(n.questions.join('; '))+'</p><p><strong>Evidence:</strong> '+esc(n.evidence.map(e=>e.relation+' '+e.target+': '+e.comment).join('; '))+'</p>':'')+(n.defects.length?'<p><strong>Source defects:</strong> '+esc(n.defects.join('; '))+'</p>':'')+'</section>').join('')||'<p>No matching nodes.</p>'}render()</script></html>'''
(ROOT/'adr-browser.html').write_text(browser.replace('DATA',json.dumps(data,ensure_ascii=False).replace('</','<\\/')))
