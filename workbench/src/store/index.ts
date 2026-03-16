import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { ModelId } from '../types/llm.types'

// =============================================================================
//  Settings Store — API keys + model preferences (persisted in localStorage)
// =============================================================================

interface SettingsState {
  apiKeys: {
    claude: string
    deepseek: string
    qwen: string
  }
  defaultModel: ModelId
  setApiKey: (provider: 'claude' | 'deepseek' | 'qwen', key: string) => void
  setDefaultModel: (id: ModelId) => void
}

export const useSettings = create<SettingsState>()(
  persist(
    (set) => ({
      apiKeys: { claude: '', deepseek: '', qwen: '' },
      defaultModel: 'claude-sonnet-4-6',
      setApiKey: (provider, key) =>
        set(s => ({ apiKeys: { ...s.apiKeys, [provider]: key } })),
      setDefaultModel: (id) => set({ defaultModel: id }),
    }),
    { name: 'chi-settings' }
  )
)

// =============================================================================
//  UI Store — panel visibility, active tab
// =============================================================================

interface UIState {
  thoughtDrawerOpen: boolean
  promptPreviewExpanded: boolean
  selectedAgentId: string | null
  toggleThoughtDrawer: () => void
  togglePromptPreview: () => void
  setSelectedAgent: (id: string | null) => void
}

export const useUI = create<UIState>()((set) => ({
  thoughtDrawerOpen: false,
  promptPreviewExpanded: false,
  selectedAgentId: null,
  toggleThoughtDrawer: () => set(s => ({ thoughtDrawerOpen: !s.thoughtDrawerOpen })),
  togglePromptPreview: () => set(s => ({ promptPreviewExpanded: !s.promptPreviewExpanded })),
  setSelectedAgent: (id) => set({ selectedAgentId: id }),
}))

// =============================================================================
//  Token Selection Store — local selection state (Phase 2 adds IndexedDB sync)
// =============================================================================

interface TokenSelectionState {
  selectedTokenIds: Set<string>
  toggleToken: (id: string) => void
  clearSelection: () => void
}

export const useTokenSelection = create<TokenSelectionState>()((set) => ({
  selectedTokenIds: new Set(),
  toggleToken: (id) =>
    set(s => {
      const next = new Set(s.selectedTokenIds)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return { selectedTokenIds: next }
    }),
  clearSelection: () => set({ selectedTokenIds: new Set() }),
}))
