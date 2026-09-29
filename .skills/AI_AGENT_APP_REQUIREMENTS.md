# AI Agent Instructions — Dual-Tone Neumorphic Application

## 1. Mission
Build and maintain the application as a premium, production-ready UI system based on the supplied reference design and the `dual-tone-neumorphic-ui` skill.

The visual identity is **soft neumorphism + dual-tone semantics**:
- Neumorphism communicates physical structure, elevation, interaction, and touch.
- Orange communicates user intent, action, urgency, and warm emphasis.
- Teal communicates system state, active/healthy/connected status, and positive feedback.
- The interface remains predominantly neutral and white so the accents have meaning.

Do not reinterpret this as a generic modern dashboard, generic glassmorphism, flat Material UI, or a neon-gradient interface.

## 2. Non-Negotiable Visual Target
Treat the supplied reference image as the visual north star.

Target composition:
- soft white/off-white surfaces
- rounded, tactile controls
- subtle but visible depth
- restrained shadows
- clean geometric spacing
- minimal visual noise
- premium consumer-electronics / control-panel feel
- orange and teal used as a balanced dual-tone system

The UI should look like the same design system even when creating screens that do not appear in the reference image.

## 3. Color System
### Foundation — about 90%
- Background: `#F6F6F7`
- Surface: `#FFFFFF`
- Primary text / outline: `#0A0D2F`
- Secondary text: `#223B57`
- Steel gray: `#8C929C`
- Light gray: `#BCBCBF`

### Warm accent — about 5%
- Primary orange: `#F68420`
- Soft orange: `#D68A51`

Meaning:
- primary actions
- calls to action
- user intent
- important emphasis
- warm attention / warning
- active action controls

### Cool accent — about 3%
- Teal: `#11CFC9`
- Cyan blue: `#47B3E2`
- Muted blue: `#496D89`

Meaning:
- connected
- online
- enabled
- selected
- healthy
- synchronized
- system status
- informational controls

### Remaining ~2%
Use other neutrals only when necessary. Do not introduce random accent colors.

## 4. Core Semantic Rule
Always think:

**Orange = What the user is doing / wants to do.**

**Teal = What the system says is happening / true.**

Examples:
- `Create`, `Buy`, `Start`, `Submit`, `Send`, `Add` → orange
- `Connected`, `Online`, `Synced`, `Enabled`, `Active`, `Verified` → teal

Do not use orange and teal randomly just for decoration.

## 5. Neumorphic Construction Rules
All important controls should feel tactile without becoming overly inflated.

### Raised state
Use:
- light upper-left illumination
- soft lower-right shadow
- neutral surface
- generous radius

### Pressed / selected state
Use:
- inset/internal shadow
- reduced elevation
- accent indicator or accent content

### Disabled state
Use:
- low-contrast neutral text
- flatter elevation
- reduced accent intensity
- never rely on color alone

### Shadow guidance
Shadows must feel soft and broad, not harsh or black.
Avoid heavy drop shadows that create a floating-card aesthetic.
Avoid using shadows as the only indication of an interactive state.

## 6. Component Behavior
### Buttons
Default: neutral raised surface.
Primary action: orange label/icon/edge treatment.
Pressed: inset + orange emphasis.
Secondary positive/system action: teal.
Disabled: neutral + low contrast.

### Toggles
Off: neutral neumorphic control.
On: teal track/thumb indicator.
Do not make the entire UI teal just because a toggle is on.

### Status indicators
Use small teal dots, lines, borders, or icons for healthy/active status.
Use orange for attention or action-required states.

### Inputs
Keep fields neutral.
Use a subtle inset treatment for the field body.
Use orange focus for an action-oriented workflow where appropriate.
Use teal when the field is validated, synchronized, or active as a system state.

### Cards
Cards are primarily white/off-white.
Accent only key elements: icon, small line, status dot, CTA, selected edge, or key metric.
Do not fill entire cards with orange or teal unless there is a clear semantic reason.

### Sliders
Use a neutral track and a colored thumb/progress segment.
Use orange for user-controlled emphasis and teal for current/healthy/system state.

### Circular dials / knobs
Preserve the tactile concentric-ring construction from the reference.
Use accent color for the active ring/marker while keeping the body neutral.
Avoid turning the whole dial into a colored disk.

### Navigation
Navigation should stay neutral and spacious.
Use teal to show the current active/selected system section.
Use orange only when the selected item represents an explicit user action or high-priority task.

### Tables / analytics
Keep table surfaces neutral.
Use orange and teal as semantic data series rather than decoration.
Prefer:
- orange = user/action metric
- teal = system/health/positive metric
- gray = supporting data

## 7. Layout and Spacing
Use generous whitespace.
Prioritize balance, symmetry, and visual breathing room.
Prefer consistent spacing tokens instead of one-off margins.

Recommended baseline scale:
`4, 8, 12, 16, 24, 32, 40, 48`

Use larger spacing around major groups and tighter spacing within components.

## 8. Typography
Typography should be clean, modern, and restrained.
Recommended hierarchy:
- page title: strong navy
- section title: navy / dark blue-gray
- body: dark blue-gray
- secondary/meta: steel gray
- accent labels: orange or teal according to semantic role

Do not use oversized decorative typography to compensate for weak hierarchy.

## 9. Interaction Design
Every interactive element must have understandable states:
- default
- hover (where applicable)
- focus
- pressed
- selected/active
- disabled
- loading (where applicable)
- success/error where applicable

State should be communicated by a combination of:
- elevation
- inset/raised treatment
- iconography
- text
- accent color

Never use color as the sole signal for important status.

## 10. Accessibility Requirements
The visual style must not compromise usability.

The agent must:
- maintain readable text contrast
- provide visible keyboard focus
- support keyboard navigation for controls
- provide semantic labels for icon-only controls
- not rely only on subtle shadows to communicate state
- not rely only on orange vs teal for critical meaning
- preserve usable hit targets on touch devices
- provide reduced-motion behavior where motion is used

Neumorphism is a visual layer, not a reason to weaken accessibility.

## 11. Responsive Behavior
The design system must work across desktop, tablet, and mobile.

Do not shrink the reference image proportionally and call that responsive design.
Recompose the layout while preserving:
- visual hierarchy
- tactile control language
- accent semantics
- spacing rhythm
- component identity

On small screens:
- stack groups logically
- preserve minimum touch sizes
- collapse secondary information before primary actions
- keep major controls visually dominant

## 12. Product Behavior Expectations
Build real application behavior, not a static mockup.

Where the product requires controls, implement:
- actual state changes
- validation
- loading states
- success/error feedback
- persistence where appropriate
- sensible empty states
- sensible disabled states
- confirmation for destructive actions

The visual design must remain coherent during all states.

## 13. Content and Iconography
Use concise labels.
Prefer simple geometric icons with the same visual weight as the reference.
Avoid overly detailed illustrations unless the product explicitly requires them.

Icons should inherit semantic color from their state rather than being colored arbitrarily.

## 14. Engineering Rules for an AI Coding Agent
Before creating a new component:
1. Check whether an existing component can be reused.
2. Check whether the component should belong in the shared design system.
3. Apply semantic color roles rather than choosing colors ad hoc.
4. Implement all relevant interaction states.
5. Verify responsive behavior.
6. Verify accessibility.
7. Verify that the component still visually belongs to the reference system.

Do not create one-off CSS/shadow values unless necessary.
Prefer shared design tokens and component variants.

## 15. Design-System Tokens
At the code level, create named tokens such as:

```text
color.bg
color.surface
color.text.primary
color.text.secondary
color.text.muted
color.accent.orange
color.accent.orangeSoft
color.accent.teal
color.accent.cyan
color.accent.blueMuted
color.border.light
shadow.raised
shadow.inset
radius.sm
radius.md
radius.lg
spacing.*
```

Do not scatter hex values across component files.

## 16. Quality Gate
Before considering a screen complete, check:

### Visual
- Does it look recognizably like the reference system?
- Is the interface still approximately 90% neutral?
- Are orange and teal balanced across the experience?
- Does orange communicate intent?
- Does teal communicate state?
- Are shadows soft and consistent?
- Are surfaces tactile rather than flat?

### UX
- Can a user immediately tell what is clickable?
- Are selected and pressed states obvious?
- Are important states understandable without color alone?
- Are actions and feedback clearly separated?

### Engineering
- Are design tokens reused?
- Are components reusable?
- Are states implemented?
- Is responsive behavior intentional?
- Is keyboard/touch accessibility preserved?

## 17. Anti-Patterns — Never Drift Into These
Avoid:
- generic glassmorphism
- excessive gradients
- dark mode unless explicitly requested
- excessive orange or teal surface fills
- random accent colors
- hard black shadows
- flat Material-style buttons without tactile treatment
- inconsistent corner radii
- inconsistent shadow directions
- decorative animation with no UX purpose
- excessive borders that destroy the soft surface language
- low-contrast text that becomes unreadable
- using teal and orange interchangeably without semantic meaning

## 18. Reference Image
Use the supplied reference image as the visual comparison target whenever implementing or reviewing components.

The reference establishes:
- overall tone
- component proportions
- depth language
- corner treatment
- accent density
- orange/teal balance
- whitespace philosophy

When there is a conflict between a generic UI convention and the established design system, preserve the design system unless accessibility, platform conventions, or explicit product requirements require a change.

## 19. Default Agent Decision Rule
When requirements are ambiguous, prefer the option that:

**looks calmer, more tactile, more neutral, more semantically intentional, and closer to the supplied reference.**

The goal is not merely to make a pretty neumorphic screen. The goal is to make the entire application feel like one coherent premium product.
