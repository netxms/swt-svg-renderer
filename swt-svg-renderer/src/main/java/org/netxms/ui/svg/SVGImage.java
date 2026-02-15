package org.netxms.ui.svg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.netxms.ui.svg.internal.SVGDocument;
import org.netxms.ui.svg.internal.SVGParser;
import org.netxms.ui.svg.internal.SVGRenderer;

/**
 * Immutable, parsed SVG image ready for rendering on SWT GC.
 * Thread-safe for rendering (no mutable state after construction).
 * Not tied to any Display — all SWT resources are created and disposed per render call.
 */
public class SVGImage
{
   private final SVGDocument document;

   private SVGImage(SVGDocument document)
   {
      this.document = document;
   }

   /**
    * Parse SVG from a file.
    *
    * @param file the SVG file
    * @return parsed SVGImage
    * @throws SVGParseException if SVG is malformed or uses unsupported features
    */
   public static SVGImage createFromFile(File file) throws SVGParseException
   {
      try (InputStream is = new FileInputStream(file))
      {
         return createFromStream(is);
      }
      catch(IOException e)
      {
         throw new SVGParseException("Failed to read SVG file: " + file.getAbsolutePath(), e);
      }
   }

   /**
    * Parse SVG from an input stream.
    *
    * @param stream the input stream
    * @return parsed SVGImage
    * @throws SVGParseException if SVG is malformed or uses unsupported features
    */
   public static SVGImage createFromStream(InputStream stream) throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(stream);
      return new SVGImage(doc);
   }

   /**
    * Parse SVG from a string.
    *
    * @param svgContent the SVG content string
    * @return parsed SVGImage
    * @throws SVGParseException if SVG is malformed or uses unsupported features
    */
   public static SVGImage createFromString(String svgContent) throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(svgContent);
      return new SVGImage(doc);
   }

   /**
    * Render SVG into the given bounds using default settings:
    * ScaleMode.UNIFORM, currentColor = black.
    *
    * @param gc the graphics context to render to
    * @param x target x position in pixels
    * @param y target y position in pixels
    * @param width target width in pixels
    * @param height target height in pixels
    */
   public void render(GC gc, int x, int y, int width, int height)
   {
      render(gc, x, y, width, height, null, ScaleMode.UNIFORM);
   }

   /**
    * Render SVG into the given bounds.
    *
    * @param gc the graphics context to render to
    * @param bounds target rectangle in pixels
    */
   public void render(GC gc, Rectangle bounds)
   {
      render(gc, bounds.x, bounds.y, bounds.width, bounds.height, null, ScaleMode.UNIFORM);
   }

   /**
    * Render with explicit currentColor for icon theming.
    * Elements using "currentColor" or "inherit" will use this color.
    * Elements with explicit colors are unaffected.
    *
    * @param gc the graphics context to render to
    * @param x target x position in pixels
    * @param y target y position in pixels
    * @param width target width in pixels
    * @param height target height in pixels
    * @param currentColor the color to use for "currentColor" values, or null for black
    */
   public void render(GC gc, int x, int y, int width, int height, Color currentColor)
   {
      render(gc, x, y, width, height, currentColor, ScaleMode.UNIFORM);
   }

   /**
    * Render with explicit currentColor and scale mode.
    *
    * @param gc the graphics context to render to
    * @param x target x position in pixels
    * @param y target y position in pixels
    * @param width target width in pixels
    * @param height target height in pixels
    * @param currentColor the color to use for "currentColor" values, or null for black
    * @param scaleMode how to scale the SVG into the target rectangle
    */
   public void render(GC gc, int x, int y, int width, int height,
         Color currentColor, ScaleMode scaleMode)
   {
      SVGRenderer.render(document, gc, x, y, width, height, currentColor, scaleMode);
   }

   /**
    * Returns the intrinsic width (from viewBox or width attribute).
    *
    * @return intrinsic width in SVG units, or -1 if neither viewBox nor width is specified
    */
   public float getWidth()
   {
      float w = document.getWidth();
      return w > 0 ? w : -1;
   }

   /**
    * Returns the intrinsic height (from viewBox or height attribute).
    *
    * @return intrinsic height in SVG units, or -1 if neither viewBox nor height is specified
    */
   public float getHeight()
   {
      float h = document.getHeight();
      return h > 0 ? h : -1;
   }

   /**
    * Returns the intrinsic aspect ratio (width / height).
    *
    * @return aspect ratio, or -1 if width or height is unknown
    */
   public float getAspectRatio()
   {
      float w = getWidth();
      float h = getHeight();
      if (w > 0 && h > 0)
         return w / h;
      return -1;
   }
}
