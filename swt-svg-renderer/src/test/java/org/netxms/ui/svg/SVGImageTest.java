package org.netxms.ui.svg;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SVGImageTest
{
   @Test
   void testCreateFromString() throws SVGParseException
   {
      SVGImage image = SVGImage.createFromString(
            "<svg viewBox=\"0 0 100 100\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect x=\"10\" y=\"10\" width=\"80\" height=\"80\"/>" +
            "</svg>");
      assertNotNull(image);
   }

   @Test
   void testGetDimensions() throws SVGParseException
   {
      SVGImage image = SVGImage.createFromString(
            "<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect x=\"0\" y=\"0\" width=\"200\" height=\"100\"/>" +
            "</svg>");
      assertEquals(200f, image.getWidth());
      assertEquals(100f, image.getHeight());
      assertEquals(2f, image.getAspectRatio(), 0.001f);
   }

   @Test
   void testGetDimensionsFromWidthHeight() throws SVGParseException
   {
      SVGImage image = SVGImage.createFromString(
            "<svg width=\"50\" height=\"25\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect x=\"0\" y=\"0\" width=\"50\" height=\"25\"/>" +
            "</svg>");
      assertEquals(50f, image.getWidth());
      assertEquals(25f, image.getHeight());
   }

   @Test
   void testMalformedSvgThrows()
   {
      assertThrows(SVGParseException.class, () -> SVGImage.createFromString("<not-svg>"));
   }

   @Test
   void testNotSvgRootThrows()
   {
      assertThrows(SVGParseException.class, () -> SVGImage.createFromString("<html></html>"));
   }

   @Test
   void testEmptyContentThrows()
   {
      assertThrows(SVGParseException.class, () -> SVGImage.createFromString(""));
   }

   @Test
   void testNoDimensionsDefault() throws SVGParseException
   {
      SVGImage image = SVGImage.createFromString(
            "<svg xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect x=\"0\" y=\"0\" width=\"50\" height=\"50\"/>" +
            "</svg>");
      // Should default to 100x100
      assertEquals(100f, image.getWidth());
      assertEquals(100f, image.getHeight());
   }

   @Test
   void testCreateFromStreamNull()
   {
      assertThrows(Exception.class, () -> SVGImage.createFromStream(null));
   }
}
