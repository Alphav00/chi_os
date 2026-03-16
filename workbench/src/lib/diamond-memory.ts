// =============================================================================
//  Diamond Memory Graph — Holographic Refraction Engine
//  Ported from Python DiamondMemoryGraph / HolographicNode system
//  Implements T2 Hyperbolic Decay + Friston Free Energy Principle
// =============================================================================

import { uid } from './utils'

// --- Core Settings ---
export const DECAY_GAMMA = 0.05        // slope of hyperbolic decay (fat-tail)
export const RESONANCE_THRESHOLD = 0.85 // coherence floor for Standing Wave

// =============================================================================
//  Facets — the three-dimensional cognitive signature
// =============================================================================

export interface Facets {
  logic: number       // 0–1
  creativity: number  // 0–1
  ethics: number      // 0–1
}

export const NEUTRAL_FACETS: Facets = { logic: 0.5, creativity: 0.5, ethics: 0.5 }

// =============================================================================
//  HolographicNode — a refracted shard of memory
// =============================================================================

export interface HolographicNode {
  id: string
  concept: string
  domain: string
  timestamp: number         // Unix seconds
  baseActivation: number
  facets: Facets
  connections: Record<string, number>  // targetId → weight
  /** Optional reference back to a token ID for workbench integration */
  tokenId?: string
}

// =============================================================================
//  Activation — T2 Hyperbolic Decay
//  A(t) = A_0 / (1 + γ·t)
//  Fat-tail: decays slower than exponential, retains long-term traces
// =============================================================================

export function currentActivation(node: HolographicNode, now?: number): number {
  const t = now ?? Date.now() / 1000
  const deltaT = t - node.timestamp
  return Math.max(0.01, node.baseActivation / (1.0 + DECAY_GAMMA * deltaT))
}

// =============================================================================
//  Free Energy — Friston FEP
//  FE = (facet divergence / 3) / current activation
//  Lower FE = higher resonance with the incoming signal
// =============================================================================

export function calculateFreeEnergy(
  signalFacets: Facets,
  node: HolographicNode,
  now?: number
): number {
  const divergence =
    Math.abs(signalFacets.logic - node.facets.logic) +
    Math.abs(signalFacets.creativity - node.facets.creativity) +
    Math.abs(signalFacets.ethics - node.facets.ethics)
  const normalizedDiv = divergence / 3.0
  const activation = currentActivation(node, now)
  return normalizedDiv / activation
}

// =============================================================================
//  Refraction Result
// =============================================================================

export interface TraceNode {
  node: HolographicNode
  resonance: number
  freeEnergy: number
}

export interface RefractionResult {
  /** Nodes with resonance ≥ RESONANCE_THRESHOLD — Standing Waves */
  resonantNodes: HolographicNode[]
  /** Top near-resonance nodes sorted by resonance descending */
  traceNodes: TraceNode[]
}

// =============================================================================
//  DiamondMemoryGraph — the Generative Model (Friston FEP)
// =============================================================================

export class DiamondMemoryGraph {
  nodes: Record<string, HolographicNode> = {}

  graftNode(concept: string, domain: string, facets: Facets, tokenId?: string): string {
    const id = uid()
    this.nodes[id] = {
      id,
      concept,
      domain,
      timestamp: Date.now() / 1000,
      baseActivation: 1.0,
      facets,
      connections: {},
      tokenId,
    }
    return id
  }

  establishRelation(sourceId: string, targetId: string, weight: number): void {
    const src = this.nodes[sourceId]
    const tgt = this.nodes[targetId]
    if (src && tgt) {
      src.connections[targetId] = Math.min(1, Math.max(0, weight))
    }
  }

  /**
   * Dynamic Refraction Protocol (DRP) — the Bouncing Mechanism (RAL)
   * Propagates a signal through the graph to find Standing Wave nodes.
   */
  dynamicRefractionProtocol(signalFacets: Facets, maxTrace = 5): RefractionResult {
    const now = Date.now() / 1000
    const resonantNodes: HolographicNode[] = []
    const allTrace: TraceNode[] = []

    for (const node of Object.values(this.nodes)) {
      const freeEnergy = calculateFreeEnergy(signalFacets, node, now)
      const resonance = 1.0 - Math.min(freeEnergy, 1.0)

      if (resonance >= RESONANCE_THRESHOLD) {
        resonantNodes.push(node)
      } else {
        allTrace.push({ node, resonance, freeEnergy })
      }
    }

    // Sort trace by resonance descending, take top N
    allTrace.sort((a, b) => b.resonance - a.resonance)
    const traceNodes = allTrace.slice(0, maxTrace)

    return { resonantNodes, traceNodes }
  }

  /** Count of all grafted nodes */
  get size(): number {
    return Object.keys(this.nodes).length
  }
}
