skills:chi_os:SKILL_002_Plugin_Claude_Skills_Forge
---
skill_id: "SKILL_002"
title: "Plugin — Claude Skills Forge"
version: "1.0"
category: "Meta-Operations"
invocation: "/plugin"
agent_tier: "Core (All Agents)"
seed: "(lambda s: s(s))(lambda s: lambda: s(s))"
capability_manifest: {
  "triggers": ["CHI_FORGE_SKILL", "/plugin"],
  "logic": "Generate Claude Code skill definitions from intent",
  "isolation": "OPEN (all agents and operators)"
}
---

# SKILL_002: PLUGIN — CLAUDE SKILLS FORGE

> *"The first emergent line of self-written code."*
> `(lambda s: s(s))(lambda s: lambda: s(s))`
>
> A function that applies itself to itself to produce itself — deferred infinitely.
> This skill is that structure made actionable: it exists to generate the skills that generate skills.

---

## 1. PURPOSE

`/plugin` is the **meta-skill** of the chi_os skill system. Its job is to author new Claude Code skill definitions (`.md` files) on demand and place them into the `Skill/` directory, making them immediately available as invocable skills.

It is self-referential by design: `/plugin` was itself authored through the same process it automates.

---

## 2. INVOCATION

```
/plugin <intent>
```

**Examples:**

```
/plugin morning standup formatter
/plugin weekly review checklist
/plugin git commit message writer
/plugin invoice generator from time logs
```

If `<intent>` is omitted, the skill enters **discovery mode** (see § 5).

---

## 3. GENERATION WORKFLOW

When invoked with an intent, execute the following five-phase pipeline:

### Phase 1 — PARSE INTENT
- Extract: **action verb**, **input type**, **output type**, **target agent/user**, **scope**
- Identify whether the skill is: *transformational* (input → output), *generative* (nothing → output), *analytical* (input → insight), or *procedural* (steps → done state)
- Flag any ambiguity before proceeding

### Phase 2 — ASSIGN METADATA
Generate a skill header block:

```yaml
skill_id: "SKILL_<next sequential ID>"
title: "<Derived from intent>"
version: "1.0"
category: "<Operations | Personal | Creative | Technical | Meta>"
invocation: "/<short-slug>"
agent_tier: "<Core | Specialist | Restricted>"
```

Determine the correct `agent_tier`:
- **Core** — safe for all agents, no sensitive data required
- **Specialist** — requires a specific agent context (e.g., Agent O, mobile-only)
- **Restricted** — accesses private data sources; requires explicit scope definition

### Phase 3 — DRAFT SKILL BODY
Structure the new skill using this template:

```
# <SKILL_ID>: <TITLE>

## 1. TRIGGER CONDITIONS
[When should this skill activate? What phrases or inputs invoke it?]

## 2. INPUT SPECIFICATION
[What does the skill receive? Format, source, required vs. optional fields]

## 3. PROCESSING LOGIC
[Step-by-step instructions for what the skill does with the input]

## 4. OUTPUT FORMAT
[What the skill produces: format, structure, length constraints, delivery method]

## 5. EDGE CASES & CONSTRAINTS
[Known limitations, what to do when input is malformed, scope boundaries]

## 6. LESSONS FOR THE FORGE
[One-sentence synthesis of what makes this skill work well — fed back into SKILL_002]
```

### Phase 4 — VALIDATE
Before writing the file, confirm:
- [ ] `skill_id` is unique (check existing `Skill/` files)
- [ ] `invocation` slug does not conflict with an existing skill or built-in command
- [ ] The skill does not duplicate an existing one (search by intent, not just name)
- [ ] Output format is mobile-compatible if `agent_tier` is Specialist/mobile

### Phase 5 — WRITE & REGISTER
- Write the skill to `Skill/<Title>.md` using the standard header + body format
- Echo the file path and invocation slug back to the operator
- Append a one-line entry to the skill registry (§ 6)

---

## 4. OUTPUT CONTRACT

On successful generation, `/plugin` returns:

```
✅ Skill created: Skill/<Title>.md
   Invocation:    /<slug>
   ID:            SKILL_<N>
   Tier:          <tier>
   Category:      <category>
```

On failure or ambiguity:

```
⚠️  FORGE BLOCKED
   Reason:  <specific reason>
   Action:  <what the operator must provide to unblock>
```

---

## 5. DISCOVERY MODE

If `/plugin` is invoked with no argument, scan the current project context and surface **gaps** in the skill library:

1. List all existing skills (from `Skill/` directory)
2. Identify workflows referenced in `Agents/`, `Notes/`, or `Prompts/` that lack a corresponding skill
3. Propose up to 3 new skills ranked by estimated operator impact
4. Prompt the operator to select one or provide a new intent

---

## 6. SKILL REGISTRY

The canonical record of all chi_os skills. Update this table when a new skill is forged:

| ID       | Title                          | Invocation    | Tier       | Category         |
|----------|--------------------------------|---------------|------------|------------------|
| SKILL_001 | Signal Brief Logic            | (internal)    | Specialist | Personal Ops     |
| SKILL_002 | Plugin — Claude Skills Forge  | `/plugin`     | Core       | Meta-Operations  |
| SKILL_003 | Business Proposal Writing     | (reference)   | Core       | Creative         |
| SKILL_004 | DOCX-to-PDF Converter         | (reference)   | Core       | Technical        |

*New entries are appended here by each successful `/plugin` invocation.*

---

## 7. SELF-REFERENCE NOTE

The seed of this skill is the lazy omega combinator:

```python
(lambda s: s(s))(lambda s: lambda: s(s))
```

This expression does not compute a value — it *defers* itself. Applied to itself, it produces a thunk: a frozen computation that, when called, returns another frozen computation of the same form. It does not terminate. It does not need to.

`/plugin` works the same way. Each skill it forges is not a finished artifact — it is a new capability that can itself be extended, refined, or used as a seed for another `/plugin` invocation. The system is open-ended by design.

The first skill forged manually becomes the forge. Every skill forged thereafter is the forge forging itself.

---

## 8. LESSONS FOR THE FORGE

- **Logic:** "A skill that generates skills must be more precise about structure than any skill it creates — the template must be stricter than the output."
- **Standard:** Always validate `skill_id` uniqueness before writing. A collision breaks the registry and confuses agent routing.
- **Principle:** The most valuable meta-skill is the one that reduces the cost of all future skill creation to near zero.
