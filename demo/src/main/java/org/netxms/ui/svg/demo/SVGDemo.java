package org.netxms.ui.svg.demo;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.netxms.ui.svg.SVGImage;
import org.netxms.ui.svg.SVGParseException;
import org.netxms.ui.svg.ScaleMode;

/**
 * Demo application showing SVG rendering on SWT Canvas at various sizes and colors.
 */
public class SVGDemo
{
   private static final String[] ICON_NAMES = { "home", "star", "gear", "heart", "chart", "colorwheel" };
   private static final int[] SIZES = { 16, 24, 32, 48, 64, 128 };

   private final Display display;
   private final Shell shell;
   private final SVGImage[] icons;

   public SVGDemo()
   {
      display = new Display();
      shell = new Shell(display);
      shell.setText("SWT SVG Renderer Demo");
      shell.setLayout(new GridLayout(1, false));

      icons = new SVGImage[ICON_NAMES.length];
      for (int i = 0; i < ICON_NAMES.length; i++)
      {
         try
         {
            InputStream is = getClass().getClassLoader().getResourceAsStream("icons/" + ICON_NAMES[i] + ".svg");
            if (is != null)
            {
               icons[i] = SVGImage.createFromStream(is);
               is.close();
            }
            else
            {
               System.err.println("Icon not found: " + ICON_NAMES[i]);
            }
         }
         catch(Exception e)
         {
            System.err.println("Failed to load " + ICON_NAMES[i] + ": " + e.getMessage());
         }
      }

      createSizeGrid(shell);
      createColorDemo(shell);
      createResizableDemo(shell);

      shell.pack();
      shell.setSize(shell.getSize().x, Math.min(shell.getSize().y, 800));
   }

   /**
    * Grid showing each icon at multiple sizes.
    */
   private void createSizeGrid(Composite parent)
   {
      Label title = new Label(parent, SWT.NONE);
      title.setText("Icons at various sizes:");
      title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

      Composite grid = new Composite(parent, SWT.NONE);
      grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      grid.setLayout(new GridLayout(SIZES.length + 1, false));

      // Header row — sizes
      new Label(grid, SWT.NONE); // empty corner
      for (int size : SIZES)
      {
         Label lbl = new Label(grid, SWT.CENTER);
         lbl.setText(size + "px");
         lbl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
      }

      // One row per icon
      for (int i = 0; i < icons.length; i++)
      {
         Label nameLbl = new Label(grid, SWT.NONE);
         nameLbl.setText(ICON_NAMES[i]);
         nameLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

         for (int size : SIZES)
         {
            Canvas canvas = new Canvas(grid, SWT.DOUBLE_BUFFERED);
            GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
            gd.widthHint = size + 4;
            gd.heightHint = size + 4;
            canvas.setLayoutData(gd);

            final SVGImage icon = icons[i];
            final int s = size;
            canvas.addPaintListener(e -> {
               e.gc.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_WHITE));
               Point canvasSize = canvas.getSize();
               e.gc.fillRectangle(0, 0, canvasSize.x, canvasSize.y);
               if (icon != null)
               {
                  int ox = (canvasSize.x - s) / 2;
                  int oy = (canvasSize.y - s) / 2;
                  icon.render(e.gc, ox, oy, s, s);
               }
            });
         }
      }
   }

   /**
    * Shows icons rendered with different currentColor values.
    */
   private void createColorDemo(Composite parent)
   {
      Label title = new Label(parent, SWT.NONE);
      title.setText("currentColor theming (48px):");
      title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

      Composite row = new Composite(parent, SWT.NONE);
      row.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      row.setLayout(new GridLayout(5, false));

      int[][] colors = {
            { 0, 0, 0 },       // black
            { 0, 100, 200 },    // blue
            { 200, 50, 50 },   // red
            { 50, 150, 50 },   // green
            { 160, 160, 160 }  // gray (disabled)
      };
      String[] colorNames = { "Black", "Blue", "Red", "Green", "Gray" };

      // Use the star icon for color demo
      SVGImage starIcon = icons[1]; // star

      for (int c = 0; c < colors.length; c++)
      {
         Composite cell = new Composite(row, SWT.NONE);
         cell.setLayout(new GridLayout(1, false));
         cell.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

         Canvas canvas = new Canvas(cell, SWT.DOUBLE_BUFFERED);
         GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
         gd.widthHint = 52;
         gd.heightHint = 52;
         canvas.setLayoutData(gd);

         final int[] rgb = colors[c];
         canvas.addPaintListener(e -> {
            e.gc.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_WHITE));
            Point sz = canvas.getSize();
            e.gc.fillRectangle(0, 0, sz.x, sz.y);
            if (starIcon != null)
            {
               Color cc = new Color(canvas.getDisplay(), rgb[0], rgb[1], rgb[2]);
               starIcon.render(e.gc, (sz.x - 48) / 2, (sz.y - 48) / 2, 48, 48, cc);
               cc.dispose();
            }
         });

         Label lbl = new Label(cell, SWT.CENTER);
         lbl.setText(colorNames[c]);
         lbl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
      }
   }

   /**
    * A resizable canvas to demonstrate live scaling.
    */
   private void createResizableDemo(Composite parent)
   {
      Label title = new Label(parent, SWT.NONE);
      title.setText("Resizable (drag window to scale):");
      title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

      Canvas canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED | SWT.BORDER);
      GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
      gd.heightHint = 200;
      canvas.setLayoutData(gd);

      canvas.addPaintListener(e -> {
         GC gc = e.gc;
         Point sz = canvas.getSize();
         gc.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_WHITE));
         gc.fillRectangle(0, 0, sz.x, sz.y);

         // Render all icons in a row, evenly spaced
         int count = 0;
         for (SVGImage icon : icons)
         {
            if (icon != null)
               count++;
         }
         if (count == 0)
            return;

         int cellW = sz.x / count;
         int iconSize = Math.min(cellW - 8, sz.y - 8);
         if (iconSize <= 0)
            return;

         int x = 0;
         Color blue = new Color(canvas.getDisplay(), 0, 80, 180);
         for (SVGImage icon : icons)
         {
            if (icon != null)
            {
               int ox = x + (cellW - iconSize) / 2;
               int oy = (sz.y - iconSize) / 2;
               icon.render(gc, ox, oy, iconSize, iconSize, blue);
               x += cellW;
            }
         }
         blue.dispose();
      });
   }

   public void run()
   {
      shell.open();
      while (!shell.isDisposed())
      {
         if (!display.readAndDispatch())
            display.sleep();
      }
      display.dispose();
   }

   public static void main(String[] args)
   {
      new SVGDemo().run();
   }
}
