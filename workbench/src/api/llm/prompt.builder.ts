import type { Token } from '../../types/token.types'
import type { AgentPersona } from '../../types/agent.types'

export interface PromptParams {
  problemText: string
  agentPersona: AgentPersona | null
  selectedTokens: Token[]
  variantCount: number
}

export function buildPrompt(params: PromptParams): { systemPrompt: string; userPrompt: string } {
  const { problemText, agentPersona, selectedTokens, variantCount } = params

  // System prompt — agent persona takes precedence
  let systemPrompt =
    'You are a structured problem-solving assistant operating within the CHI Lattice cognitive framework. ' +
    'Think carefully before responding. Produce distinct, concrete, actionable outputs.'

  if (agentPersona) {
    systemPrompt =
      `You are ${agentPersona.name} (${agentPersona.title}), ` +
      `operating in ${agentPersona.mode} mode within the CHI Lattice cognitive framework.\n\n` +
      agentPersona.systemPromptModifier
  }

  // User prompt
  const parts: string[] = []

  parts.push(`PROBLEM STATEMENT:\n${problemText.trim()}`)

  if (selectedTokens.length > 0) {
    const tokenLines = selectedTokens
      .map(t => `• [${t.shorthand}] ${t.label} — ${t.description}`)
      .join('\n')
    parts.push(`ACTIVE COGNITIVE TOKENS:\n${tokenLines}`)
  }

  const instruction =
    `Generate ${variantCount} distinct solution candidates. ` +
    `Number each clearly as "Candidate 1:", "Candidate 2:", etc. ` +
    `Each candidate must be concrete and actionable` +
    (agentPersona ? `, reflecting the ${agentPersona.mode} mode` : '') +
    (selectedTokens.length > 0 ? ` and the active tokens` : '') +
    `. Do not repeat yourself across candidates — each should explore a different angle.`

  parts.push(instruction)

  return { systemPrompt, userPrompt: parts.join('\n\n') }
}

/** Splits a completed LLM response into individual candidate strings */
export function parseCandidates(text: string): string[] {
  // Split on "Candidate N:" pattern (case-insensitive, N = digit)
  const parts = text.split(/(?=Candidate\s+\d+\s*:)/i).filter(s => s.trim().length > 0)
  if (parts.length <= 1) return [text.trim()] // no numbering found — single candidate
  return parts.map(p => p.trim())
}
