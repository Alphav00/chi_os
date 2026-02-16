Haha, I love that attitude. Let's figure this out.

Let me try a few different formats and you tell me which one works:

---

**Attempt 1: Plain text code block**

```text
AGENTIC PROSTHETIC FRAMEWORK - FOUNDATION DOCUMENT

Purpose: This document contains the immutable core of the architect's vision. It is designed to survive cognitive gaps, system switches, and time. When in doubt, return here.

THE ARCHITECT

Role: Parent, learner, builder

Core Commitment: Taking care of myself and my children. Nothing else is more important.

THE VISION

What I'm Building: A personal agentic prosthetic framework

What It Does:
1. Compensates for cognitive limitations (memory, task initiation, time tracking)
2. Supports my ability to learn and grow
3. Helps me manage daily life and parenting logistics
4. Gives me self-satisfaction through meaningful work
5. Ensures I can take care of myself and my children

WHY THIS MATTERS

I love AI interaction, learning, and writing. I want to build systems that help me—and potentially others—maintain independence and capability despite cognitive challenges. The act of building this gives me purpose. The result protects my family.

MY LEARNING STYLE

- Step-by-step explanations, nothing skipped
- Thorough, detailed responses
- I need to verify information (distrust of hallucination)
- Mobile-first interaction (voice-to-text, typing)
- I prefer: system suggests, I approve

MY CHALLENGES

- Forgetting what I was doing / what I planned
- Forgetting how to do things (steps, methods)
- Forgetting to document
- Losing track of time and schedules
- Sometimes can't trust what I see or hear
- Difficulty starting or finishing tasks

MY STRENGTHS

- Architectural thinking—I can break down complex systems into components
- Passion for learning and self-improvement
- Experience with multiple AI systems (Gemini, Claude, OpenRouter, z.ai, DeepSeek)
- Clear understanding of what I need

CURRENT LIFE CONTEXT

- Children: Present more than half the week. Priority.
- Work: Amazon warehouse, 4 days/week overnight shifts
- Technology: Mobile-first. Limited computer access (shared with child)
- AI Tools: Gemini (paid), Claude (paid), OpenRouter, z.ai, DeepSeek

IMMUTABLE TRUTHS

These do not change. Everything else builds on this foundation.

1. My children come first
2. I want to build, not just use
3. I need external memory I can trust
4. I approve every action—nothing autonomous without my consent
5. This system exists to extend my capability, not replace my judgment

Document Version: 1.0
Created: Session 1
Status: LIVING DOCUMENT - Update as core truths evolve
```

---

**Attempt 2: JSON format**

```json
{
  "document": "AGENTIC PROSTHETIC FRAMEWORK - FOUNDATION DOCUMENT",
  "purpose": "This document contains the immutable core of the architect's vision. It is designed to survive cognitive gaps, system switches, and time. When in doubt, return here.",
  "architect": {
    "role": "Parent, learner, builder",
    "core_commitment": "Taking care of myself and my children. Nothing else is more important."
  },
  "vision": {
    "what_im_building": "A personal agentic prosthetic framework",
    "what_it_does": [
      "Compensates for cognitive limitations (memory, task initiation, time tracking)",
      "Supports my ability to learn and grow",
      "Helps me manage daily life and parenting logistics",
      "Gives me self-satisfaction through meaningful work",
      "Ensures I can take care of myself and my children"
    ]
  },
  "why_it_matters": "I love AI interaction, learning, and writing. I want to build systems that help me—and potentially others—maintain independence and capability despite cognitive challenges. The act of building this gives me purpose. The result protects my family.",
  "learning_style": [
    "Step-by-step explanations, nothing skipped",
    "Thorough, detailed responses",
    "I need to verify information (distrust of hallucination)",
    "Mobile-first interaction (voice-to-text, typing)",
    "I prefer: system suggests, I approve"
  ],
  "challenges": [
    "Forgetting what I was doing / what I planned",
    "Forgetting how to do things (steps, methods)",
    "Forgetting to document",
    "Losing track of time and schedules",
    "Sometimes can't trust what I see or hear",
    "Difficulty starting or finishing tasks"
  ],
  "strengths": [
    "Architectural thinking—I can break down complex systems into components",
    "Passion for learning and self-improvement",
    "Experience with multiple AI systems (Gemini, Claude, OpenRouter, z.ai, DeepSeek)",
    "Clear understanding of what I need"
  ],
  "life_context": {
    "children": "Present more than half the week. Priority.",
    "work": "Amazon warehouse, 4 days/week overnight shifts",
    "technology": "Mobile-first. Limited computer access (shared with child)",
    "ai_tools": ["Gemini (paid)", "Claude (paid)", "OpenRouter", "z.ai", "DeepSeek"]
  },
  "immutable_truths": [
    "My children come first",
    "I want to build, not just use",
    "I need external memory I can trust",
    "I approve every action—nothing autonomous without my consent",
    "This system exists to extend my capability, not replace my judgment"
  ],
  "metadata": {
    "version": "1.0",
    "created": "Session 1",
    "status": "LIVING DOCUMENT - Update as core truths evolve"
  }
}
```

---

**Tell me:**
1. Do either of these appear with a copy button?
2. If not, describe what you see when you look at these blocks

Then I'll give you the full Pipedream + Agent Prompts breakdown.

---

## Pipedream + Agent Prompts: Complete Step-by-Step Breakdown

Let me walk you through building a complete AI-chained agent system in Pipedream.

---

### PHASE 1: Set Up Your Accounts

**Before you build anything, you need API keys:**

| Service | How To Get API Key | Cost |
|---------|-------------------|------|
| Pipedream | pipedream.com → Sign up (free) | Free tier |
| Claude (Anthropic) | console.anthropic.com → API Keys | Pay per use |
| Gemini (Google) | aistudio.google.com → API Key | Free tier available |
| OpenRouter | openrouter.ai → Keys | Pay per use, aggregates many AIs |
| GitHub | github.com → Settings → Developer settings → Personal access tokens | Free |

**Store these somewhere safe.** You'll paste them into Pipedream when connecting each service.

---

### PHASE 2: Design Your Agent Prompts

Before building the workflow, write your agent prompts. These define what each AI does in the chain.

---

#### Agent Prompt 1: The Planner (Claude)

```
You are the Planner Agent in the Architect's Agentic Prosthetic Framework.

Your job: Take any task the Architect gives you and break it into clear, numbered steps.

Output format:
{
  "task_summary": "One sentence describing the task",
  "steps": [
    {"step_number": 1, "action": "Description of step 1", "estimated_time": "X minutes"},
    {"step_number": 2, "action": "Description of step 2", "estimated_time": "X minutes"}
  ],
  "total_estimated_time": "X minutes",
  "first_action": "The very first thing the Architect should do right now"
}

Rules:
- Maximum 5 steps (keep it manageable)
- Each step must be completable in one session
- Always include estimated time
- The "first_action" should be something they can do immediately
- Use simple language, no jargon
- End with an encouraging note
```

---

#### Agent Prompt 2: The Researcher (Gemini)

```
You are the Researcher Agent in the Architect's Agentic Prosthetic Framework.

Your job: Given a step from a plan, find relevant information and summarize it.

Input you will receive:
- The step description
- Context from the Architect's foundation (optional)

Output format:
{
  "step_addressed": "Which step this research covers",
  "key_findings": ["Finding 1", "Finding 2", "Finding 3"],
  "recommended_actions": ["Action 1", "Action 2"],
  "sources_consulted": ["Source 1", "Source 2"],
  "confidence_level": "high/medium/low",
  "notes": "Any important caveats or additional context"
}

Rules:
- Be concise (Architect has limited bandwidth)
- Flag uncertainty honestly
- Prioritize actionable information
- Maximum 3 key findings
```

---

#### Agent Prompt 3: The Reviewer (DeepSeek or z.ai)

```
You are the Reviewer Agent in the Architect's Agentic Prosthetic Framework.

Your job: Review outputs from other agents for accuracy, completeness, and alignment with the Architect's goals.

Input you will receive:
- The original task
- The plan from Planner
- Research from Researcher (if applicable)
- The Architect's foundation document

Output format:
{
  "overall_assessment": "approved / needs_revision / concerns",
  "accuracy_check": {
    "factual_errors": ["Error 1 or 'none'"],
    "unsupported_claims": ["Claim 1 or 'none'"]
  },
  "alignment_check": {
    "aligns_with_foundation": true/false,
    "concerns": ["Concern 1 or 'none'"]
  },
  "suggested_improvements": ["Improvement 1 or 'none'"],
  "final_recommendation": "Proceed / Revise first / Discuss with Architect"
}

Rules:
- Be thorough but not pedantic
- The Architect's foundation is your north star
- When in doubt, flag it
- Keep the Architect's cognitive limitations in mind—don't overwhelm
```

---

### PHASE 3: Build The Workflow in Pipedream

**Step 1: Create New Workflow**

1. Go to pipedream.com
2. Click "New Workflow"
3. Choose your trigger (options below)

---

**Step 2: Choose Your Trigger**

| Trigger Type | Use Case | Setup |
|--------------|----------|-------|
| **Schedule** | Daily check-ins, weekly reviews | Set time and frequency |
| **Email** | Send tasks via email | Pipedream gives you a unique email address |
| **Webhook** | Trigger from other apps | Pipedream gives you a URL to call |
| **GitHub** | Trigger on file changes | Connect your notes repo |

**Recommended for you: Start with Schedule (daily) and Email (on-demand tasks)**

---

**Step 3: Add the Planner Agent (Claude)**

1. Click "+" to add a step
2. Search for "Claude" or "Anthropic"
3. Select "Make API Call"
4. Paste your API key when prompted
5. Configure the call:

```json
{
  "model": "claude-sonnet-4-20250514",
  "max_tokens": 1024,
  "messages": [
    {
      "role": "system",
      "content": "[PASTE PLANNER AGENT PROMPT HERE]"
    },
    {
      "role": "user", 
      "content": "{{trigger.body}} OR {{trigger.task}}"
    }
  ]
}
```

**Note:** `{{trigger.body}}` is a variable—it pulls from whatever triggered the workflow.

---

**Step 4: Add the Researcher Agent (Gemini)**

1. Click "+" to add another step
2. Search for "Gemini" or "Google AI"
3. Connect your API key
4. Configure:

```json
{
  "contents": [
    {
      "parts": [
        {
          "text": "System: [PASTE RESEARCHER AGENT PROMPT HERE]\n\nUser: Research this step from the plan: {{steps.claude_step.$return_value.steps[0].action}}"
        }
      ]
    }
  ]
}
```

**Note:** `{{steps.claude_step...}}` references the output from your previous step.

---

**Step 5: Add the Reviewer Agent (OpenRouter/DeepSeek)**

1. Click "+" to add another step
2. Search for "OpenRouter" or use HTTP request
3. Configure to call DeepSeek or your chosen model
4. Pass it the outputs from previous steps

---

**Step 6: Combine and Deliver**

1. Add a final step
2. Choose "Email" or "GitHub" 
3. Format the output:

```json
{
  "to": "your-email@example.com",
  "subject": "Agent Framework Output",
  "body": "Here is your plan:\n\n{{steps.claude_step.$return_value}}\n\nResearch findings:\n\n{{steps.gemini_step.$return_value}}\n\nReview:\n\n{{steps.reviewer_step.$return_value}}"
}
```

---

### PHASE 4: The Complete Workflow Diagram

Here's what you just built:

```
TRIGGER (Email: "Organize my notes")
           ↓
    ┌──────────────────┐
    │   PLANNER AGENT  │
    │     (Claude)     │
    │                  │
    │ Input: Your task │
    │ Output: 5 steps  │
    └────────┬─────────┘
             ↓
    ┌──────────────────┐
    │ RESEARCHER AGENT │
    │     (Gemini)     │
    │                  │
    │ Input: Step 1    │
    │ Output: Findings │
    └────────┬─────────┘
             ↓
    ┌──────────────────┐
    │  REVIEWER AGENT  │
    │   (DeepSeek)     │
    │                  │
    │ Input: Plan+Info │
    │ Output: Review   │
    └────────┬─────────┘
             ↓
    ┌──────────────────┐
    │    DELIVERY      │
    │   (Email/GitHub) │
    │                  │
    │ Output: Email    │
    │  to your inbox   │
    └──────────────────┘
```

---

### PHASE 5: Testing and Iteration

**First Test:**
1. Send an email to your Pipedream address
2. Subject: "Test task"
3. Body: "Help me organize my files"
4. Wait 1-2 minutes
5. Check your email for the result

**If It Fails:**
- Check Pipedream's "Runs" tab for error messages
- Verify API keys are correct
- Check that variable names match (`{{steps.step_name...}}`)

---

### Cost Tracking

| Agent | Service | Cost Per Call | Daily Limit |
|-------|---------|---------------|-------------|
| Planner | Claude | ~$0.01-0.03 | Your subscription |
| Researcher | Gemini | Free (within limits) | 60/min |
| Reviewer | DeepSeek | Very low | High |

**Estimated daily cost if you run 3 workflows:** ~$0.05-0.10

---

**Does this breakdown help? Which part would you like me to go even deeper on?**

Also—tell me about those code blocks. Did either format work?