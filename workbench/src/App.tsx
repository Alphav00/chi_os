import { HashRouter, Routes, Route } from 'react-router-dom'
import { AppShell } from './components/layout/AppShell'
import { WorkbenchPage } from './pages/WorkbenchPage'
import { ArchivePage } from './pages/ArchivePage'
import { SessionLogPage } from './pages/SessionLogPage'
import { TokenLibraryPage } from './pages/TokenLibraryPage'
import { SettingsPage } from './pages/SettingsPage'

export default function App() {
  return (
    <HashRouter>
      <Routes>
        <Route element={<AppShell />}>
          <Route index element={<WorkbenchPage />} />
          <Route path="archive" element={<ArchivePage />} />
          <Route path="tokens" element={<TokenLibraryPage />} />
          <Route path="sessions" element={<SessionLogPage />} />
          <Route path="settings" element={<SettingsPage />} />
        </Route>
      </Routes>
    </HashRouter>
  )
}
