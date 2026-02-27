// Phase 5 — Archive Processing
// Stub for Phase 1. Will implement file drop + pattern extraction + token inference.

export function ArchivePage() {
  return (
    <div className="p-4">
      <h2 className="text-sm font-bold text-[#4cc9f0] tracking-wider mb-2">ARCHIVE PROCESSOR</h2>
      <p className="text-xs text-gray-500 mb-6">
        Drag chi_os markdown files here to extract patterns and infer token candidates.
      </p>

      <div className="border-2 border-dashed border-[#333] rounded-lg p-10 flex flex-col items-center justify-center gap-3 text-center">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round" className="w-10 h-10 text-gray-600">
          <path d="M3.75 9.776c.112-.017.227-.026.344-.026h15.812c.117 0 .232.009.344.026m-16.5 0a2.25 2.25 0 00-1.883 2.542l.857 6a2.25 2.25 0 002.227 1.932H19.05a2.25 2.25 0 002.227-1.932l.857-6a2.25 2.25 0 00-1.883-2.542m-16.5 0V6A2.25 2.25 0 016 3.75h3.879a1.5 1.5 0 011.06.44l2.122 2.12a1.5 1.5 0 001.06.44H18A2.25 2.25 0 0120.25 9v.776" />
        </svg>
        <div>
          <div className="text-gray-500 text-sm">Coming in Phase 5</div>
          <div className="text-gray-700 text-xs mt-1">reader · extractor · token.inferrer</div>
        </div>
      </div>
    </div>
  )
}
