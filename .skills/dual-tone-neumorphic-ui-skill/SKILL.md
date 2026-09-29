---
name: dual-tone-neumorphic-ui
trigger: >-
  Design, redesign, implement, or review application UI/UX using the exact dual-tone
  neumorphic visual language defined here. Use this skill whenever the user asks for
  a neumorphic, soft-UI, tactile, premium white-surface interface with balanced orange
  and teal accents, even if they do not explicitly name this skill. Apply it to dashboards,
  control panels, admin apps, mobile/web interfaces, forms, settings, analytics, cards,
  buttons, charts, sliders, navigation, and component systems when this visual direction
  is requested or clearly implied.
description: >-
  Create a premium, minimal, soft-neumorphic UI system with a 90% neutral white/off-white
  foundation and a dual-tone semantic accent system: orange for user intent/actions and
  teal for system state/positive status. Preserve soft raised/inset depth, generous whitespace,
  rounded geometry, restrained outlines, and low-noise visual hierarchy. Treat this skill as
  the source of truth for color roles, depth, state behavior, component styling, and visual
  balance. Do not replace the style with generic glassmorphism, flat UI, heavy gradients,
  dark-mode-first styling, or arbitrary accent colors unless the user explicitly requests it.
compatibility: Requires an image-capable design/coding agent when using the bundled visual reference; otherwise the written design tokens are sufficient.
---

# Dual-Tone Neumorphic UI

## Goal

Build interfaces that feel like **premium soft physical controls** while using color as a **semantic layer** rather than as decoration.

The core principle is:

> **Neumorphism communicates physical interaction. Orange and teal communicate meaning.**

A user should understand *what is interactive* from elevation/inset depth and *what the interaction means* from the accent color.

Use the bundled `assets/original-neumorphic-reference.png` as the visual north star whenever visual comparison is possible. Match its overall airiness, soft lighting, spacing, rounded geometry, and restrained accent usage rather than copying individual labels or content.

## Non-negotiable visual identity

### Base composition

Target an approximate visual distribution across a screen:

- **90% neutral surfaces / whitespace**
- **5% orange accent presence**
- **3% teal accent presence**
- **2% other supporting colors**

This is a visual-balance heuristic, not a requirement to tint exactly 5% of pixels. Large surfaces remain white/off-white; accents stay concentrated on controls, indicators, icons, outlines, key metrics, and state markers.

### Palette

#### Foundation

| Role | Hex | Use |
|---|---|---|
| Background | `#F6F6F7` | App canvas, page background |
| Surface | `#FFFFFF` | Elevated cards, controls, panels |
| Primary outline/text | `#0A0D2F` | High-emphasis text, critical outline/icon use |
| Secondary text | `#223B57` | Headings, secondary emphasis |
| Steel gray | `#8C929C` | Muted labels, disabled text, neutral icons |
| Light gray | `#BCBCBF` | Subtle borders, dividers, low-emphasis decoration |

#### Cool accents

| Role | Hex | Use |
|---|---|---|
| Teal | `#11CFC9` | Active/positive/system state |
| Cyan blue | `#47B3E2` | Informational or secondary data emphasis |
| Muted blue | `#496D89` | Supporting information, secondary data |

#### Warm accents

| Role | Hex | Use |
|---|---|---|
| Orange | `#F68420` | Primary action, attention, user intent |
| Soft orange | `#D68A51` | Warm secondary emphasis, subtle supporting accent |

### Semantic color doctrine

Use this rule consistently:

- **Orange = Intent.** Use for actions the user is being invited to initiate: Create, Buy, Start, Submit, Send, Add, Confirm, important attention cues, and primary action emphasis.
- **Teal = State.** Use for things that are currently true or healthy: Connected, Online, Enabled, Active, Selected, Synced, Successful, current system value, and live status.
- **Blue/Cyan = Information.** Use as supporting data emphasis, navigation context, secondary metrics, and informational visualization where orange/teal would be semantically overloaded.
- **Neutral grays/navy = structure.** Use for text, quiet controls, disabled states, and structural contrast.

Do not use orange and teal randomly just to make a screen colorful. Their jobs must remain legible.

## Neumorphic material system

### Surfaces

Default surfaces should be near-white with extremely subtle tonal separation. Avoid visible hard borders when depth can communicate the hierarchy.

Use two light-shadow families:

- **Raised:** a soft light edge/highlight plus a soft darker ambient shadow creates a floating tactile object.
- **Inset:** the component appears pressed into the surface using reversed/lightly internal shadows; use for selected, pressed, or recessed controls.

Avoid cartoonishly deep shadows. The interface should feel like soft molded material, not glossy plastic.

### Geometry

Prefer:

- Rounded corners and circular controls.
- Consistent corner radius families across the system.
- Generous whitespace around groups.
- Clear alignment and optical centering.
- Compact iconography inside larger tactile surfaces.

Favor clean geometric controls over decorative ornament.

### Lighting

Use one consistent soft-light direction across a screen. Keep highlight and shadow transitions broad and feathered. The depth system must remain coherent between buttons, cards, sliders, dials, and circular shortcuts.

## Component recipes

### Buttons

**Default:** white raised surface, neutral label/icon.

**Primary action:** white or very lightly accented raised surface with orange label/icon/outline; use orange fill sparingly when the action needs strong emphasis.

**Pressed:** inset surface with stronger semantic accent.

**Selected/active system control:** inset surface with teal indicator or teal label/icon.

**Disabled:** flatter surface, muted gray text/icon, dramatically reduced accent.

Do not make every button a saturated colored pill. The white tactile surface is part of the identity.

### Cards

Keep cards predominantly white. Use accent color for small zones only:

- status dot
- icon
- thin outline
- key metric
- action control
- progress segment

Cards should have strong internal spacing and hierarchy without heavy container borders.

### Toggles

Use a white raised track with a clearly visible thumb.

- Off: neutral gray.
- On/healthy: teal.
- User-triggered destructive or attention state: orange only when semantically appropriate.

### Sliders

Keep the track mostly neutral. Use a colored thumb and/or active track segment.

Use teal for live/current system values and orange when the slider is directly controlling an active user intent.

Avoid using both accent colors on one slider unless they represent two distinct dimensions.

### Dials / knobs

Use layered white rings and soft depth. Add thin semantic accent arcs or markers.

Recommended:

- Orange for user-directed setpoint/intent marker.
- Teal for current/live value or healthy/connected indicator.

Keep the main body neutral so the depth remains visible.

### Icon buttons

Use raised or inset circular/squircle surfaces. Let icon color carry semantic meaning. Keep neutral icons neutral.

### Navigation

Keep navigation mostly neutral. Use teal as the selected/current state and orange for a primary action within navigation when necessary. Do not color the entire navigation rail.

### Alerts and statuses

Prefer concise semantic indicators over large colored panels.

- Success/connected/healthy: teal.
- Attention/action required: orange.
- Error: use strong neutral/navy structure plus orange for attention; do not introduce red unless the product explicitly needs a separate safety/error semantic.

### Charts

Keep the chart area neutral. Use:

1. Orange for the primary user/business/action-oriented series.
2. Teal for comparison, current state, or healthy system series.
3. Blue/cyan for additional informational series.
4. Gray for baselines/grid/reference data.

Do not produce rainbow charts.

## Interaction state model

Map physical depth and semantic color together:

| State | Physical treatment | Accent behavior |
|---|---|---|
| Default | Raised | Neutral or subtle semantic hint |
| Hover | Slightly more elevated | Slight accent strengthening |
| Pressed | Inset | Semantic accent becomes clearer |
| Active | Inset/controlled depth | Teal commonly signals current state |
| Primary action | Raised | Orange emphasis |
| Success/connected | Raised or softly inset | Teal indicator |
| Disabled | Flattened | Steel gray / low contrast |
| Attention | Raised with restrained emphasis | Orange |
| Error | Controlled inset/raised | Orange + strong navy structure unless a true red semantic is required |

The physical state should not contradict the semantic state. A pressed control should look pressed even if it is teal; a disabled control should look inactive even if its default role is orange.

## Typography

Favor a clean modern sans-serif with a calm, premium tone.

- Strong hierarchy without oversized display typography everywhere.
- Use `#0A0D2F` for high-emphasis headings and key numbers.
- Use `#223B57` for secondary emphasis.
- Use `#8C929C` for low-emphasis metadata.
- Avoid excessive boldness.
- Keep labels short and centered inside tactile controls.

## Layout principles

- Preserve generous breathing room.
- Use a consistent spacing scale.
- Group related controls into coherent physical clusters.
- Use large anchor controls sparingly (for example, a central dial or key metric).
- Maintain optical symmetry where the interaction model calls for it.
- Let white space carry as much hierarchy as accent color.

## Visual balance checks

Before finalizing a design, inspect it with these questions:

1. Does the screen still read as mostly white/off-white before noticing the accents?
2. Does orange clearly represent an action/intent rather than generic decoration?
3. Does teal clearly represent a live/current/positive system state?
4. Are raised and inset surfaces visually obvious enough to communicate interaction without relying on color alone?
5. Are shadows soft and consistent rather than deep or muddy?
6. Is the use of orange and teal balanced across the product without forcing both colors into every component?
7. Would the screen still look coherent if one accent were temporarily removed?
8. Are text and controls readable enough for practical use rather than optimized only for a Dribbble-style screenshot?

## Accessibility and usability guardrails

Neumorphism can reduce affordance and contrast when overused. Preserve the aesthetic without sacrificing interaction clarity:

- Never rely on shadow alone to communicate an essential state.
- Pair semantic color with text, icon, shape, position, or state change.
- Maintain readable text contrast against the surface.
- Give focus states a visible treatment that survives low-contrast displays.
- Keep touch targets comfortably sized on interactive interfaces.
- Avoid tiny gray labels that disappear into the background.
- Provide explicit labels and accessible names in implementations.

## Implementation guidance

When implementing in a component framework:

1. Centralize the tokens above as theme variables.
2. Centralize raised/inset shadow recipes rather than hand-tuning each component.
3. Encode semantic roles (`intent`, `state`, `info`, `neutral`) rather than scattering literal color values.
4. Build components so `variant`, `state`, and `tone` are independent concepts.
5. Keep surfaces neutral by default and layer color through icons, indicators, text, outlines, or small fills.
6. Avoid gradients unless they are necessary for a very subtle accent treatment; the reference aesthetic is primarily flat, soft, and tonal.

A useful component API model is:

```text
Component
  ├─ variant: raised | inset | flat
  ├─ tone: neutral | orange | teal | blue
  ├─ state: default | hover | pressed | active | disabled | error
  └─ emphasis: low | medium | high
```

This makes it easier to preserve the design language as the application scales.

## Reference image

The bundled image `assets/original-neumorphic-reference.png` is a visual reference for composition, tactile depth, spacing, control grouping, and the overall orange/teal dual-tone balance. When the agent can inspect images, compare generated/implemented screens against it at a high level.

The image is a reference for **style and system behavior**, not a requirement to copy the exact content of the sample interface.

## Do / avoid

### Do

- Keep the canvas quiet and bright.
- Use white surfaces with soft elevation.
- Use orange and teal as semantic signals.
- Use subtle outlines and accents.
- Use consistent light/shadow direction.
- Prefer restrained, premium visual density.

### Avoid

- Turning the whole screen orange or teal.
- Applying an orange-to-teal gradient to everything.
- Heavy black shadows.
- Glassmorphism blur as the primary material language.
- Random accent colors.
- Neon UI, cyberpunk styling, or high-saturation surfaces.
- Flat cards that lose all physical depth.
- Inconsistent corner radii or lighting directions.

## When adapting to a user's existing UI

Preserve the application's information architecture unless asked to redesign it. Apply this skill to:

- surfaces
- control hierarchy
- state semantics
- component styling
- color usage
- spacing rhythm
- depth/elevation

Do not blindly copy the sample interface's content or layout when a different product requires different information architecture.

When the user says “make it exact,” interpret that as **exact visual language and design system behavior**: neutral neumorphic surfaces, tactile depth, the specified palette, the orange/teal semantic split, and the approximate 90/5/3/2 visual balance.
