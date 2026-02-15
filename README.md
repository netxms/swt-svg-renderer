# SWT SVG Renderer

A lightweight SVG renderer for SWT that parses SVG icons into an immutable in-memory tree and renders them directly onto SWT `GC`. No AWT bridge, no external dependencies beyond `org.w3c.dom` (included in JDK).

Designed for icon rendering with a constrained SVG subset. Unsupported elements are silently skipped, so SVGs exported from common editors (Inkscape, Illustrator) work as long as the visible content uses supported features.

## Features

- Parse once, render at any size — resolution-independent icons
- `currentColor` support for icon theming (light/dark mode, disabled state)
- Uniform and stretch scaling modes
- Immutable after parse — thread-safe, no SWT resource ownership
- All SWT resources (`Color`, `Path`, `Transform`) created and disposed per render call
- Java 11+, zero dependencies beyond SWT and JDK

## Supported SVG Subset

**Elements:** `<svg>`, `<g>`, `<path>`, `<rect>` (with `rx`/`ry`), `<circle>`, `<ellipse>`, `<line>`, `<polyline>`, `<polygon>`

**Path commands:** M, L, H, V, C, S, Q, T, A, Z (absolute and relative)

**Presentation attributes:** `fill`, `fill-opacity`, `fill-rule`, `stroke`, `stroke-width`, `stroke-opacity`, `stroke-linecap`, `stroke-linejoin`, `opacity`, `transform`, `display`, inline `style`

**Colors:** `#RGB`, `#RRGGBB`, `rgb(r,g,b)`, 147 named CSS colors, `currentColor`, `none`, `inherit`

**Transforms:** `translate`, `scale`, `rotate`, `matrix`, `skewX`, `skewY`

## API

The public API consists of three classes in `org.netxms.ui.svg`:

### SVGImage

```java
// Parse
SVGImage icon = SVGImage.createFromFile(new File("icon.svg"));
SVGImage icon = SVGImage.createFromStream(inputStream);
SVGImage icon = SVGImage.createFromString(svgContent);

// Render (black, uniform scale)
icon.render(gc, x, y, width, height);

// Render with theme color
icon.render(gc, x, y, width, height, currentColor);

// Render with theme color and scale mode
icon.render(gc, x, y, width, height, currentColor, ScaleMode.STRETCH);

// Query intrinsic dimensions
float w = icon.getWidth();      // -1 if unknown
float h = icon.getHeight();     // -1 if unknown
float ar = icon.getAspectRatio(); // -1 if unknown
```

### ScaleMode

| Value | Behavior |
|-------|----------|
| `UNIFORM` | Fit inside the target box preserving aspect ratio, centered (default) |
| `STRETCH` | Stretch to fill the entire box |

### SVGParseException

Checked exception thrown by the `createFrom*` factory methods when the SVG XML is malformed or the root element is not `<svg>`.

## Usage Example

```java
// Load icon once at startup
SVGImage icon = SVGImage.createFromStream(
      getClass().getResourceAsStream("/icons/settings.svg"));

// Render in a paint listener — adapts to any size
canvas.addPaintListener(e -> {
    icon.render(e.gc, 0, 0, canvas.getSize().x, canvas.getSize().y,
                e.gc.getForeground());
});
```

## Building

```
mvn install
```

This builds both modules:
- `swt-svg-renderer` — the library (with javadoc and source jars)
- `demo` — a sample SWT application

### Running the Demo

```
cd demo
mvn exec:java
```

## Project Structure

```
pom.xml                             — aggregator POM
swt-svg-renderer/
  pom.xml                           — library (standalone, no parent)
  src/main/java/org/netxms/ui/svg/
    SVGImage.java                   — public API: parse + render
    SVGParseException.java          — checked parse exception
    ScaleMode.java                  — UNIFORM / STRETCH enum
  src/main/java/org/netxms/ui/svg/internal/
    ...                             — parser, renderer, node tree
  src/test/                         — JUnit 5 tests + SVG fixtures
demo/
  pom.xml                           — demo app
  src/main/java/.../SVGDemo.java    — SWT shell with sample icons
  src/main/resources/icons/         — sample SVG icons
```

## Requirements

- Java 11+
- SWT (provided scope — supplied by the host application)
