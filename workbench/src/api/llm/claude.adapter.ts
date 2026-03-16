import type { ILLMAdapter, LLMRequest, LLMResponse } from '../../types/llm.types'

const MODEL = 'claude-sonnet-4-6'
const API_URL = 'https://api.anthropic.com/v1/messages'
const API_VERSION = '2023-06-01'

export const claudeAdapter: ILLMAdapter = {
  modelId: MODEL,
  displayName: 'Claude Sonnet 4.6',
  apiKeyStorageKey: 'chi_key_claude',
  maxContextTokens: 200000,

  async complete(request: LLMRequest, apiKey: string): Promise<LLMResponse> {
    const res = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'x-api-key': apiKey,
        'anthropic-version': API_VERSION,
        'content-type': 'application/json',
      },
      body: JSON.stringify({
        model: MODEL,
        max_tokens: request.maxTokens ?? 2048,
        system: request.systemPrompt,
        messages: [{ role: 'user', content: request.userPrompt }],
      }),
    })

    if (!res.ok) {
      const body = await res.text()
      throw new Error(`Claude ${res.status}: ${body}`)
    }

    const data = await res.json()
    return {
      content: data.content[0].text,
      modelId: MODEL,
      usage: {
        promptTokens: data.usage.input_tokens,
        completionTokens: data.usage.output_tokens,
      },
      finishReason: data.stop_reason,
    }
  },

  async stream(
    request: LLMRequest,
    apiKey: string,
    onChunk: (chunk: string) => void,
    signal?: AbortSignal
  ): Promise<void> {
    const res = await fetch(API_URL, {
      method: 'POST',
      signal,
      headers: {
        'x-api-key': apiKey,
        'anthropic-version': API_VERSION,
        'content-type': 'application/json',
      },
      body: JSON.stringify({
        model: MODEL,
        max_tokens: request.maxTokens ?? 2048,
        system: request.systemPrompt,
        messages: [{ role: 'user', content: request.userPrompt }],
        stream: true,
      }),
    })

    if (!res.ok) {
      const body = await res.text()
      throw new Error(`Claude ${res.status}: ${body}`)
    }

    const reader = res.body?.getReader()
    if (!reader) throw new Error('No response body')
    const decoder = new TextDecoder()

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        const chunk = decoder.decode(value, { stream: true })
        for (const line of chunk.split('\n')) {
          if (!line.startsWith('data: ')) continue
          const raw = line.slice(6).trim()
          if (!raw || raw === '[DONE]') continue
          try {
            const ev = JSON.parse(raw)
            if (ev.type === 'content_block_delta' && ev.delta?.text) {
              onChunk(ev.delta.text)
            }
          } catch { /* ignore malformed SSE lines */ }
        }
      }
    } finally {
      reader.releaseLock()
    }
  },
}
