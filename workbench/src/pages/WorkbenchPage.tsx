import { useState, useCallback } from 'react'
import { generatePlan, mapFrequencies, extractConcepts } from '../lib/lattice-planner'
import type { PlanResult, FrequencyToken, ConceptResult } from '../lib/lattice-planner'
import type { UrgencyLevel } from '../lib/lattice-engine'

// =============================================================================
//  Result Types
// =============================================================================

type ResultView =
  | { kind: 'idle' }
  | { kind: 'frequencies'; data: FrequencyToken[] }
  | { kind: 'concepts'; data: ConceptResult }
  | { kind: 'plan'; data: PlanResult }
  | { kind: 'error'; message: string }

// =============================================================================
//  Sub-components
// =============================================================================

const URGENCY_CLASS: Record<UrgencyLevel, string> = {
  CRITICAL: 'text-red-400',
  HIGH: 'text-amber-400',
  ELEVATED: 'text-yellow-300',
  NORMAL: 'text-green-400',
  NONE: 'text-gray-500',
  LOST: 'text-red-700',
}

function TokenPill({ word, count }: FrequencyToken) {
  return (
    <span className="inline-block bg-[#222] border border-[#444] px-2 py-0.5 rounded-full text-xs mr-1 mb-1 font-mono">
      {word} <span className="text-[#ef476f] font-bold">{count}</span>
    </span>
  )
}

function PlanStepCard({ step }: { step: PlanResult['steps'][number] }) {
  return (
    <div className="border-l-2 border-[#4cc9f0] pl-3 py-1 mb-3">
      <div className="flex items-center gap-2 mb-0.5">
        <span className="text-gray-600 text-xs">#{step.step}</span>
        <span className={`text-xs font-bold ${URGENCY_CLASS[step.urgency]}`}>
          [{step.urgency}]
        </span>
        <span className="text-sm font-bold text-[#e5e5e5]">{step.name}</span>
      </div>
      <div className="text-[11px] text-gray-400">{step.reasoning}</div>
      <div className="text-[10px] text-gray-600 mt-0.5">
        Cost {step.cost.toFixed(1)}u · Net {step.net.toFixed(1)}u · Fidelity {(step.fidelity * 100).toFixed(0)}%
      </div>
    </div>
  )
}

function ResultsPanel({ result }: { result: ResultView }) {
  if (result.kind === 'idle') {
    return (
      <div className="text-gray-600 text-xs text-center py-10">
        [ Awaiting Data ]
      </div>
    )
  }

  if (result.kind === 'error') {
    return (
      <div className="bg-[#171717] border-l-4 border-[#ef476f] p-3 text-sm text-red-400">
        {result.message}
      </div>
    )
  }

  if (result.kind === 'frequencies') {
    return (
      <div className="bg-[#171717] border-l-4 border-[#4cc9f0] p-3">
        <div className="text-[#4cc9f0] font-bold text-xs mb-2 tracking-wider">
          HIGH-FREQUENCY SEMANTIC TOKENS
        </div>
        <div>
          {result.data.map(t => (
            <TokenPill key={t.word} word={t.word} count={t.count} />
          ))}
        </div>
      </div>
    )
  }

  if (result.kind === 'concepts') {
    return (
      <div className="space-y-3">
        {result.data.frameworks.length > 0 && (
          <div className="bg-[#171717] border-l-4 border-[#4cc9f0] p-3">
            <div className="text-[#4cc9f0] font-bold text-xs mb-2 tracking-wider">
              CAPITALIZED THEORETICAL FRAMEWORKS
            </div>
            <ul className="text-sm space-y-0.5 list-disc list-inside text-[#e5e5e5]">
              {result.data.frameworks.map((f, i) => <li key={i}>{f}</li>)}
            </ul>
          </div>
        )}
        {result.data.tags.length > 0 && (
          <div className="bg-[#171717] border-l-4 border-[#8B5CF6] p-3">
            <div className="text-[#8B5CF6] font-bold text-xs mb-2 tracking-wider">
              BRACKETED CONTEXT / METADATA
            </div>
            <ul className="text-sm space-y-0.5 list-disc list-inside text-[#e5e5e5]">
              {result.data.tags.map((t, i) => <li key={i}>{t}</li>)}
            </ul>
          </div>
        )}
        {result.data.frameworks.length === 0 && result.data.tags.length === 0 && (
          <div className="text-gray-500 text-xs">No concepts or tags found.</div>
        )}
      </div>
    )
  }

  if (result.kind === 'plan') {
    const { steps, warnings, energyRemaining, totalCost, totalNodes } = result.data
    return (
      <div>
        <div className="bg-[#171717] border-l-4 border-[#ef476f] p-3 mb-3">
          <div className="text-[#ef476f] font-bold text-xs mb-1 tracking-wider">
            LATTICE PLAN CRYSTALLIZED
          </div>
          <div className="text-[10px] text-gray-500">
            {steps.length} steps · {totalNodes} nodes · Energy remaining: {energyRemaining.toFixed(1)}u · Total collapse cost: {totalCost.toFixed(1)}u
          </div>
        </div>

        {warnings.map((w, i) => (
          <div key={i} className="text-yellow-400 text-xs mb-2 pl-3 border-l-2 border-yellow-400">
            {w}
          </div>
        ))}

        {steps.length === 0 && (
          <div className="text-gray-500 text-xs pl-3">
            No steps generated. Try longer input with more repeated terms.
          </div>
        )}

        {steps.map(step => (
          <PlanStepCard key={step.step} step={step} />
        ))}
      </div>
    )
  }

  return null
}

// =============================================================================
//  WorkbenchPage
// =============================================================================

export function WorkbenchPage() {
  const [input, setInput] = useState('')
  const [result, setResult] = useState<ResultView>({ kind: 'idle' })

  const handleFrequencies = useCallback(() => {
    if (!input.trim()) return
    const data = mapFrequencies(input)
    setResult(data.length ? { kind: 'frequencies', data } : { kind: 'error', message: 'No significant words found (min 4 chars, not a stop word).' })
  }, [input])

  const handleConcepts = useCallback(() => {
    if (!input.trim()) return
    const data = extractConcepts(input)
    setResult({ kind: 'concepts', data })
  }, [input])

  const handlePlan = useCallback(() => {
    if (!input.trim()) return
    try {
      const data = generatePlan(input)
      setResult({ kind: 'plan', data })
    } catch (e) {
      setResult({ kind: 'error', message: String(e) })
    }
  }, [input])

  return (
    <div className="flex flex-col h-full p-4 gap-3">
      {/* Input */}
      <div className="flex flex-col gap-1">
        <label className="text-[10px] text-gray-500 tracking-widest uppercase">
          Data Substrate
        </label>
        <textarea
          value={input}
          onChange={e => setInput(e.target.value)}
          placeholder="Paste raw unstructured notes, logs, or essays here..."
          rows={8}
          className="w-full p-3 rounded bg-[#171717] text-[#4cc9f0] border border-[#333] focus:outline-none focus:border-[#4cc9f0] text-sm font-mono resize-none"
        />
        <div className="text-[10px] text-gray-600 text-right">
          {input.length} chars · {input.split(/\s+/).filter(Boolean).length} words
        </div>
      </div>

      {/* Action Buttons */}
      <div className="grid grid-cols-3 gap-2">
        <button
          onClick={handleFrequencies}
          disabled={!input.trim()}
          className="py-3 rounded border border-[#4cc9f0] text-[#4cc9f0] bg-[#1a1a1a] text-xs font-bold tracking-wider active:bg-[#4cc9f0] active:text-black disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          MAP TOKENS
        </button>
        <button
          onClick={handleConcepts}
          disabled={!input.trim()}
          className="py-3 rounded border border-[#8B5CF6] text-[#8B5CF6] bg-[#1a1a1a] text-xs font-bold tracking-wider active:bg-[#8B5CF6] active:text-black disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          EXTRACT
        </button>
        <button
          onClick={handlePlan}
          disabled={!input.trim()}
          className="py-3 rounded border border-[#ef476f] text-[#ef476f] bg-[#1a1a1a] text-xs font-bold tracking-wider active:bg-[#ef476f] active:text-black disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          CRYSTALLIZE
        </button>
      </div>

      {/* Results */}
      <div className="flex-1 overflow-y-auto border border-[#1a1a1a] rounded bg-black p-2 min-h-[200px]">
        <ResultsPanel result={result} />
      </div>
    </div>
  )
}
