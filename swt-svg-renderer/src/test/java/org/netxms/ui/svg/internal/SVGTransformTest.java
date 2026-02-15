package org.netxms.ui.svg.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SVGTransformTest
{
   private static final float EPSILON = 0.001f;

   @Test
   void testIdentity()
   {
      float[] m = SVGTransform.identity();
      assertMatrix(m, 1, 0, 0, 1, 0, 0);
   }

   @Test
   void testNullReturnsIdentity()
   {
      assertMatrix(SVGTransform.parse(null), 1, 0, 0, 1, 0, 0);
   }

   @Test
   void testEmptyReturnsIdentity()
   {
      assertMatrix(SVGTransform.parse(""), 1, 0, 0, 1, 0, 0);
   }

   @Test
   void testTranslate()
   {
      float[] m = SVGTransform.parse("translate(10, 20)");
      assertMatrix(m, 1, 0, 0, 1, 10, 20);
   }

   @Test
   void testTranslateSingleParam()
   {
      float[] m = SVGTransform.parse("translate(10)");
      assertMatrix(m, 1, 0, 0, 1, 10, 0);
   }

   @Test
   void testScale()
   {
      float[] m = SVGTransform.parse("scale(2, 3)");
      assertMatrix(m, 2, 0, 0, 3, 0, 0);
   }

   @Test
   void testScaleUniform()
   {
      float[] m = SVGTransform.parse("scale(2)");
      assertMatrix(m, 2, 0, 0, 2, 0, 0);
   }

   @Test
   void testRotate()
   {
      float[] m = SVGTransform.parse("rotate(90)");
      float cos = (float)Math.cos(Math.toRadians(90));
      float sin = (float)Math.sin(Math.toRadians(90));
      assertMatrix(m, cos, sin, -sin, cos, 0, 0);
   }

   @Test
   void testRotateAroundPoint()
   {
      float[] m = SVGTransform.parse("rotate(90, 50, 50)");
      // rotate(90, 50, 50) = translate(50,50) * rotate(90) * translate(-50,-50)
      // Point (50, 0) should map to (100, 50)
      float x = m[0] * 50 + m[2] * 0 + m[4];
      float y = m[1] * 50 + m[3] * 0 + m[5];
      assertEquals(100, x, EPSILON);
      assertEquals(50, y, EPSILON);
   }

   @Test
   void testMatrix()
   {
      float[] m = SVGTransform.parse("matrix(1, 0, 0, 1, 10, 20)");
      assertMatrix(m, 1, 0, 0, 1, 10, 20);
   }

   @Test
   void testCompoundTransform()
   {
      // translate(10, 0) scale(2) means: first scale by 2, then translate by 10
      float[] m = SVGTransform.parse("translate(10, 0) scale(2)");
      // Point (5, 3): scale(2) -> (10, 6), translate(10, 0) -> (20, 6)
      float x = m[0] * 5 + m[2] * 3 + m[4];
      float y = m[1] * 5 + m[3] * 3 + m[5];
      assertEquals(20, x, EPSILON);
      assertEquals(6, y, EPSILON);
   }

   @Test
   void testMultiply()
   {
      float[] a = { 1, 0, 0, 1, 10, 20 }; // translate(10, 20)
      float[] b = { 2, 0, 0, 2, 0, 0 };   // scale(2)
      float[] result = SVGTransform.multiply(a, b);
      // translate(10,20) * scale(2) = scale(2) with translate(10,20)
      assertMatrix(result, 2, 0, 0, 2, 10, 20);
   }

   private static void assertMatrix(float[] m, float a, float b, float c, float d, float e, float f)
   {
      assertNotNull(m);
      assertEquals(6, m.length);
      assertEquals(a, m[0], EPSILON, "m[0]");
      assertEquals(b, m[1], EPSILON, "m[1]");
      assertEquals(c, m[2], EPSILON, "m[2]");
      assertEquals(d, m[3], EPSILON, "m[3]");
      assertEquals(e, m[4], EPSILON, "m[4]");
      assertEquals(f, m[5], EPSILON, "m[5]");
   }
}
