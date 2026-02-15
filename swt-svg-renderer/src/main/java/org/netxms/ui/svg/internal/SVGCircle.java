package org.netxms.ui.svg.internal;

/**
 * Represents an SVG &lt;circle&gt; element.
 */
public final class SVGCircle extends SVGNode
{
   private final float cx;
   private final float cy;
   private final float r;

   /**
    * @param style presentation style properties
    * @param transform 6-element affine transform matrix
    * @param display whether this element is visible
    * @param cx center x coordinate
    * @param cy center y coordinate
    * @param r radius
    */
   public SVGCircle(StyleProps style, float[] transform, boolean display,
         float cx, float cy, float r)
   {
      super(style, transform, display);
      this.cx = cx;
      this.cy = cy;
      this.r = r;
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
    * @return radius
    */
   public float getR()
   {
      return r;
   }
}
