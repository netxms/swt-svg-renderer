package org.netxms.ui.svg.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SVGColorTest
{
   @Test
   void testHex6()
   {
      SVGColor color = SVGColor.parse("#FF8040");
      assertNotNull(color);
      assertTrue(color instanceof SVGColor.Absolute);
      SVGColor.Absolute abs = (SVGColor.Absolute)color;
      assertEquals(255, abs.getR());
      assertEquals(128, abs.getG());
      assertEquals(64, abs.getB());
   }

   @Test
   void testHex3()
   {
      SVGColor color = SVGColor.parse("#F80");
      assertNotNull(color);
      assertTrue(color instanceof SVGColor.Absolute);
      SVGColor.Absolute abs = (SVGColor.Absolute)color;
      assertEquals(255, abs.getR());
      assertEquals(136, abs.getG());
      assertEquals(0, abs.getB());
   }

   @Test
   void testRgbFunction()
   {
      SVGColor color = SVGColor.parse("rgb(100, 200, 50)");
      assertNotNull(color);
      assertTrue(color instanceof SVGColor.Absolute);
      SVGColor.Absolute abs = (SVGColor.Absolute)color;
      assertEquals(100, abs.getR());
      assertEquals(200, abs.getG());
      assertEquals(50, abs.getB());
   }

   @Test
   void testNamedColor()
   {
      SVGColor color = SVGColor.parse("red");
      assertNotNull(color);
      assertTrue(color instanceof SVGColor.Absolute);
      SVGColor.Absolute abs = (SVGColor.Absolute)color;
      assertEquals(255, abs.getR());
      assertEquals(0, abs.getG());
      assertEquals(0, abs.getB());
   }

   @Test
   void testNamedColorCaseInsensitive()
   {
      SVGColor color = SVGColor.parse("DarkBlue");
      assertNotNull(color);
      assertTrue(color instanceof SVGColor.Absolute);
      SVGColor.Absolute abs = (SVGColor.Absolute)color;
      assertEquals(0, abs.getR());
      assertEquals(0, abs.getG());
      assertEquals(139, abs.getB());
   }

   @Test
   void testCurrentColor()
   {
      SVGColor color = SVGColor.parse("currentColor");
      assertNotNull(color);
      assertTrue(color instanceof SVGColor.CurrentColor);
      assertSame(SVGColor.CurrentColor.INSTANCE, color);
   }

   @Test
   void testCurrentColorCaseInsensitive()
   {
      SVGColor color = SVGColor.parse("CURRENTCOLOR");
      assertNotNull(color);
      assertTrue(color instanceof SVGColor.CurrentColor);
   }

   @Test
   void testNone()
   {
      SVGColor color = SVGColor.parse("none");
      assertNotNull(color);
      assertTrue(color instanceof SVGColor.None);
      assertSame(SVGColor.None.INSTANCE, color);
   }

   @Test
   void testInheritReturnsNull()
   {
      assertNull(SVGColor.parse("inherit"));
   }

   @Test
   void testInvalidReturnsNull()
   {
      assertNull(SVGColor.parse("not-a-color"));
   }

   @Test
   void testNullReturnsNull()
   {
      assertNull(SVGColor.parse(null));
   }

   @Test
   void testEmptyReturnsNull()
   {
      assertNull(SVGColor.parse(""));
   }

   @Test
   void testRgbClamping()
   {
      SVGColor color = SVGColor.parse("rgb(300, -10, 128)");
      assertNotNull(color);
      assertTrue(color instanceof SVGColor.Absolute);
      SVGColor.Absolute abs = (SVGColor.Absolute)color;
      assertEquals(255, abs.getR());
      assertEquals(0, abs.getG());
      assertEquals(128, abs.getB());
   }
}
