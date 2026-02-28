import type { ILLMAdapter, LLMRequest, LLMResponse } from '../../types/llm.types'
import { deepseekAdapter } from './deepseek.adapter'

// Qwen (via Alibaba DashScope OpenAI-compatible endpoint)
const MODEL = 'qwen-plus'
const BASE_URL = 'https://dashscope.aliyuncs.com/compatible-mode/v1'

// Re-use the same OpenAI-compat logic as DeepSeek, just swap base URL + model
function makeOpenAIAdapter(baseUrl: string, model: string, keyName: string): ILLMAdapter {
  const ds = deepseekAdapter
  return {
    modelId: model,
    displayName: model === 'qwen-plus' ? 'Qwen Plus' : model,
    apiKeyStorageKey: keyName,
    maxContextTokens: 131072,
    complete: async (req: LLMRequest, key: string): Promise<LLMResponse> => {
      const res = await fetch(`${baseUrl}/chat/completions`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${key}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model,
          max_tokens: req.maxTokens ?? 2048,
          stream: false,
          messages: [
            { role: 'system', content: req.systemPrompt },
            { role: 'user', content: req.userPrompt },
          ],
        }),
      })
      if (!res.ok) throw new Error(`${model} ${res.status}: ${await res.text()}`)
      const data = await res.json()
      const choice = data.choices[0]
      return {
        content: choice.message.content,
        modelId: model,
        usage: {
          promptTokens: data.usage?.prompt_tokens ?? 0,
          completionTokens: data.usage?.completion_tokens ?? 0,
        },
        finishReason: choice.finish_reason,
      }
    },
    stream: async (req: LLMRequest, key: string, onChunk: (c: string) => void, signal?: AbortSignal) => {
      const res = await fetch(`${baseUrl}/chat/completions`, {
        method: 'POST',
        signal,
        headers: {
          Authorization: `Bearer ${key}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model,
          max_tokens: req.maxTokens ?? 2048,
          stream: true,
          messages: [
            { role: 'system', content: req.systemPrompt },
            { role: 'user', content: req.userPrompt },
          ],
        }),
      })
      if (!res.ok) throw new Error(`${model} ${res.status}: ${await res.text()}`)
      const reader = res.body?.getReader()
      if (!reader) throw new Error('No response body')
      const decoder = new TextDecoder()
      try {
        while (true) {
          const { done, value } = await reader.read()
          if (done) break
          const text = decoder.decode(value, { stream: true })
          for (const line of text.split('\n')) {
            if (!line.startsWith('data: ')) continue
            const raw = line.slice(6).trim()
            if (!raw || raw === '[DONE]') continue
            try {
              const ev = JSON.parse(raw)
              const delta = ev.choices?.[0]?.delta?.content
              if (delta) onChunk(delta)
            } catch { /* ignore */ }
          }
        }
      } finally {
        reader.releaseLock()
      }
    },
  }
  // suppress unused import warning
  void ds
}

export const qwenAdapter: ILLMAdapter = makeOpenAIAdapter(BASE_URL, MODEL, 'chi_key_qwen')
