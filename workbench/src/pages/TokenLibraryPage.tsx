import { useState, useEffect } from 'react'
import type { TokenLibrary, FrequencyBand, Token } from '../types/token.types'
import { useTokenSelection } from '../store'

export function TokenLibraryPage() {
  const [library, setLibrary] = useState<TokenLibrary | null>(null)
  const [search, setSearch] = useState('')
  const [expandedBands, setExpandedBands] = useState<Set<string>>(new Set())
  const { selectedTokenIds, toggleToken } = useTokenSelection()

  useEffect(() => {
    fetch('/token-library.json')
      .then(r => r.json())
      .then(setLibrary)
      .catch(console.error)
  }, [])

  if (!library) {
    return <div className="p-4 text-gray-500 text-xs">Loading token library...</div>
  }

  const q = search.toLowerCase()

  const filteredBands: FrequencyBand[] = library.bands.map(band => ({
    ...band,
    tokens: band.tokens.filter(
      t =>
        !q ||
        t.label.toLowerCase().includes(q) ||
        t.shorthand.toLowerCase().includes(q) ||
        t.description.toLowerCase().includes(q)
    ),
  })).filter(b => b.tokens.length > 0)

  const toggleBand = (id: string) =>
    setExpandedBands(prev => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })

  const totalSelected = selectedTokenIds.size

  return (
    <div className="p-4">
      <div className="mb-4">
        <div className="flex items-center justify-between mb-2">
          <h2 className="text-sm font-bold text-[#4cc9f0] tracking-wider">TOKEN LIBRARY</h2>
          {totalSelected > 0 && (
            <span className="text-xs text-[#ef476f] font-bold">{totalSelected} selected</span>
          )}
        </div>
        <input
          type="text"
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="Search tokens..."
          className="w-full p-2 rounded bg-[#171717] border border-[#333] text-[#4cc9f0] text-sm font-mono focus:outline-none focus:border-[#4cc9f0] placeholder-gray-600"
        />
      </div>

      <div className="space-y-2">
        {filteredBands.map(band => (
          <BandSection
            key={band.id}
            band={band}
            expanded={expandedBands.has(band.id) || !!q}
            selectedTokenIds={selectedTokenIds}
            onToggleBand={() => toggleBand(band.id)}
            onToggleToken={toggleToken}
          />
        ))}
      </div>

      <div className="mt-4 text-[10px] text-gray-600 text-center">
        {library.bands.reduce((s, b) => s + b.tokens.length, 0)} tokens · {library.bands.length} bands · v{library.version}
      </div>
    </div>
  )
}

function BandSection({
  band, expanded, selectedTokenIds, onToggleBand, onToggleToken
}: {
  band: FrequencyBand
  expanded: boolean
  selectedTokenIds: Set<string>
  onToggleBand: () => void
  onToggleToken: (id: string) => void
}) {
  const selectedCount = band.tokens.filter(t => selectedTokenIds.has(t.id)).length

  return (
    <div className="border border-[#222] rounded overflow-hidden">
      <button
        onClick={onToggleBand}
        className="w-full flex items-center justify-between p-3 bg-[#0f0f0f] text-left"
      >
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full shrink-0" style={{ backgroundColor: band.color }} />
          <span className="text-sm font-bold" style={{ color: band.color }}>
            {band.name}
          </span>
          <span className="text-[10px] text-gray-600">FREQ {band.frequency}</span>
        </div>
        <div className="flex items-center gap-2">
          {selectedCount > 0 && (
            <span className="text-xs" style={{ color: band.color }}>{selectedCount}</span>
          )}
          <svg
            viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
            className={`w-3 h-3 text-gray-500 transition-transform ${expanded ? 'rotate-180' : ''}`}
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
          </svg>
        </div>
      </button>

      {expanded && (
        <div className="p-2 bg-[#0a0a0a]">
          <p className="text-[10px] text-gray-600 mb-2 px-1">{band.description}</p>
          <div className="grid grid-cols-2 gap-1.5">
            {band.tokens.map(token => (
              <TokenChip
                key={token.id}
                token={token}
                bandColor={band.color}
                selected={selectedTokenIds.has(token.id)}
                onToggle={() => onToggleToken(token.id)}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

function TokenChip({
  token, bandColor, selected, onToggle
}: {
  token: Token
  bandColor: string
  selected: boolean
  onToggle: () => void
}) {
  return (
    <button
      onClick={onToggle}
      className="text-left p-2 rounded border transition-all"
      style={{
        borderColor: selected ? bandColor : '#333',
        backgroundColor: selected ? `${bandColor}18` : '#111',
      }}
    >
      <div className="flex items-center justify-between mb-0.5">
        <span className="text-[10px] font-bold" style={{ color: bandColor }}>
          {token.shorthand}
        </span>
        {selected && (
          <svg viewBox="0 0 24 24" fill="currentColor" className="w-3 h-3" style={{ color: bandColor }}>
            <path fillRule="evenodd" d="M19.916 4.626a.75.75 0 01.208 1.04l-9 13.5a.75.75 0 01-1.154.114l-6-6a.75.75 0 011.06-1.06l5.353 5.353 8.493-12.739a.75.75 0 011.04-.208z" clipRule="evenodd" />
          </svg>
        )}
      </div>
      <div className="text-[11px] text-[#e5e5e5] leading-tight">{token.label}</div>
      <div className="text-[9px] text-gray-600 mt-0.5 leading-tight line-clamp-2">{token.usageHint}</div>
    </button>
  )
}
