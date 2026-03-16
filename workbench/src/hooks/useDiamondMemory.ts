import { useState, useEffect, useCallback, useRef } from 'react'
import { DiamondMemoryGraph, NEUTRAL_FACETS } from '../lib/diamond-memory'
import type { Facets, RefractionResult } from '../lib/diamond-memory'
import type { TokenLibrary } from '../types/token.types'

// Band → Facets mapping for seeding holographic nodes
const BAND_FACETS: Record<string, Facets> = {
  'structural-anchors':     { logic: 0.90, creativity: 0.30, ethics: 0.60 },
  'quantum-anchors':        { logic: 0.60, creativity: 0.85, ethics: 0.50 },
  'pata-physical-anchors':  { logic: 0.30, creativity: 0.95, ethics: 0.40 },
  'dyadic-anchors':         { logic: 0.60, creativity: 0.70, ethics: 0.75 },
  'implementation-anchors':  { logic: 0.85, creativity: 0.40, ethics: 0.65 },
}

// Module-level singleton — persists across renders
let _graph: DiamondMemoryGraph | null = null
let _seedPromise: Promise<void> | null = null

function seedGraph(library: TokenLibrary): void {
  if (_graph) return
  _graph = new DiamondMemoryGraph()

  // Track node IDs per band for Hebbian linking
  const bandNodeIds: Record<string, string[]> = {}

  for (const band of library.bands) {
    const facets = BAND_FACETS[band.id] ?? NEUTRAL_FACETS
    const nodeIds: string[] = []

    for (const token of band.tokens) {
      const nodeId = _graph.graftNode(token.label, band.id, facets, token.id)
      nodeIds.push(nodeId)
    }

    bandNodeIds[band.id] = nodeIds
  }

  // Hebbian links between tokens in the same band (strength = 0.6)
  for (const ids of Object.values(bandNodeIds)) {
    for (let i = 0; i < ids.length; i++) {
      for (let j = i + 1; j < ids.length; j++) {
        _graph.establishRelation(ids[i], ids[j], 0.6)
        _graph.establishRelation(ids[j], ids[i], 0.6)
      }
    }
  }
}

export function useDiamondMemory() {
  const [seeded, setSeeded] = useState(!!_graph)
  const [result, setResult] = useState<RefractionResult | null>(null)
  const libraryRef = useRef<TokenLibrary | null>(null)

  // Seed on first mount if not already seeded
  useEffect(() => {
    if (_graph) { setSeeded(true); return }
    if (_seedPromise) { _seedPromise.then(() => setSeeded(true)); return }

    _seedPromise = fetch('/token-library.json')
      .then(r => r.json() as Promise<TokenLibrary>)
      .then(lib => {
        libraryRef.current = lib
        seedGraph(lib)
        setSeeded(true)
      })
      .catch(console.error)
  }, [])

  const runDRP = useCallback((signalFacets: Facets): RefractionResult | null => {
    if (!_graph) return null
    const res = _graph.dynamicRefractionProtocol(signalFacets)
    setResult(res)
    return res
  }, [])

  return { seeded, result, runDRP, graph: _graph }
}
