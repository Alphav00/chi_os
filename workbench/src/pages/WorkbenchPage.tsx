import { useState, useCallback } from 'react'
import { generatePlan, mapFrequencies, extractConcepts } from '../lib/lattice-planner'
import type { PlanResult, FrequencyToken, ConceptResult } from '../lib/lattice-planner'
import type { UrgencyLevel } from '../lib/lattice-engine'
import { AgentSelector } from '../components/session/AgentSelector'
import { CandidateCard } from '../components/session/CandidateCard'
import { getPersona } from '../lib/agent-personas'
import { MODEL_LIST } from '../api/llm/registry'
import { useLLMGeneration } from '../hooks/useLLMGeneration'
import { useTokenLibrary } from '../hooks/useTokenLibrary'
import { useUI, useSettings, useTokenSelection } from '../store'

// =============================================================================
//  Analysis result types
// =============================================================================

type AnalysisView =
  | { kind: 'idle' }
  | { kind: 'frequencies'; data: FrequencyToken[] }
  | { kind: 'concepts'; data: ConceptResult }
  | { kind: 'plan'; data: PlanResult }
  | { kind: 'error'; message: string }

const URGENCY_CLASS: Record<UrgencyLevel, string> = {
  CRITICAL: 'text-red-400',
  HIGH:     'text-amber-400',
  ELEVATED: 'text-yellow-300',
  NORMAL:   'text-green-400',
  NONE:     'text-gray-500',
  LOST:     'text-red-700',
}

// =============================================================================
//  Display components
// =============================================================================

function TokenPill({ word, count }: FrequencyToken) {
  return (
    <span className="inline-block bg-[#222] border border-[#444] px-2 py-0.5 rounded-full text-xs mr-1 mb-1">
      {word} <span className="text-[#ef476f] font-bold">{count}</span>
    </span>
  )
}

function PlanStepRow({ step }: { step: PlanResult['steps'][number] }) {
  return (
    <div className="border-l-2 border-[#4cc9f0] pl-3 py-1 mb-3">
      <div className="flex items-center gap-2 mb-0.5">
        <span className="text-gray-600 text-xs">#{step.step}</span>
        <span className={`text-xs font-bold ${URGENCY_CLASS[step.urgency]}`}>[{step.urgency}]</span>
        <span className="text-sm font-bold">{step.name}</span>
      </div>
      <div className="text-[11px] text-gray-400">{step.reasoning}</div>
      <div className="text-[10px] text-gray-600 mt-0.5">
        Cost {step.cost.toFixed(1)}u · Net {step.net.toFixed(1)}u · Fidelity {(step.fidelity * 100).toFixed(0)}%
      </div>
    </div>
  )
}

function AnalysisPanel({ view }: { view: AnalysisView }) {
  if (view.kind === 'idle') {
    return <div className="text-gray-600 text-xs text-center py-8">[ Awaiting Data ]</div>
  }
  if (view.kind === 'error') {
    return <div className="border-l-4 border-[#ef476f] bg-[#171717] p-3 text-sm text-red-400">{view.message}</div>
  }
  if (view.kind === 'frequencies') {
    return (
      <div className="bg-[#171717] border-l-4 border-[#4cc9f0] p-3">
        <div className="text-[#4cc9f0] font-bold text-xs mb-2 tracking-wider">HIGH-FREQUENCY SEMANTIC TOKENS</div>
        <div>{view.data.map(t => <TokenPill key={t.word} {...t} />)}</div>
      </div>
    )
  }
  if (view.kind === 'concepts') {
    return (
      <div className="space-y-3">
        {view.data.frameworks.length > 0 && (
          <div className="bg-[#171717] border-l-4 border-[#4cc9f0] p-3">
            <div className="text-[#4cc9f0] font-bold text-xs mb-2 tracking-wider">CAPITALIZED FRAMEWORKS</div>
            <ul className="text-sm list-disc list-inside space-y-0.5">
              {view.data.frameworks.map((f, i) => <li key={i}>{f}</li>)}
            </ul>
          </div>
        )}
        {view.data.tags.length > 0 && (
          <div className="bg-[#171717] border-l-4 border-[#8B5CF6] p-3">
            <div className="text-[#8B5CF6] font-bold text-xs mb-2 tracking-wider">BRACKETED CONTEXT</div>
            <ul className="text-sm list-disc list-inside space-y-0.5">
              {view.data.tags.map((t, i) => <li key={i}>{t}</li>)}
            </ul>
          </div>
        )}
        {!view.data.frameworks.length && !view.data.tags.length && (
          <div className="text-gray-500 text-xs">No concepts or tags found.</div>
        )}
      </div>
    )
  }
  if (view.kind === 'plan') {
    const { steps, warnings, energyRemaining, totalCost, totalNodes } = view.data
    return (
      <div>
        <div className="bg-[#171717] border-l-4 border-[#ef476f] p-3 mb-3">
          <div className="text-[#ef476f] font-bold text-xs mb-1 tracking-wider">LATTICE PLAN CRYSTALLIZED</div>
          <div className="text-[10px] text-gray-500">
            {steps.length} steps · {totalNodes} nodes · {energyRemaining.toFixed(1)}u remaining · {totalCost.toFixed(1)}u spent
          </div>
        </div>
        {warnings.map((w, i) => (
          <div key={i} className="text-yellow-400 text-xs mb-2 pl-3 border-l-2 border-yellow-400">{w}</div>
        ))}
        {steps.length === 0 && (
          <div className="text-gray-500 text-xs pl-3">No steps generated — try longer input with repeated terms.</div>
        )}
        {steps.map(s => <PlanStepRow key={s.step} step={s} />)}
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
  const [analysisView, setAnalysisView] = useState<AnalysisView>({ kind: 'idle' })
  const [selectedModelId, setSelectedModelId] = useState('claude-sonnet-4-6')
  const [showGenPanel, setShowGenPanel] = useState(false)

  const { selectedAgentId, setSelectedAgent } = useUI()
  const { defaultModel } = useSettings()
  const { selectedTokenIds } = useTokenSelection()
  const { getTokens } = useTokenLibrary()
  const gen = useLLMGeneration()

  const activePersona = selectedAgentId ? getPersona(selectedAgentId) : undefined
  const accentColor = activePersona?.color ?? '#4cc9f0'
  const modelId = selectedModelId || defaultModel

  const handleFrequencies = useCallback(() => {
    if (!input.trim()) return
    const data = mapFrequencies(input)
    setAnalysisView(data.length ? { kind: 'frequencies', data } : { kind: 'error', message: 'No significant words found.' })
  }, [input])

  const handleConcepts = useCallback(() => {
    if (!input.trim()) return
    setAnalysisView({ kind: 'concepts', data: extractConcepts(input) })
  }, [input])

  const handlePlan = useCallback(() => {
    if (!input.trim()) return
    try {
      setAnalysisView({ kind: 'plan', data: generatePlan(input, activePersona?.sentimentMod ?? 1.0) })
    } catch (e) {
      setAnalysisView({ kind: 'error', message: String(e) })
    }
  }, [input, activePersona])

  const handleGenerate = useCallback(async () => {
    if (!input.trim()) return
    setShowGenPanel(true)
    gen.reset()
    await gen.generate({
      problemText: input,
      agentPersona: activePersona ?? null,
      selectedTokens: getTokens(selectedTokenIds),
      modelId,
      variantCount: 3,
    })
  }, [input, activePersona, selectedTokenIds, modelId, gen, getTokens])

  return (
    <div className="flex flex-col gap-3 p-4 pb-6">

      {/* Agent Selector */}
      <AgentSelector selectedId={selectedAgentId} onSelect={setSelectedAgent} />

      {/* Input */}
      <div className="flex flex-col gap-1">
        <div className="flex items-center justify-between">
          <label className="text-[10px] text-gray-500 tracking-widest uppercase">Data Substrate</label>
          {activePersona && (
            <span className="text-[10px] font-bold tracking-wider" style={{ color: accentColor }}>
              {activePersona.mode} MODE
            </span>
          )}
        </div>
        <textarea
          value={input}
          onChange={e => setInput(e.target.value)}
          placeholder="Paste raw notes, logs, or problem statement here..."
          rows={7}
          className="w-full p-3 rounded text-[#4cc9f0] text-sm font-mono resize-none focus:outline-none"
          style={{ backgroundColor: '#171717', border: `1px solid ${accentColor}55` }}
        />
        <div className="text-[10px] text-gray-600 text-right">
          {input.length} chars · {input.split(/\s+/).filter(Boolean).length} words
          {selectedTokenIds.size > 0 && (
            <span className="ml-2 text-[#4cc9f0]">{selectedTokenIds.size} tokens active</span>
          )}
        </div>
      </div>

      {/* Analysis buttons */}
      <div className="grid grid-cols-3 gap-2">
        <button onClick={handleFrequencies} disabled={!input.trim()}
          className="py-3 rounded border border-[#4cc9f0] text-[#4cc9f0] bg-[#1a1a1a] text-xs font-bold tracking-wider active:bg-[#4cc9f0] active:text-black disabled:opacity-30 transition-colors">
          MAP TOKENS
        </button>
        <button onClick={handleConcepts} disabled={!input.trim()}
          className="py-3 rounded border border-[#8B5CF6] text-[#8B5CF6] bg-[#1a1a1a] text-xs font-bold tracking-wider active:bg-[#8B5CF6] active:text-black disabled:opacity-30 transition-colors">
          EXTRACT
        </button>
        <button onClick={handlePlan} disabled={!input.trim()}
          className="py-3 rounded border text-xs font-bold tracking-wider active:text-black disabled:opacity-30 transition-colors"
          style={{ borderColor: accentColor, color: accentColor, backgroundColor: '#1a1a1a' }}>
          CRYSTALLIZE
        </button>
      </div>

      {/* Analysis results */}
      <div className="border border-[#1a1a1a] rounded bg-black p-2 min-h-[120px]">
        <AnalysisPanel view={analysisView} />
      </div>

      {/* Generation controls */}
      <div className="border border-[#1a1a1a] rounded bg-[#0d0d0d] p-3">
        <div className="flex items-center justify-between mb-3">
          <span className="text-[10px] text-gray-500 tracking-widest uppercase">Generate Candidates</span>
          <span className="text-[10px] text-gray-600">3 variants</span>
        </div>

        {/* Model picker */}
        <div className="flex gap-2 overflow-x-auto pb-1 mb-3">
          {MODEL_LIST.map(m => (
            <button key={m.modelId} onClick={() => setSelectedModelId(m.modelId)}
              className="flex-none px-3 py-1.5 rounded border text-[11px] font-bold transition-colors"
              style={{
                borderColor: modelId === m.modelId ? accentColor : '#333',
                color: modelId === m.modelId ? accentColor : '#555',
                backgroundColor: modelId === m.modelId ? `${accentColor}14` : '#111',
              }}>
              {m.displayName}
            </button>
          ))}
        </div>

        {gen.status === 'generating' ? (
          <button onClick={gen.cancel}
            className="w-full py-3 rounded border border-red-500 text-red-500 bg-[#1a1a1a] text-sm font-bold tracking-wider active:bg-red-500 active:text-black transition-colors">
            ■ STOP
          </button>
        ) : (
          <button onClick={handleGenerate} disabled={!input.trim()}
            className="w-full py-3 rounded border text-sm font-bold tracking-wider disabled:opacity-30 transition-colors"
            style={{ borderColor: accentColor, color: accentColor, backgroundColor: '#1a1a1a' }}>
            ▶ GENERATE
          </button>
        )}
      </div>

      {/* Candidate output */}
      {showGenPanel && (
        <div>
          <div className="text-[10px] text-gray-500 tracking-widest uppercase mb-2">
            Candidates
            {gen.modelUsed && (
              <span className="ml-2 text-gray-700">
                via {MODEL_LIST.find(m => m.modelId === gen.modelUsed)?.displayName ?? gen.modelUsed}
              </span>
            )}
          </div>

          {gen.status === 'error' && (
            <div className="border-l-4 border-red-500 bg-[#171717] p-3 text-sm text-red-400 rounded">
              {gen.error}
            </div>
          )}

          {gen.status === 'generating' && gen.streamText && (
            <CandidateCard index={1} content={gen.streamText} agentColor={accentColor} streaming />
          )}

          {gen.status === 'done' && gen.candidates.map((c, i) => (
            <CandidateCard key={i} index={i + 1} content={c} agentColor={accentColor} />
          ))}

          {gen.status === 'idle' && (
            <div className="text-gray-600 text-xs text-center py-4">[ Cancelled ]</div>
          )}
        </div>
      )}
    </div>
  )
}
