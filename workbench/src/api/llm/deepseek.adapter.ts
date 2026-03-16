import type { ILLMAdapter, LLMRequest, LLMResponse } from '../../types/llm.types'

const MODEL = 'deepseek-chat'
const BASE_URL = 'https://api.deepseek.com/v1'

async function openAICompletions(
  baseUrl: string,
  model: string,
  request: LLMRequest,
  apiKey: string,
  stream: false,
  signal?: AbortSignal
): Promise<LLMResponse>
async function openAICompletions(
  baseUrl: string,
  model: string,
  request: LLMRequest,
  apiKey: string,
  stream: true,
  signal: AbortSignal | undefined,
  onChunk: (chunk: string) => void
): Promise<void>
async function openAICompletions(
  baseUrl: string,
  model: string,
  request: LLMRequest,
  apiKey: string,
  streaming: boolean,
  signal: AbortSignal | undefined,
  onChunk?: (chunk: string) => void
): Promise<LLMResponse | void> {
  const res = await fetch(`${baseUrl}/chat/completions`, {
    method: 'POST',
    signal,
    headers: {
      Authorization: `Bearer ${apiKey}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      model,
      max_tokens: request.maxTokens ?? 2048,
      stream: streaming,
      messages: [
        { role: 'system', content: request.systemPrompt },
        { role: 'user', content: request.userPrompt },
      ],
    }),
  })

  if (!res.ok) {
    const body = await res.text()
    throw new Error(`${model} ${res.status}: ${body}`)
  }

  if (!streaming) {
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
  }

  // Streaming: SSE in OpenAI format
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
          if (delta) onChunk!(delta)
        } catch { /* ignore */ }
      }
    }
  } finally {
    reader.releaseLock()
  }
}

export const deepseekAdapter: ILLMAdapter = {
  modelId: MODEL,
  displayName: 'DeepSeek Chat',
  apiKeyStorageKey: 'chi_key_deepseek',
  maxContextTokens: 64000,

  complete: (req, key) => openAICompletions(BASE_URL, MODEL, req, key, false, undefined),

  stream: (req, key, onChunk, signal) =>
    openAICompletions(BASE_URL, MODEL, req, key, true, signal, onChunk),
}
