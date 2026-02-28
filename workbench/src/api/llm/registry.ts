import type { ILLMAdapter } from '../../types/llm.types'
import { claudeAdapter } from './claude.adapter'
import { deepseekAdapter } from './deepseek.adapter'
import { qwenAdapter } from './qwen.adapter'

export const ADAPTERS: Record<string, ILLMAdapter> = {
  [claudeAdapter.modelId]: claudeAdapter,
  [deepseekAdapter.modelId]: deepseekAdapter,
  [qwenAdapter.modelId]: qwenAdapter,
}

export const MODEL_LIST: ILLMAdapter[] = [claudeAdapter, deepseekAdapter, qwenAdapter]

export function getAdapter(modelId: string): ILLMAdapter {
  const adapter = ADAPTERS[modelId]
  if (!adapter) throw new Error(`No adapter registered for model: ${modelId}`)
  return adapter
}

/** Read API key from localStorage using the adapter's key name */
export function getApiKey(adapter: ILLMAdapter): string {
  return localStorage.getItem(adapter.apiKeyStorageKey) ?? ''
}
