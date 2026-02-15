package org.netxms.ui.svg.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class PathDataParserTest
{
   @Test
   void testMoveTo()
   {
      List<PathSegment> segs = PathDataParser.parse("M 10 20");
      assertEquals(1, segs.size());
      assertTrue(segs.get(0) instanceof PathSegment.MoveTo);
      PathSegment.MoveTo m = (PathSegment.MoveTo)segs.get(0);
      assertEquals(10f, m.x);
      assertEquals(20f, m.y);
   }

   @Test
   void testLineTo()
   {
      List<PathSegment> segs = PathDataParser.parse("M 0 0 L 10 20");
      assertEquals(2, segs.size());
      assertTrue(segs.get(1) instanceof PathSegment.LineTo);
      PathSegment.LineTo l = (PathSegment.LineTo)segs.get(1);
      assertEquals(10f, l.x);
      assertEquals(20f, l.y);
   }

   @Test
   void testRelativeLineTo()
   {
      List<PathSegment> segs = PathDataParser.parse("M 10 10 l 5 5");
      assertEquals(2, segs.size());
      PathSegment.LineTo l = (PathSegment.LineTo)segs.get(1);
      assertEquals(15f, l.x);
      assertEquals(15f, l.y);
   }

   @Test
   void testHorizontalLine()
   {
      List<PathSegment> segs = PathDataParser.parse("M 0 5 H 10");
      assertEquals(2, segs.size());
      PathSegment.LineTo l = (PathSegment.LineTo)segs.get(1);
      assertEquals(10f, l.x);
      assertEquals(5f, l.y);
   }

   @Test
   void testVerticalLine()
   {
      List<PathSegment> segs = PathDataParser.parse("M 5 0 V 10");
      assertEquals(2, segs.size());
      PathSegment.LineTo l = (PathSegment.LineTo)segs.get(1);
      assertEquals(5f, l.x);
      assertEquals(10f, l.y);
   }

   @Test
   void testClose()
   {
      List<PathSegment> segs = PathDataParser.parse("M 0 0 L 10 0 L 10 10 Z");
      assertEquals(4, segs.size());
      assertTrue(segs.get(3) instanceof PathSegment.Close);
   }

   @Test
   void testCubicBezier()
   {
      List<PathSegment> segs = PathDataParser.parse("M 0 0 C 10 20 30 40 50 60");
      assertEquals(2, segs.size());
      assertTrue(segs.get(1) instanceof PathSegment.CubicTo);
      PathSegment.CubicTo c = (PathSegment.CubicTo)segs.get(1);
      assertEquals(10f, c.x1);
      assertEquals(20f, c.y1);
      assertEquals(30f, c.x2);
      assertEquals(40f, c.y2);
      assertEquals(50f, c.x);
      assertEquals(60f, c.y);
   }

   @Test
   void testSmoothCubic()
   {
      List<PathSegment> segs = PathDataParser.parse("M 0 0 C 10 20 30 40 50 60 S 70 80 90 100");
      assertEquals(3, segs.size());
      assertTrue(segs.get(2) instanceof PathSegment.CubicTo);
      PathSegment.CubicTo s = (PathSegment.CubicTo)segs.get(2);
      // Reflected control point: 2*50-30=70, 2*60-40=80
      assertEquals(70f, s.x1);
      assertEquals(80f, s.y1);
      assertEquals(70f, s.x2);
      assertEquals(80f, s.y2);
      assertEquals(90f, s.x);
      assertEquals(100f, s.y);
   }

   @Test
   void testQuadBezier()
   {
      List<PathSegment> segs = PathDataParser.parse("M 0 0 Q 10 20 30 40");
      assertEquals(2, segs.size());
      assertTrue(segs.get(1) instanceof PathSegment.QuadTo);
      PathSegment.QuadTo q = (PathSegment.QuadTo)segs.get(1);
      assertEquals(10f, q.x1);
      assertEquals(20f, q.y1);
      assertEquals(30f, q.x);
      assertEquals(40f, q.y);
   }

   @Test
   void testSmoothQuad()
   {
      List<PathSegment> segs = PathDataParser.parse("M 0 0 Q 10 20 30 40 T 50 60");
      assertEquals(3, segs.size());
      assertTrue(segs.get(2) instanceof PathSegment.QuadTo);
      PathSegment.QuadTo t = (PathSegment.QuadTo)segs.get(2);
      // Reflected: 2*30-10=50, 2*40-20=60
      assertEquals(50f, t.x1);
      assertEquals(60f, t.y1);
      assertEquals(50f, t.x);
      assertEquals(60f, t.y);
   }

   @Test
   void testArcToCubics()
   {
      List<PathSegment> segs = PathDataParser.parse("M 0 0 A 25 25 0 0 1 50 0");
      assertTrue(segs.size() >= 2);
      // First is MoveTo, rest should be CubicTo segments (arc converted)
      assertTrue(segs.get(0) instanceof PathSegment.MoveTo);
      for (int i = 1; i < segs.size(); i++)
      {
         assertTrue(segs.get(i) instanceof PathSegment.CubicTo,
               "Segment " + i + " should be CubicTo");
      }
      // Last cubic should end at (50, 0)
      PathSegment.CubicTo last = (PathSegment.CubicTo)segs.get(segs.size() - 1);
      assertEquals(50f, last.x, 0.1f);
      assertEquals(0f, last.y, 0.1f);
   }

   @Test
   void testArcCompactFlags()
   {
      // "A3.6 3.6 0 1112 8.4" = rx=3.6, ry=3.6, rotation=0, largeArc=1, sweep=1, x=12, y=8.4
      List<PathSegment> segs = PathDataParser.parse("M12 15.6A3.6 3.6 0 1112 8.4a3.6 3.6 0 010 7.2z");
      assertTrue(segs.size() >= 3); // MoveTo + cubics from two arcs + Close
      assertTrue(segs.get(0) instanceof PathSegment.MoveTo);
      PathSegment.MoveTo m = (PathSegment.MoveTo)segs.get(0);
      assertEquals(12f, m.x);
      assertEquals(15.6f, m.y, 0.01f);
      // Verify it doesn't degenerate — should have multiple cubic segments
      int cubicCount = 0;
      for (PathSegment seg : segs)
      {
         if (seg instanceof PathSegment.CubicTo)
            cubicCount++;
      }
      assertTrue(cubicCount >= 4, "Expected at least 4 cubic segments for two arcs, got " + cubicCount);
      // Last segment should be Close
      assertTrue(segs.get(segs.size() - 1) instanceof PathSegment.Close);
   }

   @Test
   void testImplicitLineToAfterMove()
   {
      List<PathSegment> segs = PathDataParser.parse("M 0 0 10 20 30 40");
      assertEquals(3, segs.size());
      assertTrue(segs.get(0) instanceof PathSegment.MoveTo);
      assertTrue(segs.get(1) instanceof PathSegment.LineTo);
      assertTrue(segs.get(2) instanceof PathSegment.LineTo);
      PathSegment.LineTo l = (PathSegment.LineTo)segs.get(2);
      assertEquals(30f, l.x);
      assertEquals(40f, l.y);
   }

   @Test
   void testCompactNotation()
   {
      // No spaces, negative signs as separators
      List<PathSegment> segs = PathDataParser.parse("M0,0L10-20");
      assertEquals(2, segs.size());
      assertTrue(segs.get(0) instanceof PathSegment.MoveTo);
      PathSegment.LineTo l = (PathSegment.LineTo)segs.get(1);
      assertEquals(10f, l.x);
      assertEquals(-20f, l.y);
   }

   @Test
   void testNullReturnsEmpty()
   {
      assertTrue(PathDataParser.parse(null).isEmpty());
   }

   @Test
   void testEmptyReturnsEmpty()
   {
      assertTrue(PathDataParser.parse("").isEmpty());
   }

   @Test
   void testMultipleSubpaths()
   {
      List<PathSegment> segs = PathDataParser.parse("M 0 0 L 10 10 Z M 20 20 L 30 30 Z");
      assertEquals(6, segs.size());
      assertTrue(segs.get(0) instanceof PathSegment.MoveTo);
      assertTrue(segs.get(1) instanceof PathSegment.LineTo);
      assertTrue(segs.get(2) instanceof PathSegment.Close);
      assertTrue(segs.get(3) instanceof PathSegment.MoveTo);
      assertTrue(segs.get(4) instanceof PathSegment.LineTo);
      assertTrue(segs.get(5) instanceof PathSegment.Close);
   }

   @Test
   void testRelativeMoveTo()
   {
      List<PathSegment> segs = PathDataParser.parse("M 10 10 m 5 5 l 10 10");
      assertEquals(3, segs.size());
      PathSegment.MoveTo m = (PathSegment.MoveTo)segs.get(1);
      assertEquals(15f, m.x);
      assertEquals(15f, m.y);
   }
}
