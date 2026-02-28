import { AGENT_PERSONAS } from '../../lib/agent-personas'
import type { AgentPersona } from '../../types/agent.types'

interface Props {
  selectedId: string | null
  onSelect: (id: string | null) => void
}

export function AgentSelector({ selectedId, onSelect }: Props) {
  return (
    <div>
      <div className="flex items-center justify-between mb-2">
        <label className="text-[10px] text-gray-500 tracking-widest uppercase">
          Active Agent
        </label>
        {selectedId && (
          <button
            onClick={() => onSelect(null)}
            className="text-[10px] text-gray-600 hover:text-gray-400 transition-colors"
          >
            clear
          </button>
        )}
      </div>

      {/* Horizontal scroll — touch-friendly on Pixel 10 */}
      <div className="flex gap-2 overflow-x-auto pb-2 -mx-4 px-4 scrollbar-hide">
        {AGENT_PERSONAS.map(persona => (
          <AgentCard
            key={persona.id}
            persona={persona}
            selected={selectedId === persona.id}
            onSelect={() => onSelect(selectedId === persona.id ? null : persona.id)}
          />
        ))}
      </div>

      {/* Active persona modifier hint */}
      {selectedId && (() => {
        const p = AGENT_PERSONAS.find(a => a.id === selectedId)
        if (!p) return null
        return (
          <div
            className="mt-2 px-3 py-2 rounded border text-[10px] leading-relaxed text-gray-400"
            style={{ borderColor: `${p.color}44`, backgroundColor: `${p.color}0a` }}
          >
            <span className="font-bold" style={{ color: p.color }}>
              [{p.mode}]
            </span>{' '}
            {p.systemPromptModifier}
          </div>
        )
      })()}
    </div>
  )
}

function AgentCard({
  persona,
  selected,
  onSelect,
}: {
  persona: AgentPersona
  selected: boolean
  onSelect: () => void
}) {
  return (
    <button
      onClick={onSelect}
      className="flex-none w-[88px] flex flex-col items-center gap-1 p-2 rounded-lg border transition-all active:scale-95"
      style={{
        borderColor: selected ? persona.color : '#2a2a2a',
        backgroundColor: selected ? `${persona.color}14` : '#111',
        boxShadow: selected ? `0 0 8px ${persona.color}40` : 'none',
      }}
    >
      {/* Avatar */}
      <div
        className="w-10 h-10 rounded overflow-hidden border"
        style={{ borderColor: selected ? persona.color : '#333' }}
      >
        <img
          src={persona.svgDataUri}
          alt={persona.name}
          className="w-full h-full object-cover"
          draggable={false}
        />
      </div>

      {/* Name */}
      <div
        className="text-[10px] font-bold leading-tight text-center"
        style={{ color: selected ? persona.color : '#888' }}
      >
        {persona.name}
      </div>

      {/* Mode badge */}
      <div
        className="text-[8px] font-bold px-1 rounded leading-tight"
        style={{
          color: selected ? '#000' : '#555',
          backgroundColor: selected ? persona.color : '#222',
        }}
      >
        {persona.mode}
      </div>
    </button>
  )
}
