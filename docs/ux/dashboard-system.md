# Dashboard and UX System

## UX objective
The product should feel like a high-quality investing application rather than an admin dashboard. Information hierarchy prioritizes total portfolio state, performance, exposure/risk, positions, market context and actionable research.

## Layout modes

Design three explicit responsive experiences:
- MOBILE
- TABLET
- DESKTOP

Do not implement desktop first and merely collapse cards until they fit.

## Desktop information architecture

Primary navigation: Dashboard, Portfolio, X-Ray, Assets, Market, Macro, News/Research, Workflows, Reports, Assistant, Settings.

Dashboard priority:
1. portfolio value and selected period return;
2. large performance chart;
3. asset allocation / X-Ray summary / risk summary;
4. positions;
5. watchlist/market context;
6. news impact;
7. AI briefing/workflow status.

## Mobile navigation

Bottom navigation baseline: Home, Portfolio, X-Ray, Research, More. Assistant is globally reachable without consuming a permanent bottom-nav slot if UX tests favor an overlay/action entry.

## Tablet

Two-column adaptive layout is the default target, with touch-friendly charts/cards and no hover-only functionality.

## Configurable dashboard

A dashboard owns a layout and widget instances. Widgets are registered through a `WidgetRegistry`; feature code does not hard-code the dashboard's entire composition.

Initial widget types:
- PortfolioValue
- Performance
- Allocation
- XRayOverview
- SectorExposure
- GeographyExposure
- CurrencyExposure
- Overlap
- Risk
- Positions
- Market/Watchlist
- NewsImpact
- AiBrief
- WorkflowStatus

Layouts may differ by device class.

## Design system

Build custom tokens/components on Angular/CDK primitives rather than accepting Angular Material's default visual identity. Tokens cover typography, spacing, radius, elevation, motion, breakpoints, semantic financial states and chart palettes.

Authority notes (R45):
- Git DTCG tokens under `design/tokens/**` are implementation-authoritative and mirrored to Figma.
- Figma nodes locked by `DES-*` are composition/presentation authority.
- Screenshots are verification evidence, not design authority.

Positive/negative/risk information must not rely on color alone.

## Charting

- Apache ECharts: exposure/treemap/heatmap/sunburst/bar/scatter/etc.
- TradingView Lightweight Charts: financial time-series/candlestick style views.

Chart adapters should shield feature code from library-specific configuration where practical.

## Portfolio X-Ray page

Tabs/sections: Overview, Holdings, Overlap, Sectors, Countries, Currencies, Asset Classes, Issuer Concentration, Index Exposure, Data Coverage/Freshness.

Always display data coverage/freshness near conclusions derived from underlying composition.

## PWA/offline

Application shell/static assets may be cached. Sensitive financial data and conversations are not persisted locally for offline use by default. The application must show a clear disconnected/read-only state rather than silently presenting stale data as current.
