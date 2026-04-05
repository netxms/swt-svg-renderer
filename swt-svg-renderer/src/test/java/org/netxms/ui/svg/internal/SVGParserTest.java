package org.netxms.ui.svg.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.netxms.ui.svg.SVGParseException;

class SVGParserTest
{
   @Test
   void testParseRect() throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(loadResource("rect.svg"));
      assertEquals(100, doc.getViewBoxWidth());
      assertEquals(100, doc.getViewBoxHeight());
      assertEquals(1, doc.getChildren().size());
      assertTrue(doc.getChildren().get(0) instanceof SVGRect);
      SVGRect rect = (SVGRect)doc.getChildren().get(0);
      assertEquals(10, rect.getX());
      assertEquals(10, rect.getY());
      assertEquals(80, rect.getWidth());
      assertEquals(80, rect.getHeight());
   }

   @Test
   void testParseCircle() throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(loadResource("circle.svg"));
      assertEquals(1, doc.getChildren().size());
      assertTrue(doc.getChildren().get(0) instanceof SVGCircle);
      SVGCircle circle = (SVGCircle)doc.getChildren().get(0);
      assertEquals(50, circle.getCx());
      assertEquals(50, circle.getCy());
      assertEquals(40, circle.getR());
   }

   @Test
   void testParsePath() throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(loadResource("path.svg"));
      assertEquals(1, doc.getChildren().size());
      assertTrue(doc.getChildren().get(0) instanceof SVGPath);
      SVGPath path = (SVGPath)doc.getChildren().get(0);
      assertFalse(path.getSegments().isEmpty());
   }

   @Test
   void testParseGroup() throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(loadResource("group.svg"));
      assertEquals(1, doc.getChildren().size());
      assertTrue(doc.getChildren().get(0) instanceof SVGGroup);
      SVGGroup group = (SVGGroup)doc.getChildren().get(0);
      assertEquals(2, group.getChildren().size());
   }

   @Test
   void testParseViewBox() throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(loadResource("viewbox.svg"));
      assertEquals(10, doc.getViewBoxX());
      assertEquals(10, doc.getViewBoxY());
      assertEquals(80, doc.getViewBoxWidth());
      assertEquals(80, doc.getViewBoxHeight());
   }

   @Test
   void testParseStyleAttributes() throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(loadResource("styled.svg"));
      assertEquals(1, doc.getChildren().size());
      SVGRect rect = (SVGRect)doc.getChildren().get(0);
      StyleProps style = rect.getStyle();
      assertTrue(style.getFill() instanceof SVGColor.Absolute);
      SVGColor.Absolute fill = (SVGColor.Absolute)style.getFill();
      assertEquals(255, fill.getR());
      assertEquals(0, fill.getG());
      assertEquals(0, fill.getB());
   }

   @Test
   void testUnsupportedElementsSkipped() throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(loadResource("unsupported.svg"));
      // Should parse without error, unsupported elements skipped
      // The rect should still be present
      assertEquals(1, doc.getChildren().size());
      assertTrue(doc.getChildren().get(0) instanceof SVGRect);
   }

   @Test
   void testDisplayNone() throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(loadResource("display_none.svg"));
      assertEquals(2, doc.getChildren().size());
      // First rect has display:none
      assertFalse(doc.getChildren().get(0).isDisplay());
      // Second rect is visible
      assertTrue(doc.getChildren().get(1).isDisplay());
   }

   @Test
   void testMalformedXml()
   {
      assertThrows(SVGParseException.class, () -> SVGParser.parse("<not valid xml"));
   }

   @Test
   void testNotSvgRoot()
   {
      assertThrows(SVGParseException.class, () -> SVGParser.parse("<html></html>"));
   }

   @Test
   void testParseTransform() throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(loadResource("transform.svg"));
      assertEquals(1, doc.getChildren().size());
      SVGGroup group = (SVGGroup)doc.getChildren().get(0);
      float[] transform = group.getTransform();
      // translate(10, 20) scale(2)
      // Point (0,0) -> scale(2) -> (0,0) -> translate(10,20) -> (10,20)
      float x = transform[0] * 0 + transform[2] * 0 + transform[4];
      float y = transform[1] * 0 + transform[3] * 0 + transform[5];
      assertEquals(10, x, 0.001f);
      assertEquals(20, y, 0.001f);
   }

   @Test
   void testParseAllShapes() throws SVGParseException
   {
      SVGDocument doc = SVGParser.parse(loadResource("all_shapes.svg"));
      assertTrue(doc.getChildren().size() >= 7);
   }

   @Test
   void testParseDimensionUnits() throws SVGParseException
   {
      // 100mm = 100 * (96/25.4) ≈ 377.953, 50mm ≈ 188.976
      SVGDocument doc = SVGParser.parse(loadResource("units_mm.svg"));
      assertEquals(200, doc.getViewBoxWidth());
      assertEquals(100, doc.getViewBoxHeight());
      assertEquals(100 * 96.0f / 25.4f, doc.getWidth(), 0.01f);
      assertEquals(50 * 96.0f / 25.4f, doc.getHeight(), 0.01f);
   }

   @Test
   void testParseDimensionVariousUnits() throws SVGParseException
   {
      // Test cm: 10cm = 10 * (96/2.54) ≈ 377.953
      SVGDocument doc = SVGParser.parse("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"10cm\" height=\"5cm\"></svg>");
      assertEquals(10 * 96.0f / 2.54f, doc.getWidth(), 0.01f);
      assertEquals(5 * 96.0f / 2.54f, doc.getHeight(), 0.01f);

      // Test in: 2in = 192px
      doc = SVGParser.parse("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"2in\" height=\"3in\"></svg>");
      assertEquals(192, doc.getWidth(), 0.01f);
      assertEquals(288, doc.getHeight(), 0.01f);

      // Test pt: 72pt = 96px
      doc = SVGParser.parse("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"72pt\" height=\"36pt\"></svg>");
      assertEquals(96, doc.getWidth(), 0.01f);
      assertEquals(48, doc.getHeight(), 0.01f);

      // Test pc: 6pc = 96px
      doc = SVGParser.parse("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"6pc\" height=\"3pc\"></svg>");
      assertEquals(96, doc.getWidth(), 0.01f);
      assertEquals(48, doc.getHeight(), 0.01f);

      // Test px (unchanged): 100px = 100
      doc = SVGParser.parse("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100px\" height=\"200px\"></svg>");
      assertEquals(100, doc.getWidth(), 0.01f);
      assertEquals(200, doc.getHeight(), 0.01f);
   }

   private InputStream loadResource(String name)
   {
      InputStream is = getClass().getClassLoader().getResourceAsStream(name);
      assertNotNull(is, "Test resource not found: " + name);
      return is;
   }
}
