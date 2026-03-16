import { openDB } from 'idb'
import type { Session, Candidate } from '../types/session.types'
import type { TokenScore } from '../types/token.types'
import type { Thought } from '../types/archive.types'

const DB_NAME = 'chi-lattice-forge'
const DB_VERSION = 1

export async function getDB() {
  return openDB(DB_NAME, DB_VERSION, {
    upgrade(db) {
      if (!db.objectStoreNames.contains('sessions')) {
        const s = db.createObjectStore('sessions', { keyPath: 'id' })
        s.createIndex('by-created', 'createdAt')
      }
      if (!db.objectStoreNames.contains('candidates')) {
        const c = db.createObjectStore('candidates', { keyPath: 'id' })
        c.createIndex('by-session', 'sessionId')
      }
      if (!db.objectStoreNames.contains('token_scores')) {
        db.createObjectStore('token_scores', { keyPath: 'tokenId' })
      }
      if (!db.objectStoreNames.contains('thoughts')) {
        const t = db.createObjectStore('thoughts', { keyPath: 'id' })
        t.createIndex('by-created', 'createdAt')
      }
    },
  })
}

// =============================================================================
//  Sessions
// =============================================================================

export async function saveSession(session: Session): Promise<void> {
  const db = await getDB()
  await db.put('sessions', session)
}

export async function getSession(id: string): Promise<Session | undefined> {
  const db = await getDB()
  return db.get('sessions', id)
}

export async function getAllSessions(): Promise<Session[]> {
  const db = await getDB()
  const all = await db.getAllFromIndex('sessions', 'by-created')
  return all.reverse()
}

// =============================================================================
//  Candidates
// =============================================================================

export async function saveCandidate(candidate: Candidate): Promise<void> {
  const db = await getDB()
  await db.put('candidates', candidate)
}

export async function getCandidatesForSession(sessionId: string): Promise<Candidate[]> {
  const db = await getDB()
  return db.getAllFromIndex('candidates', 'by-session', sessionId)
}

// =============================================================================
//  Token Scores
// =============================================================================

export async function getTokenScore(tokenId: string): Promise<TokenScore | undefined> {
  const db = await getDB()
  return db.get('token_scores', tokenId)
}

export async function saveTokenScore(score: TokenScore): Promise<void> {
  const db = await getDB()
  await db.put('token_scores', score)
}

// =============================================================================
//  Thoughts
// =============================================================================

export async function saveThought(thought: Thought): Promise<void> {
  const db = await getDB()
  await db.put('thoughts', thought)
}

export async function getAllThoughts(): Promise<Thought[]> {
  const db = await getDB()
  const all = await db.getAllFromIndex('thoughts', 'by-created')
  return all.reverse()
}
