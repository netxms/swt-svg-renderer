package org.netxms.ui.svg.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StylePropsTest
{
   @Test
   void testEmptyIsAllUnset()
   {
      StyleProps props = StyleProps.EMPTY;
      assertNull(props.getFill());
      assertNull(props.getStroke());
      assertTrue(Float.isNaN(props.getFillOpacity()));
      assertTrue(Float.isNaN(props.getStrokeOpacity()));
      assertTrue(Float.isNaN(props.getStrokeWidth()));
      assertTrue(Float.isNaN(props.getOpacity()));
      assertEquals(StyleProps.FILL_RULE_UNSET, props.getFillRule());
      assertEquals(StyleProps.LINE_CAP_UNSET, props.getLineCap());
      assertEquals(StyleProps.LINE_JOIN_UNSET, props.getLineJoin());
   }

   @Test
   void testRootDefaults()
   {
      StyleProps props = StyleProps.ROOT_DEFAULTS;
      assertTrue(props.getFill() instanceof SVGColor.Absolute);
      assertTrue(props.getStroke() instanceof SVGColor.None);
      assertEquals(1.0f, props.getFillOpacity());
      assertEquals(1.0f, props.getStrokeOpacity());
      assertEquals(1.0f, props.getStrokeWidth());
      assertEquals(1.0f, props.getOpacity());
      assertEquals(StyleProps.FILL_RULE_NONZERO, props.getFillRule());
      assertEquals(StyleProps.LINE_CAP_BUTT, props.getLineCap());
      assertEquals(StyleProps.LINE_JOIN_MITER, props.getLineJoin());
   }

   @Test
   void testResolveInheritsFromParent()
   {
      StyleProps child = StyleProps.EMPTY;
      StyleProps resolved = child.resolve(StyleProps.ROOT_DEFAULTS);

      assertTrue(resolved.getFill() instanceof SVGColor.Absolute);
      assertTrue(resolved.getStroke() instanceof SVGColor.None);
      assertEquals(1.0f, resolved.getFillOpacity());
      assertEquals(StyleProps.FILL_RULE_NONZERO, resolved.getFillRule());
   }

   @Test
   void testResolveChildOverridesParent()
   {
      SVGColor red = new SVGColor.Absolute(255, 0, 0);
      StyleProps child = new StyleProps(red, null, 0.5f, Float.NaN, Float.NaN, Float.NaN,
            StyleProps.FILL_RULE_EVENODD, StyleProps.LINE_CAP_UNSET, StyleProps.LINE_JOIN_UNSET);

      StyleProps resolved = child.resolve(StyleProps.ROOT_DEFAULTS);

      assertEquals(red, resolved.getFill());
      assertTrue(resolved.getStroke() instanceof SVGColor.None); // inherited
      assertEquals(0.5f, resolved.getFillOpacity());
      assertEquals(1.0f, resolved.getStrokeOpacity()); // inherited
      assertEquals(StyleProps.FILL_RULE_EVENODD, resolved.getFillRule());
      assertEquals(StyleProps.LINE_CAP_BUTT, resolved.getLineCap()); // inherited
   }

   @Test
   void testResolveCurrentColor()
   {
      StyleProps child = new StyleProps(SVGColor.CurrentColor.INSTANCE, null,
            Float.NaN, Float.NaN, Float.NaN, Float.NaN,
            StyleProps.FILL_RULE_UNSET, StyleProps.LINE_CAP_UNSET, StyleProps.LINE_JOIN_UNSET);

      StyleProps resolved = child.resolve(StyleProps.ROOT_DEFAULTS);
      assertTrue(resolved.getFill() instanceof SVGColor.CurrentColor);
   }

   @Test
   void testResolveNone()
   {
      StyleProps child = new StyleProps(SVGColor.None.INSTANCE, null,
            Float.NaN, Float.NaN, Float.NaN, Float.NaN,
            StyleProps.FILL_RULE_UNSET, StyleProps.LINE_CAP_UNSET, StyleProps.LINE_JOIN_UNSET);

      StyleProps resolved = child.resolve(StyleProps.ROOT_DEFAULTS);
      assertTrue(resolved.getFill() instanceof SVGColor.None);
   }

   @Test
   void testResolveChain()
   {
      SVGColor blue = new SVGColor.Absolute(0, 0, 255);
      StyleProps grandparent = new StyleProps(blue, null, 0.8f, Float.NaN, 2.0f, Float.NaN,
            StyleProps.FILL_RULE_UNSET, StyleProps.LINE_CAP_ROUND, StyleProps.LINE_JOIN_UNSET);

      StyleProps parent = StyleProps.EMPTY.resolve(grandparent.resolve(StyleProps.ROOT_DEFAULTS));
      StyleProps child = StyleProps.EMPTY.resolve(parent);

      assertEquals(blue, child.getFill());
      assertEquals(0.8f, child.getFillOpacity());
      assertEquals(2.0f, child.getStrokeWidth());
      assertEquals(StyleProps.LINE_CAP_ROUND, child.getLineCap());
   }

   @Test
   void testResolveMultipleLevels()
   {
      SVGColor green = new SVGColor.Absolute(0, 128, 0);
      StyleProps level1 = new StyleProps(green, null, Float.NaN, Float.NaN, 3.0f, Float.NaN,
            StyleProps.FILL_RULE_UNSET, StyleProps.LINE_CAP_UNSET, StyleProps.LINE_JOIN_BEVEL);
      StyleProps level2 = new StyleProps(null, SVGColor.CurrentColor.INSTANCE, 0.7f, Float.NaN, Float.NaN, Float.NaN,
            StyleProps.FILL_RULE_UNSET, StyleProps.LINE_CAP_UNSET, StyleProps.LINE_JOIN_UNSET);

      StyleProps resolved1 = level1.resolve(StyleProps.ROOT_DEFAULTS);
      StyleProps resolved2 = level2.resolve(resolved1);

      assertEquals(green, resolved2.getFill()); // inherited through chain
      assertTrue(resolved2.getStroke() instanceof SVGColor.CurrentColor); // overridden
      assertEquals(0.7f, resolved2.getFillOpacity()); // overridden
      assertEquals(3.0f, resolved2.getStrokeWidth()); // inherited
      assertEquals(StyleProps.LINE_JOIN_BEVEL, resolved2.getLineJoin()); // inherited
   }
}
