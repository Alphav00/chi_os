export type ModelId = 'claude-sonnet-4-6' | 'deepseek-chat' | 'qwen-plus'

export interface LLMRequest {
  systemPrompt: string
  userPrompt: string
  maxTokens?: number
  temperature?: number
  stream?: boolean
}

export interface LLMResponse {
  content: string
  modelId: string
  usage: {
    promptTokens: number
    completionTokens: number
  }
  finishReason: string
}

export interface ILLMAdapter {
  modelId: string
  displayName: string
  apiKeyStorageKey: string
  maxContextTokens: number
  complete(request: LLMRequest, apiKey: string): Promise<LLMResponse>
  stream?(
    request: LLMRequest,
    apiKey: string,
    onChunk: (chunk: string) => void,
    signal?: AbortSignal
  ): Promise<void>
}
