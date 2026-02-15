package org.netxms.ui.svg.internal;

/**
 * Represents an SVG &lt;ellipse&gt; element.
 */
public final class SVGEllipse extends SVGNode
{
   private final float cx;
   private final float cy;
   private final float rx;
   private final float ry;

   /**
    * @param style presentation style properties
    * @param transform 6-element affine transform matrix
    * @param display whether this element is visible
    * @param cx center x coordinate
    * @param cy center y coordinate
    * @param rx horizontal radius
    * @param ry vertical radius
    */
   public SVGEllipse(StyleProps style, float[] transform, boolean display,
         float cx, float cy, float rx, float ry)
   {
      super(style, transform, display);
      this.cx = cx;
      this.cy = cy;
      this.rx = rx;
      this.ry = ry;
   }

   /**
    * @return center x coordinate
    */
   public float getCx()
   {
      return cx;
   }

   /**
    * @return center y coordinate
    */
   public float getCy()
   {
      return cy;
   }

   /**
    * @return horizontal radius
    */
   public float getRx()
   {
      return rx;
   }

   /**
    * @return vertical radius
    */
   public float getRy()
   {
      return ry;
   }
}
