package org.netxms.ui.svg.internal;

/**
 * Represents an SVG &lt;rect&gt; element.
 */
public final class SVGRect extends SVGNode
{
   private final float x;
   private final float y;
   private final float width;
   private final float height;
   private final float rx;
   private final float ry;

   /**
    * @param style presentation style properties
    * @param transform 6-element affine transform matrix
    * @param display whether this element is visible
    * @param x rectangle x position
    * @param y rectangle y position
    * @param width rectangle width
    * @param height rectangle height
    * @param rx horizontal corner radius
    * @param ry vertical corner radius
    */
   public SVGRect(StyleProps style, float[] transform, boolean display,
         float x, float y, float width, float height, float rx, float ry)
   {
      super(style, transform, display);
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.rx = rx;
      this.ry = ry;
   }

   /**
    * @return rectangle x position
    */
   public float getX()
   {
      return x;
   }

   /**
    * @return rectangle y position
    */
   public float getY()
   {
      return y;
   }

   /**
    * @return rectangle width
    */
   public float getWidth()
   {
      return width;
   }

   /**
    * @return rectangle height
    */
   public float getHeight()
   {
      return height;
   }

   /**
    * @return horizontal corner radius (0 for sharp corners)
    */
   public float getRx()
   {
      return rx;
   }

   /**
    * @return vertical corner radius (0 for sharp corners)
    */
   public float getRy()
   {
      return ry;
   }
}
