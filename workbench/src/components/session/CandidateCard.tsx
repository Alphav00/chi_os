import { useState } from 'react'

interface Props {
  index: number
  content: string
  agentColor?: string
  streaming?: boolean
}

export function CandidateCard({ index, content, agentColor = '#4cc9f0', streaming = false }: Props) {
  const [copied, setCopied] = useState(false)

  const copy = async () => {
    await navigator.clipboard.writeText(content)
    setCopied(true)
    setTimeout(() => setCopied(false), 1500)
  }

  return (
    <div
      className="rounded border mb-3 overflow-hidden"
      style={{ borderColor: `${agentColor}44` }}
    >
      {/* Header */}
      <div
        className="flex items-center justify-between px-3 py-1.5"
        style={{ backgroundColor: `${agentColor}14` }}
      >
        <div className="flex items-center gap-2">
          <span className="text-[10px] font-bold" style={{ color: agentColor }}>
            CANDIDATE {index}
          </span>
          {streaming && (
            <span className="text-[9px] text-gray-500 animate-pulse">streaming...</span>
          )}
        </div>
        <button
          onClick={copy}
          disabled={streaming}
          className="text-[10px] text-gray-500 hover:text-gray-300 transition-colors disabled:opacity-30"
        >
          {copied ? 'copied' : 'copy'}
        </button>
      </div>

      {/* Content */}
      <div className="p-3 bg-[#0d0d0d]">
        <pre
          className="text-sm text-[#e5e5e5] whitespace-pre-wrap font-mono leading-relaxed"
          style={{ wordBreak: 'break-word' }}
        >
          {content}
          {streaming && <span className="animate-pulse text-gray-600">█</span>}
        </pre>
      </div>

      {/* Validation — Phase 4 placeholder */}
      {!streaming && (
        <div className="px-3 py-1.5 border-t border-[#1a1a1a] flex items-center gap-2">
          <span className="text-[10px] text-gray-700">Rate:</span>
          {[1, 2, 3, 4, 5].map(n => (
            <button
              key={n}
              className="text-[14px] text-gray-700 hover:text-yellow-400 transition-colors"
              title={`${n} star${n > 1 ? 's' : ''}`}
            >
              ★
            </button>
          ))}
          <span className="text-[10px] text-gray-700 ml-auto">Phase 4</span>
        </div>
      )}
    </div>
  )
}
