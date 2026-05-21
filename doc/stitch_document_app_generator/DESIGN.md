---
name: Doyu Vitality Craft
colors:
  surface: '#fdf9f5'
  surface-dim: '#ddd9d6'
  surface-bright: '#fdf9f5'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f7f3ef'
  surface-container: '#f1ede9'
  surface-container-high: '#ebe7e4'
  surface-container-highest: '#e5e2de'
  on-surface: '#1c1c19'
  on-surface-variant: '#554245'
  inverse-surface: '#31302e'
  inverse-on-surface: '#f4f0ec'
  outline: '#887274'
  outline-variant: '#dbc0c3'
  surface-tint: '#9e3d54'
  primary: '#9e3d54'
  on-primary: '#ffffff'
  primary-container: '#e8778e'
  on-primary-container: '#640f29'
  inverse-primary: '#ffb2be'
  secondary: '#366756'
  on-secondary: '#ffffff'
  secondary-container: '#b9eed7'
  on-secondary-container: '#3d6e5c'
  tertiary: '#356289'
  on-tertiary: '#ffffff'
  tertiary-container: '#739ec8'
  on-tertiary-container: '#003556'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffd9de'
  primary-fixed-dim: '#ffb2be'
  on-primary-fixed: '#3f0015'
  on-primary-fixed-variant: '#7f253d'
  secondary-fixed: '#b9eed7'
  secondary-fixed-dim: '#9ed1bc'
  on-secondary-fixed: '#002117'
  on-secondary-fixed-variant: '#1d4f3f'
  tertiary-fixed: '#cee5ff'
  tertiary-fixed-dim: '#a0cbf7'
  on-tertiary-fixed: '#001d33'
  on-tertiary-fixed-variant: '#194a6f'
  background: '#fdf9f5'
  on-background: '#1c1c19'
  surface-variant: '#FFF0F3'
  doyu-petal: '#F48FA4'
  doyu-coral: '#FF8F75'
  outline-soft: '#E0CDD0'
  text-primary: '#201A1C'
  text-secondary: '#524347'
typography:
  display-lg:
    fontFamily: Noto Sans SC
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.25px
  display-md:
    fontFamily: Noto Sans SC
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
  headline-lg:
    fontFamily: Noto Sans SC
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  headline-lg-mobile:
    fontFamily: Noto Sans SC
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  title-lg:
    fontFamily: Noto Sans SC
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 26px
  body-lg:
    fontFamily: Noto Sans SC
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Noto Sans SC
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  label-md:
    fontFamily: Noto Sans SC
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
  label-sm:
    fontFamily: Noto Sans SC
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 24px
  xxl: 48px
  gutter: 16px
  margin-page: 24px
---

## Brand & Style

The design system for the **Doyu** community is built on the philosophy of **"Restrained Vitality."** It balances the warmth of a handmade craft community with the precision of a modern e-commerce platform. The aesthetic is curated specifically for young women, blending **Apple’s minimalist spatial logic** (generous whitespace, clear hierarchy) with **Material You’s organic vibrancy**.

The visual language avoids "childish" tropes by utilizing refined typography and sophisticated frosted glass effects. The "perler bead" identity is subtly integrated through pixel-inspired structural elements and "BeadCluster" decorative motifs rather than literal toy-like decorations. The result is an interface that feels expressive and creative during discovery, yet professional and trustworthy during commerce transactions.

**Key Stylistic Pillars:**
- **Minimalism:** Use of Apple-style white space to let user-generated bead art stand out.
- **Modern Craft:** Tactile feedback through spring-based motion and subtle elevation.
- **Professional Playfulness:** A "light, cute, handmade" feel achieved through soft roundedness and a petal-inspired palette, avoiding neon or aggressive gradients.

## Colors

The palette is anchored by **Warm White (#FFFBF7)**, which provides a softer, more "paper-like" canvas than pure white, enhancing the handmade craft feel. 

- **Primary (Petal Pink):** Used for core interactive elements and brand signaling. It is tuned for readability against the warm background.
- **Secondary (Mint Green):** Dedicated to success states, rewards, and "Check-in" features.
- **Tertiary (Sky Blue):** Utilized for informational tooltips and AI-powered feature highlights.
- **Doyu Coral:** Reserved exclusively for "Hot" or "Recommended" tags to provide a warm, energetic accent without breaking the soft palette.

**Usage Rules:**
- In **Dark Mode**, surfaces transition to a deep "Warm Black" (#1A1215) and use **Surface Tinting** (tonal overlays) rather than traditional drop shadows to maintain depth.
- Avoid neon or high-saturation purples. Use **Surface Variant (#FFF0F3)** for subtle section grouping.

## Typography

The system utilizes **Noto Sans SC** to ensure a clean, modern, and highly legible experience across all languages. 

- **Display & Headline:** Bold weights are used for page titles and large numeric counters (like bead counts). For mobile screens, headlines scale down to ensure no text wrapping or "crowding" occurs.
- **Body & Content:** A generous 0.5px letter spacing is applied to `body-lg` to improve readability in long-form community posts.
- **Commerce:** Price points and inventory status must always use `title-lg` or `body-lg` with high contrast to ensure trust and clarity.
- **Labels:** Used for tags and metadata; these should remain uppercase or medium weight to distinguish them from body copy.

## Layout & Spacing

The layout follows a **4dp baseline grid** to ensure mathematical harmony. 

- **Grid Model:** A flexible 12-column system is used for tablet/desktop, while mobile relies on a single-column flow with **24px (SpaceXl)** page margins.
- **Rhythm:** Internal card padding is strictly **16px (SpaceLg)** to maintain the "Apple-style" airy feel.
- **Reflow Rules:**
    - **Community Feed:** Uses a staggered masonry grid for bead patterns.
    - **Commerce/Mall:** Uses a structured, symmetrical grid to emphasize stability and trust.
- **Haze Effects:** Top navigation bars and bottom tabs utilize a 20px background blur (frosted glass) to allow content to bleed through subtly, maintaining a sense of depth during scrolling.

## Elevation & Depth

Hierarchy is established through **Tonal Layering** and **Ambient Shadows**.

- **Shadow Character:** Shadows are extra-diffused and low-opacity, tinted with a hint of the primary brand color (`#E8778E` at 5-10% opacity) to avoid "dirty" grey shadows on the warm white background.
- **Levels:**
    - **Level 1 (1dp):** Standard community cards.
    - **Level 2 (3dp):** Interactive elements like Bottom Navigation or hovered items.
    - **Level 3 (6dp):** Floating Action Buttons (FAB) used for "Create" or "AI Puzzle" triggers.
    - **Level 4 (12dp):** Modal sheets and critical commerce dialogs.
- **Dark Mode Adjustment:** Shadows are disabled in Dark Mode. Instead, depth is conveyed by shifting surface colors toward the primary tint (the higher the elevation, the lighter the surface).

## Shapes

The shape language is **Organic and Friendly**, mirroring the rounded nature of perler beads.

- **Base Radius (8px):** Used for small inputs, search bars, and utility tags.
- **Standard Radius (16px):** Applied to primary buttons and dialog containers.
- **Large Radius (20px - 28px):** Used for hero cards and Bottom Sheets. 
- **Consistency:** All containers must feature rounded corners; sharp 90-degree angles are strictly forbidden to maintain the "soft" brand personality.

## Components

- **Buttons:** Primary buttons use a solid Petal Pink fill with white text. Secondary buttons use the `primaryContainer` (#FFE0E6) background. All buttons utilize a "Spring" animation (Scale 1.0 → 0.97) on press for tactile feedback.
- **Chips & Tags:** Small, pill-shaped containers with `label-md` text. Use `surfaceVariant` for neutral tags and `DoyuMint` for "Completed" patterns.
- **Cards:** Community cards should be borderless with Level 1 elevation. Commerce cards require a subtle `outline-soft` border to define the product boundaries clearly.
- **Input Fields:** Soft-rounded (ShapeXs) with a warm-grey border. On focus, the border transitions to Petal Pink with a soft outer glow.
- **Navigation:** The bottom bar uses frosted glass with clear, minimalist linear icons. Avoid emoji; use custom "pixel-bead" styled icons for the AI and Community tabs.
- **Progress/Loading:** Utilize "BeadDot" skeleton screens where gray circles pulse in a grid pattern, mimicking the layout of a perler bead pegboard.