// =============================================================================
//  Lattice Engine — Collapse Verifier Module
//  Ported from the HTML prototype (CHI: Lattice Project Forge)
//  Implements quantum-inspired node fidelity decay + energy-aware scheduling
// =============================================================================

export enum NodeState {
  SUP = 'SUP', // superposition — active, decaying
  COL = 'COL', // collapsed — resolved, done
  PHA = 'PHA', // phantom — fidelity below threshold, rescue needed
  DEC = 'DEC', // decohered — lost, unrecoverable
  GHO = 'GHO', // ghost — primed by Hebbian link from collapsed neighbour
}

export const PHANTOM_THRESHOLD = 0.30
export const DECOHERE_THRESHOLD = 0.05
export const MIN_FIDELITY = 0.01
export const COST_CAP_MULT = 5.0
export const ENERGY_RECOVERY = 3.0

export type UrgencyLevel = 'CRITICAL' | 'HIGH' | 'ELEVATED' | 'NORMAL' | 'NONE' | 'LOST'

export interface CollapseResult {
  success: boolean
  reason?: string
  cost: number
  recovery: number
  net: number
  fidelity: number
}

export interface SuggestResult {
  recommendation: string | null
  name: string
  urgency: UrgencyLevel
  score: number
  fidelity: number
  currentCost: number
  energyDelta: number
  reasoning: string
}

// =============================================================================
//  LatticeNode
// =============================================================================

export class LatticeNode {
  id: string
  name: string
  complexity: number
  status: NodeState
  createdAt: number   // Unix seconds
  fidelity: number
  links: Record<string, number>   // targetId → strength [0,1]
  collapsedAt: number | null

  constructor(name: string, complexity = 1.0, createdAt: number | null = null) {
    this.id = 'n' + Math.random().toString(36).substring(2, 9)
    this.name = name
    this.complexity = complexity
    this.status = NodeState.SUP
    this.createdAt = createdAt ?? Date.now() / 1000
    this.fidelity = 1.0
    this.links = {}
    this.collapsedAt = null
  }

  updateFidelity(now: number | null = null): number {
    const t = now ?? Date.now() / 1000
    if (
      this.status === NodeState.SUP ||
      this.status === NodeState.PHA ||
      this.status === NodeState.GHO
    ) {
      const elapsed = t - this.createdAt
      const tau = 120.0 / this.complexity
      this.fidelity = Math.max(MIN_FIDELITY, Math.exp(-elapsed / tau))

      if (this.fidelity < DECOHERE_THRESHOLD) {
        this.status = NodeState.DEC
      } else if (this.fidelity < PHANTOM_THRESHOLD && this.status !== NodeState.GHO) {
        this.status = NodeState.PHA
      }
    }
    return this.fidelity
  }

  timeToPhantom(now: number | null = null): number | null {
    if (this.status !== NodeState.SUP && this.status !== NodeState.GHO) return null
    const t = now ?? Date.now() / 1000
    const tau = 120.0 / this.complexity
    const tCross = -tau * Math.log(PHANTOM_THRESHOLD)
    const elapsed = t - this.createdAt
    return Math.max(0, tCross - elapsed)
  }

  timeToDecohere(now: number | null = null): number {
    if (this.status === NodeState.DEC) return 0
    const t = now ?? Date.now() / 1000
    const tau = 120.0 / this.complexity
    const tCross = -tau * Math.log(DECOHERE_THRESHOLD)
    const elapsed = t - this.createdAt
    return Math.max(0, tCross - elapsed)
  }

  collapseCost(fidelity: number | null = null, sentimentMod = 1.0): number {
    const q = fidelity !== null ? fidelity : this.fidelity
    const raw = (this.complexity * 10.0 / q) * sentimentMod
    const cap = this.complexity * 10.0 * COST_CAP_MULT
    return Math.min(raw, cap)
  }

  futureCost(secondsFromNow: number, now: number | null = null, sentimentMod = 1.0): number {
    const t = now ?? Date.now() / 1000
    const tau = 120.0 / this.complexity
    const elapsed = t - this.createdAt
    const futureQ = Math.max(MIN_FIDELITY, Math.exp(-(elapsed + secondsFromNow) / tau))
    return this.collapseCost(futureQ, sentimentMod)
  }
}

// =============================================================================
//  LatticeEngine
// =============================================================================

export class LatticeEngine {
  nodes: Record<string, LatticeNode>
  energy: number
  history: string[]   // ordered collapsed node IDs

  constructor() {
    this.nodes = {}
    this.energy = 0
    this.history = []
  }

  addNode(name: string, complexity: number, createdAt: number | null = null): string {
    const node = new LatticeNode(name, complexity, createdAt)
    this.nodes[node.id] = node
    this._recalcEnergy()
    return node.id
  }

  private _recalcEnergy(): void {
    const total = Object.values(this.nodes).reduce((s, n) => s + n.complexity, 0)
    this.energy = total * 15
  }

  addLink(srcId: string, dstId: string, strength = 0.5): boolean {
    if (!this.nodes[srcId] || !this.nodes[dstId]) return false
    this.nodes[srcId].links[dstId] = Math.min(1, Math.max(0, strength))
    return true
  }

  collapse(nodeId: string, sentimentMod = 1.0, now: number | null = null): CollapseResult {
    const node = this.nodes[nodeId]
    const zero = { success: false, cost: 0, recovery: 0, net: 0, fidelity: 0 }

    if (!node) return { ...zero, reason: 'Node not found' }
    if (node.status === NodeState.COL) return { ...zero, reason: 'Already collapsed' }
    if (node.status === NodeState.DEC) return { ...zero, reason: 'Decohered — lost' }

    const q = node.updateFidelity(now)
    const cost = node.collapseCost(q, sentimentMod)

    if (this.energy < cost) {
      return { ...zero, reason: `Insufficient energy (need ${cost.toFixed(1)}u, have ${this.energy.toFixed(1)}u)` }
    }

    node.status = NodeState.COL
    node.fidelity = q
    node.collapsedAt = now ?? Date.now() / 1000
    this.energy -= cost

    const recovery = node.complexity * ENERGY_RECOVERY
    this.energy += recovery
    this.history.push(nodeId)

    // Propagate GHOST state via Hebbian links
    for (const targetId in node.links) {
      const target = this.nodes[targetId]
      if (target && target.status === NodeState.SUP) {
        target.status = NodeState.GHO
      }
    }

    return { success: true, cost, recovery, net: cost - recovery, fidelity: q }
  }

  computeScore(nodeId: string, now: number | null = null): number {
    const node = this.nodes[nodeId]
    if (!node) return -1
    if (node.status === NodeState.COL || node.status === NodeState.DEC) return -1

    node.updateFidelity(now)
    const decayScore = (1 - node.fidelity) * node.complexity

    let linkBonus = 0
    if (this.history.length > 0) {
      const lastId = this.history[this.history.length - 1]
      const lastNode = this.nodes[lastId]
      if (lastNode && lastNode.links[nodeId] !== undefined) {
        linkBonus = lastNode.links[nodeId]
      }
    }

    return decayScore + linkBonus
  }

  suggest(now: number | null = null): SuggestResult {
    const t = now ?? Date.now() / 1000
    const noRec: SuggestResult = {
      recommendation: null,
      name: '',
      urgency: 'NONE',
      score: 0,
      fidelity: 0,
      currentCost: 0,
      energyDelta: 0,
      reasoning: 'All nodes resolved.',
    }

    const candidates = Object.values(this.nodes).filter(
      n => n.status !== NodeState.COL && n.status !== NodeState.DEC
    )
    if (candidates.length === 0) return noRec

    const scored = candidates
      .map(n => ({ id: n.id, score: this.computeScore(n.id, t) }))
      .sort((a, b) => b.score - a.score)

    const best = scored[0]
    const bestNode = this.nodes[best.id]

    const ttp = bestNode.timeToPhantom(t)
    const ttd = bestNode.timeToDecohere(t)
    const currentCost = bestNode.collapseCost()
    const futureCost = bestNode.futureCost(60, t)
    const energyDelta = futureCost - currentCost

    let urgency: UrgencyLevel = 'NORMAL'
    if (bestNode.status === NodeState.DEC) {
      urgency = 'LOST'
    } else if (
      bestNode.status === NodeState.PHA ||
      bestNode.status === NodeState.GHO ||
      (ttp !== null && ttp < 30)
    ) {
      urgency = 'CRITICAL'
    } else if (ttp !== null && ttp < 90) {
      urgency = 'HIGH'
    } else if (best.score > 1.0) {
      urgency = 'ELEVATED'
    }

    const parts: string[] = []
    parts.push(`Score ${best.score.toFixed(2)}`)
    if (ttp !== null && ttp < 60) parts.push(`PHANTOM in ${ttp.toFixed(0)}s`)
    if (ttd < 120) parts.push(`DECOHERED in ${ttd.toFixed(0)}s`)
    if (energyDelta > 5) parts.push(`Waiting 60s adds +${energyDelta.toFixed(1)}u`)
    if (bestNode.status === NodeState.GHO) parts.push('Primed by Hebbian path')

    return {
      recommendation: best.id,
      name: bestNode.name,
      urgency,
      score: best.score,
      fidelity: bestNode.fidelity,
      currentCost,
      energyDelta,
      reasoning: parts.join(' · '),
    }
  }

  /** Summary stats for display */
  stats() {
    const nodes = Object.values(this.nodes)
    return {
      total: nodes.length,
      collapsed: nodes.filter(n => n.status === NodeState.COL).length,
      active: nodes.filter(n => n.status === NodeState.SUP || n.status === NodeState.GHO).length,
      phantom: nodes.filter(n => n.status === NodeState.PHA).length,
      decohered: nodes.filter(n => n.status === NodeState.DEC).length,
      energy: this.energy,
    }
  }
}
