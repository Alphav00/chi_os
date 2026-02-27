export interface ArchiveSection {
  heading: string
  content: string
  level: number
}

export interface ExtractedPattern {
  id: string
  text: string
  regexKey: string
  sourceSection: string
}

export interface LearnedPattern {
  id: string
  sourceFile: string
  patternText: string
  regexKey: string
  inferredTokenIds: string[]
  confidence: number
  createdAt: number
}

export interface Thought {
  id: string
  content: string
  sessionId: string | null
  tags: string[]
  createdAt: number
}
