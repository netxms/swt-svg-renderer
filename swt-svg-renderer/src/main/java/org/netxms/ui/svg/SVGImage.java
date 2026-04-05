package org.netxms.ui.svg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
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
    * ScaleMode.UNIFORM, currentColor = black (default fill uses currentColor).
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

   /**
    * Rasterize SVG to an SWT Image with proper alpha transparency. SWT GC drawing does not update the alpha channel of the target
    * image, so this method uses the dual-render technique: it renders on both black and white backgrounds, then computes per-pixel
    * alpha from the difference.
    *
    * @param display display to create image on
    * @param width target width in pixels
    * @param height target height in pixels
    * @return rasterized SWT Image with alpha transparency
    */
   public Image rasterize(Display display, int width, int height)
   {
      return rasterize(display, width, height, null, ScaleMode.UNIFORM);
   }

   /**
    * Rasterize SVG to an SWT Image with proper alpha transparency and explicit currentColor.
    *
    * @param display display to create image on
    * @param width target width in pixels
    * @param height target height in pixels
    * @param currentColor the color to use for "currentColor" values, or null for black
    * @param scaleMode how to scale the SVG into the target rectangle
    * @return rasterized SWT Image with alpha transparency
    */
   public Image rasterize(Display display, int width, int height, Color currentColor, ScaleMode scaleMode)
   {
      ImageData onBlack = renderOnBackground(display, width, height, 0, 0, 0, currentColor, scaleMode);
      ImageData onWhite = renderOnBackground(display, width, height, 255, 255, 255, currentColor, scaleMode);

      PaletteData palette = new PaletteData(0x00FF0000, 0x0000FF00, 0x000000FF);
      ImageData result = new ImageData(width, height, 32, palette);
      result.alphaData = new byte[width * height];

      for(int y = 0; y < height; y++)
      {
         for(int x = 0; x < width; x++)
         {
            int pb = onBlack.getPixel(x, y);
            int pw = onWhite.getPixel(x, y);

            int rb = (pb >> 16) & 0xFF, gb = (pb >> 8) & 0xFF, bb = pb & 0xFF;
            int rw = (pw >> 16) & 0xFF, gw = (pw >> 8) & 0xFF, bw = pw & 0xFF;

            // alpha = 255 - max(white_channel - black_channel)
            int alpha = 255 - Math.max(Math.max(rw - rb, gw - gb), bw - bb);
            result.alphaData[y * width + x] = (byte)alpha;

            if (alpha > 0)
            {
               // Recover original color: source = black_pixel * 255 / alpha
               int r = Math.min(255, rb * 255 / alpha);
               int g = Math.min(255, gb * 255 / alpha);
               int b = Math.min(255, bb * 255 / alpha);
               result.setPixel(x, y, (r << 16) | (g << 8) | b);
            }
         }
      }

      return new Image(display, result);
   }

   /**
    * Render SVG onto a solid color background and return the resulting ImageData.
    */
   private ImageData renderOnBackground(Display display, int width, int height, int bgR, int bgG, int bgB,
         Color currentColor, ScaleMode scaleMode)
   {
      Image image = new Image(display, width, height);
      GC gc = new GC(image);
      Color bg = new Color(display, bgR, bgG, bgB);
      gc.setBackground(bg);
      gc.fillRectangle(0, 0, width, height);
      bg.dispose();
      gc.setAdvanced(true);
      gc.setAntialias(SWT.ON);
      SVGRenderer.render(document, gc, 0, 0, width, height, currentColor, scaleMode);
      gc.dispose();
      ImageData data = image.getImageData();
      image.dispose();
      return data;
   }
}
