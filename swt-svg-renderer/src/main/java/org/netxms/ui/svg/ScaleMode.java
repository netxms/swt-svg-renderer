package org.netxms.ui.svg;

/**
 * Scaling mode for SVG rendering.
 */
public enum ScaleMode
{
   /**
    * Fit SVG inside the box preserving aspect ratio (xMidYMid meet).
    * SVG is centered within the box.
    */
   UNIFORM,

   /**
    * Stretch SVG to fill the entire box, distorting if necessary.
    */
   STRETCH
}
