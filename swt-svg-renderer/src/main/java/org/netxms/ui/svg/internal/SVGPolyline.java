package org.netxms.ui.svg.internal;

/**
 * Represents an SVG &lt;polyline&gt; element.
 */
public final class SVGPolyline extends SVGNode
{
   private final float[] points;

   /**
    * @param style presentation style properties
    * @param transform 6-element affine transform matrix
    * @param display whether this element is visible
    * @param points alternating x,y coordinates
    */
   public SVGPolyline(StyleProps style, float[] transform, boolean display, float[] points)
   {
      super(style, transform, display);
      this.points = points.clone();
   }

   /**
    * @return copy of the alternating x,y coordinate array
    */
   public float[] getPoints()
   {
      return points.clone();
   }
}
