export function uid(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2, 7)
}

export function formatDate(ts: number): string {
  return new Intl.DateTimeFormat('en-US', {
    month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  }).format(new Date(ts))
}

export function clamp(val: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, val))
}

/** Weighted moving average for token scores */
export function updateTokenScore(oldScore: number, sessionAvg: number, alpha = 0.3): number {
  return oldScore * (1 - alpha) + sessionAvg * alpha
}
