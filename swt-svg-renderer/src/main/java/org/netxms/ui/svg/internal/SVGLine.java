package org.netxms.ui.svg.internal;

/**
 * Represents an SVG &lt;line&gt; element.
 */
public final class SVGLine extends SVGNode
{
   private final float x1;
   private final float y1;
   private final float x2;
   private final float y2;

   /**
    * @param style presentation style properties
    * @param transform 6-element affine transform matrix
    * @param display whether this element is visible
    * @param x1 start point x coordinate
    * @param y1 start point y coordinate
    * @param x2 end point x coordinate
    * @param y2 end point y coordinate
    */
   public SVGLine(StyleProps style, float[] transform, boolean display,
         float x1, float y1, float x2, float y2)
   {
      super(style, transform, display);
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
   }

   /**
    * @return start point x coordinate
    */
   public float getX1()
   {
      return x1;
   }

   /**
    * @return start point y coordinate
    */
   public float getY1()
   {
      return y1;
   }

   /**
    * @return end point x coordinate
    */
   public float getX2()
   {
      return x2;
   }

   /**
    * @return end point y coordinate
    */
   public float getY2()
   {
      return y2;
   }
}
