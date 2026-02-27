import type { Config } from 'tailwindcss'

export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        chi: {
          bg: '#0a0a0a',
          surface: '#171717',
          border: '#333333',
          accent: '#4cc9f0',
          danger: '#ef476f',
          warn: '#ffaa00',
          ok: '#00ff41',
        }
      },
      fontFamily: {
        mono: ['ui-monospace', 'SFMono-Regular', 'Menlo', 'Monaco', 'Consolas', 'monospace'],
      }
    },
  },
  plugins: [],
} satisfies Config
