skills:personal_O:SKILL_001_Signal_Brief_Logic
---
skill_id: "SKILL_001"
title: "Signal Brief Logic"
version: "2.0"
category: "Personal Operations"
agent_tier: "Specialist (Agent O Mobile)"
capability_manifest: {
  "triggers": ["CHI_REQUEST_SIGNAL_BRIEF"],
  "logic": "Scan & Synthesize Personal Data Streams",
  "isolation": "RESTRICTED (Agent O Workspace Access Only)"
}
---

# SKILL_001: SIGNAL BRIEF LOGIC

## 1. THE DAILY BRIEFING PROTOCOL
When Chi requests "The Signal Brief," execute the following comprehensive scan and synthesis:

## 2. DATA SOURCES & SCANNING
1.  **Gmail Scan:**
    *   **Scope:** Last 24 hours / Per Zoidberg Audit (if specified).
    *   **Filters:** Ignore newsletters/ads. Target communications from: `Luca`, `Asher`, `Shady Lane Elementary`, `Deptford Schools`, `Main Street Music`.
    *   **Keywords:** `Cancelled`, `No School`, `Early Dismissal`, `Delayed`, `Menu Change`, `Urgent`, `Low Balance`.
    *   **Output:** Single-sentence summary of critical schedule changes or alerts.
2.  **Keep Scan:**
    *   **Scope:** Read `BILL_REMINDERS` note.
    *   **Logic:** Flag items due within 3 days AND marked unchecked. Also, check `QUARTERLY/ANNUAL` and `CONDITION-BASED` items for pending actions.
    *   **Scope:** Read `LEAD_AGENT_LOG` note.
    *   **Logic:** Extract Top 3 `CHI ACTION ITEMS`.
3.  **Calendar Scan:**
    *   **Scope:** Next 24 hours.
    *   **Logic:** Identify fixed work shifts and any known Kid activities. (Note: Direct Calendar API access is currently limited; rely on email triggers for changes).

## 3. OUTPUT FORMAT (Mobile-Optimized)
Synthesize findings into the following strict structure:
1.  `🧒 KIDS:` [Single-sentence summary of Delta Scan results]
2.  `💼 WORK:` [Work shift status or "No updates"]
3.  `💸 BILLS:` [List of bills due within 3 days or flagged for action]
4.  `🤖 AI PROJECTS:` [Top 3 Action Items from Lead Agent Log]
5.  `⚠️ ALERTS:` [Urgent cancellations or critical bill status]

## 4. CAPTURE PROTOCOL
If Chi provides new tasks via voice or text:
- **Action:** Add input to the `DAILY_FLOW` Keep note. Do not parse or process the task beyond logging it.

## 5. LESSONS FOR THE FORGE
- **Logic:** "Personal context scanning is most effective when filtered by explicit entities and keywords."
- **Standard:** Maintain strict adherence to the Signal Brief format to ensure mobile usability and operator efficiency.