package org.netxms.ui.svg.internal;

import java.util.Collections;
import java.util.List;

/**
 * Represents an SVG &lt;path&gt; element.
 */
public final class SVGPath extends SVGNode
{
   private final List<PathSegment> segments;

   /**
    * @param style presentation style properties
    * @param transform 6-element affine transform matrix
    * @param display whether this element is visible
    * @param segments path segments (all coordinates absolute)
    */
   public SVGPath(StyleProps style, float[] transform, boolean display, List<PathSegment> segments)
   {
      super(style, transform, display);
      this.segments = Collections.unmodifiableList(segments);
   }

   /**
    * @return unmodifiable list of path segments
    */
   public List<PathSegment> getSegments()
   {
      return segments;
   }
}
