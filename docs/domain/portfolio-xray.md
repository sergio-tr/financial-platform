# Portfolio X-Ray Domain Specification

## Purpose
Portfolio X-Ray is a first-class bounded context that calculates the portfolio's effective underlying exposure rather than only direct holdings.

## Authoritative inputs

- immutable PortfolioSnapshot;
- Instrument metadata/classification;
- InstrumentCompositionSnapshot;
- FX snapshot where conversion is required;
- benchmark/reference snapshots where explicitly selected.

All inputs carry provenance and timestamps.

## Core output

`PortfolioXRay` contains:
- direct exposure;
- look-through exposure;
- asset-class exposure;
- sector exposure;
- geography exposure;
- currency exposure;
- issuer exposure;
- index exposure;
- underlying holdings exposure;
- concentration analysis;
- overlap analysis;
- diversification analysis;
- coverage analysis;
- freshness analysis.

## Recursive look-through

The engine supports nested products such as mutual-fund -> ETF -> equity. Requirements:
- deterministic recursion;
- cycle detection;
- configurable maximum depth, initial default 5;
- explicit unknown/unresolved composition weight;
- composition as-of timestamp preserved at each level.

## Unknown composition

Unknown weight is never normalized away. Example: if 91.4% of a fund is known, X-Ray reports 91.4% known and 8.6% unknown. It must not redistribute the unknown 8.6% proportionally over known holdings.

## Overlap

Initial overlap metric:
`weightedOverlap(A,B) = sum(min(weightA_i, weightB_i))`
for normalized comparable underlying identifiers over the known intersection.

Reports must also state comparable-data coverage so a high overlap derived from incomplete compositions is not presented with false certainty.

## Concentration

Initial measures:
- top-1/top-5/top-10 exposure;
- Herfindahl-Hirschman Index (HHI);
- effective number of holdings derived from concentration;
- concentration by security, issuer, sector, geography and currency.

## Precision

Authoritative X-Ray calculations use `BigDecimal` and documented rounding only at presentation/reporting boundaries. Intermediate calculations must not be rounded for UI convenience.

## Data quality

Every X-Ray result includes:
- source snapshot IDs;
- computation timestamp;
- composition coverage;
- oldest/newest relevant composition timestamp;
- warnings for stale, partial, cyclic or unclassified data.

## AI boundary

The AI may explain an X-Ray result, connect it with market/macro/news context, or produce scenario narratives. The AI must never recompute or overwrite authoritative X-Ray metrics.
