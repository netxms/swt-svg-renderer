# CLAUDE.md

## Project Overview

SWT SVG Renderer — a lightweight library that parses SVG icons and renders them onto SWT `GC`. Multi-module Maven project targeting Java 11.

## Build & Test

```bash
mvn install          # build all modules, run tests
mvn test             # run tests only (from root or swt-svg-renderer/)
cd demo && mvn exec:java   # launch demo app
```

All 71 tests should pass. The `[Fatal Error]` lines in test output are expected — they come from SVGParserTest intentionally parsing malformed XML.

## Module Layout

- `swt-svg-renderer/` — the library (standalone POM, no parent dependency)
- `demo/` — SWT demo app that depends on the library
- Root `pom.xml` — aggregator only, no shared config

## Code Conventions

- Java 11 — no records, sealed interfaces, pattern matching instanceof, switch expressions, or text blocks
- Brace style: opening brace on next line (Allman style)
- 3-space indentation
- All node/data classes are immutable and `final`
- Internal classes live in `org.netxms.ui.svg.internal`; public API in `org.netxms.ui.svg` (3 classes: `SVGImage`, `SVGParseException`, `ScaleMode`)
- SWT dependency is `provided` scope in the library — the host application supplies it

## Architecture

Parse phase (no SWT needed):
`SVG XML → DOM → SVGParser → SVGDocument (immutable tree of SVGNode subclasses)`

Render phase (requires SWT GC):
`SVGDocument + GC + bounds + currentColor → SVGRenderer → pixels`

Key design decisions:
- Arcs (A/a path command) are converted to cubic Beziers at parse time (SVG spec F.6.5)
- All shapes rendered via SWT `Path` objects for float precision under transforms
- Color objects cached per `render()` call, disposed at end
- Style inheritance resolved at render time (not parse time) because `currentColor` depends on the render call

## Key Files

| File | Role |
|------|------|
| `SVGImage.java` | Public API — factory methods + render overloads |
| `SVGParser.java` | DOM walk, element dispatch, builds node tree |
| `SVGRenderer.java` | Walks node tree, manages GC state, draws shapes |
| `PathDataParser.java` | SVG path `d` attribute tokenizer + parser |
| `SVGColor.java` | Color parsing (hex, rgb, named, currentColor) |
| `SVGTransform.java` | Transform attribute → affine matrix |
| `StyleProps.java` | Style property parsing + inheritance resolution |

## Testing

Tests are in `swt-svg-renderer/src/test/`. JUnit 5. Test SVG files in `src/test/resources/`.

- `SVGColorTest` — color parsing (hex, rgb, named, currentColor, none, inherit)
- `StylePropsTest` — style inheritance chain
- `SVGTransformTest` — each transform type, compound transforms
- `PathDataParserTest` — all path commands, compact notation, arc flag parsing
- `SVGParserTest` — end-to-end parse of SVG files
- `SVGImageTest` — public API, dimensions, error handling

## Common Pitfalls

- SVG arc flags are single-digit (0/1) and don't need separators — handled by `Tokenizer.nextFlag()`
- SWT `Color`, `Path`, `Transform` must be disposed — renderer handles this in try/finally
- `GC.setTransform()` uses a mutable `Transform` object — save/restore around each node
