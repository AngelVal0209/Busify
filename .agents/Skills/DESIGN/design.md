---
name: Efficient Transit
colors:
  surface: '#f8f9ff'
  surface-dim: '#ccdbf3'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e6eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d5e3fc'
  on-surface: '#0d1c2e'
  on-surface-variant: '#414754'
  inverse-surface: '#233144'
  inverse-on-surface: '#eaf1ff'
  outline: '#727785'
  outline-variant: '#c1c6d6'
  surface-tint: '#005bc0'
  primary: '#005bbf'
  on-primary: '#ffffff'
  primary-container: '#1a73e8'
  on-primary-container: '#ffffff'
  inverse-primary: '#adc7ff'
  secondary: '#795900'
  on-secondary: '#ffffff'
  secondary-container: '#febf0d'
  on-secondary-container: '#6d5000'
  tertiary: '#006d2c'
  on-tertiary: '#ffffff'
  tertiary-container: '#008939'
  on-tertiary-container: '#ffffff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d8e2ff'
  primary-fixed-dim: '#adc7ff'
  on-primary-fixed: '#001a41'
  on-primary-fixed-variant: '#004493'
  secondary-fixed: '#ffdfa0'
  secondary-fixed-dim: '#fbbc05'
  on-secondary-fixed: '#261a00'
  on-secondary-fixed-variant: '#5c4300'
  tertiary-fixed: '#89fa9b'
  tertiary-fixed-dim: '#6ddd81'
  on-tertiary-fixed: '#002108'
  on-tertiary-fixed-variant: '#005320'
  background: '#f8f9ff'
  on-background: '#0d1c2e'
  surface-variant: '#d5e3fc'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 12px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  container-margin: 16px
  gutter: 12px
---

## Brand & Style

The brand personality focuses on three core pillars: efficiency, safety, and user-friendliness. The design system is engineered to reduce cognitive load for travelers who may be in high-stress, time-sensitive environments. 

The aesthetic follows a **Corporate / Modern** direction with a focus on high-utility components. It utilizes a card-based architecture to organize complex travel data into digestible segments. The style emphasizes clarity through ample whitespace and a rigorous hierarchy, ensuring that critical information like departure times and gate numbers are never missed. The emotional response should be one of calm reliability—positioning the application as a dependable companion for daily commutes and long-distance travel.

## Colors

The palette is anchored by **Deep Trust Blue**, a color chosen to communicate institutional stability and safety. **Energy Orange** serves as a high-contrast accent for primary calls to action (CTAs), ensuring they remain visible even in bright outdoor light or low-quality screen settings. 

The neutral scale utilizes a **Slate Gray** range to maintain a sophisticated, balanced look without the harshness of pure black. A success green (tertiary) is included specifically for "Confirmed" statuses and "On-time" indicators.

- **Primary:** Navigation, headers, and active states.
- **Secondary:** High-impact CTAs like "Book Now" or "Pay."
- **Neutrals:** Text hierarchy, borders, and background layering.
- **Surface:** Off-white backgrounds (#F8FAFC) to reduce glare and improve readability.

## Typography

This design system uses **Inter** for all typography levels to leverage its exceptional legibility on mobile screens, particularly for numerical data. 

The scale is designed for high-density information. **Headline-LG** is reserved for main page titles, while **Headline-MD** is the standard for card titles (e.g., Destination Names). **Label-SM** is utilized for metadata such as "Platform Number" or "Bus ID," using a heavier weight and uppercase styling to ensure small-scale clarity. 

Line heights are generous to prevent visual crowding in multi-row bus schedules.

## Layout & Spacing

The layout utilizes a **fluid grid** optimized for mobile devices. It follows an 8px rhythmic scale for consistent vertical and horizontal pacing.

- **Mobile (Base):** A 4-column grid with 16px side margins and 12px gutters.
- **Tablet:** An 8-column grid with 32px side margins.
- **Desktop:** A max-width fixed container of 1140px, centered, with a 12-column grid for administrative or web-booking views.

Spacing is applied to create "Logical Grouping." For example, 4px (XS) separates a label from its value, while 16px (MD) separates distinct sections within a booking card. Touch targets for interactive elements are strictly maintained at a minimum of 48x48px.

## Elevation & Depth

Visual hierarchy is established through **Ambient Shadows** and **Tonal Layers**. 

1.  **Level 0 (Base):** The background surface uses a subtle Slate-50 tint (#F8FAFC).
2.  **Level 1 (Cards/Lists):** Content is housed in white containers with a soft, diffused shadow (0px 4px 12px rgba(0, 0, 0, 0.05)). This separates travel options from the background.
3.  **Level 2 (Floating Elements):** Actionable floating buttons or active modals use a more pronounced shadow (0px 8px 24px rgba(0, 0, 0, 0.12)) to indicate their proximity to the user.
4.  **Interactive Depth:** When pressed, cards and buttons lose their elevation (scale down slightly or shadow reduction) to provide tactile feedback.

## Shapes

The shape language is defined by **Rounded** geometry. Consistent with the user-friendly brand personality, sharp corners are avoided to make the interface feel more approachable.

- **Standard Elements:** Cards, input fields, and large containers use a 16px (`rounded-lg`) corner radius.
- **Small Elements:** Buttons, chips, and checkboxes use an 8px (`rounded-md`) radius.
- **Indicators:** Status badges (e.g., "On Time") use a full pill-shape to distinguish them from interactive buttons.

## Components

### Buttons
- **Primary:** Filled Blue (#1A73E8) with white text.
- **Action (CTA):** Filled Orange (#FBBC04) with dark slate text for maximum contrast.
- **Secondary:** Outlined Blue with 1.5px border width.

### Cards
Booking cards are the primary vehicle for data. They must include:
- A header area for Route Name and Price.
- A central body for the "Timeline" visualization (Dots and lines connecting departure/arrival).
- A footer for amenities (Wi-Fi, Power, etc.) using clear icons.

### Input Fields
Inputs use a 16px corner radius with a Slate-200 border. On focus, the border thickens to 2px in Primary Blue. Labels are always visible above the field to ensure context is never lost during data entry.

### Data Visualization: Timetables
Timetables must be rendered linearly. Use a vertical or horizontal line (Slate-200) with Primary Blue nodes to represent stops. Current time is indicated with a Secondary Orange pulse if the bus is currently in transit.

### Chips & Status
Use low-saturation background tints with high-saturation text for status chips (e.g., a light green background with dark green text for "Confirmed").