import { Outlet } from 'react-router-dom'
import { BottomNav } from './BottomNav'

export function AppShell() {
  return (
    <div className="flex flex-col h-screen bg-[#0a0a0a] text-[#e5e5e5] font-mono">
      {/* Header */}
      <header className="px-4 py-2 border-b border-[#1a1a1a] flex items-center justify-between shrink-0">
        <div>
          <h1 className="text-sm font-bold text-[#4cc9f0] tracking-widest">CHI // LATTICE FORGE</h1>
          <p className="text-[10px] text-gray-600">collapse-verifier · zero-network · client-only</p>
        </div>
        <div className="w-2 h-2 rounded-full bg-[#00ff41] animate-pulse" title="Local only — no network" />
      </header>

      {/* Page content */}
      <main className="flex-1 overflow-y-auto pb-16">
        <Outlet />
      </main>

      <BottomNav />
    </div>
  )
}
