package org.netxms.ui.svg.internal;

import java.util.Collections;
import java.util.List;

/**
 * Represents an SVG &lt;g&gt; (group) element.
 */
public final class SVGGroup extends SVGNode
{
   private final List<SVGNode> children;

   /**
    * @param style presentation style properties
    * @param transform 6-element affine transform matrix
    * @param display whether this group is visible
    * @param children child SVG nodes
    */
   public SVGGroup(StyleProps style, float[] transform, boolean display, List<SVGNode> children)
   {
      super(style, transform, display);
      this.children = Collections.unmodifiableList(children);
   }

   /**
    * @return unmodifiable list of child nodes
    */
   public List<SVGNode> getChildren()
   {
      return children;
   }
}
