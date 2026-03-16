export interface Session {
  id: string
  createdAt: number
  problemStatement: string
  entropyTokens: string[]
  selectedTokenIds: string[]
  assembledPrompt: string
  modelId: string
  candidateIds: string[]
  status: 'active' | 'complete' | 'archived'
  tags: string[]
}

export interface Candidate {
  id: string
  sessionId: string
  modelId: string
  content: string
  generatedAt: number
  index: number
}

export interface ValidationRecord {
  id: string
  candidateId: string
  sessionId: string
  score: number
  notes: string
  createdAt: number
  updatedAt: number
}
