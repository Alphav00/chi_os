---
skill_id: "SKILL_004"
title: "UI/UX Design Standards for Android Apps"
version: "1.0"
category: "Design System"
agent_tier: "Core (All Agents)"
capability_manifest: {
  "triggers": ["CHI_BUILD_UI", "CHI_NEW_COMPONENT", "CHI_STYLE_CHECK"],
  "logic": "Enforce visual consistency, touch-first design, and dark-mode terminal aesthetic across all CHI Android applications",
  "isolation": "SHARED (All workbench agents inherit these standards)"
}
---

# CHI UI/UX Design Standards — Android Apps

> "The interface is a prosthetic. It must feel like skin, not scaffolding."

This skill defines the canonical design system for all CHI Android applications built with the React + Capacitor stack. Every component, page, and interaction must conform to these standards unless explicitly overridden by a domain-specific requirement.

---

## 1. DESIGN PRINCIPLES

1. **Dark-Mode Terminal Aesthetic** — Pure dark backgrounds, monospace typography, cyan-accented visual hierarchy. The interface should feel like a command center, not a consumer app.
2. **Touch-First, Always** — Every interactive element must be comfortable on a Pixel 10 screen held one-handed. Minimum touch target: 44px in at least one dimension.
3. **Color as Semantic Signal** — Color is never decorative. Every color communicates state, urgency, or domain.
4. **No Shadows, Use Borders** — Depth is communicated through border color and background layer, not box-shadow. Exception: glow effects on active/resonant elements.
5. **Monospace Is Identity** — All text is rendered in the system monospace stack. This is non-negotiable.
6. **Agent-Tinted UI** — Active agent persona tints interactive elements via `accentColor` prop. The UI adapts to the cognitive mode.
7. **Minimal Animation** — Only `transition-colors` for state changes and `animate-pulse` for live indicators. No spring physics, no parallax, no decorative motion.

---

## 2. COLOR PALETTE

### Background Layers (Darkest to Lightest)

| Token         | Hex       | Usage                                    |
|---------------|-----------|------------------------------------------|
| `base`        | `#0a0a0a` | Page background, status bar              |
| `deep`        | `#0d0d0d` | Card content areas, candidate bodies     |
| `surface-0`   | `#0f0f0f` | Bottom nav, secondary panels             |
| `surface-1`   | `#111111` | Unselected card backgrounds              |
| `surface-2`   | `#131313` | Trace node chips (inactive)              |
| `surface-3`   | `#171717` | Input fields, analysis panels            |
| `surface-4`   | `#1a1a1a` | Button backgrounds, panel borders        |
| `elevated`    | `#222222` | Token pills, hover surfaces              |

### Accent Colors

| Token         | Hex       | Semantic Role                            |
|---------------|-----------|------------------------------------------|
| `chi-cyan`    | `#4cc9f0` | Primary brand, active states, default CTA|
| `chi-red`     | `#ef476f` | Danger, critical urgency, error states   |
| `chi-orange`  | `#FFA500` | Warning, caution, adversarial agent      |
| `chi-yellow`  | `#EFFF00` | Structural agent, elevated urgency       |
| `chi-gold`    | `#FFD700` | Scout agent, star ratings                |
| `chi-green`   | `#00ff41` | Success, OK status, implementation band  |
| `chi-purple`  | `#8B5CF6` | Secondary accent, concept extraction     |
| `chi-pink`    | `#FF007F` | Divergent agent, pata-physical band      |
| `chi-sky`     | `#87CEEB` | Operational agent                        |
| `chi-teal`    | `#00FFFF` | Synthetic agent                          |

### Text Colors

| Token         | Hex/Class     | Usage                              |
|---------------|---------------|------------------------------------|
| `text-primary`| `#e5e5e5`     | Body text, high-contrast content   |
| `text-mid`    | `text-gray-500`| Labels, secondary info            |
| `text-dim`    | `text-gray-600`| Timestamps, char counts, metadata |
| `text-muted`  | `text-gray-700`| Placeholders, disabled text       |

### Alpha Transparency Convention

When tinting backgrounds or borders with an agent's accent color, use hex alpha suffixes:

| Suffix | Opacity | Usage                           |
|--------|---------|----------------------------------|
| `14`   | ~8%     | Selected card background tint    |
| `33`   | 20%     | Active chip background           |
| `40`   | 25%     | Glow shadow (`boxShadow`)        |
| `44`   | ~27%    | Border tint, inactive glow       |
| `55`   | ~33%    | Input border with agent color    |

---

## 3. TYPOGRAPHY

### Font Stack

```css
font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
```

Applied globally via Tailwind `font-mono`. No serif or sans-serif fonts anywhere.

### Size Scale

| Class          | Size   | Usage                                         |
|----------------|--------|-----------------------------------------------|
| `text-[8px]`   | 8px    | Mode badges, micro-labels                     |
| `text-[9px]`   | 9px    | Resonance percentages, streaming indicator    |
| `text-[10px]`  | 10px   | Section labels, nav text, metadata (MOST COMMON) |
| `text-[11px]`  | 11px   | Chip labels, model picker buttons             |
| `text-xs`      | 12px   | Small body text, button labels                |
| `text-sm`      | 14px   | Standard content, section titles, CTA buttons |
| `text-base`    | 16px   | Input text (sparingly used)                   |

### Text Treatment Patterns

**Section Label (uppercase tracker):**
```
text-[10px] text-gray-500 tracking-widest uppercase
```

**Section Title (accented):**
```
text-[10px] font-bold tracking-wider
style={{ color: accentColor }}
```

**Body Content:**
```
text-sm text-[#e5e5e5] whitespace-pre-wrap font-mono leading-relaxed
```

---

## 4. SPACING SYSTEM

All spacing uses Tailwind's 4px base grid. Preferred values:

| Token | Tailwind | Pixels | Usage                        |
|-------|----------|--------|------------------------------|
| `0.5` | `p-0.5`  | 2px   | Micro gaps (dot indicators)  |
| `1`   | `p-1`    | 4px   | Minimal internal padding     |
| `1.5` | `py-1.5` | 6px   | Card headers, nav items      |
| `2`   | `p-2`    | 8px   | Standard chip/panel padding  |
| `3`   | `p-3`    | 12px  | Standard section padding     |
| `4`   | `p-4`    | 16px  | Page-level padding           |
| `6`   | `pb-6`   | 24px  | Page bottom (above nav)      |
| `16`  | `pb-16`  | 64px  | Main content bottom (nav clearance) |

### Gap Scale

- `gap-0.5` — Icon + label pairs in nav
- `gap-1` — Chip groups, tight layouts
- `gap-2` — Standard flex/grid gap
- `gap-3` — Page-level section spacing

---

## 5. COMPONENT PATTERNS

### 5.1 Buttons

**Primary CTA (full-width):**
```jsx
<button
  className="w-full py-3 rounded border text-sm font-bold tracking-wider
             disabled:opacity-30 transition-colors"
  style={{ borderColor: accentColor, color: accentColor, backgroundColor: '#1a1a1a' }}
>
  LABEL
</button>
```
- Active state: `active:bg-[color] active:text-black`
- Disabled: `disabled:opacity-30`
- Touch target: Full width, 48px+ height via `py-3`

**Grid Button (1/3 width):**
```
py-3 rounded border border-[color] text-[color] bg-[#1a1a1a]
text-xs font-bold tracking-wider
active:bg-[color] active:text-black disabled:opacity-30 transition-colors
```

**Model Picker Chip:**
```
flex-none px-3 py-1.5 rounded border text-[11px] font-bold transition-colors
```
- Selected: `borderColor: accentColor, color: accentColor, backgroundColor: ${accentColor}14`
- Unselected: `borderColor: '#333', color: '#555', backgroundColor: '#111'`

**Icon/Text Button:**
```
text-[10px] text-gray-500 hover:text-gray-300 transition-colors disabled:opacity-30
```

### 5.2 Cards

**Agent Selector Card (touch-optimized):**
- Width: `w-[88px]` (fixed, fits 4 cards with scroll on Pixel 10)
- Structure: Avatar (40x40) + Name + Mode badge
- Selected border: `persona.color`, unselected: `#2a2a2a`
- Selected glow: `boxShadow: 0 0 8px ${color}40`
- Press feedback: `active:scale-95 transition-all`
- Horizontal scroll container: `overflow-x-auto pb-2`

**Candidate Card:**
- Border: `border` with `borderColor: ${agentColor}44`
- Header: `backgroundColor: ${agentColor}14`, flex justify-between
- Content: `bg-[#0d0d0d]`, `pre` tag with `whitespace-pre-wrap`
- Footer: `border-t border-[#1a1a1a]`, rating stars

**Analysis Panel:**
- Left accent border: `border-l-4 border-[color]`
- Background: `bg-[#171717]`
- Padding: `p-3`
- Section label: `text-[color] font-bold text-xs mb-2 tracking-wider`

### 5.3 Chips / Pills

**Token Pill:**
```
inline-block bg-[#222] border border-[#444] px-2 py-0.5 rounded-full text-xs
```

**Resonance Chip (Standing Wave):**
```jsx
style={{
  backgroundColor: active ? `${accentColor}33` : '#171717',
  borderWidth: 1,
  borderColor: active ? accentColor : `${accentColor}44`,
  color: accentColor,
  boxShadow: `0 0 6px ${accentColor}44`,
}}
```
- Includes pulsing dot: `w-1.5 h-1.5 rounded-full animate-pulse`

**Resonance Chip (Trace):**
- Opacity scales with resonance: `0.4 + resonance * 0.6`
- Includes percentage label: `text-[9px] text-gray-600`

### 5.4 Input Fields

**Textarea:**
```jsx
<textarea
  className="w-full p-3 rounded text-[#4cc9f0] text-sm font-mono
             resize-none focus:outline-none"
  style={{ backgroundColor: '#171717', border: `1px solid ${accentColor}55` }}
  rows={7}
/>
```

**Text Input:**
```
w-full p-2 rounded bg-[#171717] border border-[#333]
text-sm text-[#4cc9f0] font-mono placeholder-gray-600
focus:border-[#4cc9f0] focus:outline-none
```

### 5.5 Bottom Navigation

- Position: `fixed bottom-0 left-0 right-0 z-40`
- Background: `bg-[#0f0f0f]`
- Border: `border-t border-[#222]`
- Items: flex, equal width, `py-2`
- Active: `text-[#4cc9f0]`, Inactive: `text-gray-600`
- Icon: `w-5 h-5`, Label: `text-[10px]`
- Page content must have `pb-16` to clear the nav

---

## 6. LAYOUT RULES

### Page Structure
```
<div className="flex flex-col gap-3 p-4 pb-6">
  {/* Section 1 */}
  {/* Section 2 */}
  {/* ... */}
</div>
```
- Single column, no breakpoints
- Vertical stack with `gap-3`
- Page padding: `p-4`, extra bottom: `pb-6`

### Grid Patterns
- **3-column button row:** `grid grid-cols-3 gap-2`
- **2-column token grid:** `grid grid-cols-2 gap-1.5`
- **Horizontal scroll:** `flex overflow-x-auto pb-2 gap-2`

### Content Hierarchy
1. Selector row (horizontal scroll cards)
2. Status/resonance panel
3. Input area with metadata footer
4. Action buttons (grid)
5. Results panel
6. Generation controls
7. Output cards

---

## 7. TOUCH & INTERACTION STANDARDS

### Minimum Touch Targets
- **Buttons:** 44px minimum height (`py-3` on standard buttons = ~48px)
- **Cards:** 88px width minimum for tappable cards
- **Chips:** `px-2 py-0.5` minimum (adequate for non-critical taps)
- **Nav items:** Full-width flex children with `py-2`
- **Star ratings:** `text-[14px]` with adequate horizontal spacing

### Interaction Feedback
- **Color transition:** `transition-colors` on all interactive elements
- **Press scale:** `active:scale-95 transition-all` on cards
- **Color inversion:** `active:bg-[color] active:text-black` on buttons
- **Pulse:** `animate-pulse` only for live/streaming indicators

### Scrolling
- **Vertical:** Native scroll, `overscroll-behavior: none`
- **Horizontal:** `overflow-x-auto` with `pb-2` for scroll padding
- **Scrollbar:** 4px wide, `#333` thumb, transparent track, 2px radius

### BEHAVIORAL RULES
- Never use `hover:` as the only interaction state — touch devices have no hover
- Always pair `hover:` with `active:` for press feedback
- Remove default tap highlight: `-webkit-tap-highlight-color: transparent`
- Disable pinch zoom: `maximum-scale=1.0, user-scalable=no`

---

## 8. VIEWPORT & ANDROID CONFIGURATION

### Meta Tags (index.html)
```html
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<meta name="theme-color" content="#0a0a0a" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
```

### Capacitor Config
```ts
{
  appId: 'com.chi.latticeforge',
  appName: 'CHI Lattice Forge',
  webDir: 'dist',
  android: { backgroundColor: '#0a0a0a' }
}
```

### Global CSS Resets
```css
html, body, #root {
  background: #0a0a0a;
  color: #e5e5e5;
  font-family: ui-monospace, ..., monospace;
  -webkit-tap-highlight-color: transparent;
  overscroll-behavior: none;
}
```

---

## 9. AGENT COLOR SYSTEM

Each agent persona defines a `color` property that tints the entire UI when active:

| Agent    | Mode        | Color     | Hex       |
|----------|-------------|-----------|-----------|
| Elle     | DIVERGENT   | Pink      | `#FF007F` |
| Stowie   | SCOUT       | Gold      | `#FFD700` |
| Tyger    | ADVERSARIAL | Orange    | `#FFA500` |
| Kiefer   | STRUCTURAL  | Yellow    | `#EFFF00` |
| ChiKa    | SYNTHETIC   | Teal      | `#00FFFF` |
| John     | OPERATIONAL | Sky Blue  | `#87CEEB` |
| (none)   | Default     | Cyan      | `#4cc9f0` |

**How tinting works:**
- Components accept `accentColor` or `agentColor` prop
- Borders, text, backgrounds use the color with alpha suffixes
- Default fallback is always `#4cc9f0`

---

## 10. SELF-CHECK — Before Shipping Any Component

- [ ] All text uses `font-mono` (no sans-serif leaks)
- [ ] Touch targets are >= 44px in at least one dimension
- [ ] Interactive elements have `transition-colors` or `transition-all`
- [ ] Buttons have both `active:` and `disabled:` states
- [ ] No hardcoded accent colors — uses `accentColor` prop or `#4cc9f0` default
- [ ] Background follows the layer hierarchy (base → surface → elevated)
- [ ] Text sizes use the defined scale (no arbitrary px values outside the scale)
- [ ] No `hover:`-only states without `active:` pairing
- [ ] Horizontal lists use `overflow-x-auto` not wrapping grids
- [ ] Page content has `pb-16` to clear bottom nav
- [ ] No decorative animations — only `transition-colors` and `animate-pulse`
- [ ] Alpha suffixes on dynamic colors use the convention (14/33/40/44/55)

---

## 11. LIMITS

- **No light theme.** Dark mode only. Do not build theme toggling.
- **No custom fonts.** System monospace only. No Google Fonts, no icon fonts.
- **No CSS-in-JS libraries.** Tailwind utility classes + inline `style` for dynamic values only.
- **No responsive breakpoints.** Single-column mobile layout. Horizontal scroll for overflow.
- **No drop shadows.** Borders and background layers communicate depth.
- **No opacity below 0.30 for disabled states.** Must remain visible.
- **Maximum 7 background layers.** If you need more, you're nesting too deep.
