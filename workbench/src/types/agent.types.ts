export type AgentMode =
  | 'DIVERGENT'    // Elle Cinco — open exploration, superposition hold
  | 'SCOUT'        // Stowie Way — edge-finding, hidden dependencies
  | 'ADVERSARIAL'  // Tyger Lyon — stress-test, break before shipping
  | 'STRUCTURAL'   // Kiefer — constraints enforced, deterministic output
  | 'SYNTHETIC'    // ChiKa — pattern bridges, inductive synthesis
  | 'OPERATIONAL'  // John Knight — execution steps, zero vagueness

export interface AgentPersona {
  id: string
  name: string
  title: string
  mode: AgentMode
  svgDataUri: string
  /** Prepended to system prompt in Phase 3 LLM generation */
  systemPromptModifier: string
  /** Token bands this persona prefers (highlighted in library) */
  preferredBandIds: string[]
  /** Multiplier on lattice collapse cost — affects plan order + cost */
  sentimentMod: number
  /** UI accent color */
  color: string
}
