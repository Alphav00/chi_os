import { useState, useEffect } from 'react'
import type { TokenLibrary, Token } from '../types/token.types'

// Module-level cache — only fetched once per session
let _cache: TokenLibrary | null = null
let _promise: Promise<TokenLibrary> | null = null

function loadLibrary(): Promise<TokenLibrary> {
  if (_cache) return Promise.resolve(_cache)
  if (_promise) return _promise
  _promise = fetch('/token-library.json')
    .then(r => r.json() as Promise<TokenLibrary>)
    .then(data => { _cache = data; return data })
  return _promise
}

export function useTokenLibrary() {
  const [library, setLibrary] = useState<TokenLibrary | null>(_cache)

  useEffect(() => {
    if (_cache) return
    loadLibrary().then(setLibrary).catch(console.error)
  }, [])

  function getTokens(ids: Set<string>): Token[] {
    if (!library) return []
    const result: Token[] = []
    for (const band of library.bands) {
      for (const token of band.tokens) {
        if (ids.has(token.id)) result.push(token)
      }
    }
    return result
  }

  return { library, getTokens }
}
