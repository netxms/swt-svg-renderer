package org.netxms.ui.svg.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for SVG path "d" attribute data. Converts all commands to absolute
 * coordinates and converts arcs to cubic Bezier segments at parse time.
 */
public final class PathDataParser
{
   private PathDataParser()
   {
   }

   /**
    * Parse SVG path data string into a list of path segments.
    * All coordinates are converted to absolute. Arcs are converted to cubics.
    *
    * @param d the path data string
    * @return list of path segments, empty list on malformed input
    */
   public static List<PathSegment> parse(String d)
   {
      if (d == null || d.trim().isEmpty())
         return new ArrayList<>();

      List<PathSegment> segments = new ArrayList<>();
      Tokenizer tokenizer = new Tokenizer(d);

      float cx = 0, cy = 0; // current point
      float sx = 0, sy = 0; // start of current subpath
      float lastCx2 = 0, lastCy2 = 0; // last cubic control point
      float lastQx1 = 0, lastQy1 = 0; // last quad control point
      char lastCmd = 0;

      try
      {
         while (tokenizer.hasMore())
         {
            char cmd = tokenizer.peekCommand();
            if (cmd == 0)
            {
               // Implicit repeat of last command (except M -> L)
               if (lastCmd == 0)
                  break;
               cmd = lastCmd;
               if (cmd == 'M')
                  cmd = 'L';
               else if (cmd == 'm')
                  cmd = 'l';
            }
            else
            {
               tokenizer.consumeCommand();
            }

            boolean relative = Character.isLowerCase(cmd);
            char upperCmd = Character.toUpperCase(cmd);

            switch(upperCmd)
            {
               case 'M':
               {
                  float x = tokenizer.nextFloat();
                  float y = tokenizer.nextFloat();
                  if (relative)
                  {
                     x += cx;
                     y += cy;
                  }
                  segments.add(new PathSegment.MoveTo(x, y));
                  cx = x;
                  cy = y;
                  sx = x;
                  sy = y;
                  lastCx2 = cx;
                  lastCy2 = cy;
                  lastQx1 = cx;
                  lastQy1 = cy;
                  lastCmd = relative ? 'l' : 'L'; // subsequent coords are implicit LineTo
                  // Handle implicit line-to after moveto
                  while (tokenizer.hasMoreNumbers())
                  {
                     x = tokenizer.nextFloat();
                     y = tokenizer.nextFloat();
                     if (relative)
                     {
                        x += cx;
                        y += cy;
                     }
                     segments.add(new PathSegment.LineTo(x, y));
                     cx = x;
                     cy = y;
                     lastCx2 = cx;
                     lastCy2 = cy;
                     lastQx1 = cx;
                     lastQy1 = cy;
                  }
                  continue;
               }
               case 'L':
               {
                  do
                  {
                     float x = tokenizer.nextFloat();
                     float y = tokenizer.nextFloat();
                     if (relative)
                     {
                        x += cx;
                        y += cy;
                     }
                     segments.add(new PathSegment.LineTo(x, y));
                     cx = x;
                     cy = y;
                     lastCx2 = cx;
                     lastCy2 = cy;
                     lastQx1 = cx;
                     lastQy1 = cy;
                  }
                  while (tokenizer.hasMoreNumbers());
                  break;
               }
               case 'H':
               {
                  do
                  {
                     float x = tokenizer.nextFloat();
                     if (relative)
                        x += cx;
                     segments.add(new PathSegment.LineTo(x, cy));
                     cx = x;
                     lastCx2 = cx;
                     lastCy2 = cy;
                     lastQx1 = cx;
                     lastQy1 = cy;
                  }
                  while (tokenizer.hasMoreNumbers());
                  break;
               }
               case 'V':
               {
                  do
                  {
                     float y = tokenizer.nextFloat();
                     if (relative)
                        y += cy;
                     segments.add(new PathSegment.LineTo(cx, y));
                     cy = y;
                     lastCx2 = cx;
                     lastCy2 = cy;
                     lastQx1 = cx;
                     lastQy1 = cy;
                  }
                  while (tokenizer.hasMoreNumbers());
                  break;
               }
               case 'C':
               {
                  do
                  {
                     float x1 = tokenizer.nextFloat();
                     float y1 = tokenizer.nextFloat();
                     float x2 = tokenizer.nextFloat();
                     float y2 = tokenizer.nextFloat();
                     float x = tokenizer.nextFloat();
                     float y = tokenizer.nextFloat();
                     if (relative)
                     {
                        x1 += cx;
                        y1 += cy;
                        x2 += cx;
                        y2 += cy;
                        x += cx;
                        y += cy;
                     }
                     segments.add(new PathSegment.CubicTo(x1, y1, x2, y2, x, y));
                     lastCx2 = x2;
                     lastCy2 = y2;
                     cx = x;
                     cy = y;
                     lastQx1 = cx;
                     lastQy1 = cy;
                  }
                  while (tokenizer.hasMoreNumbers());
                  break;
               }
               case 'S':
               {
                  do
                  {
                     // Reflect last control point
                     float x1 = 2 * cx - lastCx2;
                     float y1 = 2 * cy - lastCy2;
                     float x2 = tokenizer.nextFloat();
                     float y2 = tokenizer.nextFloat();
                     float x = tokenizer.nextFloat();
                     float y = tokenizer.nextFloat();
                     if (relative)
                     {
                        x2 += cx;
                        y2 += cy;
                        x += cx;
                        y += cy;
                     }
                     segments.add(new PathSegment.CubicTo(x1, y1, x2, y2, x, y));
                     lastCx2 = x2;
                     lastCy2 = y2;
                     cx = x;
                     cy = y;
                     lastQx1 = cx;
                     lastQy1 = cy;
                  }
                  while (tokenizer.hasMoreNumbers());
                  break;
               }
               case 'Q':
               {
                  do
                  {
                     float x1 = tokenizer.nextFloat();
                     float y1 = tokenizer.nextFloat();
                     float x = tokenizer.nextFloat();
                     float y = tokenizer.nextFloat();
                     if (relative)
                     {
                        x1 += cx;
                        y1 += cy;
                        x += cx;
                        y += cy;
                     }
                     segments.add(new PathSegment.QuadTo(x1, y1, x, y));
                     lastQx1 = x1;
                     lastQy1 = y1;
                     cx = x;
                     cy = y;
                     lastCx2 = cx;
                     lastCy2 = cy;
                  }
                  while (tokenizer.hasMoreNumbers());
                  break;
               }
               case 'T':
               {
                  do
                  {
                     // Reflect last quad control point
                     float x1 = 2 * cx - lastQx1;
                     float y1 = 2 * cy - lastQy1;
                     float x = tokenizer.nextFloat();
                     float y = tokenizer.nextFloat();
                     if (relative)
                     {
                        x += cx;
                        y += cy;
                     }
                     segments.add(new PathSegment.QuadTo(x1, y1, x, y));
                     lastQx1 = x1;
                     lastQy1 = y1;
                     cx = x;
                     cy = y;
                     lastCx2 = cx;
                     lastCy2 = cy;
                  }
                  while (tokenizer.hasMoreNumbers());
                  break;
               }
               case 'A':
               {
                  do
                  {
                     float rx = tokenizer.nextFloat();
                     float ry = tokenizer.nextFloat();
                     float xAxisRotation = tokenizer.nextFloat();
                     float largeArcFlag = tokenizer.nextFlag();
                     float sweepFlag = tokenizer.nextFlag();
                     float x = tokenizer.nextFloat();
                     float y = tokenizer.nextFloat();
                     if (relative)
                     {
                        x += cx;
                        y += cy;
                     }
                     arcToCubics(segments, cx, cy, rx, ry, xAxisRotation,
                           largeArcFlag != 0, sweepFlag != 0, x, y);
                     cx = x;
                     cy = y;
                     lastCx2 = cx;
                     lastCy2 = cy;
                     lastQx1 = cx;
                     lastQy1 = cy;
                  }
                  while (tokenizer.hasMoreNumbers());
                  break;
               }
               case 'Z':
               {
                  segments.add(PathSegment.Close.INSTANCE);
                  cx = sx;
                  cy = sy;
                  lastCx2 = cx;
                  lastCy2 = cy;
                  lastQx1 = cx;
                  lastQy1 = cy;
                  break;
               }
               default:
                  // Unknown command, skip
                  break;
            }
            lastCmd = cmd;
         }
      }
      catch(Exception e)
      {
         // Malformed path data — return what we have so far
         return segments.isEmpty() ? new ArrayList<>() : segments;
      }

      return segments;
   }

   /**
    * Convert an SVG arc to one or more cubic Bezier segments.
    * Implements SVG spec F.6.5 algorithm.
    */
   private static void arcToCubics(List<PathSegment> segments,
         float x1, float y1, float rxIn, float ryIn,
         float xAxisRotationDeg, boolean largeArc, boolean sweep,
         float x2, float y2)
   {
      // F.6.2: If endpoints are identical, skip
      if (x1 == x2 && y1 == y2)
         return;

      // F.6.6.1: If rx or ry is 0, treat as straight line
      float rx = Math.abs(rxIn);
      float ry = Math.abs(ryIn);
      if (rx == 0 || ry == 0)
      {
         segments.add(new PathSegment.LineTo(x2, y2));
         return;
      }

      double phi = Math.toRadians(xAxisRotationDeg);
      double cosPhi = Math.cos(phi);
      double sinPhi = Math.sin(phi);

      // F.6.5.1: Compute (x1', y1')
      double dx2 = (x1 - x2) / 2.0;
      double dy2 = (y1 - y2) / 2.0;
      double x1p = cosPhi * dx2 + sinPhi * dy2;
      double y1p = -sinPhi * dx2 + cosPhi * dy2;

      // F.6.6.2: Ensure radii are large enough
      double x1pSq = x1p * x1p;
      double y1pSq = y1p * y1p;
      double rxSq = (double)rx * rx;
      double rySq = (double)ry * ry;

      double lambda = x1pSq / rxSq + y1pSq / rySq;
      if (lambda > 1)
      {
         double sqrtLambda = Math.sqrt(lambda);
         rx = (float)(sqrtLambda * rx);
         ry = (float)(sqrtLambda * ry);
         rxSq = (double)rx * rx;
         rySq = (double)ry * ry;
      }

      // F.6.5.2: Compute (cx', cy')
      double num = rxSq * rySq - rxSq * y1pSq - rySq * x1pSq;
      double den = rxSq * y1pSq + rySq * x1pSq;
      double sq = Math.max(0, num / den);
      double sc = Math.sqrt(sq);
      if (largeArc == sweep)
         sc = -sc;

      double cxp = sc * rx * y1p / ry;
      double cyp = -sc * ry * x1p / rx;

      // F.6.5.3: Compute (cx, cy)
      double cxd = cosPhi * cxp - sinPhi * cyp + (x1 + x2) / 2.0;
      double cyd = sinPhi * cxp + cosPhi * cyp + (y1 + y2) / 2.0;

      // F.6.5.5: Compute theta1 and dtheta
      double ux = (x1p - cxp) / rx;
      double uy = (y1p - cyp) / ry;
      double vx = (-x1p - cxp) / rx;
      double vy = (-y1p - cyp) / ry;

      double theta1 = vectorAngle(1, 0, ux, uy);
      double dtheta = vectorAngle(ux, uy, vx, vy);

      if (!sweep && dtheta > 0)
         dtheta -= 2 * Math.PI;
      else if (sweep && dtheta < 0)
         dtheta += 2 * Math.PI;

      // Split into segments of <= 90 degrees
      int numSegments = (int)Math.ceil(Math.abs(dtheta) / (Math.PI / 2));
      if (numSegments == 0)
         numSegments = 1;

      double segmentAngle = dtheta / numSegments;
      double currentX = x1;
      double currentY = y1;

      for (int i = 0; i < numSegments; i++)
      {
         double t1 = theta1 + i * segmentAngle;
         double t2 = theta1 + (i + 1) * segmentAngle;

         // Compute cubic bezier control points for arc segment
         double alpha = Math.sin(segmentAngle) * (Math.sqrt(4 + 3 * Math.pow(Math.tan(segmentAngle / 2), 2)) - 1) / 3;

         double cos1 = Math.cos(t1);
         double sin1 = Math.sin(t1);
         double cos2 = Math.cos(t2);
         double sin2 = Math.sin(t2);

         // End point of this segment on the unit circle (pre-transform)
         double ex1 = rx * cos1;
         double ey1 = ry * sin1;
         double ex2 = rx * cos2;
         double ey2 = ry * sin2;

         // Derivatives
         double dx1 = -rx * sin1;
         double dy1 = ry * cos1;
         double dx2d = -rx * sin2;
         double dy2d = ry * cos2;

         // Control points (in rotated coordinate space, then transformed)
         double cp1x = ex1 + alpha * dx1;
         double cp1y = ey1 + alpha * dy1;
         double cp2x = ex2 - alpha * dx2d;
         double cp2y = ey2 - alpha * dy2d;

         // Transform to original coordinate space
         float bx1 = (float)(cosPhi * cp1x - sinPhi * cp1y + cxd);
         float by1 = (float)(sinPhi * cp1x + cosPhi * cp1y + cyd);
         float bx2 = (float)(cosPhi * cp2x - sinPhi * cp2y + cxd);
         float by2 = (float)(sinPhi * cp2x + cosPhi * cp2y + cyd);
         float bx = (float)(cosPhi * ex2 - sinPhi * ey2 + cxd);
         float by = (float)(sinPhi * ex2 + cosPhi * ey2 + cyd);

         // Use exact endpoint for last segment
         if (i == numSegments - 1)
         {
            bx = x2;
            by = y2;
         }

         segments.add(new PathSegment.CubicTo(bx1, by1, bx2, by2, bx, by));
         currentX = bx;
         currentY = by;
      }
   }

   /**
    * Compute the angle between two vectors.
    */
   private static double vectorAngle(double ux, double uy, double vx, double vy)
   {
      double sign = (ux * vy - uy * vx) < 0 ? -1 : 1;
      double dot = ux * vx + uy * vy;
      double lenU = Math.sqrt(ux * ux + uy * uy);
      double lenV = Math.sqrt(vx * vx + vy * vy);
      double cos = dot / (lenU * lenV);
      // Clamp to [-1, 1] for floating point errors
      cos = Math.max(-1, Math.min(1, cos));
      return sign * Math.acos(cos);
   }

   /**
    * Simple tokenizer for SVG path data.
    */
   private static class Tokenizer
   {
      private final String data;
      private int pos;

      Tokenizer(String data)
      {
         this.data = data;
         this.pos = 0;
      }

      boolean hasMore()
      {
         skipWhitespaceAndCommas();
         return pos < data.length();
      }

      boolean hasMoreNumbers()
      {
         skipWhitespaceAndCommas();
         if (pos >= data.length())
            return false;
         char c = data.charAt(pos);
         return c == '-' || c == '+' || c == '.' || (c >= '0' && c <= '9');
      }

      char peekCommand()
      {
         skipWhitespaceAndCommas();
         if (pos >= data.length())
            return 0;
         char c = data.charAt(pos);
         if (isCommand(c))
            return c;
         return 0;
      }

      void consumeCommand()
      {
         skipWhitespaceAndCommas();
         if (pos < data.length() && isCommand(data.charAt(pos)))
            pos++;
      }

      /**
       * Read an arc flag (exactly one digit: '0' or '1').
       * Per SVG spec, arc flags are always a single digit and do not
       * require separators from adjacent numbers.
       */
      float nextFlag()
      {
         skipWhitespaceAndCommas();
         if (pos >= data.length())
            throw new IllegalStateException("Unexpected end of path data");
         char c = data.charAt(pos);
         if (c == '0' || c == '1')
         {
            pos++;
            return c - '0';
         }
         throw new IllegalStateException("Expected flag (0 or 1) at position " + pos);
      }

      float nextFloat()
      {
         skipWhitespaceAndCommas();
         if (pos >= data.length())
            throw new IllegalStateException("Unexpected end of path data");

         int start = pos;
         boolean hasDecimal = false;
         boolean hasExponent = false;

         // Handle sign
         if (pos < data.length() && (data.charAt(pos) == '-' || data.charAt(pos) == '+'))
            pos++;

         // Digits before decimal
         while (pos < data.length() && data.charAt(pos) >= '0' && data.charAt(pos) <= '9')
            pos++;

         // Decimal point
         if (pos < data.length() && data.charAt(pos) == '.')
         {
            hasDecimal = true;
            pos++;
            while (pos < data.length() && data.charAt(pos) >= '0' && data.charAt(pos) <= '9')
               pos++;
         }

         // Exponent
         if (pos < data.length() && (data.charAt(pos) == 'e' || data.charAt(pos) == 'E'))
         {
            hasExponent = true;
            pos++;
            if (pos < data.length() && (data.charAt(pos) == '+' || data.charAt(pos) == '-'))
               pos++;
            while (pos < data.length() && data.charAt(pos) >= '0' && data.charAt(pos) <= '9')
               pos++;
         }

         if (pos == start)
            throw new IllegalStateException("Expected number at position " + pos);

         return Float.parseFloat(data.substring(start, pos));
      }

      private void skipWhitespaceAndCommas()
      {
         while (pos < data.length())
         {
            char c = data.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == ',')
               pos++;
            else
               break;
         }
      }

      private static boolean isCommand(char c)
      {
         return "MmZzLlHhVvCcSsQqTtAa".indexOf(c) >= 0;
      }
   }
}
