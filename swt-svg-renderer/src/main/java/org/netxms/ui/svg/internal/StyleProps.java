package org.netxms.ui.svg.internal;

import org.w3c.dom.Element;

/**
 * Immutable style properties for an SVG element.
 * Null fields mean "inherit from parent". Float.NaN means "unset".
 * -1 for int fields means "unset".
 */
public final class StyleProps
{
   private final SVGColor fill;
   private final SVGColor stroke;
   private final float fillOpacity;
   private final float strokeOpacity;
   private final float strokeWidth;
   private final float opacity;
   private final int fillRule;
   private final int lineCap;
   private final int lineJoin;

   public static final int FILL_RULE_UNSET = -1;
   public static final int FILL_RULE_NONZERO = 0;
   public static final int FILL_RULE_EVENODD = 1;

   public static final int LINE_CAP_UNSET = -1;
   public static final int LINE_CAP_BUTT = 0;
   public static final int LINE_CAP_ROUND = 1;
   public static final int LINE_CAP_SQUARE = 2;

   public static final int LINE_JOIN_UNSET = -1;
   public static final int LINE_JOIN_MITER = 0;
   public static final int LINE_JOIN_ROUND = 1;
   public static final int LINE_JOIN_BEVEL = 2;

   /**
    * Create a new style properties instance.
    *
    * @param fill fill color, or null for inherit
    * @param stroke stroke color, or null for inherit
    * @param fillOpacity fill opacity (0.0-1.0), or Float.NaN for inherit
    * @param strokeOpacity stroke opacity (0.0-1.0), or Float.NaN for inherit
    * @param strokeWidth stroke width, or Float.NaN for inherit
    * @param opacity element-level opacity (0.0-1.0), or Float.NaN for inherit
    * @param fillRule fill rule constant, or FILL_RULE_UNSET for inherit
    * @param lineCap line cap constant, or LINE_CAP_UNSET for inherit
    * @param lineJoin line join constant, or LINE_JOIN_UNSET for inherit
    */
   public StyleProps(SVGColor fill, SVGColor stroke, float fillOpacity, float strokeOpacity,
         float strokeWidth, float opacity, int fillRule, int lineCap, int lineJoin)
   {
      this.fill = fill;
      this.stroke = stroke;
      this.fillOpacity = fillOpacity;
      this.strokeOpacity = strokeOpacity;
      this.strokeWidth = strokeWidth;
      this.opacity = opacity;
      this.fillRule = fillRule;
      this.lineCap = lineCap;
      this.lineJoin = lineJoin;
   }

   /**
    * Default (all-inherit) style.
    */
   public static final StyleProps EMPTY = new StyleProps(
         null, null, Float.NaN, Float.NaN, Float.NaN, Float.NaN,
         FILL_RULE_UNSET, LINE_CAP_UNSET, LINE_JOIN_UNSET);

   /**
    * Root default style: fill=currentColor (falls back to black when no
    * currentColor is provided), stroke=none, opacities=1.
    */
   public static final StyleProps ROOT_DEFAULTS = new StyleProps(
         SVGColor.CurrentColor.INSTANCE, SVGColor.None.INSTANCE,
         1.0f, 1.0f, 1.0f, 1.0f,
         FILL_RULE_NONZERO, LINE_CAP_BUTT, LINE_JOIN_MITER);

   /**
    * @return fill color, or null if inherited
    */
   public SVGColor getFill()
   {
      return fill;
   }

   /**
    * @return stroke color, or null if inherited
    */
   public SVGColor getStroke()
   {
      return stroke;
   }

   /**
    * @return fill opacity (0.0-1.0), or Float.NaN if inherited
    */
   public float getFillOpacity()
   {
      return fillOpacity;
   }

   /**
    * @return stroke opacity (0.0-1.0), or Float.NaN if inherited
    */
   public float getStrokeOpacity()
   {
      return strokeOpacity;
   }

   /**
    * @return stroke width, or Float.NaN if inherited
    */
   public float getStrokeWidth()
   {
      return strokeWidth;
   }

   /**
    * @return element-level opacity (0.0-1.0), or Float.NaN if inherited
    */
   public float getOpacity()
   {
      return opacity;
   }

   /**
    * @return fill rule constant (FILL_RULE_NONZERO, FILL_RULE_EVENODD, or FILL_RULE_UNSET)
    */
   public int getFillRule()
   {
      return fillRule;
   }

   /**
    * @return line cap constant (LINE_CAP_BUTT, LINE_CAP_ROUND, LINE_CAP_SQUARE, or LINE_CAP_UNSET)
    */
   public int getLineCap()
   {
      return lineCap;
   }

   /**
    * @return line join constant (LINE_JOIN_MITER, LINE_JOIN_ROUND, LINE_JOIN_BEVEL, or LINE_JOIN_UNSET)
    */
   public int getLineJoin()
   {
      return lineJoin;
   }

   /**
    * Resolve this style against a parent style. Any unset property in this style
    * is inherited from the parent.
    *
    * @param parent the parent style to inherit unset properties from
    * @return new StyleProps with all properties resolved
    */
   public StyleProps resolve(StyleProps parent)
   {
      return new StyleProps(
            fill != null ? fill : parent.fill,
            stroke != null ? stroke : parent.stroke,
            !Float.isNaN(fillOpacity) ? fillOpacity : parent.fillOpacity,
            !Float.isNaN(strokeOpacity) ? strokeOpacity : parent.strokeOpacity,
            !Float.isNaN(strokeWidth) ? strokeWidth : parent.strokeWidth,
            !Float.isNaN(opacity) ? opacity : parent.opacity,
            fillRule != FILL_RULE_UNSET ? fillRule : parent.fillRule,
            lineCap != LINE_CAP_UNSET ? lineCap : parent.lineCap,
            lineJoin != LINE_JOIN_UNSET ? lineJoin : parent.lineJoin);
   }

   /**
    * Parse style properties from a DOM element. Reads both XML attributes
    * and inline style attribute (style takes precedence).
    *
    * @param element the DOM element to parse
    * @return parsed style properties, or {@link #EMPTY} if no style attributes are present
    */
   public static StyleProps parse(Element element)
   {
      SVGColor fill = null;
      SVGColor stroke = null;
      float fillOpacity = Float.NaN;
      float strokeOpacity = Float.NaN;
      float strokeWidth = Float.NaN;
      float opacity = Float.NaN;
      int fillRule = FILL_RULE_UNSET;
      int lineCap = LINE_CAP_UNSET;
      int lineJoin = LINE_JOIN_UNSET;

      // Read from attributes first
      fill = parseColorAttr(element, "fill");
      stroke = parseColorAttr(element, "stroke");
      fillOpacity = parseFloatAttr(element, "fill-opacity");
      strokeOpacity = parseFloatAttr(element, "stroke-opacity");
      strokeWidth = parseFloatAttr(element, "stroke-width");
      opacity = parseFloatAttr(element, "opacity");
      fillRule = parseFillRule(element.getAttribute("fill-rule"));
      lineCap = parseLineCap(element.getAttribute("stroke-linecap"));
      lineJoin = parseLineJoin(element.getAttribute("stroke-linejoin"));

      // Override with inline style (higher precedence)
      String style = element.getAttribute("style");
      if (style != null && !style.isEmpty())
      {
         String[] declarations = style.split(";");
         for (String decl : declarations)
         {
            int colon = decl.indexOf(':');
            if (colon < 0)
               continue;
            String prop = decl.substring(0, colon).trim().toLowerCase();
            String value = decl.substring(colon + 1).trim();

            switch(prop)
            {
               case "fill":
                  fill = SVGColor.parse(value);
                  break;
               case "stroke":
                  stroke = SVGColor.parse(value);
                  break;
               case "fill-opacity":
                  fillOpacity = parseFloat(value);
                  break;
               case "stroke-opacity":
                  strokeOpacity = parseFloat(value);
                  break;
               case "stroke-width":
                  strokeWidth = parseFloat(value);
                  break;
               case "opacity":
                  opacity = parseFloat(value);
                  break;
               case "fill-rule":
                  fillRule = parseFillRule(value);
                  break;
               case "stroke-linecap":
                  lineCap = parseLineCap(value);
                  break;
               case "stroke-linejoin":
                  lineJoin = parseLineJoin(value);
                  break;
            }
         }
      }

      // If everything is at default, return EMPTY to avoid allocation
      if (fill == null && stroke == null && Float.isNaN(fillOpacity) && Float.isNaN(strokeOpacity) &&
            Float.isNaN(strokeWidth) && Float.isNaN(opacity) &&
            fillRule == FILL_RULE_UNSET && lineCap == LINE_CAP_UNSET && lineJoin == LINE_JOIN_UNSET)
      {
         return EMPTY;
      }

      return new StyleProps(fill, stroke, fillOpacity, strokeOpacity, strokeWidth, opacity,
            fillRule, lineCap, lineJoin);
   }

   private static SVGColor parseColorAttr(Element element, String name)
   {
      String value = element.getAttribute(name);
      if (value == null || value.isEmpty())
         return null;
      return SVGColor.parse(value);
   }

   private static float parseFloatAttr(Element element, String name)
   {
      String value = element.getAttribute(name);
      if (value == null || value.isEmpty())
         return Float.NaN;
      return parseFloat(value);
   }

   private static float parseFloat(String value)
   {
      try
      {
         return Float.parseFloat(value.trim());
      }
      catch(NumberFormatException e)
      {
         return Float.NaN;
      }
   }

   private static int parseFillRule(String value)
   {
      if (value == null || value.isEmpty())
         return FILL_RULE_UNSET;
      switch(value.trim().toLowerCase())
      {
         case "nonzero":
            return FILL_RULE_NONZERO;
         case "evenodd":
            return FILL_RULE_EVENODD;
         default:
            return FILL_RULE_UNSET;
      }
   }

   private static int parseLineCap(String value)
   {
      if (value == null || value.isEmpty())
         return LINE_CAP_UNSET;
      switch(value.trim().toLowerCase())
      {
         case "butt":
            return LINE_CAP_BUTT;
         case "round":
            return LINE_CAP_ROUND;
         case "square":
            return LINE_CAP_SQUARE;
         default:
            return LINE_CAP_UNSET;
      }
   }

   private static int parseLineJoin(String value)
   {
      if (value == null || value.isEmpty())
         return LINE_JOIN_UNSET;
      switch(value.trim().toLowerCase())
      {
         case "miter":
            return LINE_JOIN_MITER;
         case "round":
            return LINE_JOIN_ROUND;
         case "bevel":
            return LINE_JOIN_BEVEL;
         default:
            return LINE_JOIN_UNSET;
      }
   }
}
