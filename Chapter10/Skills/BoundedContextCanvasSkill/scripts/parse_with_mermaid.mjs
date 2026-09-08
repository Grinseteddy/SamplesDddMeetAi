// Authoritative Mermaid parse check for a folder of canvases.
//
// check_canvases.py catches the traps that actually break canvases without any
// dependencies. This runs the real Mermaid parser, so it is the last word on
// whether a diagram renders. Optional -- use it when a canvas looks fine and
// still fails in the target tool.
//
//   npm install mermaid jsdom
//   node parse_with_mermaid.mjs bounded-context-canvases/*.canvas.md
//
// Exits 1 if any block fails to parse.

import { JSDOM } from 'jsdom';
import fs from 'fs';

const dom = new JSDOM('<!DOCTYPE html><body></body>', { pretendToBeVisual: true });
global.window = dom.window;
global.document = dom.window.document;
global.HTMLElement = dom.window.HTMLElement;
global.SVGElement = dom.window.SVGElement;

const mermaid = (await import('mermaid')).default;
mermaid.initialize({ startOnLoad: false, securityLevel: 'loose' });

const files = process.argv.slice(2);
if (files.length === 0) {
    console.error('usage: node parse_with_mermaid.mjs <canvas files>');
    process.exit(2);
}

let bad = 0;
for (const file of files) {
    const text = fs.readFileSync(file, 'utf8');
    const blocks = file.endsWith('.mmd')
        ? [text]
        : [...text.matchAll(/```mermaid\n([\s\S]*?)\n```/g)].map((m) => m[1]);

    if (blocks.length === 0) {
        console.log(`NO BLOCK  ${file}`);
        bad++;
        continue;
    }
    for (const [i, block] of blocks.entries()) {
        try {
            await mermaid.parse(block);
            console.log(`OK        ${file} #${i}`);
        } catch (err) {
            bad++;
            const msg = String(err?.message ?? err).split('\n').slice(0, 8).join('\n    ');
            console.log(`FAIL      ${file} #${i}\n    ${msg}`);
        }
    }
}

process.exit(bad ? 1 : 0);