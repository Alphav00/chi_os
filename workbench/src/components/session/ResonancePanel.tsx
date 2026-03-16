import type { RefractionResult } from '../../lib/diamond-memory'
import { useTokenSelection } from '../../store'

interface Props {
  result: RefractionResult | null
  accentColor?: string
}

export function ResonancePanel({ result, accentColor = '#4cc9f0' }: Props) {
  const { selectedTokenIds, toggleToken } = useTokenSelection()

  if (!result) {
    return (
      <div className="text-gray-700 text-[10px] text-center py-2 tracking-widest">
        [ DIAMOND MEMORY IDLE ]
      </div>
    )
  }

  const { resonantNodes, traceNodes } = result

  return (
    <div className="border border-[#1a1a1a] rounded bg-[#0d0d0d] p-2 space-y-2">
      {/* Standing Waves */}
      {resonantNodes.length > 0 && (
        <div>
          <div className="text-[10px] font-bold tracking-wider mb-1" style={{ color: accentColor }}>
            STANDING WAVE
          </div>
          <div className="flex flex-wrap gap-1">
            {resonantNodes.map(node => {
              const active = node.tokenId ? selectedTokenIds.has(node.tokenId) : false
              return (
                <button
                  key={node.id}
                  onClick={() => node.tokenId && toggleToken(node.tokenId)}
                  className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-bold transition-colors"
                  style={{
                    backgroundColor: active ? `${accentColor}33` : '#171717',
                    borderWidth: 1,
                    borderColor: active ? accentColor : `${accentColor}44`,
                    color: accentColor,
                    boxShadow: `0 0 6px ${accentColor}44`,
                  }}
                >
                  <span className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ backgroundColor: accentColor }} />
                  {node.concept}
                </button>
              )
            })}
          </div>
        </div>
      )}

      {/* Trace Nodes */}
      {traceNodes.length > 0 && (
        <div>
          <div className="text-[10px] font-bold tracking-wider text-gray-600 mb-1">
            TRACE
          </div>
          <div className="flex flex-wrap gap-1">
            {traceNodes.map(({ node, resonance }) => {
              const active = node.tokenId ? selectedTokenIds.has(node.tokenId) : false
              const opacity = 0.4 + resonance * 0.6
              return (
                <button
                  key={node.id}
                  onClick={() => node.tokenId && toggleToken(node.tokenId)}
                  className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] transition-colors"
                  style={{
                    backgroundColor: active ? '#222' : '#131313',
                    borderWidth: 1,
                    borderColor: active ? '#555' : '#2a2a2a',
                    color: `rgba(200, 200, 200, ${opacity})`,
                  }}
                >
                  {node.concept}
                  <span className="text-[9px] text-gray-600">{(resonance * 100).toFixed(0)}%</span>
                </button>
              )
            })}
          </div>
        </div>
      )}

      {resonantNodes.length === 0 && traceNodes.length === 0 && (
        <div className="text-gray-700 text-[10px] text-center py-1">No resonant nodes found.</div>
      )}
    </div>
  )
}
