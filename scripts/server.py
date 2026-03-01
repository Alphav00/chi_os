"""
DJC³ Web Server — Phone-accessible agentic pipeline via FastAPI + SSE.

HOW TO RUN (from the scripts/ directory):
  OPENROUTER_API_KEY="sk-or-v1-..." uvicorn server:app --host 0.0.0.0 --port 8000

HOW TO ACCESS FROM YOUR PHONE (free, no account needed):
  cloudflared tunnel --url http://localhost:8000
  → prints a public HTTPS URL — open it in any browser on any device

INSTALL CLOUDFLARED (Termux):
  pkg install cloudflared
  OR: download binary from https://github.com/cloudflare/cloudflared/releases

INSTALL PYTHON DEPS:
  pip install -r requirements.txt
"""

import asyncio
import json
import time
import uuid

from fastapi import FastAPI, Request
from fastapi.responses import HTMLResponse
from sse_starlette.sse import EventSourceResponse

from djc3_triadic_reasoning import (
    PHASE_DEFS,
    DJC3Pipeline,
    HERMIT_THRESHOLD,
    init_db,
    get_last_session_time,
    save_session,
)

app = FastAPI()

# ---------------------------------------------------------------------------
# Startup
# ---------------------------------------------------------------------------

@app.on_event("startup")
async def startup():
    init_db()

# ---------------------------------------------------------------------------
# Phone-friendly UI  (dark, monospace, agent-colored, SSE-driven)
# ---------------------------------------------------------------------------

HTML = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>DJC³</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }

    body {
      font-family: 'Courier New', monospace;
      background: #0a0a0a;
      color: #d0d0d0;
      padding: 1rem;
      font-size: 15px;
    }

    h1 {
      color: #888;
      font-size: 1rem;
      letter-spacing: 0.18em;
      margin-bottom: 1rem;
      border-bottom: 1px solid #222;
      padding-bottom: 0.6rem;
    }

    textarea {
      width: 100%;
      min-height: 110px;
      background: #111;
      color: #e0e0e0;
      border: 1px solid #2a2a2a;
      border-radius: 4px;
      padding: 0.75rem;
      font-family: inherit;
      font-size: 15px;
      resize: vertical;
    }
    textarea:focus { outline: none; border-color: #444; }

    button {
      width: 100%;
      margin-top: 0.6rem;
      padding: 0.9rem;
      background: #111827;
      color: #7090e0;
      border: 1px solid #1e2a40;
      border-radius: 4px;
      font-family: inherit;
      font-size: 16px;
      letter-spacing: 0.1em;
      cursor: pointer;
      transition: background 0.15s;
    }
    button:disabled { opacity: 0.45; cursor: not-allowed; }
    button:not(:disabled):active { background: #1a2030; }

    #status {
      margin-top: 0.6rem;
      font-size: 13px;
      color: #555;
      min-height: 1.4em;
    }

    #hermit-banner {
      display: none;
      margin: 0.75rem 0;
      padding: 0.6rem 0.85rem;
      background: #141000;
      border-left: 4px solid #7a6000;
      color: #a89020;
      font-size: 13px;
      letter-spacing: 0.07em;
    }

    #output { margin-top: 0.85rem; }

    /* Phase cards */
    .phase {
      margin-bottom: 0.9rem;
      border-radius: 4px;
      background: #0f0f0f;
      overflow: hidden;
    }
    .phase-header {
      padding: 0.45rem 0.85rem;
      font-size: 11px;
      letter-spacing: 0.14em;
      font-weight: bold;
    }
    .phase-body {
      padding: 0.75rem 0.85rem;
      white-space: pre-wrap;
      word-break: break-word;
      font-size: 13.5px;
      line-height: 1.65;
      color: #c8c8c8;
    }
    .phase-thinking { color: #3a3a3a; font-style: italic; }

    /* Agent identity colors */
    .agent-dimitri   { border-left: 4px solid #708090; }
    .agent-dimitri   .phase-header { color: #708090; }

    .agent-jessica   { border-left: 4px solid #FF4500; }
    .agent-jessica   .phase-header { color: #FF4500; }
    /* Jessica pulse: 14 BPM ≈ one cycle every 4.29s */
    .agent-jessica.active {
      animation: jessica-pulse 4.29s ease-in-out infinite;
    }

    .agent-chi       { border-left: 4px solid #00CED1; }
    .agent-chi       .phase-header { color: #00CED1; }

    .agent-chrysalis { border-left: 4px solid #C0A060; }
    .agent-chrysalis .phase-header { color: #C0A060; }

    @keyframes jessica-pulse {
      0%   { opacity: 0.82; }
      50%  { opacity: 1;    }
      100% { opacity: 0.82; }
    }
  </style>
</head>
<body>

  <h1>◈ DJC³ TRIADIC REASONING ◈</h1>

  <form id="djc3-form">
    <textarea name="problem" placeholder="Enter the problem space…" required></textarea>
    <button type="submit" id="run-btn">▶ RUN PIPELINE</button>
  </form>

  <div id="status"></div>
  <div id="hermit-banner">⟳ HERMIT_AWAKENING — Lattice recalibrating after silence.</div>
  <div id="output"></div>

  <script>
    const form   = document.getElementById('djc3-form');
    const btn    = document.getElementById('run-btn');
    const status = document.getElementById('status');
    const output = document.getElementById('output');
    const hermit = document.getElementById('hermit-banner');

    let activeSource = null;

    form.addEventListener('submit', function(e) {
      e.preventDefault();
      const problem = form.problem.value.trim();
      if (!problem) return;

      // Reset UI
      if (activeSource) { activeSource.close(); activeSource = null; }
      output.innerHTML = '';
      hermit.style.display = 'none';
      status.textContent = '⟳ Connecting to Lattice…';
      btn.disabled = true;

      const es = new EventSource('/run?problem=' + encodeURIComponent(problem));
      activeSource = es;

      es.addEventListener('hermit_awakening', function() {
        hermit.style.display = 'block';
      });

      es.addEventListener('phase_start', function(e) {
        const data = JSON.parse(e.data);
        status.textContent = '⟳ ' + data.label;

        const card = document.createElement('div');
        card.className = 'phase agent-' + data.agent_id + ' active';
        card.id = 'phase-' + data.agent_id;
        card.innerHTML =
          '<div class="phase-header">[' + data.label + ']</div>' +
          '<div class="phase-body phase-thinking">Reasoning…</div>';
        output.appendChild(card);
        card.scrollIntoView({ behavior: 'smooth', block: 'start' });
      });

      es.addEventListener('phase_output', function(e) {
        const data = JSON.parse(e.data);
        const card = document.getElementById('phase-' + data.agent_id);
        if (card) {
          card.classList.remove('active');
          const body = card.querySelector('.phase-body');
          body.classList.remove('phase-thinking');
          body.textContent = data.output;
        }
      });

      es.addEventListener('done', function() {
        status.textContent = '✓ Pipeline complete.';
        btn.disabled = false;
        es.close();
        activeSource = null;
      });

      es.onerror = function() {
        status.textContent = '⚠ Connection error — check API key / rate limits, then retry.';
        btn.disabled = false;
        es.close();
        activeSource = null;
      };
    });
  </script>

</body>
</html>
"""

# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------

@app.get("/")
async def index():
    return HTMLResponse(HTML)


@app.get("/run")
async def run_pipeline(problem: str, request: Request):
    """
    SSE endpoint: streams DJC³ phases as they complete.
    Events emitted:
      hermit_awakening  — fired once if inactivity > 5 min
      phase_start       — { agent_id, label }
      phase_output      — { agent_id, label, output }
      done              — pipeline finished
    """
    last_time = get_last_session_time()
    hermit_mode = last_time > 0 and (time.time() - last_time) > HERMIT_THRESHOLD

    session_id = str(uuid.uuid4())
    save_session(session_id, problem, hermit_mode)

    async def generator():
        if await request.is_disconnected():
            return

        if hermit_mode:
            yield {"event": "hermit_awakening", "data": "HERMIT_AWAKENING"}

        pipeline = DJC3Pipeline(problem, session_id=session_id, hermit_mode=hermit_mode)
        loop = asyncio.get_running_loop()

        for agent_id, label, prompt_template in PHASE_DEFS:
            if await request.is_disconnected():
                return

            # Dimitri's prompt is the only one with a {problem_space} placeholder
            prompt = (
                prompt_template.format(problem_space=problem)
                if agent_id == "dimitri"
                else prompt_template
            )

            yield {
                "event": "phase_start",
                "data": json.dumps({"agent_id": agent_id, "label": label}),
            }

            # Run blocking API call in thread pool (keeps event loop free)
            output = await loop.run_in_executor(
                None, pipeline.run_phase, prompt, agent_id
            )

            yield {
                "event": "phase_output",
                "data": json.dumps({"agent_id": agent_id, "label": label, "output": output}),
            }

        yield {"event": "done", "data": "complete"}

    return EventSourceResponse(generator())
