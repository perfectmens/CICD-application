# Design Tokens — Dual-Tone Neumorphic UI

```css
:root {
  --ui-bg: #F6F6F7;
  --ui-surface: #FFFFFF;
  --ui-navy: #0A0D2F;
  --ui-blue-gray: #223B57;
  --ui-steel: #8C929C;
  --ui-light-gray: #BCBCBF;

  --ui-teal: #11CFC9;
  --ui-cyan: #47B3E2;
  --ui-muted-blue: #496D89;

  --ui-orange: #F68420;
  --ui-soft-orange: #D68A51;
}
```

## Semantic aliases

```css
:root {
  --tone-intent: var(--ui-orange);
  --tone-intent-soft: var(--ui-soft-orange);
  --tone-state: var(--ui-teal);
  --tone-info: var(--ui-cyan);
  --tone-info-muted: var(--ui-muted-blue);
  --tone-neutral-strong: var(--ui-navy);
  --tone-neutral: var(--ui-blue-gray);
  --tone-muted: var(--ui-steel);
}
```

Keep accent colors out of large surfaces by default. Prefer using semantic aliases in component APIs.
