import { useState, useCallback, useRef } from 'react'
import { getAdapter, getApiKey } from '../api/llm/registry'
import { buildPrompt, parseCandidates } from '../api/llm/prompt.builder'
import type { Token } from '../types/token.types'
import type { AgentPersona } from '../types/agent.types'

export type GenerationStatus = 'idle' | 'generating' | 'done' | 'error'

export interface GenerationState {
  status: GenerationStatus
  streamText: string          // raw accumulated stream text
  candidates: string[]        // parsed after stream completes
  error: string | null
  modelUsed: string | null
}

interface GenerateParams {
  problemText: string
  agentPersona: AgentPersona | null
  selectedTokens: Token[]
  modelId: string
  variantCount?: number
}

export function useLLMGeneration() {
  const [state, setState] = useState<GenerationState>({
    status: 'idle',
    streamText: '',
    candidates: [],
    error: null,
    modelUsed: null,
  })

  const abortRef = useRef<AbortController | null>(null)

  const generate = useCallback(async (params: GenerateParams) => {
    const { problemText, agentPersona, selectedTokens, modelId, variantCount = 3 } = params

    // Cancel any in-flight request
    abortRef.current?.abort()
    abortRef.current = new AbortController()
    const signal = abortRef.current.signal

    const adapter = getAdapter(modelId)
    const apiKey = getApiKey(adapter)

    if (!apiKey) {
      setState(s => ({
        ...s,
        status: 'error',
        error: `No API key set for ${adapter.displayName}. Add it in Settings.`,
      }))
      return
    }

    setState({ status: 'generating', streamText: '', candidates: [], error: null, modelUsed: modelId })

    const { systemPrompt, userPrompt } = buildPrompt({
      problemText,
      agentPersona,
      selectedTokens,
      variantCount,
    })

    let accumulated = ''

    try {
      if (adapter.stream) {
        await adapter.stream(
          { systemPrompt, userPrompt, maxTokens: 3000 },
          apiKey,
          (chunk) => {
            accumulated += chunk
            setState(s => ({ ...s, streamText: accumulated }))
          },
          signal
        )
      } else {
        const response = await adapter.complete(
          { systemPrompt, userPrompt, maxTokens: 3000 },
          apiKey
        )
        accumulated = response.content
      }

      if (!signal.aborted) {
        const candidates = parseCandidates(accumulated)
        setState(s => ({ ...s, status: 'done', candidates }))
      }
    } catch (err) {
      if (signal.aborted) return
      setState(s => ({
        ...s,
        status: 'error',
        error: err instanceof Error ? err.message : String(err),
      }))
    }
  }, [])

  const cancel = useCallback(() => {
    abortRef.current?.abort()
    setState(s => ({ ...s, status: 'idle' }))
  }, [])

  const reset = useCallback(() => {
    abortRef.current?.abort()
    setState({ status: 'idle', streamText: '', candidates: [], error: null, modelUsed: null })
  }, [])

  return { ...state, generate, cancel, reset }
}
