package org.netxms.ui.svg.internal;

/**
 * Represents an SVG &lt;polygon&gt; element (closed polyline).
 */
public final class SVGPolygon extends SVGNode
{
   private final float[] points;

   /**
    * @param style presentation style properties
    * @param transform 6-element affine transform matrix
    * @param display whether this element is visible
    * @param points alternating x,y coordinates (path is implicitly closed)
    */
   public SVGPolygon(StyleProps style, float[] transform, boolean display, float[] points)
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
