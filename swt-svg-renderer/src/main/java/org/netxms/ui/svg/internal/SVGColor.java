package org.netxms.ui.svg.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an SVG color value. Abstract base with three subclass types:
 * Absolute (specific RGB), CurrentColor (resolved at render time), and None (no paint).
 */
public abstract class SVGColor
{
   private SVGColor()
   {
   }

   /**
    * An absolute RGB color.
    */
   public static final class Absolute extends SVGColor
   {
      private final int r;
      private final int g;
      private final int b;

      /**
       * Create an absolute RGB color.
       *
       * @param r red component (0-255)
       * @param g green component (0-255)
       * @param b blue component (0-255)
       */
      public Absolute(int r, int g, int b)
      {
         this.r = r;
         this.g = g;
         this.b = b;
      }

      /**
       * @return red component (0-255)
       */
      public int getR()
      {
         return r;
      }

      /**
       * @return green component (0-255)
       */
      public int getG()
      {
         return g;
      }

      /**
       * @return blue component (0-255)
       */
      public int getB()
      {
         return b;
      }

      @Override
      public boolean equals(Object o)
      {
         if (this == o)
            return true;
         if (!(o instanceof Absolute))
            return false;
         Absolute other = (Absolute)o;
         return r == other.r && g == other.g && b == other.b;
      }

      @Override
      public int hashCode()
      {
         return (r << 16) | (g << 8) | b;
      }

      @Override
      public String toString()
      {
         return "Absolute(" + r + "," + g + "," + b + ")";
      }
   }

   /**
    * Represents "currentColor" — resolved at render time.
    */
   public static final class CurrentColor extends SVGColor
   {
      public static final CurrentColor INSTANCE = new CurrentColor();

      private CurrentColor()
      {
      }

      @Override
      public String toString()
      {
         return "CurrentColor";
      }
   }

   /**
    * Represents "none" — no paint applied.
    */
   public static final class None extends SVGColor
   {
      public static final None INSTANCE = new None();

      private None()
      {
      }

      @Override
      public String toString()
      {
         return "None";
      }
   }

   /**
    * Parse an SVG color string.
    *
    * @param value the color string
    * @return parsed SVGColor, or null for "inherit" or unrecognized values
    */
   public static SVGColor parse(String value)
   {
      if (value == null)
         return null;

      String s = value.trim();
      if (s.isEmpty())
         return null;

      // Check for inherit
      if (s.equalsIgnoreCase("inherit"))
         return null;

      // Check for none
      if (s.equalsIgnoreCase("none"))
         return None.INSTANCE;

      // Check for currentColor
      if (s.equalsIgnoreCase("currentColor") || s.equalsIgnoreCase("currentcolor"))
         return CurrentColor.INSTANCE;

      // Hex colors
      if (s.startsWith("#"))
      {
         String hex = s.substring(1);
         if (hex.length() == 3)
         {
            int r = Integer.parseInt(hex.substring(0, 1), 16);
            int g = Integer.parseInt(hex.substring(1, 2), 16);
            int b = Integer.parseInt(hex.substring(2, 3), 16);
            return new Absolute(r * 17, g * 17, b * 17);
         }
         else if (hex.length() == 6)
         {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new Absolute(r, g, b);
         }
         return null;
      }

      // rgb() function
      if (s.startsWith("rgb(") && s.endsWith(")"))
      {
         String inner = s.substring(4, s.length() - 1).trim();
         String[] parts = inner.split("\\s*,\\s*");
         if (parts.length == 3)
         {
            try
            {
               int r = Integer.parseInt(parts[0].trim());
               int g = Integer.parseInt(parts[1].trim());
               int b = Integer.parseInt(parts[2].trim());
               return new Absolute(clamp(r), clamp(g), clamp(b));
            }
            catch(NumberFormatException e)
            {
               return null;
            }
         }
         return null;
      }

      // Named colors (case-insensitive)
      SVGColor named = NAMED_COLORS.get(s.toLowerCase());
      return named; // null if not found
   }

   private static int clamp(int value)
   {
      return Math.max(0, Math.min(255, value));
   }

   private static final Map<String, SVGColor> NAMED_COLORS = new HashMap<>();
   static
   {
      NAMED_COLORS.put("aliceblue", new Absolute(240, 248, 255));
      NAMED_COLORS.put("antiquewhite", new Absolute(250, 235, 215));
      NAMED_COLORS.put("aqua", new Absolute(0, 255, 255));
      NAMED_COLORS.put("aquamarine", new Absolute(127, 255, 212));
      NAMED_COLORS.put("azure", new Absolute(240, 255, 255));
      NAMED_COLORS.put("beige", new Absolute(245, 245, 220));
      NAMED_COLORS.put("bisque", new Absolute(255, 228, 196));
      NAMED_COLORS.put("black", new Absolute(0, 0, 0));
      NAMED_COLORS.put("blanchedalmond", new Absolute(255, 235, 205));
      NAMED_COLORS.put("blue", new Absolute(0, 0, 255));
      NAMED_COLORS.put("blueviolet", new Absolute(138, 43, 226));
      NAMED_COLORS.put("brown", new Absolute(165, 42, 42));
      NAMED_COLORS.put("burlywood", new Absolute(222, 184, 135));
      NAMED_COLORS.put("cadetblue", new Absolute(95, 158, 160));
      NAMED_COLORS.put("chartreuse", new Absolute(127, 255, 0));
      NAMED_COLORS.put("chocolate", new Absolute(210, 105, 30));
      NAMED_COLORS.put("coral", new Absolute(255, 127, 80));
      NAMED_COLORS.put("cornflowerblue", new Absolute(100, 149, 237));
      NAMED_COLORS.put("cornsilk", new Absolute(255, 248, 220));
      NAMED_COLORS.put("crimson", new Absolute(220, 20, 60));
      NAMED_COLORS.put("cyan", new Absolute(0, 255, 255));
      NAMED_COLORS.put("darkblue", new Absolute(0, 0, 139));
      NAMED_COLORS.put("darkcyan", new Absolute(0, 139, 139));
      NAMED_COLORS.put("darkgoldenrod", new Absolute(184, 134, 11));
      NAMED_COLORS.put("darkgray", new Absolute(169, 169, 169));
      NAMED_COLORS.put("darkgreen", new Absolute(0, 100, 0));
      NAMED_COLORS.put("darkgrey", new Absolute(169, 169, 169));
      NAMED_COLORS.put("darkkhaki", new Absolute(189, 183, 107));
      NAMED_COLORS.put("darkmagenta", new Absolute(139, 0, 139));
      NAMED_COLORS.put("darkolivegreen", new Absolute(85, 107, 47));
      NAMED_COLORS.put("darkorange", new Absolute(255, 140, 0));
      NAMED_COLORS.put("darkorchid", new Absolute(153, 50, 204));
      NAMED_COLORS.put("darkred", new Absolute(139, 0, 0));
      NAMED_COLORS.put("darksalmon", new Absolute(233, 150, 122));
      NAMED_COLORS.put("darkseagreen", new Absolute(143, 188, 143));
      NAMED_COLORS.put("darkslateblue", new Absolute(72, 61, 139));
      NAMED_COLORS.put("darkslategray", new Absolute(47, 79, 79));
      NAMED_COLORS.put("darkslategrey", new Absolute(47, 79, 79));
      NAMED_COLORS.put("darkturquoise", new Absolute(0, 206, 209));
      NAMED_COLORS.put("darkviolet", new Absolute(148, 0, 211));
      NAMED_COLORS.put("deeppink", new Absolute(255, 20, 147));
      NAMED_COLORS.put("deepskyblue", new Absolute(0, 191, 255));
      NAMED_COLORS.put("dimgray", new Absolute(105, 105, 105));
      NAMED_COLORS.put("dimgrey", new Absolute(105, 105, 105));
      NAMED_COLORS.put("dodgerblue", new Absolute(30, 144, 255));
      NAMED_COLORS.put("firebrick", new Absolute(178, 34, 34));
      NAMED_COLORS.put("floralwhite", new Absolute(255, 250, 240));
      NAMED_COLORS.put("forestgreen", new Absolute(34, 139, 34));
      NAMED_COLORS.put("fuchsia", new Absolute(255, 0, 255));
      NAMED_COLORS.put("gainsboro", new Absolute(220, 220, 220));
      NAMED_COLORS.put("ghostwhite", new Absolute(248, 248, 255));
      NAMED_COLORS.put("gold", new Absolute(255, 215, 0));
      NAMED_COLORS.put("goldenrod", new Absolute(218, 165, 32));
      NAMED_COLORS.put("gray", new Absolute(128, 128, 128));
      NAMED_COLORS.put("green", new Absolute(0, 128, 0));
      NAMED_COLORS.put("greenyellow", new Absolute(173, 255, 47));
      NAMED_COLORS.put("grey", new Absolute(128, 128, 128));
      NAMED_COLORS.put("honeydew", new Absolute(240, 255, 240));
      NAMED_COLORS.put("hotpink", new Absolute(255, 105, 180));
      NAMED_COLORS.put("indianred", new Absolute(205, 92, 92));
      NAMED_COLORS.put("indigo", new Absolute(75, 0, 130));
      NAMED_COLORS.put("ivory", new Absolute(255, 255, 240));
      NAMED_COLORS.put("khaki", new Absolute(240, 230, 140));
      NAMED_COLORS.put("lavender", new Absolute(230, 230, 250));
      NAMED_COLORS.put("lavenderblush", new Absolute(255, 240, 245));
      NAMED_COLORS.put("lawngreen", new Absolute(124, 252, 0));
      NAMED_COLORS.put("lemonchiffon", new Absolute(255, 250, 205));
      NAMED_COLORS.put("lightblue", new Absolute(173, 216, 230));
      NAMED_COLORS.put("lightcoral", new Absolute(240, 128, 128));
      NAMED_COLORS.put("lightcyan", new Absolute(224, 255, 255));
      NAMED_COLORS.put("lightgoldenrodyellow", new Absolute(250, 250, 210));
      NAMED_COLORS.put("lightgray", new Absolute(211, 211, 211));
      NAMED_COLORS.put("lightgreen", new Absolute(144, 238, 144));
      NAMED_COLORS.put("lightgrey", new Absolute(211, 211, 211));
      NAMED_COLORS.put("lightpink", new Absolute(255, 182, 193));
      NAMED_COLORS.put("lightsalmon", new Absolute(255, 160, 122));
      NAMED_COLORS.put("lightseagreen", new Absolute(32, 178, 170));
      NAMED_COLORS.put("lightskyblue", new Absolute(135, 206, 250));
      NAMED_COLORS.put("lightslategray", new Absolute(119, 136, 153));
      NAMED_COLORS.put("lightslategrey", new Absolute(119, 136, 153));
      NAMED_COLORS.put("lightsteelblue", new Absolute(176, 196, 222));
      NAMED_COLORS.put("lightyellow", new Absolute(255, 255, 224));
      NAMED_COLORS.put("lime", new Absolute(0, 255, 0));
      NAMED_COLORS.put("limegreen", new Absolute(50, 205, 50));
      NAMED_COLORS.put("linen", new Absolute(250, 240, 230));
      NAMED_COLORS.put("magenta", new Absolute(255, 0, 255));
      NAMED_COLORS.put("maroon", new Absolute(128, 0, 0));
      NAMED_COLORS.put("mediumaquamarine", new Absolute(102, 205, 170));
      NAMED_COLORS.put("mediumblue", new Absolute(0, 0, 205));
      NAMED_COLORS.put("mediumorchid", new Absolute(186, 85, 211));
      NAMED_COLORS.put("mediumpurple", new Absolute(147, 111, 219));
      NAMED_COLORS.put("mediumseagreen", new Absolute(60, 179, 113));
      NAMED_COLORS.put("mediumslateblue", new Absolute(123, 104, 238));
      NAMED_COLORS.put("mediumspringgreen", new Absolute(0, 250, 154));
      NAMED_COLORS.put("mediumturquoise", new Absolute(72, 209, 204));
      NAMED_COLORS.put("mediumvioletred", new Absolute(199, 21, 133));
      NAMED_COLORS.put("midnightblue", new Absolute(25, 25, 112));
      NAMED_COLORS.put("mintcream", new Absolute(245, 255, 250));
      NAMED_COLORS.put("mistyrose", new Absolute(255, 228, 225));
      NAMED_COLORS.put("moccasin", new Absolute(255, 228, 181));
      NAMED_COLORS.put("navajowhite", new Absolute(255, 222, 173));
      NAMED_COLORS.put("navy", new Absolute(0, 0, 128));
      NAMED_COLORS.put("oldlace", new Absolute(253, 245, 230));
      NAMED_COLORS.put("olive", new Absolute(128, 128, 0));
      NAMED_COLORS.put("olivedrab", new Absolute(107, 142, 35));
      NAMED_COLORS.put("orange", new Absolute(255, 165, 0));
      NAMED_COLORS.put("orangered", new Absolute(255, 69, 0));
      NAMED_COLORS.put("orchid", new Absolute(218, 112, 214));
      NAMED_COLORS.put("palegoldenrod", new Absolute(238, 232, 170));
      NAMED_COLORS.put("palegreen", new Absolute(152, 251, 152));
      NAMED_COLORS.put("paleturquoise", new Absolute(175, 238, 238));
      NAMED_COLORS.put("palevioletred", new Absolute(219, 112, 147));
      NAMED_COLORS.put("papayawhip", new Absolute(255, 239, 213));
      NAMED_COLORS.put("peachpuff", new Absolute(255, 218, 185));
      NAMED_COLORS.put("peru", new Absolute(205, 133, 63));
      NAMED_COLORS.put("pink", new Absolute(255, 192, 203));
      NAMED_COLORS.put("plum", new Absolute(221, 160, 221));
      NAMED_COLORS.put("powderblue", new Absolute(176, 224, 230));
      NAMED_COLORS.put("purple", new Absolute(128, 0, 128));
      NAMED_COLORS.put("rebeccapurple", new Absolute(102, 51, 153));
      NAMED_COLORS.put("red", new Absolute(255, 0, 0));
      NAMED_COLORS.put("rosybrown", new Absolute(188, 143, 143));
      NAMED_COLORS.put("royalblue", new Absolute(65, 105, 225));
      NAMED_COLORS.put("saddlebrown", new Absolute(139, 69, 19));
      NAMED_COLORS.put("salmon", new Absolute(250, 128, 114));
      NAMED_COLORS.put("sandybrown", new Absolute(244, 164, 96));
      NAMED_COLORS.put("seagreen", new Absolute(46, 139, 87));
      NAMED_COLORS.put("seashell", new Absolute(255, 245, 238));
      NAMED_COLORS.put("sienna", new Absolute(160, 82, 45));
      NAMED_COLORS.put("silver", new Absolute(192, 192, 192));
      NAMED_COLORS.put("skyblue", new Absolute(135, 206, 235));
      NAMED_COLORS.put("slateblue", new Absolute(106, 90, 205));
      NAMED_COLORS.put("slategray", new Absolute(112, 128, 144));
      NAMED_COLORS.put("slategrey", new Absolute(112, 128, 144));
      NAMED_COLORS.put("snow", new Absolute(255, 250, 250));
      NAMED_COLORS.put("springgreen", new Absolute(0, 255, 127));
      NAMED_COLORS.put("steelblue", new Absolute(70, 130, 180));
      NAMED_COLORS.put("tan", new Absolute(210, 180, 140));
      NAMED_COLORS.put("teal", new Absolute(0, 128, 128));
      NAMED_COLORS.put("thistle", new Absolute(216, 191, 216));
      NAMED_COLORS.put("tomato", new Absolute(255, 99, 71));
      NAMED_COLORS.put("turquoise", new Absolute(64, 224, 208));
      NAMED_COLORS.put("violet", new Absolute(238, 130, 238));
      NAMED_COLORS.put("wheat", new Absolute(245, 222, 179));
      NAMED_COLORS.put("white", new Absolute(255, 255, 255));
      NAMED_COLORS.put("whitesmoke", new Absolute(245, 245, 245));
      NAMED_COLORS.put("yellow", new Absolute(255, 255, 0));
      NAMED_COLORS.put("yellowgreen", new Absolute(154, 205, 50));
   }
}
