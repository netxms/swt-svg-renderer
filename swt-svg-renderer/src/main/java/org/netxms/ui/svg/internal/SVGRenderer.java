package org.netxms.ui.svg.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Transform;
import org.netxms.ui.svg.ScaleMode;

/**
 * Renders an SVGDocument onto an SWT GC.
 */
public final class SVGRenderer
{
   private SVGRenderer()
   {
   }

   /**
    * Render an SVG document onto a GC within the given bounds.
    *
    * @param doc the parsed SVG document
    * @param gc the graphics context to render to
    * @param x target x position
    * @param y target y position
    * @param w target width
    * @param h target height
    * @param currentColor the color to use for "currentColor" values (null = black)
    * @param scaleMode scaling mode
    */
   public static void render(SVGDocument doc, GC gc, int x, int y, int w, int h,
         Color currentColor, ScaleMode scaleMode)
   {
      if (w <= 0 || h <= 0)
         return;

      float vbW = doc.getViewBoxWidth();
      float vbH = doc.getViewBoxHeight();
      if (vbW <= 0 || vbH <= 0)
         return;

      // Save GC state
      boolean oldAdvanced = gc.getAdvanced();
      int oldAntialias = gc.getAntialias();
      int oldTextAntialias = gc.getTextAntialias();
      Color oldFg = gc.getForeground();
      Color oldBg = gc.getBackground();
      int oldAlpha = gc.getAlpha();
      int oldLineWidth = gc.getLineWidth();
      int oldLineCap = gc.getLineCap();
      int oldLineJoin = gc.getLineJoin();
      Transform oldTransform = new Transform(gc.getDevice());
      gc.getTransform(oldTransform);

      // Color cache for this render call
      Map<Integer, Color> colorCache = new HashMap<>();

      try
      {
         gc.setAdvanced(true);
         gc.setAntialias(SWT.ON);

         // Compute viewport transform
         Transform viewportTransform = new Transform(gc.getDevice());
         gc.getTransform(viewportTransform);

         float vbX = doc.getViewBoxX();
         float vbY = doc.getViewBoxY();

         if (scaleMode == ScaleMode.STRETCH)
         {
            float sx = w / vbW;
            float sy = h / vbH;
            Transform t = new Transform(gc.getDevice());
            t.setElements(sx, 0, 0, sy, x - vbX * sx, y - vbY * sy);
            viewportTransform.multiply(t);
            t.dispose();
         }
         else
         {
            // UNIFORM (default)
            float scale = Math.min(w / vbW, h / vbH);
            float tx = x + (w - vbW * scale) / 2f;
            float ty = y + (h - vbH * scale) / 2f;
            Transform t = new Transform(gc.getDevice());
            t.setElements(scale, 0, 0, scale, tx - vbX * scale, ty - vbY * scale);
            viewportTransform.multiply(t);
            t.dispose();
         }

         gc.setTransform(viewportTransform);
         viewportTransform.dispose();

         // Render tree
         renderNodes(doc.getChildren(), gc, StyleProps.ROOT_DEFAULTS, currentColor, colorCache);
      }
      finally
      {
         // Dispose cached colors
         for (Color c : colorCache.values())
            c.dispose();

         // Restore GC state
         gc.setTransform(oldTransform);
         oldTransform.dispose();
         gc.setAdvanced(oldAdvanced);
         gc.setAntialias(oldAntialias);
         gc.setTextAntialias(oldTextAntialias);
         gc.setForeground(oldFg);
         gc.setBackground(oldBg);
         gc.setAlpha(oldAlpha);
         gc.setLineWidth(oldLineWidth);
         gc.setLineCap(oldLineCap);
         gc.setLineJoin(oldLineJoin);
      }
   }

   private static void renderNodes(List<SVGNode> nodes, GC gc, StyleProps parentStyle,
         Color currentColor, Map<Integer, Color> colorCache)
   {
      for (SVGNode node : nodes)
      {
         if (!node.isDisplay())
            continue;

         // Save transform
         Transform saved = new Transform(gc.getDevice());
         gc.getTransform(saved);

         try
         {
            // Apply node transform
            float[] nodeTransform = node.getTransform();
            if (nodeTransform != null && !isIdentity(nodeTransform))
            {
               Transform current = new Transform(gc.getDevice());
               gc.getTransform(current);
               Transform nodeT = new Transform(gc.getDevice(),
                     nodeTransform[0], nodeTransform[1], nodeTransform[2],
                     nodeTransform[3], nodeTransform[4], nodeTransform[5]);
               current.multiply(nodeT);
               gc.setTransform(current);
               nodeT.dispose();
               current.dispose();
            }

            // Resolve style
            StyleProps resolved = node.getStyle().resolve(parentStyle);

            // Compute element-level alpha
            float opacity = Float.isNaN(resolved.getOpacity()) ? 1.0f : resolved.getOpacity();
            int elementAlpha = Math.round(opacity * 255);

            if (node instanceof SVGGroup)
            {
               renderNodes(((SVGGroup)node).getChildren(), gc, resolved, currentColor, colorCache);
            }
            else if (node instanceof SVGPath)
            {
               renderPath((SVGPath)node, gc, resolved, currentColor, colorCache, elementAlpha);
            }
            else if (node instanceof SVGRect)
            {
               renderRect((SVGRect)node, gc, resolved, currentColor, colorCache, elementAlpha);
            }
            else if (node instanceof SVGCircle)
            {
               renderCircle((SVGCircle)node, gc, resolved, currentColor, colorCache, elementAlpha);
            }
            else if (node instanceof SVGEllipse)
            {
               renderEllipse((SVGEllipse)node, gc, resolved, currentColor, colorCache, elementAlpha);
            }
            else if (node instanceof SVGLine)
            {
               renderLine((SVGLine)node, gc, resolved, currentColor, colorCache, elementAlpha);
            }
            else if (node instanceof SVGPolyline)
            {
               renderPolyline((SVGPolyline)node, gc, resolved, currentColor, colorCache, elementAlpha);
            }
            else if (node instanceof SVGPolygon)
            {
               renderPolygon((SVGPolygon)node, gc, resolved, currentColor, colorCache, elementAlpha);
            }
         }
         finally
         {
            gc.setTransform(saved);
            saved.dispose();
         }
      }
   }

   private static void renderPath(SVGPath node, GC gc, StyleProps style,
         Color currentColor, Map<Integer, Color> colorCache, int elementAlpha)
   {
      Path path = new Path(gc.getDevice());
      try
      {
         buildPath(path, node.getSegments());
         drawShape(gc, path, style, currentColor, colorCache, elementAlpha);
      }
      finally
      {
         path.dispose();
      }
   }

   private static void renderRect(SVGRect node, GC gc, StyleProps style,
         Color currentColor, Map<Integer, Color> colorCache, int elementAlpha)
   {
      Path path = new Path(gc.getDevice());
      try
      {
         float x = node.getX();
         float y = node.getY();
         float w = node.getWidth();
         float h = node.getHeight();
         float rx = node.getRx();
         float ry = node.getRy();

         if (rx > 0 || ry > 0)
         {
            // Clamp radii
            rx = Math.min(rx, w / 2);
            ry = Math.min(ry, h / 2);
            // Rounded rectangle as path
            path.moveTo(x + rx, y);
            path.lineTo(x + w - rx, y);
            addArcToPath(path, x + w - rx, y, x + w, y + ry, rx, ry);
            path.lineTo(x + w, y + h - ry);
            addArcToPath(path, x + w, y + h - ry, x + w - rx, y + h, rx, ry);
            path.lineTo(x + rx, y + h);
            addArcToPath(path, x + rx, y + h, x, y + h - ry, rx, ry);
            path.lineTo(x, y + ry);
            addArcToPath(path, x, y + ry, x + rx, y, rx, ry);
            path.close();
         }
         else
         {
            path.addRectangle(x, y, w, h);
         }
         drawShape(gc, path, style, currentColor, colorCache, elementAlpha);
      }
      finally
      {
         path.dispose();
      }
   }

   /**
    * Add a quarter-ellipse arc as a cubic Bezier. Goes from (startX, startY) to (endX, endY)
    * as a 90-degree arc with the given radii.
    */
   private static void addArcToPath(Path path, float startX, float startY,
         float endX, float endY, float rx, float ry)
   {
      // Kappa = 4*(sqrt(2)-1)/3 for 90-degree arc approximation
      float kx = rx * 0.5522847498f;
      float ky = ry * 0.5522847498f;

      float dx = endX - startX;
      float dy = endY - startY;

      // Determine arc direction from the delta
      float cx1, cy1, cx2, cy2;
      if (dx > 0 && dy > 0)
      {
         // Top-right corner: going right then down
         cx1 = startX + kx;
         cy1 = startY;
         cx2 = endX;
         cy2 = endY - ky;
      }
      else if (dx < 0 && dy > 0)
      {
         // Bottom-right corner: going down then left
         cx1 = startX;
         cy1 = startY + ky;
         cx2 = endX + kx;
         cy2 = endY;
      }
      else if (dx < 0 && dy < 0)
      {
         // Bottom-left corner: going left then up
         cx1 = startX - kx;
         cy1 = startY;
         cx2 = endX;
         cy2 = endY + ky;
      }
      else
      {
         // Top-left corner: going up then right
         cx1 = startX;
         cy1 = startY - ky;
         cx2 = endX - kx;
         cy2 = endY;
      }

      path.cubicTo(cx1, cy1, cx2, cy2, endX, endY);
   }

   private static void renderCircle(SVGCircle node, GC gc, StyleProps style,
         Color currentColor, Map<Integer, Color> colorCache, int elementAlpha)
   {
      Path path = new Path(gc.getDevice());
      try
      {
         addEllipsePath(path, node.getCx(), node.getCy(), node.getR(), node.getR());
         drawShape(gc, path, style, currentColor, colorCache, elementAlpha);
      }
      finally
      {
         path.dispose();
      }
   }

   private static void renderEllipse(SVGEllipse node, GC gc, StyleProps style,
         Color currentColor, Map<Integer, Color> colorCache, int elementAlpha)
   {
      Path path = new Path(gc.getDevice());
      try
      {
         addEllipsePath(path, node.getCx(), node.getCy(), node.getRx(), node.getRy());
         drawShape(gc, path, style, currentColor, colorCache, elementAlpha);
      }
      finally
      {
         path.dispose();
      }
   }

   /**
    * Build an ellipse path using four cubic Bezier arcs.
    */
   private static void addEllipsePath(Path path, float cx, float cy, float rx, float ry)
   {
      float kx = rx * 0.5522847498f;
      float ky = ry * 0.5522847498f;

      path.moveTo(cx + rx, cy);
      path.cubicTo(cx + rx, cy + ky, cx + kx, cy + ry, cx, cy + ry);
      path.cubicTo(cx - kx, cy + ry, cx - rx, cy + ky, cx - rx, cy);
      path.cubicTo(cx - rx, cy - ky, cx - kx, cy - ry, cx, cy - ry);
      path.cubicTo(cx + kx, cy - ry, cx + rx, cy - ky, cx + rx, cy);
      path.close();
   }

   private static void renderLine(SVGLine node, GC gc, StyleProps style,
         Color currentColor, Map<Integer, Color> colorCache, int elementAlpha)
   {
      Path path = new Path(gc.getDevice());
      try
      {
         path.moveTo(node.getX1(), node.getY1());
         path.lineTo(node.getX2(), node.getY2());
         // Lines only have stroke, no fill
         drawStroke(gc, path, style, currentColor, colorCache, elementAlpha);
      }
      finally
      {
         path.dispose();
      }
   }

   private static void renderPolyline(SVGPolyline node, GC gc, StyleProps style,
         Color currentColor, Map<Integer, Color> colorCache, int elementAlpha)
   {
      Path path = new Path(gc.getDevice());
      try
      {
         float[] pts = node.getPoints();
         if (pts.length >= 2)
         {
            path.moveTo(pts[0], pts[1]);
            for (int i = 2; i < pts.length - 1; i += 2)
               path.lineTo(pts[i], pts[i + 1]);
         }
         drawShape(gc, path, style, currentColor, colorCache, elementAlpha);
      }
      finally
      {
         path.dispose();
      }
   }

   private static void renderPolygon(SVGPolygon node, GC gc, StyleProps style,
         Color currentColor, Map<Integer, Color> colorCache, int elementAlpha)
   {
      Path path = new Path(gc.getDevice());
      try
      {
         float[] pts = node.getPoints();
         if (pts.length >= 2)
         {
            path.moveTo(pts[0], pts[1]);
            for (int i = 2; i < pts.length - 1; i += 2)
               path.lineTo(pts[i], pts[i + 1]);
            path.close();
         }
         drawShape(gc, path, style, currentColor, colorCache, elementAlpha);
      }
      finally
      {
         path.dispose();
      }
   }

   private static void drawShape(GC gc, Path path, StyleProps style,
         Color currentColor, Map<Integer, Color> colorCache, int elementAlpha)
   {
      // Fill
      SVGColor fillColor = style.getFill();
      if (fillColor != null && !(fillColor instanceof SVGColor.None))
      {
         Color swtColor = resolveColor(fillColor, currentColor, colorCache, gc);
         if (swtColor != null)
         {
            gc.setBackground(swtColor);
            float fillOpacity = Float.isNaN(style.getFillOpacity()) ? 1.0f : style.getFillOpacity();
            gc.setAlpha(Math.round(elementAlpha * fillOpacity));

            // Fill rule
            if (style.getFillRule() == StyleProps.FILL_RULE_EVENODD)
               gc.setFillRule(SWT.FILL_EVEN_ODD);
            else
               gc.setFillRule(SWT.FILL_WINDING);

            gc.fillPath(path);
         }
      }

      // Stroke
      drawStroke(gc, path, style, currentColor, colorCache, elementAlpha);
   }

   private static void drawStroke(GC gc, Path path, StyleProps style,
         Color currentColor, Map<Integer, Color> colorCache, int elementAlpha)
   {
      SVGColor strokeColor = style.getStroke();
      if (strokeColor != null && !(strokeColor instanceof SVGColor.None))
      {
         Color swtColor = resolveColor(strokeColor, currentColor, colorCache, gc);
         if (swtColor != null)
         {
            gc.setForeground(swtColor);
            float strokeOpacity = Float.isNaN(style.getStrokeOpacity()) ? 1.0f : style.getStrokeOpacity();
            gc.setAlpha(Math.round(elementAlpha * strokeOpacity));

            float strokeWidth = Float.isNaN(style.getStrokeWidth()) ? 1.0f : style.getStrokeWidth();
            gc.setLineWidth(Math.max(1, Math.round(strokeWidth)));

            // Line cap
            switch(style.getLineCap())
            {
               case StyleProps.LINE_CAP_ROUND:
                  gc.setLineCap(SWT.CAP_ROUND);
                  break;
               case StyleProps.LINE_CAP_SQUARE:
                  gc.setLineCap(SWT.CAP_SQUARE);
                  break;
               default:
                  gc.setLineCap(SWT.CAP_FLAT);
                  break;
            }

            // Line join
            switch(style.getLineJoin())
            {
               case StyleProps.LINE_JOIN_ROUND:
                  gc.setLineJoin(SWT.JOIN_ROUND);
                  break;
               case StyleProps.LINE_JOIN_BEVEL:
                  gc.setLineJoin(SWT.JOIN_BEVEL);
                  break;
               default:
                  gc.setLineJoin(SWT.JOIN_MITER);
                  break;
            }

            gc.drawPath(path);
         }
      }
   }

   private static Color resolveColor(SVGColor svgColor, Color currentColor,
         Map<Integer, Color> colorCache, GC gc)
   {
      if (svgColor instanceof SVGColor.Absolute)
      {
         SVGColor.Absolute abs = (SVGColor.Absolute)svgColor;
         int key = (abs.getR() << 16) | (abs.getG() << 8) | abs.getB();
         Color cached = colorCache.get(key);
         if (cached == null)
         {
            cached = new Color(gc.getDevice(), abs.getR(), abs.getG(), abs.getB());
            colorCache.put(key, cached);
         }
         return cached;
      }
      else if (svgColor instanceof SVGColor.CurrentColor)
      {
         if (currentColor != null)
            return currentColor;
         // Default to black
         int key = 0;
         Color cached = colorCache.get(key);
         if (cached == null)
         {
            cached = new Color(gc.getDevice(), 0, 0, 0);
            colorCache.put(key, cached);
         }
         return cached;
      }
      return null;
   }

   private static void buildPath(Path path, List<PathSegment> segments)
   {
      for (PathSegment seg : segments)
      {
         if (seg instanceof PathSegment.MoveTo)
         {
            PathSegment.MoveTo m = (PathSegment.MoveTo)seg;
            path.moveTo(m.x, m.y);
         }
         else if (seg instanceof PathSegment.LineTo)
         {
            PathSegment.LineTo l = (PathSegment.LineTo)seg;
            path.lineTo(l.x, l.y);
         }
         else if (seg instanceof PathSegment.CubicTo)
         {
            PathSegment.CubicTo c = (PathSegment.CubicTo)seg;
            path.cubicTo(c.x1, c.y1, c.x2, c.y2, c.x, c.y);
         }
         else if (seg instanceof PathSegment.QuadTo)
         {
            PathSegment.QuadTo q = (PathSegment.QuadTo)seg;
            path.quadTo(q.x1, q.y1, q.x, q.y);
         }
         else if (seg instanceof PathSegment.Close)
         {
            path.close();
         }
      }
   }

   private static boolean isIdentity(float[] m)
   {
      return m[0] == 1 && m[1] == 0 && m[2] == 0 && m[3] == 1 && m[4] == 0 && m[5] == 0;
   }
}
