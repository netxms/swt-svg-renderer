package org.netxms.ui.svg.internal;

import java.util.Collections;
import java.util.List;

/**
 * Represents a parsed SVG document with viewBox metadata and child elements.
 */
public final class SVGDocument
{
   private final float viewBoxX;
   private final float viewBoxY;
   private final float viewBoxWidth;
   private final float viewBoxHeight;
   private final float width;
   private final float height;
   private final List<SVGNode> children;

   /**
    * @param viewBoxX viewBox origin x coordinate
    * @param viewBoxY viewBox origin y coordinate
    * @param viewBoxWidth viewBox width
    * @param viewBoxHeight viewBox height
    * @param width intrinsic document width
    * @param height intrinsic document height
    * @param children top-level SVG nodes
    */
   public SVGDocument(float viewBoxX, float viewBoxY, float viewBoxWidth, float viewBoxHeight,
         float width, float height, List<SVGNode> children)
   {
      this.viewBoxX = viewBoxX;
      this.viewBoxY = viewBoxY;
      this.viewBoxWidth = viewBoxWidth;
      this.viewBoxHeight = viewBoxHeight;
      this.width = width;
      this.height = height;
      this.children = Collections.unmodifiableList(children);
   }

   /**
    * @return viewBox origin x coordinate
    */
   public float getViewBoxX()
   {
      return viewBoxX;
   }

   /**
    * @return viewBox origin y coordinate
    */
   public float getViewBoxY()
   {
      return viewBoxY;
   }

   /**
    * @return viewBox width
    */
   public float getViewBoxWidth()
   {
      return viewBoxWidth;
   }

   /**
    * @return viewBox height
    */
   public float getViewBoxHeight()
   {
      return viewBoxHeight;
   }

   /**
    * @return intrinsic document width
    */
   public float getWidth()
   {
      return width;
   }

   /**
    * @return intrinsic document height
    */
   public float getHeight()
   {
      return height;
   }

   /**
    * @return unmodifiable list of top-level child nodes
    */
   public List<SVGNode> getChildren()
   {
      return children;
   }
}
