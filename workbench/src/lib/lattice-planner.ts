// =============================================================================
//  Lattice Planner
//  Converts raw unstructured text → Lattice nodes → simulated collapse plan
//  Completing the generatePlan() logic cut off in the HTML prototype
// =============================================================================

import { LatticeEngine } from './lattice-engine'
import type { UrgencyLevel } from './lattice-engine'

export interface PlanStep {
  step: number
  name: string
  urgency: UrgencyLevel
  score: number
  fidelity: number
  cost: number
  net: number
  recovery: number
  reasoning: string
}

export interface PlanResult {
  steps: PlanStep[]
  warnings: string[]
  energyRemaining: number
  totalCost: number
  totalNodes: number
}

export interface FrequencyToken {
  word: string
  count: number
}

export interface ConceptResult {
  frameworks: string[]
  tags: string[]
}

// =============================================================================
//  Text Analysis Utilities (ported from HTML prototype Analyze object)
// =============================================================================

const STOP_WORDS = new Set([
  'that', 'this', 'with', 'from', 'your', 'have', 'more', 'will', 'what',
  'about', 'which', 'when', 'there', 'their', 'they', 'than', 'then',
  'been', 'into', 'some', 'such', 'also', 'just', 'like', 'very',
])

export function mapFrequencies(text: string): FrequencyToken[] {
  const words = text
    .toLowerCase()
    .replace(/[^\w\s]/g, '')
    .split(/\s+/)
    .filter(w => w.length > 3 && !STOP_WORDS.has(w))

  const counts: Record<string, number> = {}
  words.forEach(w => { counts[w] = (counts[w] || 0) + 1 })

  return Object.entries(counts)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 30)
    .map(([word, count]) => ({ word, count }))
}

export function extractConcepts(text: string): ConceptResult {
  const conceptRegex = /([A-Z][a-z]+(?:\s+[A-Z][a-z]+)+)/g
  const frameworks = [...new Set(text.match(conceptRegex) ?? [])]

  const bracketRegex = /\[(.*?)\]|\((.*?)\)/g
  const tags: string[] = []
  let match: RegExpExecArray | null
  while ((match = bracketRegex.exec(text)) !== null) {
    const tag = match[1] ?? match[2]
    if (tag) tags.push(tag)
  }

  return { frameworks, tags: [...new Set(tags)] }
}

// =============================================================================
//  generatePlan — the completed version of the HTML prototype's cut-off function
// =============================================================================

export function generatePlan(text: string): PlanResult {
  const MAX_STEPS = 20

  // 1. Parse sentences
  const sentences = text.split(/[.!?]+/).filter(s => s.trim().length > 0)

  // 2. Extract candidate terms: capitalized phrases take priority over single words
  const phraseSet = new Set<string>()
  const conceptRegex = /([A-Z][a-z]+(?:\s+[A-Z][a-z]+)+)/g
  let match: RegExpExecArray | null
  while ((match = conceptRegex.exec(text)) !== null) phraseSet.add(match[0])

  const wordSet = new Set<string>(
    text
      .toLowerCase()
      .replace(/[^\w\s]/g, '')
      .split(/\s+/)
      .filter(w => w.length > 3 && !STOP_WORDS.has(w))
  )

  const phrases = Array.from(phraseSet)
  const words = Array.from(wordSet).filter(
    w => !phrases.some(p => p.toLowerCase().includes(w))
  )
  const allTerms = [...phrases, ...words]

  if (allTerms.length === 0) {
    return { steps: [], warnings: ['No significant terms found in input.'], energyRemaining: 0, totalCost: 0, totalNodes: 0 }
  }

  // 3. Build frequency map and first-occurrence index (by sentence index)
  const termFreq: Record<string, number> = {}
  const firstOccurrence: Record<string, number> = {}
  allTerms.forEach(t => { termFreq[t] = 0 })

  sentences.forEach((sent, idx) => {
    const lower = sent.toLowerCase()
    allTerms.forEach(term => {
      if (lower.includes(term.toLowerCase())) {
        termFreq[term] = (termFreq[term] || 0) + 1
        if (firstOccurrence[term] === undefined) firstOccurrence[term] = idx
      }
    })
  })

  const activeTerms = allTerms.filter(t => (termFreq[t] ?? 0) > 0)
  if (activeTerms.length === 0) {
    return { steps: [], warnings: ['No terms with frequency > 0.'], energyRemaining: 0, totalCost: 0, totalNodes: 0 }
  }

  // 4. Create nodes — complexity ∝ frequency; older first-occurrence = more aged
  const maxFreq = Math.max(...activeTerms.map(t => termFreq[t] ?? 1))
  const engine = new LatticeEngine()
  const now = Date.now() / 1000

  activeTerms.forEach(term => {
    const complexity = 1.0 + ((termFreq[term] ?? 1) / maxFreq) * 2.0
    // Each sentence index = ~10 seconds of age. Later sentences → younger nodes.
    const ageOffset = (sentences.length - (firstOccurrence[term] ?? 0)) * 10
    const createdAt = now - ageOffset
    engine.addNode(term, complexity, createdAt)
  })

  // 5. Build Hebbian links via co-occurrence within sentences
  const nodeMap: Record<string, string> = {}
  Object.values(engine.nodes).forEach(n => { nodeMap[n.name] = n.id })

  const cooc: Record<string, Record<string, number>> = {}
  activeTerms.forEach(t => { cooc[t] = {} })

  sentences.forEach(sent => {
    const lower = sent.toLowerCase()
    const present = activeTerms.filter(t => lower.includes(t.toLowerCase()))
    for (let i = 0; i < present.length; i++) {
      for (let j = i + 1; j < present.length; j++) {
        const a = present[i], b = present[j]
        cooc[a]![b] = (cooc[a]![b] ?? 0) + 1
        cooc[b]![a] = (cooc[b]![a] ?? 0) + 1
      }
    }
  })

  let maxCooc = 0
  activeTerms.forEach(a =>
    activeTerms.forEach(b => {
      const v = cooc[a]?.[b]
      if (v) maxCooc = Math.max(maxCooc, v)
    })
  )

  activeTerms.forEach(a => {
    activeTerms.forEach(b => {
      if (a !== b) {
        const v = cooc[a]?.[b]
        if (v) {
          const srcId = nodeMap[a]
          const dstId = nodeMap[b]
          if (srcId && dstId) {
            engine.addLink(srcId, dstId, v / (maxCooc || 1))
          }
        }
      }
    })
  })

  // 6. Run simulation — collapse nodes in priority order
  const steps: PlanStep[] = []
  const warnings: string[] = []
  let stepNum = 1
  let totalCost = 0

  while (stepNum <= MAX_STEPS) {
    const sug = engine.suggest(now)

    if (!sug.recommendation || sug.urgency === 'NONE') break

    const node = engine.nodes[sug.recommendation]
    const result = engine.collapse(sug.recommendation, 1.0, now)

    if (!result.success) {
      warnings.push(`Step ${stepNum}: Collapse failed for "${node?.name ?? sug.recommendation}" — ${result.reason}`)
      break
    }

    totalCost += result.cost

    steps.push({
      step: stepNum++,
      name: node.name,
      urgency: sug.urgency,
      score: sug.score,
      fidelity: result.fidelity,
      cost: result.cost,
      net: result.net,
      recovery: result.recovery,
      reasoning: sug.reasoning,
    })
  }

  return {
    steps,
    warnings,
    energyRemaining: engine.energy,
    totalCost,
    totalNodes: activeTerms.length,
  }
}
