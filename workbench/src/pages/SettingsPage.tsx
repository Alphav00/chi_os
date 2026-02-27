import { useSettings } from '../store'
import type { ModelId } from '../types/llm.types'

const MODEL_OPTIONS: { id: ModelId; label: string; keyField: 'claude' | 'deepseek' | 'qwen' }[] = [
  { id: 'claude-sonnet-4-6', label: 'Claude Sonnet 4.6', keyField: 'claude' },
  { id: 'deepseek-chat', label: 'DeepSeek Chat', keyField: 'deepseek' },
  { id: 'qwen-plus', label: 'Qwen Plus', keyField: 'qwen' },
]

export function SettingsPage() {
  const { apiKeys, defaultModel, setApiKey, setDefaultModel } = useSettings()

  return (
    <div className="p-4 space-y-6">
      <div>
        <h2 className="text-sm font-bold text-[#4cc9f0] tracking-wider mb-1">SETTINGS</h2>
        <p className="text-[10px] text-gray-600">Keys stored in localStorage only. Never transmitted anywhere.</p>
      </div>

      {/* API Keys */}
      <section>
        <h3 className="text-xs font-bold text-gray-400 tracking-wider mb-3 uppercase">API Keys</h3>
        <div className="space-y-3">
          {MODEL_OPTIONS.map(m => (
            <div key={m.id}>
              <label className="block text-[11px] text-gray-500 mb-1">{m.label}</label>
              <input
                type="password"
                value={apiKeys[m.keyField]}
                onChange={e => setApiKey(m.keyField, e.target.value)}
                placeholder={`${m.keyField === 'claude' ? 'sk-ant-' : m.keyField === 'deepseek' ? 'sk-' : 'sk-'}...`}
                className="w-full p-2 rounded bg-[#171717] border border-[#333] text-[#4cc9f0] text-sm font-mono focus:outline-none focus:border-[#4cc9f0] placeholder-gray-700"
              />
              {apiKeys[m.keyField] && (
                <div className="text-[10px] text-green-500 mt-0.5">Key set ({apiKeys[m.keyField].length} chars)</div>
              )}
            </div>
          ))}
        </div>
      </section>

      {/* Default Model */}
      <section>
        <h3 className="text-xs font-bold text-gray-400 tracking-wider mb-3 uppercase">Default Model</h3>
        <div className="space-y-2">
          {MODEL_OPTIONS.map(m => (
            <button
              key={m.id}
              onClick={() => setDefaultModel(m.id)}
              className={`w-full flex items-center justify-between p-3 rounded border text-sm transition-colors ${
                defaultModel === m.id
                  ? 'border-[#4cc9f0] bg-[#4cc9f018] text-[#4cc9f0]'
                  : 'border-[#333] bg-[#111] text-gray-400'
              }`}
            >
              <span>{m.label}</span>
              {defaultModel === m.id && (
                <svg viewBox="0 0 24 24" fill="currentColor" className="w-4 h-4">
                  <path fillRule="evenodd" d="M19.916 4.626a.75.75 0 01.208 1.04l-9 13.5a.75.75 0 01-1.154.114l-6-6a.75.75 0 011.06-1.06l5.353 5.353 8.493-12.739a.75.75 0 011.04-.208z" clipRule="evenodd" />
                </svg>
              )}
            </button>
          ))}
        </div>
      </section>

      {/* Info */}
      <section className="border border-[#222] rounded p-3">
        <h3 className="text-xs font-bold text-gray-500 tracking-wider mb-2">BUILD INFO</h3>
        <div className="space-y-1 text-[10px] text-gray-600">
          <div>Phase 1 — Lattice Engine + Skeleton</div>
          <div>Branch: claude/collapse-verifier-module-JzTb4</div>
          <div>Stack: React 18 · Vite 5 · Tailwind · Zustand · Capacitor 6</div>
        </div>
      </section>
    </div>
  )
}
