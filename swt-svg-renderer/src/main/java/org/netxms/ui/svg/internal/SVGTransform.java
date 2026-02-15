package org.netxms.ui.svg.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for SVG transform attributes. Produces 6-element affine matrices [a, b, c, d, e, f]
 * representing the transformation:
 * <pre>
 *   | a c e |
 *   | b d f |
 *   | 0 0 1 |
 * </pre>
 */
public final class SVGTransform
{
   private static final Pattern TRANSFORM_PATTERN = Pattern.compile(
         "(matrix|translate|scale|rotate|skewX|skewY)\\s*\\(([^)]+)\\)");

   private SVGTransform()
   {
   }

   /**
    * Returns the identity matrix.
    *
    * @return 6-element identity affine matrix [1, 0, 0, 1, 0, 0]
    */
   public static float[] identity()
   {
      return new float[] { 1, 0, 0, 1, 0, 0 };
   }

   /**
    * Parse a transform attribute string into a combined affine matrix.
    * Multiple transforms are applied right-to-left per SVG spec.
    *
    * @param value the transform attribute value
    * @return 6-element affine matrix, or identity if null/empty/invalid
    */
   public static float[] parse(String value)
   {
      if (value == null || value.trim().isEmpty())
         return identity();

      List<float[]> matrices = new ArrayList<>();
      Matcher m = TRANSFORM_PATTERN.matcher(value);
      while (m.find())
      {
         String type = m.group(1);
         float[] params = parseParams(m.group(2));
         float[] matrix = toMatrix(type, params);
         if (matrix != null)
            matrices.add(matrix);
      }

      if (matrices.isEmpty())
         return identity();

      // Apply right-to-left: result = M1 * M2 * ... * Mn
      float[] result = matrices.get(0);
      for (int i = 1; i < matrices.size(); i++)
      {
         result = multiply(result, matrices.get(i));
      }
      return result;
   }

   /**
    * Multiply two affine matrices: result = a * b.
    *
    * @param a left-hand 6-element affine matrix
    * @param b right-hand 6-element affine matrix
    * @return 6-element affine matrix representing the combined transformation
    */
   public static float[] multiply(float[] a, float[] b)
   {
      return new float[] {
            a[0] * b[0] + a[2] * b[1],
            a[1] * b[0] + a[3] * b[1],
            a[0] * b[2] + a[2] * b[3],
            a[1] * b[2] + a[3] * b[3],
            a[0] * b[4] + a[2] * b[5] + a[4],
            a[1] * b[4] + a[3] * b[5] + a[5]
      };
   }

   private static float[] toMatrix(String type, float[] p)
   {
      switch(type)
      {
         case "matrix":
            if (p.length >= 6)
               return new float[] { p[0], p[1], p[2], p[3], p[4], p[5] };
            return null;

         case "translate":
            if (p.length >= 2)
               return new float[] { 1, 0, 0, 1, p[0], p[1] };
            else if (p.length == 1)
               return new float[] { 1, 0, 0, 1, p[0], 0 };
            return null;

         case "scale":
            if (p.length >= 2)
               return new float[] { p[0], 0, 0, p[1], 0, 0 };
            else if (p.length == 1)
               return new float[] { p[0], 0, 0, p[0], 0, 0 };
            return null;

         case "rotate":
         {
            if (p.length < 1)
               return null;
            float angle = (float)Math.toRadians(p[0]);
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            if (p.length >= 3)
            {
               // rotate(angle, cx, cy) = translate(cx,cy) * rotate(angle) * translate(-cx,-cy)
               float cx = p[1];
               float cy = p[2];
               return new float[] {
                     cos, sin, -sin, cos,
                     cx - cos * cx + sin * cy,
                     cy - sin * cx - cos * cy
               };
            }
            return new float[] { cos, sin, -sin, cos, 0, 0 };
         }

         case "skewX":
            if (p.length >= 1)
            {
               float tan = (float)Math.tan(Math.toRadians(p[0]));
               return new float[] { 1, 0, tan, 1, 0, 0 };
            }
            return null;

         case "skewY":
            if (p.length >= 1)
            {
               float tan = (float)Math.tan(Math.toRadians(p[0]));
               return new float[] { 1, tan, 0, 1, 0, 0 };
            }
            return null;

         default:
            return null;
      }
   }

   private static float[] parseParams(String paramStr)
   {
      // Split on commas or whitespace
      String[] tokens = paramStr.trim().split("[\\s,]+");
      List<Float> values = new ArrayList<>();
      for (String token : tokens)
      {
         if (!token.isEmpty())
         {
            try
            {
               values.add(Float.parseFloat(token));
            }
            catch(NumberFormatException e)
            {
               // skip invalid tokens
            }
         }
      }
      float[] result = new float[values.size()];
      for (int i = 0; i < values.size(); i++)
         result[i] = values.get(i);
      return result;
   }
}
