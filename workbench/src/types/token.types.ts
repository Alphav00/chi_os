export interface Token {
  id: string
  label: string
  shorthand: string
  description: string
  usageHint: string
  defaultWeight: number
}

export interface FrequencyBand {
  id: string
  name: string
  description: string
  frequency: number
  color: string
  tokens: Token[]
}

export interface TokenLibrary {
  version: string
  lastUpdated: string
  bands: FrequencyBand[]
}

export interface TokenScore {
  tokenId: string
  score: number
  useCount: number
  highScoreCount: number
  lastUsed: number
}
