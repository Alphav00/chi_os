// Phase 2/4 — Session History + Validation Log
// Stub for Phase 1. Will wire to IndexedDB via useSessionHistory hook.

export function SessionLogPage() {
  return (
    <div className="p-4">
      <h2 className="text-sm font-bold text-[#4cc9f0] tracking-wider mb-2">SESSION LOG</h2>
      <p className="text-xs text-gray-500 mb-6">
        Past sessions, validation scores, and token effectiveness history.
      </p>

      <div className="border border-[#1a1a1a] rounded p-6 text-center">
        <div className="text-gray-600 text-xs">No sessions yet.</div>
        <div className="text-gray-700 text-[10px] mt-1">
          Complete a generation session on the Forge tab to log it here.
        </div>
      </div>

      <div className="mt-4 border border-[#1a1a1a] rounded p-3">
        <div className="text-xs font-bold text-gray-500 tracking-wider mb-2">COMING IN PHASE 2 / 4</div>
        <ul className="text-[10px] text-gray-700 space-y-1 list-disc list-inside">
          <li>IndexedDB session persistence</li>
          <li>Per-candidate star rating (1–5)</li>
          <li>Freeform notes per candidate</li>
          <li>Token effectiveness score tracking</li>
          <li>Export session as JSON / Markdown</li>
        </ul>
      </div>
    </div>
  )
}
