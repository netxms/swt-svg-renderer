package org.netxms.ui.svg.internal;

/**
 * Abstract base class for SVG path segments. All coordinates are absolute.
 */
public abstract class PathSegment
{
   private PathSegment()
   {
   }

   /**
    * Move-to segment: sets the current point without drawing.
    */
   public static final class MoveTo extends PathSegment
   {
      /** Target x coordinate. */
      public final float x;
      /** Target y coordinate. */
      public final float y;

      /**
       * @param x absolute x coordinate
       * @param y absolute y coordinate
       */
      public MoveTo(float x, float y)
      {
         this.x = x;
         this.y = y;
      }
   }

   /**
    * Line-to segment: draws a straight line from the current point.
    */
   public static final class LineTo extends PathSegment
   {
      /** Target x coordinate. */
      public final float x;
      /** Target y coordinate. */
      public final float y;

      /**
       * @param x absolute x coordinate
       * @param y absolute y coordinate
       */
      public LineTo(float x, float y)
      {
         this.x = x;
         this.y = y;
      }
   }

   /**
    * Cubic Bezier segment: draws a curve with two control points.
    */
   public static final class CubicTo extends PathSegment
   {
      /** First control point x. */
      public final float x1;
      /** First control point y. */
      public final float y1;
      /** Second control point x. */
      public final float x2;
      /** Second control point y. */
      public final float y2;
      /** End point x. */
      public final float x;
      /** End point y. */
      public final float y;

      /**
       * @param x1 first control point x coordinate
       * @param y1 first control point y coordinate
       * @param x2 second control point x coordinate
       * @param y2 second control point y coordinate
       * @param x end point x coordinate
       * @param y end point y coordinate
       */
      public CubicTo(float x1, float y1, float x2, float y2, float x, float y)
      {
         this.x1 = x1;
         this.y1 = y1;
         this.x2 = x2;
         this.y2 = y2;
         this.x = x;
         this.y = y;
      }
   }

   /**
    * Quadratic Bezier segment: draws a curve with one control point.
    */
   public static final class QuadTo extends PathSegment
   {
      /** Control point x. */
      public final float x1;
      /** Control point y. */
      public final float y1;
      /** End point x. */
      public final float x;
      /** End point y. */
      public final float y;

      /**
       * @param x1 control point x coordinate
       * @param y1 control point y coordinate
       * @param x end point x coordinate
       * @param y end point y coordinate
       */
      public QuadTo(float x1, float y1, float x, float y)
      {
         this.x1 = x1;
         this.y1 = y1;
         this.x = x;
         this.y = y;
      }
   }

   /**
    * Close-path segment: draws a line back to the start of the current subpath.
    */
   public static final class Close extends PathSegment
   {
      /** Singleton instance. */
      public static final Close INSTANCE = new Close();

      private Close()
      {
      }
   }
}
