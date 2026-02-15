package org.netxms.ui.svg.internal;

/**
 * Abstract base class for all SVG elements in the parsed tree.
 */
public abstract class SVGNode
{
   private final StyleProps style;
   private final float[] transform;
   private final boolean display;

   /**
    * @param style presentation style properties for this element
    * @param transform 6-element affine transform matrix, or identity
    * @param display whether this element is visible (false if display:none)
    */
   protected SVGNode(StyleProps style, float[] transform, boolean display)
   {
      this.style = style;
      this.transform = transform;
      this.display = display;
   }

   /**
    * @return presentation style properties for this element
    */
   public StyleProps getStyle()
   {
      return style;
   }

   /**
    * @return 6-element affine transform matrix [a, b, c, d, e, f]
    */
   public float[] getTransform()
   {
      return transform;
   }

   /**
    * @return true if this element should be rendered, false if display:none
    */
   public boolean isDisplay()
   {
      return display;
   }
}
